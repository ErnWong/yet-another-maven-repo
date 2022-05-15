package io.github.matteas.trivial;

public class StandardLibrary {
    public static final String STANDARD_LIBRARY = ""
        + "B = S (K S) K;" // Bluebird. \abc->a(bc)
        + "C = S (S (K (S (K S) K)) S) (K K);" // Cardinal. \abc->acb
        + "W = S S (S K);"
        + "B1 = B B B;" // Blackbird. \abcd->a(bcd)
        + "M = S I I;" // Mockingbird. \a->aa
        + "T = C I;" // Thrush. \ab->ba
        + "Fn.Identity = I;"
        + "Fn.Compose = B;"
        + "Fn.Swap2nd3rd = C;"
        + "Fn.Discard2nd = K;"
        + "Fn.Duplicate2nd = W;"
        + "Fn.Compose2 = B1;"
        + "Bool.True = K;"
        + "Bool.False = K I;"
        + "Bool.If = I;"
        + "Bool.Not = C;"
        + "Bool.Or = M;"
        + "Bool.And = S S K;"
        + "Bool.Xor = C (B S (C B C)) I;"
        + "Bool.Eq = Fn.Compose2 Bool.Not Bool.Xor;"
        + "Pair = B C T;"
        + "Pair.First = T K;"
        + "Pair.Second = T (K I);"
        + "Pair.Swap = T (C Pair);"
        //+ "Pair.ShiftRight = ;"
        //+ "Pair.ShiftLeft = ;"
        + "Nat.0 = K I;"
        + "Nat.Succ = B (S B) I;"
        + "Nat.Pred._helperIncrement = S (C Pair Pair.Second) (C Nat.Succ Pair.Second);"
        + "Nat.Pred = B Pair.First (C (C (Nat.Pred._helperIncrement)) (Pair Nat.0 Nat.0));"
        + "Nat.Add = C I Nat.Succ;"
        + "Nat.Sub = C I Nat.Pred;"
        + "Nat.Mult = B;"
        + "Nat.Exp = B (C I) I;"
        + "Nat.IsZero = C (C (K Bool.False)) Bool.True;"
        + "Nat.Leq = B (B Nat.IsZero) Nat.Sub;"
        + "Nat.Eq = S (B S (B (B Bool.And) Nat.Leq)) (C Nat.Leq);"
        + "Nat.1 = Nat.Succ Nat.0;"
        + "Nat.2 = Nat.Succ Nat.1;"
        + "Nat.3 = Nat.Succ Nat.2;"
        + "Nat.4 = Nat.Succ Nat.3;"
        + "Nat.5 = Nat.Succ Nat.4;"
        + "Nat.6 = Nat.Succ Nat.5;"
        + "Nat.7 = Nat.Succ Nat.6;"
        + "Nat.8 = Nat.Succ Nat.7;"
        + "Nat.9 = Nat.Succ Nat.8;"
        + "Nat.10 = Nat.Succ Nat.9;"
        + "Nat.11 = Nat.Succ Nat.10;"
        + "Nat.12 = Nat.Succ Nat.11;"
        + "Nat.13 = Nat.Succ Nat.12;"
        + "Nat.14 = Nat.Succ Nat.13;"
        + "Nat.15 = Nat.Succ Nat.14;"
        + "Nat.16 = Nat.Succ Nat.15;"
        + "Nat.17 = Nat.Succ Nat.16;"
        + "Nat.18 = Nat.Succ Nat.17;"
        + "Nat.19 = Nat.Succ Nat.18;"
        + "Nat.20 = Nat.Succ Nat.19;"
        + "Nat.21 = Nat.Succ Nat.20;"
        + "Nat.22 = Nat.Succ Nat.21;"
        + "Nat.23 = Nat.Succ Nat.22;"
        + "Nat.24 = Nat.Succ Nat.23;"
        + "Nat.25 = Nat.Succ Nat.24;"
        + "Nat.26 = Nat.Succ Nat.25;"
        + "Nat.27 = Nat.Succ Nat.26;"
        + "Nat.28 = Nat.Succ Nat.27;"
        + "Nat.29 = Nat.Succ Nat.28;"
        + "Nat.30 = Nat.Succ Nat.29;"
        + "Nat.31 = Nat.Succ Nat.30;"
        + "Nat.32 = Nat.Succ Nat.31;"
        + "Nat.33 = Nat.Succ Nat.32;"
        + "Nat.34 = Nat.Succ Nat.33;"
        + "Nat.35 = Nat.Succ Nat.34;"
        + "Nat.36 = Nat.Succ Nat.35;"
        + "Nat.37 = Nat.Succ Nat.36;"
        + "Nat.38 = Nat.Succ Nat.37;"
        + "Nat.39 = Nat.Succ Nat.38;"
        + "Nat.40 = Nat.Succ Nat.39;"
        + "Nat.41 = Nat.Succ Nat.40;"
        + "Nat.42 = Nat.Succ Nat.41;"
        + "Nat.43 = Nat.Succ Nat.42;"
        + "Nat.44 = Nat.Succ Nat.43;"
        + "Nat.45 = Nat.Succ Nat.44;"
        + "Nat.46 = Nat.Succ Nat.45;"
        + "Nat.47 = Nat.Succ Nat.46;"
        + "Nat.48 = Nat.Succ Nat.47;"
        + "Nat.49 = Nat.Succ Nat.48;"
        + "Nat.50 = Nat.Succ Nat.49;"
        + "Nat.51 = Nat.Succ Nat.50;"
        + "Nat.52 = Nat.Succ Nat.51;"
        + "Nat.53 = Nat.Succ Nat.52;"
        + "Nat.54 = Nat.Succ Nat.53;"
        + "Nat.55 = Nat.Succ Nat.54;"
        + "Nat.56 = Nat.Succ Nat.55;"
        + "Nat.57 = Nat.Succ Nat.56;"
        + "Nat.58 = Nat.Succ Nat.57;"
        + "Nat.59 = Nat.Succ Nat.58;"
        + "Nat.60 = Nat.Succ Nat.59;"
        + "Nat.61 = Nat.Succ Nat.60;"
        + "Nat.62 = Nat.Succ Nat.61;"
        + "Nat.63 = Nat.Succ Nat.62;"
        + "Nat.64 = Nat.Succ Nat.63;"
        + "Nat.65 = Nat.Succ Nat.64;"
        + "Nat.66 = Nat.Succ Nat.65;"
        + "Nat.67 = Nat.Succ Nat.66;"
        + "Nat.68 = Nat.Succ Nat.67;"
        + "Nat.69 = Nat.Succ Nat.68;"
        + "Nat.70 = Nat.Succ Nat.69;"
        + "Nat.71 = Nat.Succ Nat.70;"
        + "Nat.72 = Nat.Succ Nat.71;"
        + "Nat.73 = Nat.Succ Nat.72;"
        + "Nat.74 = Nat.Succ Nat.73;"
        + "Nat.75 = Nat.Succ Nat.74;"
        + "Nat.76 = Nat.Succ Nat.75;"
        + "Nat.77 = Nat.Succ Nat.76;"
        + "Nat.78 = Nat.Succ Nat.77;"
        + "Nat.79 = Nat.Succ Nat.78;"
        + "Nat.80 = Nat.Succ Nat.79;"
        + "Nat.81 = Nat.Succ Nat.80;"
        + "Nat.82 = Nat.Succ Nat.81;"
        + "Nat.83 = Nat.Succ Nat.82;"
        + "Nat.84 = Nat.Succ Nat.83;"
        + "Nat.85 = Nat.Succ Nat.84;"
        + "Nat.86 = Nat.Succ Nat.85;"
        + "Nat.87 = Nat.Succ Nat.86;"
        + "Nat.88 = Nat.Succ Nat.87;"
        + "Nat.89 = Nat.Succ Nat.88;"
        + "Nat.90 = Nat.Succ Nat.89;"
        + "Nat.91 = Nat.Succ Nat.90;"
        + "Nat.92 = Nat.Succ Nat.91;"
        + "Nat.93 = Nat.Succ Nat.92;"
        + "Nat.94 = Nat.Succ Nat.93;"
        + "Nat.95 = Nat.Succ Nat.94;"
        + "Nat.96 = Nat.Succ Nat.95;"
        + "Nat.97 = Nat.Succ Nat.96;"
        + "Nat.98 = Nat.Succ Nat.97;"
        + "Nat.99 = Nat.Succ Nat.98;"
        + "Nat.100 = Nat.Succ Nat.99;"
        //+ "Nat.Div = some scary stuff TODO;"
        ;
}
