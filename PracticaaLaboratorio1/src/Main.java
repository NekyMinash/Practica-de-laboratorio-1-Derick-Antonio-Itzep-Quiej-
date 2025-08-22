
import java.util.Scanner;
import java.util.Random;

/* ==================== MODELO JUGADOR (sin colecciones) ==================== */
class Jugador {
    String nombre, username;
    int edad;


    int tr_ganadas = 0, tr_empates = 0, tr_perdidas = 0;

    // Carreras: conteo de posiciones logradas (1..4) acumuladas en varias partidas
    int[] car_posiciones = new int[5]; // index 1..4 (0 sin usar)


    int ad_ganadas = 0;
    int[] ad_intentosHist = new int[100];
    int ad_histCount = 0;

    Jugador(String n, int e, String u) { nombre = n; edad = e; username = u; }

    void registrarIntentosAdivina(int intentos) {
        if (ad_histCount < ad_intentosHist.length) ad_intentosHist[ad_histCount++] = intentos;
    }
}

class AlmacenJugadores {
    static final int MAX = 20;
    Jugador[] lista = new Jugador[MAX];
    int total = 0;

    Jugador registrar(Scanner sc) {
        if (total >= MAX) {
            System.out.println("Capacidad máxima de jugadores alcanzada.");
            return null;
        }
        System.out.print("Nombre: ");
        String n = sc.nextLine().trim();
        int e = leerEntero(sc, "Edad: ", 1, 150);
        String u;
        while (true) {
            System.out.print("Username: ");
            u = sc.nextLine().trim();
            if (u.isEmpty()) { System.out.println("No puede estar vacío."); continue; }
            if (buscarPorUsername(u) != null) { System.out.println("Ya existe. Elige otro."); continue; }
            break;
        }
        Jugador j = new Jugador(n, e, u);
        lista[total++] = j;
        System.out.println("Registrado: " + j.username + " (" + j.nombre + ", " + j.edad + ")");
        return j;
    }

    Jugador buscarPorUsername(String u) {
        for (int i = 0; i < total; i++) if (lista[i].username.equals(u)) return lista[i];
        return null;
    }

    Jugador elegir(Scanner sc, String etiqueta) {
        while (true) {
            System.out.println("\n" + etiqueta + ": (1) Nuevo  (2) Existente  (3) Regresar");
            System.out.print("> ");
            String op = sc.nextLine().trim();
            if (op.equals("1")) return registrar(sc);
            if (op.equals("2")) {
                if (total == 0) { System.out.println("No hay jugadores. Crea uno."); continue; }
                listarCompacto();
                int idx = leerEntero(sc, "Seleccione índice (1.." + total + "): ", 1, total) - 1;
                return lista[idx];
            }
            if (op.equals("3")) return null;
            System.out.println("Opción inválida.");
        }
    }

    void listar() {
        if (total == 0) { System.out.println("No hay jugadores."); return; }
        System.out.println("\n*** Lista De Jugadores ***");
        for (int i = 0; i < total; i++) {
            Jugador j = lista[i];
            System.out.println((i+1) + ") " + j.username + " | Nombre: " + j.nombre + " | Edad: " + j.edad +
                    " | TR[G:" + j.tr_ganadas + " E:" + j.tr_empates + " P:" + j.tr_perdidas + "]" +
                    " | CAR(Pos1:" + j.car_posiciones[1] + ", Pos2:" + j.car_posiciones[2] +
                    ", Pos3:" + j.car_posiciones[3] + ", Pos4:" + j.car_posiciones[4] + ")" +
                    " | ADV(G:" + j.ad_ganadas + ", Hist:" + j.ad_histCount + ")");
        }
    }
    void listarCompacto() {
        System.out.println("\nJugadores:");
        for (int i = 0; i < total; i++)
            System.out.println((i+1) + ") " + lista[i].username + " (" + lista[i].nombre + ")");
    }

    static int leerEntero(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
            } catch (Exception ignored) {}
            System.out.println("Valor inválido. Intenta de " + min + " a " + max + ".");
        }
    }
}

//JUEGO 1: TRES EN RAYA
class TresEnRaya {
    final Scanner sc;
    final Random rnd = new Random();
    final AlmacenJugadores store;

    int n = 3;                 // tamaño del tablero
    char[][] tab;              // tablero
    Jugador a = null, b = null; // jugadores activos
    char sa = 'X', sb = 'O';   // símbolos

    TresEnRaya(Scanner sc, AlmacenJugadores store) { this.sc = sc; this.store = store; }

    void submenu() {
        if (!seleccionarPareja()) return;
        n = 3; reiniciarTablero();

        while (true) {
            System.out.println("\n*** Submenú Totito ***");
            System.out.println("1) Nueva partida");
            System.out.println("2) Cambiar tamaño (actual: " + n + ")");
            System.out.println("3) Cambiar jugadores");
            System.out.println("4) Regresar");
            System.out.print("> ");
            String op = sc.nextLine().trim();
            if (op.equals("1")) partida();
            else if (op.equals("2")) { n = AlmacenJugadores.leerEntero(sc, "Nuevo tamaño (>=3): ", 3, 99); reiniciarTablero(); }
            else if (op.equals("3")) { if (!seleccionarPareja()) return; reiniciarTablero(); }
            else if (op.equals("4")) return;
            else System.out.println("Opción inválida.");
        }
    }

    boolean seleccionarPareja() {
        System.out.println("\n— Seleccionar jugadores para Tres en Raya —");
        a = store.elegir(sc, "Jugador 1");
        if (a == null) return false;
        b = store.elegir(sc, "Jugador 2");
        if (b == null) return false;
        while (b == a) {
            System.out.println("No puedes usar el mismo jugador. Elige otro para Jugador 2.");
            b = store.elegir(sc, "Jugador 2");
            if (b == null) return false;
        }
        sa = solicitarSimbolo("Símbolo para " + a.username + " (ej. X): ");
        do {
            sb = solicitarSimbolo("Símbolo para " + b.username + " (ej. O): ");
            if (sb == sa) System.out.println("Debe ser distinto al del Jugador 1.");
        } while (sb == sa);
        return true;
    }

    void reiniciarTablero() {
        tab = new char[n][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) tab[i][j] = ' ';
    }

    void partida() {
        reiniciarTablero();
        Jugador turno = rnd.nextBoolean() ? a : b;
        char st = (turno == a) ? sa : sb;
        int jugadas = 0, total = n * n;
        System.out.println("[Info] Empieza al azar: " + turno.username + " (" + st + ")");
        imprimir(null);

        while (true) {
            System.out.println("\nTurno de " + turno.username + " (" + st + ")");
            int x = AlmacenJugadores.leerEntero(sc, "Fila (1.." + n + "): ", 1, n) - 1;
            int y = AlmacenJugadores.leerEntero(sc, "Columna (1.." + n + "): ", 1, n) - 1;
            if (tab[x][y] != ' ') { System.out.println("Casilla ocupada, elige otra."); continue; }
            tab[x][y] = st; jugadas++; imprimir(null);

            int[][] linea = lineaGanadora(st);
            if (linea != null) {
                imprimir(linea);
                System.out.println("[OK] Ganó " + turno.username + " (" + turno.nombre + ", " + turno.edad + ")");
                turno.tr_ganadas++;
                (turno == a ? b : a).tr_perdidas++;
                return;
            }
            if (jugadas == total) {
                System.out.println("[Info] ¡Empate! (+1 para ambos)");
                a.tr_empates++; b.tr_empates++;
                a.tr_ganadas++; b.tr_ganadas++;
                return;
            }
            if (turno == a) { turno = b; st = sb; } else { turno = a; st = sa; }
        }
    }

    int[][] lineaGanadora(char s) {
        // filas
        for (int i = 0; i < n; i++) {
            boolean ok = true;
            for (int j = 0; j < n; j++) if (tab[i][j] != s) { ok = false; break; }
            if (ok) { int[][] L = new int[n][2]; for (int j = 0; j < n; j++){ L[j][0]=i; L[j][1]=j; } return L; }
        }
        // columnas
        for (int j = 0; j < n; j++) {
            boolean ok = true;
            for (int i = 0; i < n; i++) if (tab[i][j] != s) { ok = false; break; }
            if (ok) { int[][] L = new int[n][2]; for (int i = 0; i < n; i++){ L[i][0]=i; L[i][1]=j; } return L; }
        }
        // diagonales
        boolean ok = true;
        for (int k = 0; k < n; k++) if (tab[k][k] != s) { ok = false; break; }
        if (ok) { int[][] L = new int[n][2]; for (int k=0;k<n;k++){ L[k][0]=k; L[k][1]=k; } return L; }
        ok = true;
        for (int k = 0; k < n; k++) if (tab[k][n-1-k] != s) { ok = false; break; }
        if (ok) { int[][] L = new int[n][2]; for (int k=0;k<n;k++){ L[k][0]=k; L[k][1]=n-1-k; } return L; }
        return null;
    }

    void imprimir(int[][] linea) {
        boolean[][] mask = null;
        if (linea != null) {
            mask = new boolean[n][n];
            for (int i = 0; i < linea.length; i++) mask[linea[i][0]][linea[i][1]] = true;
        }
        System.out.println();
        System.out.print("    ");
        for (int j = 1; j <= n; j++) System.out.print(String.format("%2d ", j));
        System.out.println();
        System.out.print("   ");
        for (int j = 0; j < n; j++) System.out.print("---");
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.printf("%2d|", i+1);
            for (int j = 0; j < n; j++) {
                char c = (tab[i][j]==' ') ? '.' : tab[i][j];
                if (mask!=null && mask[i][j]) System.out.print("[" + c + "]");
                else System.out.print(" " + c + " ");
            }
            System.out.println();
        }
    }

    char solicitarSimbolo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.length() > 0) return s.charAt(0);
            System.out.println("Ingresa un carácter.");
        }
    }
}

// JUEGO 2: CARRERAS
class Carreras {
    final Scanner sc;
    final Random rnd = new Random();
    final AlmacenJugadores store;

    // Configuración de pistas
    final String[] nombresPista = {"Corta", "Media", "Larga"};
    final int[] metrosPista = {60, 100, 160}; // a discreción
    int pistaIdx = 1; // Media por defecto

    // Selección de jugadores (2 a 4) o 1 humano + CPU
    Jugador[] enCarrera = new Jugador[4];
    int cant = 0;
    boolean cpuPresente = false;
    String cpuName = "CPU";

    Carreras(Scanner sc, AlmacenJugadores store) { this.sc = sc; this.store = store; }

    void submenu() {
        if (!configurarJugadores()) return;
        while (true) {
            System.out.println("\n*** Submenú Carreras ***");
            System.out.println("1) Nueva carrera");
            System.out.println("2) Cambiar pista (actual: " + nombresPista[pistaIdx] + " - " + metrosPista[pistaIdx] + "m)");
            System.out.println("3) Cambiar jugadores");
            System.out.println("4) Regresar");
            System.out.print("> ");
            String op = sc.nextLine().trim();
            if (op.equals("1")) carrera();
            else if (op.equals("2")) elegirPista();
            else if (op.equals("3")) { if (!configurarJugadores()) return; }
            else if (op.equals("4")) return;
            else System.out.println("Opción inválida.");
        }
    }

    boolean configurarJugadores() {
        cant = 0; cpuPresente = false;
        System.out.println("\n— Configurar jugadores de Carreras —");
        System.out.println("1) 2-4 jugadores humanos");
        System.out.println("2) 1 humano contra la computadora");
        System.out.print("> ");
        String op = sc.nextLine().trim();

        if (op.equals("2")) {
            Jugador j = store.elegir(sc, "Jugador Humano");
            if (j == null) return false;
            enCarrera[cant++] = j;
            cpuPresente = true;
            System.out.println("Compites contra la CPU.");
            return true;
        }
        int cuantos = AlmacenJugadores.leerEntero(sc, "¿Cuántos jugadores (2..4)?: ", 2, 4);
        for (int i = 0; i < cuantos; i++) {
            Jugador j = store.elegir(sc, "Jugador " + (i+1));
            if (j == null) return false;
            boolean repetido = false;
            for (int k = 0; k < cant; k++) if (enCarrera[k] == j) repetido = true;
            if (repetido) { System.out.println("Jugador repetido, elige otro."); i--; continue; }
            enCarrera[cant++] = j;
        }
        return true;
    }

    void elegirPista() {
        System.out.println("\nElige pista:");
        for (int i = 0; i < nombresPista.length; i++)
            System.out.println((i+1) + ") " + nombresPista[i] + " (" + metrosPista[i] + "m)");
        pistaIdx = AlmacenJugadores.leerEntero(sc, "> ", 1, nombresPista.length) - 1;
    }

    int tirarDados() { return (rnd.nextInt(6)+1) + (rnd.nextInt(6)+1); }

    void carrera() {
        int meta = metrosPista[pistaIdx];
        int[] pos = new int[4];
        String[] nombres = new String[4];
        for (int i = 0; i < cant; i++) nombres[i] = enCarrera[i].username;
        if (cpuPresente) { nombres[1] = cpuName; /* enCarrera[1] se usa solo conceptualmente */ }

        boolean fin = false;
        int[] ordenLlegada = new int[4]; int llegados = 0;
        System.out.println("\n[Info] Pista: " + nombresPista[pistaIdx] + " (" + meta + "m). ¡Arranca!");

        while (!fin) {

            int[] avance = new int[4];
            String[] eventos = new String[4];
            for (int i = 0; i < cant; i++) {
                int base = tirarDados();
                double r = rnd.nextDouble();
                if (r < 0.10) { // trampa
                    int penal = Math.max(1, base / 2);
                    avance[i] = base - penal;
                    eventos[i] = "Trampa (-" + penal + "m)";
                } else if (r > 0.90) {
                    int extra = Math.max(1, base / 2);
                    avance[i] = base + extra;
                    eventos[i] = "Booster (+" + extra + "m)";
                } else {
                    avance[i] = base;
                    eventos[i] = "—";
                }
            }
            for (int i = 0; i < cant; i++) {
                pos[i] += avance[i];
                if (pos[i] < 0) pos[i] = 0;
            }

            System.out.println("\n— Turno —");
            for (int i = 0; i < cant; i++) {
                System.out.println(nombres[i] + " avanza " + avance[i] + "m [" + eventos[i] + "], total: " + pos[i] + "m");
            }

            for (int i = 0; i < cant; i++) {
                if (pos[i] >= meta && !yaEnLista(ordenLlegada, llegados, i)) {
                    ordenLlegada[llegados++] = i;
                }
            }

            boolean todosLlegaron = true;
            for (int i = 0; i < cant; i++) if (pos[i] < meta) { todosLlegaron = false; break; }
            if (llegados > 0 && (todosLlegaron || llegados == cant)) fin = true;

        }

        if (llegados == 0) {
            int[] idx = new int[cant]; for (int i=0;i<cant;i++) idx[i]=i;
            for (int i=0;i<cant-1;i++) for (int j=0;j<cant-1-i;j++)
                if (pos[idx[j]] < pos[idx[j+1]]) { int t=idx[j]; idx[j]=idx[j+1]; idx[j+1]=t; }
            for (int i=0;i<cant;i++) ordenLlegada[llegados++]=idx[i];
        }

        System.out.println("\n=== RESULTADOS ===");
        for (int p = 0; p < llegados; p++) {
            int idx = ordenLlegada[p];
            String nom = nombres[idx];
            System.out.println((p+1) + "º: " + nom + " (" + pos[idx] + "m)");
        }
        for (int p = 0; p < llegados; p++) {
            int idx = ordenLlegada[p];
            Jugador j = null;
            if (cpuPresente) {
                if (idx == 1) j = null; // CPU, no registrar
                else j = enCarrera[idx==0?0:1];
            } else {
                j = enCarrera[idx];
            }
            if (j != null) {
                int posicion = p + 1; // 1..cant
                if (posicion >= 1 && posicion <= 4) j.car_posiciones[posicion]++;
            }
        }
    }

    boolean yaEnLista(int[] arr, int len, int val) {
        for (int i = 0; i < len; i++) if (arr[i] == val) return true;
        return false;
    }
}

// JUEGO 3: ADIVINA EL NÚMERO
class AdivinaNumero {
    final Scanner sc;
    final Random rnd = new Random();
    final AlmacenJugadores store;

    // Modos
    static class Modo { String nombre; int rangoMax; int turnos; Modo(String n,int r,int t){nombre=n;rangoMax=r;turnos=t;} }
    Modo[] modos = {
            new Modo("Fácil", 50, 5),
            new Modo("Medio", 100, 6),
            new Modo("Difícil", 200, 7)
    };
    int modoIdx = 1; // Medio por defecto

    Jugador[] lista = new Jugador[5];
    int cant = 0;

    AdivinaNumero(Scanner sc, AlmacenJugadores store) { this.sc = sc; this.store = store; }

    void submenu() {
        if (!configurarJugadores()) return;
        while (true) {
            System.out.println("\n*** Submenú Adivina El Número ***");
            System.out.println("1) Nueva partida");
            System.out.println("2) Cambiar modo (actual: " + modos[modoIdx].nombre + ")");
            System.out.println("3) Cambiar jugadores");
            System.out.println("4) Regresar");
            System.out.print("> ");
            String op = sc.nextLine().trim();
            if (op.equals("1")) partida();
            else if (op.equals("2")) elegirModo();
            else if (op.equals("3")) { if (!configurarJugadores()) return; }
            else if (op.equals("4")) return;
            else System.out.println("Opción inválida.");
        }
    }

    boolean configurarJugadores() {
        System.out.println("\n— Configurar jugadores (1..5) —");
        cant = AlmacenJugadores.leerEntero(sc, "¿Cuántos jugadores?: ", 1, 5);
        for (int i = 0; i < cant; i++) {
            Jugador j = store.elegir(sc, "Jugador " + (i+1));
            if (j == null) return false;
            boolean rep = false; for (int k=0;k<i;k++) if (lista[k]==j) rep=true;
            if (rep) { System.out.println("Repetido, elige otro."); i--; continue; }
            lista[i] = j;
        }
        return true;
    }

    void elegirModo() {
        System.out.println("\nElige modo:");
        for (int i = 0; i < modos.length; i++)
            System.out.println((i+1) + ") " + modos[i].nombre + " (1-" + modos[i].rangoMax + ", turnos " + modos[i].turnos + ")");
        modoIdx = AlmacenJugadores.leerEntero(sc, "> ", 1, modos.length) - 1;
    }

    void partida() {
        Modo m = modos[modoIdx];
        int secreto = rnd.nextInt(m.rangoMax) + 1;
        int[] orden = barajarOrden(cant);
        int[] intentosPorJugador = new int[cant];

        System.out.println("\n[Info] Modo " + m.nombre + " | Rango 1-" + m.rangoMax + " | " + m.turnos + " turnos por jugador");
        System.out.print("Orden de juego: ");
        for (int i=0;i<cant;i++) System.out.print(lista[orden[i]].username + (i<cant-1?", ":""));
        System.out.println();

        int ganadorIdx = -1;
        outer:
        for (int t = 0; t < m.turnos; t++) {
            System.out.println("\n— Ronda " + (t+1) + " —");
            for (int k = 0; k < cant; k++) {
                int idx = orden[k];
                Jugador j = lista[idx];
                int intento = AlmacenJugadores.leerEntero(sc, j.username + " adivina (1.." + m.rangoMax + "): ", 1, m.rangoMax);
                intentosPorJugador[idx]++;
                if (intento == secreto) {
                    System.out.println("[OK] ¡" + j.username + " acertó! El número era " + secreto);
                    ganadorIdx = idx;
                    break outer;
                } else if (intento < secreto) System.out.println("Mayor...");
                else System.out.println("Menor...");
            }
        }

        if (ganadorIdx >= 0) {
            Jugador g = lista[ganadorIdx];
            g.ad_ganadas++;
            g.registrarIntentosAdivina(intentosPorJugador[ganadorIdx]);
            System.out.println("Ganador: " + g.username + " | Intentos usados: " + intentosPorJugador[ganadorIdx]);
        } else {
            System.out.println("Nadie acertó. El número era: " + secreto);
        }
    }

    int[] barajarOrden(int n) {
        int[] a = new int[n];
        for (int i=0;i<n;i++) a[i]=i;
        for (int i = n-1; i > 0; i--) {
            int j = (int)Math.floor(Math.random()*(i+1));
            int tmp = a[i]; a[i]=a[j]; a[j]=tmp;
        }
        return a;
    }
}

//Menú Principal Y MAIN
public class Main {
    static final Scanner SC = new Scanner(System.in);
    static final AlmacenJugadores STORE = new AlmacenJugadores();

    public static void main(String[] args) {
        if (args.length > 0) {
            String p = args[0].toLowerCase();
            if (p.equals("tres")) { new TresEnRaya(SC, STORE).submenu(); return; }
            if (p.equals("carreras")) { new Carreras(SC, STORE).submenu(); return; }
            if (p.equals("adivina")) { new AdivinaNumero(SC, STORE).submenu(); return; }
        }
        menuPrincipal();
    }

    static void menuPrincipal() {
        while (true) {
            System.out.println("\n*** Menú Principal ***");
            System.out.println("1) Totito ");
            System.out.println("2) Carreras");
            System.out.println("3) Adivina el Número");
            System.out.println("4) Ver jugadores");
            System.out.println("5) Salir");
            System.out.print("> ");
            String op = SC.nextLine().trim();
            if (op.equals("1")) new TresEnRaya(SC, STORE).submenu();
            else if (op.equals("2")) new Carreras(SC, STORE).submenu();
            else if (op.equals("3")) new AdivinaNumero(SC, STORE).submenu();
            else if (op.equals("4")) STORE.listar();
            else if (op.equals("5")) { System.out.println("¡Gracias Por Jugar uwu !"); return; }
            else System.out.println("Opción inválida.");
        }
    }
}


