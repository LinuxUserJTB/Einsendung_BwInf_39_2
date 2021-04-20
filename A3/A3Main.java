import java.util.*;

public class A3Main {
  public static void main(String[] args) {
    // Umfang U des Sees, Anzahl N der Häuser
    int U, N;
    // Adressen der Häuser
    int[] adr;
    // kleinste Anzahl an Stimmen für Umzug
    int minN;
    Scanner sc = new Scanner(System.in);
    U = sc.nextInt();
    N = sc.nextInt();
    minN = N / 2 + 1;
    adr = new int[N];
    for (int i = 0; i < N; ++i) {
      adr[i] = sc.nextInt();
    }
    sc.close();
    long totalTime = System.nanoTime();
    List<int[]> stable = new ArrayList<int[]>();
    // Adressen sortieren
    Arrays.sort(adr);
    // Prefixsummenarray zur Abfrage: wie viele Häuser stehen im Intervall
    // [a,b[? als prefixS[b] - prefixS[a]
    long prefixComputeTime = System.nanoTime();
    int[] prefixS = new int[U + 1];
    // Zunächst: wie viele Häuser stehen an Position x?
    for (int x : adr) ++prefixS[x + 1];
    for (int i = 1; i <= U; ++i) prefixS[i] += prefixS[i - 1];
    prefixComputeTime = System.nanoTime() - prefixComputeTime;
    System.out.println(
        "Berechnung des Prefixsummenarrays in " + fmtTime(prefixComputeTime));
    // Gegeben Eisbuden a und x, die b Positionen weiter im Uhrzeigersinn von a
    // steht: Wo steht die (eine) "optimale" Eisbude zwischen a und b, sodass
    // sich der Weg zu möglichst vielen Häusern verkürzt?
    long optimalMiddleComputeTime = System.nanoTime();
    int[][] optimalMiddleNum = new int[U][U];
    for (int a = 0; a < U; ++a) for (int b = 0; b < U; ++b) {
      int totalHouses = housesInInterval(prefixS, N, (a + 1) % U, (a + b) % U);
      if (totalHouses < 2) {
        optimalMiddleNum[a][b] = totalHouses;
        continue;
      }
      // initialisieren, u. a. für Trivialfälle b < 2
      int maxN = 0;
      // alle Positionen probieren
      for (int p = 1; p < b; ++p) {
        // erste Position im Bereich, in dem sich der Weg verkürzt
        int firstIn = ((p + 0 + 2) / 2 + a) % U;
        // erste Position, ab der sich der Weg nicht verkürzt
        int firstOut = ((p + b + 1) / 2 + a) % U;
        int numIn = housesInInterval(prefixS, N, firstIn, firstOut);
        if (numIn > maxN) {
          maxN = numIn;
        }
      }
      optimalMiddleNum[a][b] = maxN;
    }
    optimalMiddleComputeTime = System.nanoTime() - optimalMiddleComputeTime;
    System.out.println("Berechnung der optimalen Eisbudenposition zwischen"
        + " zwei gegebenen Eisbuden in " + fmtTime(optimalMiddleComputeTime));
    // alle Konstellationen durchgehen
    long bruteForceTime = System.nanoTime();
    for (int a = 0; a < U; ++a)
      for (int b = a + 1; b < U; ++b) for (int c = b + 1; c < U; ++c) {
        int[] positions = new int[] { a, b, c };
        int numBest = -1;
        // alle Klassen für beste Möglichkeiten probieren:
        // 1. 2 + 1 Eisbuden: hier gibt es 6 Möglichkeiten
        for (int x = 0; x < 3; ++x) for (int z = 1; z < 3; ++z) {
          // x: Intervall, in dem 2 Eisbuden stehen
          // y: Intervall, in dem 1 Eisbude steht
          int y = (x + z) % 3;
          // Positionen der 2 Eisbuden: Anfang und Ende von x
          int p0 = (positions[x] + 1) % U;
          int p1 = (positions[(x + 1) % 3] + U - 1) % U;
          // Intervallstart und -länge von y
          int y0 = positions[y];
          int yl = (positions[(y + 1) % 3] + U - y0) % U;
          // Häuser, die in x und y stehen (= Häuser, bei denen sich der Weg
          // verkürzen wird)
          int inX = housesInInterval(prefixS, N, p0, (p1 + 1) % U);
          int inY = optimalMiddleNum[y0][yl];
          if (inX < 0 || inY < 0) continue; // die Intervallgröße war negativ
          // Anzahl Stimmen für diese Option
          int num = inX + inY;
          if (numBest < num) {
            numBest = num;
          }
        }
        // 2. 1 + 1 + 1 Eisbuden: nur eine Möglichkeit
        int num = 0;
        for (int i = 0; i < 3; ++i) {
          int i0 = positions[i];
          int i1 = (positions[(i + 1) % 3] + U - positions[i]) % U;
          num += optimalMiddleNum[i0][i1];
        }
        if (numBest < num) {
          numBest = num;
        }
        // generierte optimale Konstellation L' überprüfen:
        if (numBest < minN) {
          stable.add(positions);
        }
      }
    bruteForceTime = System.nanoTime() - bruteForceTime;
    System.out.println(
        "Absuchen aller Eisbudenkonstellationen in " + fmtTime(bruteForceTime));
    totalTime = System.nanoTime() - totalTime;
    System.out.println("Gesamte Berechnung in " + fmtTime(totalTime));
    System.out
        .println("Es gibt " + stable.size() + " stabile Konstellationen:");
    if (stable.size() == 0) System.out.println("<empty list>");
    else for (int[] c : stable) System.out.println(Arrays.toString(c));
  }

// Häuser im Intervall [start,end[
  private static int housesInInterval(int[] prefixS, int N, int start,
      int end) {
    int num = prefixS[end] - prefixS[start];
    // Sonderfall beachten
    if (start > end) num += N;
    return num;
  }

// Zeitangaben formatieren (konstant 6 Nachkommastellen)
  private static String fmtTime(long nanos) {
    double time = nanos / 1000000000.0;
    return ((int) time) + "." + (Integer
        .toString(1000000 + (int) ((time % 1.0) * 1000000 + 0.5)).substring(1))
        + "s";
  }
}
