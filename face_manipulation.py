import os
import sys
import cv2
dir = os.path.dirname(__file__)
sys.path.append(dir)
from tf2net_openvino import Object_Segmentation_TF2


class FaceMainipulation:

    def __init__(self) -> None:
        self.model_path = os.path.join(dir, 'saved_model/saved_model.xml')
        self.labelmap_path = os.path.join(dir, 'saved_model/labelmap.pbtxt')         
        self.debug_dir = os.path.join(dir, 'debug')
        os.makedirs(self.debug_dir, exist_ok=True)

    def load_model(self):
        self.segmentor = Object_Segmentation_TF2(self.model_path, self.labelmap_path, 0.65)
        self.segmentor.load_model()
    
    def free_model(self):
        del self.segmentor

    def segment_modified(self, image_path):
        try:
            image = cv2.imread(image_path)
            bboxes, segment_masks = self.segmentor(image)
            if len(segment_masks) > 0:
                merge_mask = segment_masks[0]
                for i in range(len(segment_masks)):
                    merge_mask = cv2.bitwise_or(merge_mask, segment_masks[i])
                alpha = 0.3
                colormap = cv2.applyColorMap(merge_mask, cv2.COLORMAP_JET)
                colormap = cv2.addWeighted(colormap, alpha, image.copy(), 1 - alpha, 0)
            else:
                colormap = image.copy()
            return colormap, merge_mask
        except Exception as e:
            print(e)
            return None

