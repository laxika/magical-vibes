package com.github.laxika.magicalvibes.model.effect;

/**
 * Which permanent {@link PutTargetOnTopOfLibraryEffect} puts on top of its owner's library.
 */
public enum PutOnTopOfLibraryScope {
    TARGET,
    SELF,
    /** Both the source permanent and the chosen target permanent (Void Stalker). */
    SELF_AND_TARGET
}
