package com.github.laxika.magicalvibes.model.rules;

/** A change to a permanent that is not a zone change but that abilities trigger on. */
public enum PermanentChange {
    /** It phased out (CR 702.26a). Abilities that trigger on it look back in time (CR 603.10b). */
    PHASED_OUT,
    /** It phased in (CR 702.26a). */
    PHASED_IN,
    /** It transformed from its front face to its back face (CR 701.27a). */
    TRANSFORMED_TO_BACK,
    /** It transformed from its back face to its front face (CR 701.27a). */
    TRANSFORMED_TO_FRONT,
    /** It was turned face up (CR 708.8). */
    TURNED_FACE_UP
}
