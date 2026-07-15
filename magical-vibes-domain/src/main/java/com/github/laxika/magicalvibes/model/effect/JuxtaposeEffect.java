package com.github.laxika.magicalvibes.model.effect;

/**
 * Juxtapose: "You and target player exchange control of the creature you each control with the
 * greatest mana value. Then exchange control of artifacts the same way. If two or more permanents a
 * player controls are tied for greatest, their controller chooses one of them."
 *
 * <p>Targets a single player ({@link #canTargetPlayer()}); the controller and that player each
 * contribute their greatest-mana-value creature (then, separately, artifact) and swap control. The
 * two exchanges are resolved one type at a time — creatures first, then artifacts on the updated
 * board — so an artifact creature can be moved in both steps. Ties are resolved by the controlling
 * player choosing which tied permanent participates. Driven by {@code JuxtaposeSupport}.
 */
public record JuxtaposeEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.PLAYER); }
}
