package com.github.laxika.magicalvibes.model.effect;

/**
 * Gain control of all lands the target player controls, permanently (Gilt-Leaf Archdruid).
 *
 * <p>Targets a player; at resolution the controller gains control of every land that player
 * controls at that moment via the standard layer-2 control machinery (one permanent
 * {@link GainControlOfTargetEffect} floating effect per land).
 */
public record GainControlOfAllLandsTargetPlayerControlsEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.PLAYER); }
}
