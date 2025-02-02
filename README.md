# Rock-Paper-Scissors Online Game

## 🚀 Launch Instructions
To run the game, you need to build the project and launch the resulting artifact in any suitable environment.

A **Docker file** is available for building if needed.

Upon startup, the server will open sockets on port `12345`.  
This port can be changed either by modifying `application.yaml`:
```yaml
socket.port: 12345
```
or by setting an environment variable.

🎮 Gameplay

To play the game, open two Telnet connections, for example, using:
```sh
nc localhost 12345
```
Once at least two players have connected, the game will start, and each player will be invited to choose:
🪨 rock, ✂️ scissors, or 📜 paper.