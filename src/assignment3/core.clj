(ns assignment3.core
  (:gen-class)
  (:import [java.awt.image BufferedImage])
  (:import [javax.imageio ImageIO])
  (:import [java.io File])
)

(def filters [
               [-1, 0, 1, -2, 0, 2, -1, 0, 1],
               [-2, -1, 0, -1,  0, 1, 0,  1, 2],
               [-1, -2, -1, 0, 0, 0, 1, 2, 1],
               [0, -1, -2, 1, 0, -1, 2, 1, 0]
               [1, 0, -1, 2, 0, -2, 1, 0, -1],
               [2, 1, 0, 1, 0, -1, 0, -1, -2],
               [1, 2, 1, 0, 0, 0, -1, -2, -1],
               [0, 1, 2, -1, 0, 1, -2, -1, 0]
             ]
)


(defn new-image
  "Function to create a new image."
  [width height]
  (BufferedImage. width height BufferedImage/TYPE_INT_RGB)
)

(defn read-image-slow
  "Function to read an image from a file."
  [filename]
    (let [file (File. filename)]
      (ImageIO/read file)
    )
)

(def read-image (memoize read-image-slow))

(defn save-image
  "Function to save an image with a particular extension to a file."
  [image extension filename]
    (let [file (File. filename)]
      (ImageIO/write image extension file)
    )
)

(defn get-width-slow
  "Function to get the width of an image."
  [image]
  (.getWidth image)
)
(def get-width (memoize get-width-slow))

(defn get-height-slow
  "Function to get the height of an image."
  [image]
  (.getHeight image)
)
(def get-height (memoize get-height-slow))

(defn get-rgb-slow
  "Function to get the RGB components of a pixel in a vector of length 3."
  [image x y]
    (let [rgb (.getRGB image x y)
          red (bit-shift-right (bit-and rgb 0xFF0000) 16)
          blue (bit-shift-right (bit-and rgb 0xFF00) 8)
          green (bit-and rgb 0xFF)
          ]
        (vec (list red green blue))
      )
)
(def get-rgb (memoize get-rgb-slow))

(defn set-rgb
  "Function to set the RGB components of a pixel."
  [image x y [red green blue]]
     (let [rgb (+ (bit-shift-left red 16)
                  (bit-shift-left blue 8)
                  (bit-shift-left green 0) ) ]
       (.setRGB image x y rgb)
   )
)

(defn set-grey
  "Function to set the grey value of a pixel."
  [image x y grey]
  (set-rgb image x y [grey grey grey])
)

(defn get-val-slow 
  "Gets the average value from RGB"
  [image x y]
  (int (/ (reduce + (get-rgb image x y) ) 3.0))
)
(def get-val (memoize get-val-slow))

(defn apply-filter-slow 
  "Apllies the given filter i to the image at [x, y]"
  [image output x y i]
  (let [iwidth  (- (get-width image) 1)
        iheight (- (get-height image) 1)]
  (def new_val 0)
  (dotimes [h 3]
    (dotimes [w 3]
      (if (or (< (+ x (- w 1)) 0 ) (< (+ y (- h 1)) 0) (> (+ x (- w 1)) iwidth) (> (+ y (- h 1)) iheight)) ()
        (def new_val  (+ new_val 
                          (* 
                             (get-val image (+ x (- w 1)) (+ y (- h 1)))
                             (nth  (nth filters i) 
                                   (+ (* h 3) 
                                      w
                                   )
                             )
                          )
                      )
        )
      )
    )
  )
  (set-grey output x y (min 255 (max 0 (+ 127 new_val))))
  )
)
(def apply-filter (memoize apply-filter-slow))

(defn kirsh-slow 
  "Applies the given kirsh filter i to the input image"
  [file i]
  (let  [ 
          input (read-image file)
          iwidth (get-width input)
          iheight (get-height input)
          output (new-image iwidth iheight)
        ]
        (dotimes [x iwidth]
          (dotimes [y iheight]
            (apply-filter input output x y i)
          )
        )
        (do output)
  )
)
(def kirsh (memoize kirsh-slow))

(defn apply-all-kirsh-slow 
  "Applies all the kirsh filters to the given image"
  [file]
  (doall (map #(kirsh file %) (range 8)))
)
(def apply-all-kirsh (memoize apply-all-kirsh-slow))

(defn normalize-slow 
  "Normalizes a given histogram"
  [histogram]
  (vec (doall (map #(double(/ % (reduce + histogram))) histogram)))
)
(def normalize (memoize normalize-slow))

(defn bin-image-slow 
  "Takes the vales of each pixel in an image, and sorts them in to n_bins bins"
  [image width height n_bins]
  (let  [
          bin_size (int (/ 256 n_bins))
        ]
        (def bins (vec (make-array Integer/TYPE n_bins)))
        (dotimes [x width]
          (dotimes [y height]
            (def bins (assoc bins (int (/ (get-val image x y) bin_size)) (inc (nth bins (int (/ (get-val image x y) bin_size))))))
          )
        )
        (do bins)
  )
)
(def bin-image (memoize bin-image-slow))

(defn edge-magnitude-hist-slow 
  "Returns a normalized histogram of the edge magnitute of image file"
  [file]
  (let [
          images    (apply-all-kirsh file)
          first_img (nth images 0)
          iwidth    (get-width first_img)
          iheight   (get-height first_img)
          mag       (new-image iwidth iheight)
        ]

        ;; combine each image to get the magnitude image
        (dotimes [x iwidth]
          (dotimes [y iheight]
            (set-grey mag x y (apply max (doall (for [img images] (get-val img x y)))))
          )
        )

        ;; Sort into 8 different bins & normalize
        (normalize (bin-image mag iwidth iheight 8))
  )
)
(def edge-magnitude-hist (memoize edge-magnitude-hist-slow))

(defn get-index-slow 
  "Gets the indexes of any value in items that matches `item`"
  [item items]
  (keep-indexed #(when (= %2 item) %1) items)
)
(def get-index (memoize get-index-slow))

(defn argmax-slow 
  "Gets the argmax of the given image at x,y"
  [images x y]
  (let  [
          image_values (vec (doall (for [img images] (get-val img x y))))
        ]
        (first (get-index (apply max (doall image_values)) image_values))
  )
)
(def argmax (memoize argmax-slow))

(defn edge-direction-hist-slow 
  "Returns a normalized histogram of the edge direction of image file"
  [file]
  (let [
          images    (apply-all-kirsh file)
          first_img (nth images 0)
          iwidth    (get-width first_img)
          iheight   (get-height first_img)
          dir       (new-image iwidth iheight)
        ]

        ;; combine each image to get the direction image
        (def bins (vec (make-array Integer/TYPE 8)))
        (dotimes [x iwidth]
          (dotimes [y iheight]
            (def bins (assoc bins (argmax images x y) (inc (nth bins (argmax images x y)))))
          )
        )

        ;; Sort into 8 different bins & normalize
        (normalize bins)
  )
)
(def edge-direction-hist (memoize edge-direction-hist-slow))

(defn intensity-slow 
  "Returns the intensity histogram for the given image file"
  [file]
  (let [
          image     (read-image file)
          iwidth    (get-width image)
          iheight   (get-height image)
        ]
        (normalize (bin-image image iwidth iheight 8))
  )
)
(def intensity (memoize intensity-slow))

(defn image-descriptor-slow 
  "Returns the image descriptor for the given image"
  [file]
  (let [
    edge_dir  (vec (edge-direction-hist file))
    edge_mag  (vec (edge-magnitude-hist file))
    intensity (vec (intensity file))
  ]
  (normalize (concat edge_dir edge_mag intensity))
)
)
(def image-descriptor (memoize image-descriptor-slow))

(defn image-similarity-slow 
  "Returns a number between 0->1 which describes how similar two image files are"
  [file1 file2]
  (let  [
          image1 (image-descriptor file1)
          image2 (image-descriptor file2)
        ]
        (reduce + (doall (map min image1 image2)))
)
)
(def image-similarity (memoize image-similarity-slow))

(defn -main
  "Takes 2 images and outputs the similarity"
  [& args]
  (println "Files:     " (first args) (second args))
  (println "Similarity:" (image-similarity (first args) (second args)))
)
