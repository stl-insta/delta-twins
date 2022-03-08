# deltatwinsMEI
# deltatwinsMEI

Algorithms presented in this code aim to solve the eternal-twins and delta-twins listing problem. Given a linkstream G = (T,V,E), two vertices of V are eternal-twins if their neighbourhoods are identical in V\{u,v} at each instant t of T. Given a linkstream G and an interger delta, two vertices of V are delta-twins if their neighbourhoods are identical in V\{u,v} for at least delta consecutive time instants of T.

The datafolder contains 4 datasets, 3 of which are collected from real-life data. Each linkstream is presentend by prompting its E, i.e its set of timed edges, each line corresponding to out timed edge, with the indexes of the two vertices connected by the edge and the number of the time instant at which the edge is present.

results.csv lists results of the benchmarks run in order to ascertain correctness and computation time of those algorithms.
