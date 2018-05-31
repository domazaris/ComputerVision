# assignment3

## Usage

### Compare Two Images

```
lein run img1 img2
```

### Run the experiment (Very slow if not cached)

```
lein run experiment
```

### Run the experiment without cache

```
mkdir cache_old
mv cache_dir/* cache_old/
lein run experiment
```

### Expected output

```
Average cars
         vs cars:    0.83291775
         vs trains:  0.74960124
         vs planes:  0.4894615
Average trains
         vs cars:    0.74960124
         vs trains:  0.7272747
         vs planes:  0.5034534
Average planes
         vs cars:    0.4894615
         vs trains:  0.5034534
         vs planes:  0.53174484
```