package com.matrix_maeny.onlinetictactoe2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.onlinetictactoe2.connectionDialog.ConnectDialog;
import com.matrix_maeny.onlinetictactoe2.databinding.ActivityMainBinding;
import com.matrix_maeny.onlinetictactoe2.gameboard.GameBoard;
import com.matrix_maeny.onlinetictactoe2.gameboard.GameBoardMoves;
import com.matrix_maeny.onlinetictactoe2.registerActivities.LoginActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ConnectDialog.ConnectDialogListener,
        GameBoard.GameBoardListener, ResultDialog.ResultDialogListener {

    private ActivityMainBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private boolean sounds = true; // for sound .. to enable or disable sounds
    private boolean connected = false; // for connection status


    private String currentUserUid = null; // the current user id
    private String currentUsername = null; // current user name
    public static String connectedUsername_FromDialog = null;

    private boolean isClientOnline = false; // for getting online status of the client
    public static boolean host = false; // to defining who is the host
    private boolean singleCall = true; // to set the host username at once
    private boolean alreadySetuped = false; // to set listener at once

    private boolean isRestartGame = false;
    private String connectedUserUid = null;
    private String connectUsername = null;

    // for host side
    private String hostUsername = null;
    // for client
    private String clientUsername = null;

    // the gamespace key
    private String gameSpaceKey = null;


    private Moves moves = new Moves();
    private GameBoardMoves gameBoardMoves;

    final Handler handler = new Handler();

    private boolean gameOver = false;
    private ResultDialog resultDialog = new ResultDialog();
    private MediaPlayer mediaPlayer = null;
    ConnectDialog dialog = new ConnectDialog();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();
        setUpClientValueListeners();
    }

    private void initialize() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserUid = Objects.requireNonNull(auth.getCurrentUser()).getUid();


        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Fetching data...");
        progressDialog.show();

        getCurrentUserData();
        setUserStatus(true);
        gameBoardMoves = binding.gameBoard.getBoardMoves();


    }


    private void setUserStatus(boolean online) {
        firebaseDatabase.getReference().child("Users").child(currentUserUid)
                .child("statusOnline").setValue(online);
    }

    private void setUpClientValueListeners() {

        DatabaseReference reference = firebaseDatabase.getReference().child("Users").child(currentUserUid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!host) {
                    clientUsername = currentUsername;
                    setupPlayer1Board("You", 0);
                    UserModel model = snapshot.getValue(UserModel.class);
                    if (model != null && !model.getConnectedTo().equals("")) {
                        connectedUserUid = model.getConnectedTo();
//                        clientUsername = model.getUsername();
                        if (!alreadySetuped) {
                            setupListenerForHost();
                        }

                        if (singleCall) {
                            firebaseDatabase.getReference().child("Users").child(connectedUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    UserModel model1 = snapshot.getValue(UserModel.class);
                                    if (model1 != null) {
                                        hostUsername = model1.getUsername();


                                        //                                    binding.mainTurnTv.setText(hostUsername);
                                        moves.setFirstMove(hostUsername);
                                        moves.setTurn(hostUsername);
                                        moves.setSecondMove(clientUsername);
                                        Log.i("establish1", "in-1");
                                        enterIntoGameSpace();
                                        singleCall = false;
                                        setupPlayer2Board(hostUsername, 0);
                                        try {
                                            dialog.dismiss();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
//                                        setUpUiForResult(model1.getUsername(), true);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }


                        if (!singleCall) {
                            enterIntoGameSpace();
                            Log.i("establish1", "in-2");

                        }
                    } else {
                        if (connectedUserUid != null && gameSpaceKey != null) {
                            destroyConnection();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setupListenerForHost() {
        String mainId = null;
        if (host) {
            mainId = currentUserUid;
        } else {
            mainId = connectedUserUid;
        }
        firebaseDatabase.getReference().child("Users").child(mainId).child("connectedTo")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        if (value != null && value.equals("") && gameSpaceKey != null) {
                            destroyConnection();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        alreadySetuped = true;
    }

    private void setupGameSpaceKey() {
        Log.i("users", "host : " + hostUsername + "client: " + clientUsername);
        if (host) {
            Log.i("host", "yes : current id : " + currentUserUid + "client: " + connectedUserUid);
            gameSpaceKey = currentUserUid + connectedUserUid;
        } else {
            Log.i("host", "no : current id : " + currentUserUid + "client: " + connectedUserUid);
            gameSpaceKey = connectedUserUid + currentUserUid;
        }
    }

    private void enterIntoGameSpace() {

//        if (host) {
//            gameSpaceKey = currentUserUid + connectedUserUid;
//        } else {
//            gameSpaceKey = connectedUserUid + currentUserUid;
//        }
        setupGameSpaceKey();


        firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey).setValue(moves);
        DatabaseReference reference = firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey);

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                connected = true;
                ConnectDialog.connected = true;
                getClientOnlineStatus();

                moves = snapshot.getValue(Moves.class);

                if (moves != null && moves.getFirstMove().equals("") && moves.getTurn().equals("")) {
                    connected = ConnectDialog.connected = false;
                    return;
                }

                if (moves != null) {

                    if (isRestartGame) { // this boolean for accessing restart in both devices
                        playAgain(); // resetting every thing
//                        binding.gameBoard.invalidate();
                        try {
                            resultDialog.dismiss(); // dismissing the existed result dialog
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isRestartGame = false;
                    }
                    if (!gameOver) {
                        updateGameBoard(); // if not winstatus && not gameover
                    }

                    if (!gameOver && checkGameOverStatus()) {
                        gameOver = true;
                        // show winner
                        startResultLoaderDialog("Loading...", "wait few seconds..."); // starting animation for result
                        handler.postDelayed(() -> {
                            try {
                                progressDialog.dismiss(); // you've to show it first, if not you'll get illegal state exception
                                showResultDialog();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            isRestartGame = true;
                        }, 1500);

                    }
                }


                // for online status
//                getClientOnlineStatus();
//
//                moves = snapshot.getValue(Moves.class);
//                if (moves.getTurn().equals("") || moves.getFirstMove().equals("")) {
//                    binding.mainTurnTv.setText("Error: try to reconnect");
//                    return;
//                }
//
//                if (moves != null) {
//
//                    if (isRestartGame) { // this boolean for accessing restart in both devices
//                        playAgain(); // resetting every thing
//                        try {
//                            dialog.dismiss(); // dismissing the existed result dialog
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        isRestartGame = false;
//                    }
//                    if (!winStatus && !gameOver) { // to update the board/ to continue the game, they shouldn't win and the
//                        // game shouldn't complete
//                        updateGameBoard();
//                    }
//
//                    if (!winStatus && checkWinStatus()) { // the boolean is for single access into the if statement
//                        // a person won the game
//
//                        startResultLoaderDialog("Loading Winner...", "wait few seconds..."); // starting animation for result
//
//                        handler.postDelayed(() -> {                 // updating winner / uploading winner takes time... so
//                            // we are using handler for 1.5 seconds
//                            binding.mainTurnTv.setText("Winner: " + moves.getWinner());
//                            try {
//                                progressDialog.dismiss(); // you've to show it first, if not you'll get illegal state exception
//                                showResultDialog();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            isRestartGame = true;
//                        }, 1000);
//
//                        winStatus = true;
//                        gameOver = true;
//                    } else {
//                        if (!gameOver && checkGameOverStatus()) { // here game over is also used for the single access
//                            // the game is completed without winning
//
//                            startResultLoaderDialog("Loading...", "wait few seconds..."); // starting animation for result
//
//                            handler.postDelayed(() -> {                 // updating winner / uploading winner takes time... so
//                                // we are using handler for 1.5 seconds
//                                binding.mainTurnTv.setText(moves.getWinner());
//                                try {
//                                    progressDialog.dismiss(); // you've to show it first, if not you'll get illegal state exception
//                                    showResultDialog();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//
//                                isRestartGame = true;
//                            }, 1000);
//                            gameOver = true;
//                        }
//                    }
//
//
//                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void playAgain() {
        moves.setPlayer2Score(0);
        moves.setPlayer1Score(0);
        if (host) {
            setupPlayer1Board("You", 0);
            setupPlayer2Board(clientUsername, 0);

        } else {
            setupPlayer1Board("You", 0);
            setupPlayer2Board(hostUsername, 0);

        }


        gameBoardMoves.resetGame();
        binding.gameBoard.invalidate();

        moves.setTouchCount(-1);
        moves.setWinner("DRAW");
        moves.resetMoves();
        gameOver = false;

        firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey)
                .setValue(moves).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
//                        gameSpaceKey = null;
                    }
                });
    }


    private void showResultDialog() {
        if (moves.getWinner().equals(currentUsername)) {
            ResultDialog.win = "WON";
            playWin();
        } else {
            ResultDialog.win = "LOSE";
            playLost();
        }

        if (moves.getWinner().equals("DRAW")) {
            ResultDialog.win = "DRAW";
            playDraw();
        }
        resultDialog.show(getSupportFragmentManager(), "Result dialog");
    }

    @SuppressLint("SetTextI18n")
    private void updateGameBoard() {
        for (int i = 0; i < 108; i++) {
            if (moves.getGameMoves().get(i).getPlayer() != -1) {
                gameBoardMoves.setMoves(moves.getGameMoves().get(i).getRow() - 1,
                        moves.getGameMoves().get(i).getColumn() - 1, moves.getGameMoves().get(i).getPlayer());
            }

            if (moves.getMatchedBoxes().get(i).getPlayer() != -1) {
                gameBoardMoves.createMatch(moves.getMatchedBoxes().get(i).getPlayer(),
                        moves.getMatchedBoxes().get(i).getR1(), moves.getMatchedBoxes().get(i).getC1(),
                        moves.getMatchedBoxes().get(i).getR2(), moves.getMatchedBoxes().get(i).getC2(),
                        moves.getMatchedBoxes().get(i).getR3(), moves.getMatchedBoxes().get(i).getC3());
            }
        }

        binding.gameBoard.invalidate();
        if (host) {
            setupPlayer1Board("You", moves.getPlayer1Score());
            setupPlayer2Board(clientUsername, moves.getPlayer2Score());

        } else {
            setupPlayer1Board("You", moves.getPlayer2Score());
            setupPlayer2Board(hostUsername, moves.getPlayer1Score());

        }

        if(moves.getExtraTurn()==1){
            if(host){
                binding.extraTurnTv.setText("You got one more chance");
            }else{
                binding.extraTurnTv.setText(hostUsername+" got one more chance");
            }
            binding.extraTurnTv.setVisibility(View.VISIBLE);
        }else if(moves.getExtraTurn() == 2){
            if(host){
                binding.extraTurnTv.setText(clientUsername+" got one more chance");
            }else{
                binding.extraTurnTv.setText("You got one more chance");
            }
            binding.extraTurnTv.setVisibility(View.VISIBLE);
        }else{
            binding.extraTurnTv.setVisibility(View.INVISIBLE);
        }
        moves.setExtraTurn(-1);
        setTurn();

    }


    private void getClientOnlineStatus() {
        firebaseDatabase.getReference().child("Users").child(connectedUserUid)
                .child("statusOnline").addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isClientOnline = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
//                        Toast.makeText(MainActivity.this, "" + isOnline, Toast.LENGTH_SHORT).show();

                        if (connected) {
                            if (isClientOnline) {
                                binding.player2OnlineStatusTv.setText("online");
                                binding.player2OnlineStatusTv.setBackgroundResource(R.drawable.status_txt_online_bg);
                            } else {
                                binding.player2OnlineStatusTv.setText("offline");

                                binding.player2OnlineStatusTv.setBackgroundResource(R.drawable.status_txt_offline_bg);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void getCurrentUserData() {
        firebaseDatabase.getReference().child("Users").child(currentUserUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {
                            currentUsername = model.getUsername(); // assigning value for current username

                            ConnectDialog.currentUsername_ForDialog = currentUsername; // setting the value to check usernames when establishing connection

                            String temp = "<u>" + currentUsername + "  </u>"; // creating an underlined String from html
                            Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml(temp)); // setting title of the toolbar
                            setupPlayer1Board("You", 0);
                        }
                        try {
                            progressDialog.dismiss(); // dismissing the waiting dialog
                            dialog.show(getSupportFragmentManager(), "initial show");
                        } catch (Exception e) {
                            e.printStackTrace(); // in case of any illegal state exception
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setupPlayer1Board(String name, int score) {
        if (host) {
            binding.colorCode.setTextColor(getResources().getColor(R.color.player_one_disk_color));
        } else {
            binding.colorCode.setTextColor(getResources().getColor(R.color.player_two_disk_color));
        }
        binding.player1NameTv.setText(name);
        binding.player1ScoreTv.setText(String.valueOf(score));
    }

    private void setupPlayer2Board(String name, int score) {
        binding.player2NameTv.setText(name);
        binding.player2ScoreTv.setText(String.valueOf(score));
    }


    @Override
    protected void onDestroy() {
        setUserStatus(false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        setUserStatus(false);
        super.onPause();
    }

    @Override
    protected void onStart() {
        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
        setUserStatus(true);

        super.onStart();
    }

    @Override
    protected void onResume() {

        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
        setUserStatus(true);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        setUserStatus(false);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_:
                // go to about activity
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.sounds_:
                // sounds off or on
                sounds = !sounds;
                if (sounds) {
                    item.setTitle("Sounds off");
                    item.setIcon(R.drawable.ic_baseline_volume_up_24);
                } else {
                    item.setTitle("Sounds on");
                    item.setIcon(R.drawable.ic_baseline_volume_off_24);
                }
                break;

            case R.id.connect_:
                showConnectionDialog();
                break;
            case R.id.log_out:
                destroyConnection();
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    private void showConnectionDialog() {
        dialog.show(getSupportFragmentManager(), "Supports connection");
    }

    private void startResultLoaderDialog(String title, String message) {
        progressDialog.setMessage(message);
        progressDialog.setTitle(title);
        progressDialog.show();
    }

    @Override
    public void establishConnections() {
        // after establishment of connection you need to change variable 'connected' to --- true --- in this and in connect dialog
//        Toast.makeText(this, "connection established", Toast.LENGTH_SHORT).show();
        connectUsername = connectedUsername_FromDialog;
        host = true;
        establishConnection();

    }

    private void establishConnection() {

        // here i'm setting both users ids in both sides... so client id is seated in the current user data, and current user id
        // is seated in the client data;

        DatabaseReference reference = firebaseDatabase.getReference().child("Users");
        startResultLoaderDialog("Searching User...", "Wait few seconds...");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                long j = snapshot.getChildrenCount();
                for (DataSnapshot s : snapshot.getChildren()) {
                    i++;
                    UserModel model = s.getValue(UserModel.class);
                    if (model != null) {
                        if (connectUsername.equals(model.getUsername())) {

                            if (model.isStatusOnline()) {
                                connectedUserUid = s.getKey();
                                clientUsername = model.getUsername();
                                hostUsername = currentUsername;

                                setupPlayer2Board(clientUsername, 0);

                                if (!alreadySetuped) {
                                    setupListenerForHost();
                                }
                                model.setConnectedTo(currentUserUid); // setting value in client

                                // setting value to client data
                                reference.child(connectedUserUid).setValue(model).addOnCompleteListener(task -> {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "connected to : " + model.getUsername(), Toast.LENGTH_SHORT).show();
                                        ConnectDialog.connected = true;
                                        // setting value to current data;
                                        reference.child(currentUserUid).child("connectedTo").setValue(connectedUserUid);

//                                        setUpUiForResult(model.getUsername(), true);

                                        moves.setTurn(hostUsername);
                                        moves.setFirstMove(hostUsername);
                                        gameOver = false;
                                        enterIntoGameSpace();


                                    } else {
                                        Toast.makeText(MainActivity.this, "connection failed", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();

                                });

                            } else {
                                Toast.makeText(MainActivity.this, "User not active", Toast.LENGTH_SHORT).show();
                            }


                            progressDialog.dismiss();
                            break;

                        } else if (i == j) {
                            Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    public void destroyConnection() {
        // after establishment of connection you need to change variable 'connected' to --- false --- in this and in connect dialog
        destroy();
    }

    private void destroy() {
        if (gameSpaceKey == null) {
            return;
        }
        resetGame();


//        binding.mainTurnTv.setText("Turn appears here");

        firebaseDatabase.getReference().child("Users").child(currentUserUid).child("connectedTo").setValue("")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseDatabase.getReference().child("Users").child(connectedUserUid).child("connectedTo").setValue("");
//                        setUpUiForResult(null, false);
                        ConnectDialog.connected = false;
                        connected = false;
                        gameSpaceKey = null;
                        host = false;
                        gameOver = false;
                        singleCall = true;
                        binding.extraTurnTv.setVisibility(View.INVISIBLE);
                        binding.player2OnlineStatusTv.setBackgroundResource(R.drawable.status_txt_online_bg);
                        Toast.makeText(this, "connection destroyed", Toast.LENGTH_SHORT).show();
//                        try {
//                            dialog.show(getSupportFragmentManager(),"to connect");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @SuppressLint("SetTextI18n")
    private void resetGame() {
        setupPlayer1Board("You", 0);
        setupPlayer2Board("Player 2", 0);
        binding.player2OnlineStatusTv.setText("Status");

        gameBoardMoves.resetGame();
        binding.gameBoard.invalidate();
        Moves moves = new Moves();
        this.moves = moves;
//        setAllValuesToInitialStatus();
        firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey)
                .setValue(moves).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
//                            return;
                        }
//                        gameSpaceKey = null;
//                        hideAll();
//                        try {
////                            dialog.dismiss();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }
                });
    }

    @Override
    public void moveHappened() { // from game board
//        Toast.makeText(this, "row: " + gameBoardMoves.getSelectedRow() + " Column: " + gameBoardMoves.getSelectedColumn(), Toast.LENGTH_SHORT).show();
        if (!gameOver) {
            if (connected) {
                if (isClientOnline) {
                    if (gameBoardMoves.getGameMoves()[gameBoardMoves.getSelectedRow() - 1][gameBoardMoves.getSelectedColumn() - 1] == -1) {
                        if (moves.getTurn().equals(currentUsername)) {
                            if (host) {
                                gameBoardMoves.setSelectedMove(1);
                            } else {
                                gameBoardMoves.setSelectedMove(2);

                            }
                            setGamePlayMoves();                    // upload content

                        } else {
                            Toast.makeText(this, "It's not your turn", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "already filled", Toast.LENGTH_SHORT).show();
                        binding.gameBoard.invalidate();
                    }
                } else {
                    Toast.makeText(this, "User not active", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "You're not yet connected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Game over", Toast.LENGTH_SHORT).show();
        }

    }


    private void setGamePlayMoves() {

        // fit the values into the moves


        // for uploading content

        if (isClientOnline) {
//            if () { // condition for filled or not
//                Toast.makeText(this, "Already filled", Toast.LENGTH_SHORT).show();
//                return;
//            }
            moves.setTouchCount(moves.getTouchCount() + 1);


//        setIconsOnBoard(value);
//            Toast.makeText(this, "" + moves.getTouchCount(), Toast.LENGTH_SHORT).show();

            if (moves.getTurn().equals(hostUsername)) {
                moves.setMove(1, gameBoardMoves.getSelectedRow(), gameBoardMoves.getSelectedColumn());
            } else if (moves.getTurn().equals(clientUsername)) {
                moves.setMove(2, gameBoardMoves.getSelectedRow(), gameBoardMoves.getSelectedColumn());

            }


            changeTurn();
            setScore(gameBoardMoves.getGameMoves());


            firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey).setValue(moves);
            binding.gameBoard.invalidate();
        } else {
            Toast.makeText(this, "User is not active", Toast.LENGTH_SHORT).show();
        }


    }

    private void changeTurn() {
        if (moves.getTurn().equals(clientUsername)) {
            moves.setTurn(hostUsername);

        } else {
            moves.setTurn(clientUsername);

        }
    }

    private void setTurn() {
        if (moves.getTurn().equals(currentUsername)) {
            binding.player1TurnV.setBackgroundResource(R.drawable.turn_bg);
            binding.player2TurnV.setBackgroundColor(Color.TRANSPARENT);
        } else {
            binding.player2TurnV.setBackgroundResource(R.drawable.turn_bg);
            binding.player1TurnV.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private boolean checkGameOverStatus() {
        boolean isGameOver = true;
        for (int i = 0; i < 108; i++) {
            if (moves.getGameMoves().get(i).getPlayer() == -1) {
                isGameOver = false;
                break;
            }
        }

        return isGameOver;
    }

    private void setScore(final int[][] s) {

        int score = getScore(s);

        if (moves.getTurn().equals(hostUsername)) { // but the score should be moved to previous player,
            // because the turn is changed after playing
            moves.setPlayer1Score(moves.getPlayer1Score() + score);
        } else {
            moves.setPlayer2Score(moves.getPlayer2Score() + score);
        }
        setWinStatus();


    }

    @SuppressLint("SetTextI18n")
    private int getScore(final int[][] s) {
        short[][] t = new short[18][15]; // to avoid outOfIndex exception while calc score
        int score = 0;
        int i, j;

        int y = -5;
        if (currentUsername.equals(clientUsername)) {
            y = 4;
        } else {
            y = 3;
        }

        for (i = 0; i < 18; i++) { // filling temp matrix  at middle
            for (j = 0; j < 15; j++) {
                if ((i >= 3 && i <= 14) && (j >= 3 && j <= 11)) {
                    int r = i - 3;
                    int c = j - 3;
                    t[i][j] = (short) s[r][c];
                    continue;
                }
                t[i][j] = -1;
            }
        }

//        i = 0;
//
//        while (moves.getGameMoves().get(i).getPlayer() != -1) {
//            i++;
//        }
//
//        i--;

        final int r = gameBoardMoves.getSelectedRow() + 2;
        final int c = gameBoardMoves.getSelectedColumn() + 2;

        // checking matches
        // initially centre matches, procedural approach

//        if ((t[r][c] == t[r - 1][c - 1]) && (t[r][c] == t[r + 1][c + 1]) && (t[r - 1][c - 1] == t[r + 1][c + 1])) { // first diagonal
//            score += 5;
//        }
//
//        if ((t[r][c] == t[r - 1][c]) && (t[r][c] == t[r + 1][c]) && (t[r - 1][c] == t[r + 1][c])) { // first vertical line
//            score += 5;
//        }
//
//        if ((t[r][c] == t[r - 1][c + 1]) && (t[r][c] == t[r + 1][c - 1]) && (t[r - 1][c + 1] == t[r + 1][c - 1])) { // second diagonal
//            score += 5;
//        }
//
//        if ((t[r][c] == t[r][c - 1]) && (t[r][c] == t[r][c + 1]) && (t[r][c - 1] == t[r][c + 1])) { // horizontal line
//            score += 5;
//        }

        for (int x = 0; x < 4; x++) {
            int k, l, m, n;
            switch (x) {
                case 0: // first diagonal
//                    k = l = r - 1;
//                    m = n = c + 1;
                    k = r - 1;
                    l = c - 1;
                    m = r + 1;
                    n = c + 1;
                    break;
                case 1: // first vertical
                    k = r - 1;
                    l = c;
                    m = r + 1;
                    n = c;
                    break;
                case 2: // second diagonal
                    k = r - 1;
                    l = c + 1;
                    m = r + 1;
                    n = c - 1;
                    break;
                case 3: // horizontal line
                    k = r;
                    l = c - 1;
                    m = r;
                    n = c + 1;
                    break;
                default:
                    k = l = m = n = 0;

            }
            if ((t[r][c] == t[k][l]) && (t[r][c] == t[m][n])/* && (t[r + k][c + l] == t[r + m][c + n])*/) { // horizontal line
                score += 5;
                moves.setTurn(currentUsername);

//                Toast.makeText(this, "You got one more chance", Toast.LENGTH_SHORT).show();
                binding.extraTurnTv.setText("You got one more chance");
                binding.extraTurnTv.setVisibility(View.VISIBLE);
                if (host) { // but the score should be moved to previous player,
                    // because the turn is changed after playing
                    moves.setExtraTurn(1);
                    gameBoardMoves.createMatch(3, r - 3, c - 3, k - 3, l - 3, m - 3, n - 3);
                    moves.createMatch(3, r - 3, c - 3, k - 3, l - 3, m - 3, n - 3);
                    Log.i("matches1", (r - 2) + " " + (c - 2) + "--: " + (k - 2) + " " + (l - 2) + "--: " + (m - 2) + " " + (n - 2));
                } else {
                    moves.setExtraTurn(2);
                    gameBoardMoves.createMatch(4, r - 3, c - 3, k - 3, l - 3, m - 3, n - 3);
                    moves.createMatch(4, r - 3, c - 3, k - 3, l - 3, m - 3, n - 3);
                }
            }

        } // checking at centre

        for (int x = 0; x < 8; x++) {

            int k = 0, l = 0, m = 0, n = 0, o = 0, p = 0;

            switch (x) {
                case 0:
                    k = r - 1;
                    l = c - 1;

                    m = r - 2;
                    n = c - 2;

                    o = r - 3;
                    p = c - 3;
                    break;
                case 1:
                    k = r - 1;
                    l = c;

                    m = r - 2;
                    n = c;

                    o = r - 3;
                    p = c;
                    break;
                case 2:
                    k = r - 1;
                    l = c + 1;

                    m = r - 2;
                    n = c + 2;

                    o = r - 3;
                    p = c + 3;
                    break;
                case 3:
                    k = r;
                    l = c + 1;

                    m = r;
                    n = c + 2;

                    o = r;
                    p = c + 3;
                    break;
                case 4:
                    k = r + 1;
                    l = c + 1;

                    m = r + 2;
                    n = c + 2;

                    o = r + 3;
                    p = c + 3;
                    break;
                case 5:
                    k = r + 1;
                    l = c;

                    m = r + 2;
                    n = c;

                    o = r + 3;
                    p = c;
                    break;
                case 6:
                    k = r + 1;
                    l = c - 1;

                    m = r + 2;
                    n = c - 2;

                    o = r + 3;
                    p = c - 3;
                    break;
                case 7:
                    k = r;
                    l = c - 1;

                    m = r;
                    n = c - 2;

                    o = r;
                    p = c - 3;
                    break;
            }

            if ((t[r][c] == t[k][l]) && (t[r][c] == t[m][n]) && ((t[r][c] != y) && (t[r][c] != t[o][p]))) {
                score += 5;
                moves.setTurn(currentUsername);
//                Toast.makeText(this, "You got one more chance", Toast.LENGTH_SHORT).show();
                binding.extraTurnTv.setText("You got one more chance");
                binding.extraTurnTv.setVisibility(View.VISIBLE);

                if (host) { // but the score should be moved to previous player,
                    // because the turn is changed after playing
                    moves.setExtraTurn(1);
                    gameBoardMoves.createMatch(3, r - 3, c - 3, k - 3, l - 3, m - 3, n - 3);
                    moves.createMatch(3, r - 3, c - 3, k - 3, l - 3, m - 3, n - 3);
                    Log.i("matches2", (r - 2) + " " + (c - 2) + "--: " + (k - 2) + " " + (l - 2) + "--: " + (m - 2) + " " + (n - 2));
                } else {
                    moves.setExtraTurn(2);
                    gameBoardMoves.createMatch(4, r - 3, c - 3, k - 3, l - 3, m - 3, n - 3);
                    moves.createMatch(4, r - 3, c - 3, k - 3, l - 3, m - 3, n - 3);
                }
            }
        }


        return score;
    }

    private void setWinStatus() {

        if (moves.getPlayer1Score() > moves.getPlayer2Score()) {
            moves.setWinner(hostUsername);
        } else if (moves.getPlayer1Score() < moves.getPlayer2Score()) {
            moves.setWinner(clientUsername);
        } else {
            moves.setWinner("DRAW");
        }


    }


    @Override
    public void restartGame() {
        changeTurn();
        playAgain();
    }

    @Override
    public void dialogBackPressed() {
        finish();
    }


    void stopSound() {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void playWin() {
        stopSound();
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.win);

        startM();
    }

    void playLost() {
        stopSound();
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.lost_match);

        startM();
    }

    void playDraw() {
        stopSound();
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.draw_match);

        startM();
    }

    void startM() {
        if (sounds) {
            try {
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}