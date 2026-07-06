package com.github.laxika.magicalvibes.model.effect;

/** Who mills cards when a {@link MillEffect} resolves, relative to the effect's controller. */
public enum MillRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    EACH_OPPONENT
}
