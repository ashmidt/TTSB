package socketfx;

import java.net.*;


public class FxSocketServer extends GenericSocket implements SocketListener {

    private SocketListener fxListener;
    private ServerSocket serverSocket;

    /**
     * Called whenever a message is read from the socket.  In
     * JavaFX, this method must be run on the main thread and is
     * accomplished by the Platform.runLater() call.  Failure to do so
     * *will* result in strange errors and exceptions.
     * @param line Line of text read from the socket.
     */
    @Override
    public void onMessage(final String line) {
        javafx.application.Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fxListener.onMessage(line);
            }
        });
    }

    /**
     * Called whenever the open/closed status of the Socket
     * changes.  In JavaFX, this method must be run on the main thread and is
     * accomplished by the Platform.runLater() call.  Failure to do so
     * *will* result in strange errors and exceptions.
     * @param isClosed true if the socket is closed
     */
    @Override
    public void onClosedStatus(final boolean isClosed) {
        javafx.application.Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fxListener.onClosedStatus(isClosed);
            }
        });
    }

    /**
     * Initialize the FxSocketServer up to and including issuing the 
     * accept() method on its socketConnection.
     * @throws java.net.SocketException
     */
    @Override
    protected void initSocketConnection() throws SocketException {
        try {
            /*
             * Create server socket
             */
            serverSocket = new ServerSocket(port);
            /*
             * Allows the socket to be bound even though a previous
             * connection is in a timeout state.
             */
            serverSocket.setReuseAddress(true);
            /*
             * Wait for connection
             */
            if (debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
                System.out.println("Waiting for connection");
            }
            socketConnection = serverSocket.accept();
            if (debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
                System.out.println("Connection received from " + socketConnection.getInetAddress().getHostName());
            }
        } catch (Exception e) {
            if (debugFlagIsSet(Constants.instance().DEBUG_EXCEPTIONS)) {
                e.printStackTrace();
            }
            throw new SocketException();
        }
    }

    /**
     * For FxSocketServer class, additional ServerSocket instance has
     * to be closed.
     */
    @Override
    protected void closeAdditionalSockets() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public FxSocketServer(SocketListener fxListener, int port, int debugFlags) {
        super(port, debugFlags);
        this.fxListener = fxListener;
    }

    public FxSocketServer(SocketListener fxListener) {
        this(fxListener, Constants.instance().DEFAULT_PORT, Constants.instance().DEBUG_NONE);
    }

    public FxSocketServer(SocketListener fxListener, int port) {
        this(fxListener, port, Constants.instance().DEBUG_NONE);
    }
}
