package com.github.laxika.magicalvibes.model.effect;

/** Who gains the life applied by a {@link GainLifeEffect}. */
public enum GainLifeRecipient {
    /** The controller of the spell/ability (the default for almost every card). */
    CONTROLLER,
    /** The controller of the effect's target permanent (e.g. Condemn: "its controller gains life"). */
    TARGET_CONTROLLER
}
