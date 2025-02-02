package com.home.RockPaperScissors.modal;

import lombok.Builder;
import lombok.Data;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

@Data
@Builder
public class Player {
    private final String nickname;
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private Player opponent;

}
