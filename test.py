#! /usr/bin/env python3
import os
import sys
import heapq
import random
import pathlib
import subprocess
import multiprocessing

def cmp_image(images):
    ''' Compares two images and returns a float from 0->1 '''
    cmd = "lein run {} {}".format(images["base"], images["cmp"])
    print("$ {}".format(cmd))
    val = float(subprocess.run(cmd, shell=True, check=True, stdout=subprocess.PIPE).stdout.decode())
    return {"cmp" : images["cmp"], "val" : val }

def find_images():
    ''' Returns a random image '''
    image_dir = "{}/{}".format(pathlib.Path(__file__).parent, "vehicle_images/")
    images = os.listdir(image_dir)

    image_paths = []
    for img in images:
        image_paths.append("{}/{}".format("vehicle_images", img))
    return image_paths

def test():
    ''' Picks a random image, then compares it to every other image in folder '''
    all_images = find_images()[:4]
    base_image = random.choice(all_images)
    print("Base Image: {}".format(base_image))

    experiments = []
    for img in all_images:
        experiments.append({ "base" : base_image, "cmp" : img })

    print("Running Experiments...")
    with multiprocessing.Pool(processes=4) as pool:
        results = pool.map(cmp_image, experiments)

        for res in results:
            print("{}:\t{}".format(res["cmp"], res["val"]))

    # for img in all_images:
    #     if img is not base_image:
    #         test_results[img] = cmp_image(base_image, img)
    # print("Top 5 Results:")
    # print(heapq.nlargest(5, test_results, key=test_results.get))



def main():
    ''' Main func'''
    test()
    

if __name__ == '__main__':
    main()