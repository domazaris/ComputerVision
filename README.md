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
