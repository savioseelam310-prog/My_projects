import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/* <applet code="sam.class" width="1000" height="800"></applet> */

public class sam extends Applet implements Runnable, KeyListener, ActionListener, ItemListener {
    private final int MAX_UNITS = 1000;
    private final int UNIT_SIZE = 15;
    private int x[] = new int[MAX_UNITS];
    private int y[] = new int[MAX_UNITS];

    private int snakeLength = 3;
    private int foodx, foody;
    private int score = 0;
    private char direction = 'R';
    private boolean running = false;

    private Thread gameThread;
    private Button startbtn, menu, credits, logo;
    private Random random = new Random();
    private AudioClip backgroundMusic, endmusic = null;
    private Color colorbg = Color.green;

    // For Double Buffering (Fixes Flickering)
    private Image offscreenImage;
    private Graphics offscreenGraphics;
    private Choice choice1, choice2;

    @Override
    public void init() {
        setSize(1000, 800);
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

        startbtn = new Button("Start Game");
        startbtn.addActionListener(this);
        menu = new Button("Menu");
        menu.addActionListener(this);

        add(startbtn);
        add(menu);

        try {
            backgroundMusic = getAudioClip(getCodeBase(), "musicbg.wav");
        } catch (Exception e) {
            System.out.println("Audio file not found.");
        }
    }

    public void resetGame() {
        snakeLength = 3;
        score = 0;
        direction = 'R';

        // Set initial positions
        for (int i = 0; i < snakeLength; i++) {
            x[i] = 60 - (i * UNIT_SIZE);
            y[i] = 60;
        }

        spawnFood();
        running = true;

        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void run() {
        while (running) {
            move();
            checkCollision();
            repaint();
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void move() {
        for (int i = snakeLength; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] -= UNIT_SIZE;
                break;
            case 'D':
                y[0] += UNIT_SIZE;
                break;
            case 'L':
                x[0] -= UNIT_SIZE;
                break;
            case 'R':
                x[0] += UNIT_SIZE;
                break;
        }
    }

    public void checkCollision() {
        // Body Collision (Snake can't hit itself until it's at least 5 units long)
        for (int i = snakeLength; i > 0; i--) {
            if ((snakeLength > 4) && (x[0] == x[i]) && (y[0] == y[i])) {
                gameOver();
            }
        }

        // Wall Collision
        if (x[0] < 0 || x[0] >= getWidth() || y[0] < 0 || y[0] >= getHeight()) {
            gameOver();
        }

        // Food Collision
        if ((x[0] == foodx) && (y[0] == foody)) {
            snakeLength++;
            score += 10;
            spawnFood();
        }
    }

    private void gameOver() {
        running = false;
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
        endmusic = getAudioClip(getCodeBase(), "musicbg.wav");
        endmusic.loop();

    }

    public void spawnFood() {
        int columns = (getWidth() / UNIT_SIZE);
        int rows = (getHeight() / UNIT_SIZE);
        if (columns > 0 && rows > 0) {
            foodx = random.nextInt(columns) * UNIT_SIZE;
            foody = random.nextInt(rows) * UNIT_SIZE;
        }
    }

    // Double Buffering Logic to stop flickering
    @Override
    public void update(Graphics g) {
        if (offscreenImage == null) {
            offscreenImage = createImage(getWidth(), getHeight());
            offscreenGraphics = offscreenImage.getGraphics();
        }
        offscreenGraphics.setColor(getBackground());
        offscreenGraphics.fillRect(0, 0, getWidth(), getHeight());
        paint(offscreenGraphics);
        g.drawImage(offscreenImage, 0, 0, this);
    }

    @Override
    public void paint(Graphics g) {
        if (running) {
            // Food
            g.setColor(Color.red);
            g.fillOval(foodx, foody, UNIT_SIZE, UNIT_SIZE);

            // Snake
            for (int i = 0; i < snakeLength; i++) {
                if (i == 0) {
                    g.setColor(colorbg);
                    g.fillOval(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    if (colorbg == Color.red) {
                        g.setColor(new Color(255, 112, 112));
                    }
                    if (colorbg == Color.green) {
                        g.setColor(new Color(112, 255, 112));
                    }
                    if (colorbg == Color.blue) {
                        g.setColor(new Color(112, 112, 255));
                    }

                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
        } else {
            showMenuScreen(g);
        }

        // Score
        g.setColor(Color.white);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Score: " + score, 20, 20);
    }

    private void showMenuScreen(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));

        if (score > 0 || !running) {
            g.drawString("SNAKE GAME", getWidth() / 2 - 60, getHeight() / 2 - 80);
        }

        if (score > 0 && !running) {
            g.drawString("GAME OVER", getWidth() / 2 - 55, getHeight() / 2 - 40);
            g.drawString("Final Score: " + score, getWidth() / 2 - 65, getHeight() / 2 - 10);
        }

        g.drawString("Click Start to Play", getWidth() / 2 - 80, getHeight() / 2 + 30);
        startbtn.setVisible(true);
        menu.setVisible(true);
    }

    public void menuoption() {
        final Frame f = new Frame("Menu");
        f.setLayout(new FlowLayout());
        f.setSize(250, 250);

        choice1 = new Choice();
        Label l1 = new Label("Choose Color:");
        Label l2 = new Label("Choose Music:");
        credits = new Button("credits");
        logo = new Button("logo");

        credits.setPreferredSize(new Dimension(100, 50));
        logo.setPreferredSize(new Dimension(100, 50));
        f.add(l1);

        choice1.add("red");
        choice1.add("green");
        choice1.add("blue");
        f.add(choice1);
        f.add(l2);
        choice2 = new Choice();
        choice2.add("musicbg.wav");
        choice2.add("musicbg.wav");

        f.add(choice2);
        choice1.addItemListener(this);
        choice2.addItemListener(this);
        f.add(credits);
        f.add(logo);
        credits.addActionListener(this);
        logo.addActionListener(this);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                f.dispose();
            }
        });
        f.setVisible(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ENTER && !running) {
            startGameSequence();
        }

        if ((key == KeyEvent.VK_LEFT) && (direction != 'R'))
            direction = 'L';
        if ((key == KeyEvent.VK_RIGHT) && (direction != 'L'))
            direction = 'R';
        if ((key == KeyEvent.VK_UP) && (direction != 'D'))
            direction = 'U';
        if ((key == KeyEvent.VK_DOWN) && (direction != 'U'))
            direction = 'D';
    }

    private void startGameSequence() {
        running = true;
        if (endmusic != null) {
            endmusic.stop();
        }
        startbtn.setVisible(false);
        menu.setVisible(false);
        resetGame();
        if (backgroundMusic != null) {
            backgroundMusic.loop();
        }
        this.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startbtn) {
            startGameSequence();
        }
        if (e.getSource() == menu) {
            menuoption();
            repaint();
        }
        if (e.getSource() == credits) {
            outromethod();
        }
        if (e.getSource() == logo) {
            logomethod();
        }

    }

    public void keyReleased(KeyEvent ke) {
    }

    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == choice1) {
            if (choice1.getSelectedItem() == "red") {
                colorbg = Color.red;
            } else if (choice1.getSelectedItem() == "green") {
                colorbg = Color.green;
            } else if (choice1.getSelectedItem() == "blue") {
                colorbg = Color.blue;
            }
        }
        if (e.getSource() == choice2) {
            if (choice2.getSelectedItem() == "musicbg.wav") {
                backgroundMusic = getAudioClip(getCodeBase(), "musicbg.wav");
            } else if (choice2.getSelectedItem() == "musicbg.wav") {
                backgroundMusic = getAudioClip(getCodeBase(), "musicbg.wav");
            } else if (choice2.getSelectedItem() == "musicbg.wav") {
                backgroundMusic = getAudioClip(getCodeBase(), "musicbg.wav");
            } else if (choice2.getSelectedItem() == "musicbg.wav") {
                backgroundMusic = getAudioClip(getCodeBase(), "musicbg.wav");
            }
        }
        repaint();
    }

    void outromethod() {
        Frame f2 = new Frame("Credits");

        f2.setSize(400, 400);
        f2.setBackground(Color.BLACK);
        f2.setLayout(new GridLayout(9, 1));

        String[] credits = { "THANK YOU FOR PLAYING", "Created by:", "savio,manoj,balaji,sai","262", "Music:", "musicbg", "Art:", "microsoft ai" };

        for (int i = 0; i < credits.length; i++) {
            Label l = new Label(credits[i], Label.CENTER);
            l.setFont(new Font("Arial", i == 0 ? Font.BOLD : Font.PLAIN, i == 0 ? 24 : 16));
            l.setForeground(i == 0 ? Color.YELLOW : Color.WHITE);
            f2.add(l);
        }

        f2.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                f2.dispose();
            }
        });
        f2.setVisible(true);
    }

    void logomethod() {
        Frame f3 = new Frame("Credits");
        Image img = getImage(getDocumentBase(), "logo.png");

        f3.setSize(500, 500);
        f3.setBackground(Color.BLACK);
        f3.setLayout(new FlowLayout());

        Canvas canvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                g.drawImage(img, 75, 30, 250, 250, this);
            }
        };
        canvas.setPreferredSize(new Dimension(350, 400));
        f3.add(canvas);

        f3.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                f3.dispose();
            }
        });
        f3.setVisible(true);
    }

}