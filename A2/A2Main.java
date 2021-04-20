import java.util.*;

public class A2Main {
  // Obstsortenregister (String > int, int > String)
  private static Map<String, Integer> fruits = new HashMap<String, Integer>();
  private static String[]             names;

  // Obstsorte registrieren oder abrufen
  private static int getFruit(String name) {
    Integer v = fruits.get(name);
    if (v == null) {
      int s = fruits.size();
      fruits.put(name, Integer.valueOf(s));
      names[s] = name;
      return s;
    }
    return v.intValue();
  }

  public static void main(String[] args) {
    // Anzahl der Obstsorten
    int size;
    // gewünschte Obstsorten
    int[] requests;
    // Anzahl der Beobachtungen
    int n;
    // Kenntnisstand
    Matrix fruitMatrix;

    Scanner sc = new Scanner(System.in);
    size = sc.nextInt();
    sc.nextLine();
    names = new String[size];

    // initialer Kenntnisstand: jedes Obst könnte sich in jeder Schüssel
    // befinden
    fruitMatrix = new Matrix(size);

    // Wunschsorten einlesen
    String[] tokens = sc.nextLine().split(" ");
    requests = new int[tokens.length];
    for (int i = 0; i < tokens.length; ++i) requests[i] = getFruit(tokens[i]);

    // Beobachtungen einlesen
    n = sc.nextInt();
    sc.nextLine();
    for (int i = 0; i < n; ++i) {
      // Nummern der Schüsseln
      tokens = sc.nextLine().split(" ");
      int[] b = new int[tokens.length];
      for (int j = 0; j < tokens.length; ++j)
        b[j] = Integer.parseInt(tokens[j]) - 1;

      // Obstsorten
      tokens = sc.nextLine().split(" ");
      int[] a = new int[tokens.length];
      for (int j = 0; j < tokens.length; ++j) a[j] = getFruit(tokens[j]);

      // Beobachtungsaussagen kombinieren
      Matrix observation = new Matrix(size, a, b);
      fruitMatrix.combine(observation);
    }
    sc.close();
    System.out.println("Finaler Kenntnisstand bezüglich der Zuordnung:");
    for (int i = 0; i < size; ++i) {
      System.out.println(names[i] + ": "
          + Arrays.toString(bitMaskToArray(fruitMatrix.getBowls(i))));
    }
    // Menge der Wunschschüsseln
    int x = 0;
    // Bitmaske aller gewünschten Obstsorten
    int requestBitmask = 0;
    for (int i : requests) {
      requestBitmask |= 1 << i;
    }
    // weitere nötige Beobachtungen der Form int[] {<Obstgruppe als Bitmaske>,
    // <Schüsselgruppe als Bitmaske>}
    List<int[]> critical = new ArrayList<int[]>();
    int criticalMask = 0;
    for (int i : requests) {
      int bowls = fruitMatrix.getBowls(i);
      x |= bowls;
      int fruits = fruitMatrix.getFruits(bowls);
      // prüfen, ob in möglichen Schüsseln auch unerwünschte Obstsorten
      // enthalten sein können, d. h. ob in fruits Bits gesetzt sind, die nicht
      // in requestBitmask gesetzt sind -> aber nicht doppelt warnen
      if ((fruits | requestBitmask) != requestBitmask
          && (criticalMask & fruits) == 0) {
        critical.add(new int[] { fruits, bowls });
        criticalMask |= fruits;
      }
    }
    // Ausgabe der Ergebnisse
    System.out.println(
        "Menge der Schüsseln, in denen sich die Wunschsorten befinden:");
    System.out.println(Arrays.toString(bitMaskToArray(x)));
    if (Integer.bitCount(x) > requests.length) {
      System.out.println("Achtung! Schüsseln könnten auch andere Obstsorten "
          + "beinhalten! Kritisch sind folgende Gruppen:");
      for (int[] g : critical) {
        int[] fruitsInt = bitMaskToArray(g[0]);
        String[] fruitsString = new String[fruitsInt.length];
        for (int i = 0; i < fruitsString.length; ++i)
          fruitsString[i] = names[fruitsInt[i] - 1];
        System.out.println(Arrays.toString(fruitsString) + " in "
            + Arrays.toString(bitMaskToArray(g[1])));
      }
      System.out.println("Diese Schüsseln und Obstsorten "
          + "sollten genauer beobachtet werden!");
    }
  }

  // alle in mask gesetzten Bits als int[] zurückgeben
  private static int[] bitMaskToArray(int mask) {
    int[] rs = new int[Integer.bitCount(mask)];
    for (int i = 0; mask > 0; ++i) {
      // kleinstes Bit ermitteln
      rs[i] = Integer.numberOfTrailingZeros(mask) + 1;
      // kleinstes Bit entfernen
      mask = mask ^ (mask & -mask);
    }
    return rs;
  }

  private static class Matrix {
    // zeilenweise als Bitmaske speichern
    private int[] data;

    // alles auf 1 setzen
    public Matrix(int size) {
      data = new int[size];
      int mask = (1 << size) - 1;
      for (int i = 0; i < size; ++i) data[i] = mask;
    }

    // Matrix für eine Beobachtung erstellen
    public Matrix(int size, int[] a, int[] b) {
      data = new int[size];
      // alle Schüsseln
      int all = (1 << size) - 1;
      int mask = 0;
      // alle Schüsseln in b markieren
      for (int x : b) mask |= 1 << x;
      // Die Schüsselmaske für alle Obstsorten in a ist mask (die Schüsseln in
      // b), für alle anderen ist es ~mask bezüglich all, also all ^ mask
      int inv = all ^ mask;
      // alle auf inv setzen
      for (int i = 0; i < size; ++i) data[i] = inv;
      // und die Obstsorten in a "zurück"setzen
      for (int x : a) data[x] = mask;
    }

    // Nummern der Schüsseln für ein bestimmtes Obst
    public int getBowls(int f) {
      return data[f];
    }

    // Obstsorten für eine Schüsselmenge
    public int getFruits(int bowlMask) {
      int rs = 0;
      for (int i = 0; i < data.length; ++i) {
        if ((data[i] & bowlMask) != 0) rs |= 1 << i;
      }
      return rs;
    }

    // komponentenweise multiplizieren (oder logisches &), um Aussagen zu
    // verknüpfen
    public void combine(Matrix o) {
      for (int i = 0; i < data.length; ++i) data[i] &= o.data[i];
    }
  }
}
