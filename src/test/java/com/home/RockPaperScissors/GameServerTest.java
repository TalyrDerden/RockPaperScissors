package com.home.RockPaperScissors;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.home.RockPaperScissors.modal.Player;
import com.home.RockPaperScissors.service.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


@SpringBootTest
public class GameServerTest {

    @Mock
    private Socket socket1;
    @Mock
    private Socket socket2;
    @Mock
    private BufferedReader reader1;
    @Mock
    private BufferedReader reader2;
    @Mock
    private PrintWriter writer1;
    @Mock
    private PrintWriter writer2;
    @Mock
    private Player player1;
    @Mock
    private Player player2;
    @InjectMocks
    private GameServer gameServer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(player1.getSocket()).thenReturn(socket1);
        when(player1.getIn()).thenReturn(reader1);
        when(player1.getOut()).thenReturn(writer1);

        when(player2.getSocket()).thenReturn(socket2);
        when(player2.getIn()).thenReturn(reader2);
        when(player2.getOut()).thenReturn(writer2);
    }

    @Test
    void testWaitForValidMove() throws IOException {
        when(reader1.readLine()).thenReturn("invalid").thenReturn("paper");
        String move1 = gameServer.waitForValidMove(player1);
        assertNotEquals("invalid", move1);
        String move2 = gameServer.waitForValidMove(player1);
        assertEquals("paper", move2);
    }

    @Test
    void testPlayGame() throws IOException {
        when(reader1.readLine()).thenReturn("rock").thenReturn("paper");;
        when(reader2.readLine()).thenReturn("rock").thenReturn("scissors");;
        gameServer.playGame(player1, player2);
        verify(writer1).println("It's a tie! Try again.");
        verify(writer2).println("It's a tie! Try again.");
        verify(writer1).println("You lose!");
        verify(writer2).println("You win!");
        verify(socket1).close();
        verify(socket2).close();
    }
}