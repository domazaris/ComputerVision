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

(defn get-width
  "Function to get the width of an image."
  [image]
  (.getWidth image)
)

(defn get-height
  "Function to get the height of an image."
  [image]
  (.getHeight image)
)

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

(defn get-rgb-pr
  "Function to get the RGB components of a pixel in a vector of length 3."
  [image x y]
  (print image)
  (get-rgb image x y)
)

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

(defn get-val [image x y]
  (int (/ (reduce + (get-rgb image x y) ) 3.0))
)

(defn apply-filter-slow [image output x y i]
  (def new_val 0)
  (def iwidth (- (get-width image) 1))
  (def iheight (- (get-height image) 1))
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

(def apply-filter (memoize apply-filter-slow))

(defn kirsh-slow [file i] 
  (def input (read-image file))
  (def iwidth (get-width input))
  (def iheight (get-height input))
  (def output (new-image iwidth iheight))

  (dotimes [x iwidth]
    (dotimes [y iheight]
      (apply-filter input output x y i)
    )
  )
  (do output)
)

(def kirsh (memoize kirsh-slow))

(defn apply-all-kirsh-slow [file]
  (doall (map kirsh [file file file file file file file file] (range 8)))
)

(def apply-all-kirsh (memoize apply-all-kirsh-slow))


(defn edge-magnitude-hist [file] 
    ;; Get process image with all filters
    (def images (apply-all-kirsh file))

    ;; Get dimension
    (def first_img (nth images 0))
    (def iwidth (get-width first_img))
    (def iheight (get-height first_img))

    ;; Def new image
    (def mag (new-image iwidth iheight))

    ;; combine each image to get the magnitude image
    (dotimes [x iwidth]
      (dotimes [y iheight]
        ;(println (max (doall (for [img images] (get-val img x y)))))
        (set-grey mag x y (apply max (doall (for [img images] (get-val img x y)))))
      )
    )
    (save-image mag "jpg" "/tmp/ass3mag.jpg")

    ;; Sort into 8 different bins (0->31, 32->63...)
)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (def file "vehicle_images/car1.jpg")
  (edge-magnitude-hist file)
  ; (save-image (kirsh file 0) "jpg" "/tmp/ass3out0.jpg")
  ; (save-image (kirsh file 1) "jpg" "/tmp/ass3out1.jpg")
  ; (save-image (kirsh file 2) "jpg" "/tmp/ass3out2.jpg")
  ; (save-image (kirsh file 3) "jpg" "/tmp/ass3out3.jpg")
  ; (save-image (kirsh file 4) "jpg" "/tmp/ass3out4.jpg")
  ; (save-image (kirsh file 5) "jpg" "/tmp/ass3out5.jpg")
  ; (save-image (kirsh file 6) "jpg" "/tmp/ass3out6.jpg")
  ; (save-image (kirsh file 7) "jpg" "/tmp/ass3out7.jpg")
)
 