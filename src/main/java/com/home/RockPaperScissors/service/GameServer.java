package com.home.RockPaperScissors.service;

import com.home.RockPaperScissors.modal.Player;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class GameServer {
    @Value("${socket.port:12345}")
    private int socketPort;
    private final ExecutorService clientHandlerPool = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    public final Queue<Player> waitingPlayers = new ConcurrentLinkedQueue<>();

    @PostConstruct
    public void init() {
        new Thread(this::startServer).start();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(socketPort)) {
            log.info("Server started on port " + serverSocket.getLocalPort());
            while (running) {
                var socket = serverSocket.accept();
                clientHandlerPool.submit(() -> handleClient(socket));
            }
        } catch (Exception e) {
            log.error("Server error", e);
        } finally {
            shutdownServer();
        }
    }

    public void stopServer() {
        running = false;
        shutdownServer();
    }

    private void shutdownServer() {
        try {
            clientHandlerPool.shutdown();
            log.info("Server stopped.");
        } catch (Exception e) {
            log.error("Error while shutting down server", e);
        }
    }

    public void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Enter your nickname:");
            String nickname = in.readLine();
            Player player = Player.builder()
                    .nickname(nickname)
                    .socket(socket)
                    .in(in)
                    .out(out)
                    .build();
            waitingPlayers.add(player);
            out.println("Waiting for an opponent...");
            while (player.getOpponent() == null) {
                Thread.sleep(500);
                matchPlayers();
            }
        } catch (Exception e) {
            log.error("Error while handling client", e);
        }
    }

    public synchronized void matchPlayers() {
        if (waitingPlayers.size() >= 2) {
            var p1 = waitingPlayers.poll();
            var p2 = waitingPlayers.poll();
            p1.setOpponent(p2);
            p2.setOpponent(p1);
            p1.getOut().println("Match created: " + p1.getNickname() + " vs " + p2.getNickname());
            p2.getOut().println("Match created: " + p1.getNickname() + " vs " + p2.getNickname());
            log.info("Match created: " + p1.getNickname() + " vs " + p2.getNickname());
            playGame(p1, p2);
        }
    }

    public void playGame(Player player1, Player player2) {
        try (ExecutorService gameExecutor = Executors.newFixedThreadPool(2)) {
            AtomicReference<String> move1 = new AtomicReference<>();
            AtomicReference<String> move2 = new AtomicReference<>();
            while (true) {
                CountDownLatch latch = new CountDownLatch(2);
                gameExecutor.submit(() -> {
                    try {
                        move1.set(waitForValidMove(player1));
                        latch.countDown();
                    } catch (IOException e) {
                        log.error("Error while getting move from player 1", e);
                    }
                });
                gameExecutor.submit(() -> {
                    try {
                        move2.set(waitForValidMove(player2));
                        latch.countDown();
                    } catch (IOException e) {
                        log.error("Error while getting move from player 2", e);
                    }
                });

                try {
                    latch.await();
                    var move1Value = move1.get();
                    var move2Value = move2.get();
                    if (move1Value == null || move2Value == null)
                        return;
                    if (move1Value.equals(move2Value)) {
                        sendToBoth(player1, player2, "It's a tie! Try again.");
                        continue;
                    }
                    if (isWinner(move1Value, move2Value)) {
                        player1.getOut().println("You win!");
                        player2.getOut().println("You lose!");
                    } else {
                        player1.getOut().println("You lose!");
                        player2.getOut().println("You win!");
                    }
                    player1.getSocket().close();
                    player2.getSocket().close();
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Game interrupted", e);
                    break;
                } catch (IOException e) {
                    log.error("Error while prepare result", e);
                    break;
                }
            }
        }
    }

    private void sendToBoth(Player p1, Player p2, String message) {
        p1.getOut().println(message);
        p2.getOut().println(message);
    }

    public String waitForValidMove(Player player) throws IOException {
        String move;
        do {
            player.getOut().println("Enter Rock, Paper, or Scissors:");
            move = player.getIn().readLine();
            if (move == null) {
                player.getOut().println("Connection lost.");
                player.getSocket().close();
                return null;
            }
            move = move.trim().toLowerCase();
            if (!isValidMove(move))
                player.getOut().println("Invalid move! Try again.");
        } while (!isValidMove(move));
        return move;
    }

    public boolean isValidMove(String move) {
        return move.equals("rock") || move.equals("paper") || move.equals("scissors");
    }

    public boolean isWinner(String move1, String move2) {
        return (move1.equals("rock") && move2.equals("scissors")) ||
                (move1.equals("scissors") && move2.equals("paper")) ||
                (move1.equals("paper") && move2.equals("rock"));
    }
}