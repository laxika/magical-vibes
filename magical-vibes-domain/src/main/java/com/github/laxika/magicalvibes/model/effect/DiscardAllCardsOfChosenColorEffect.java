package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a color at resolution, then the player carried on the stack entry reveals
 * their hand and discards every card of that color. The normal form targets that player (Persecute);
 * {@link #damagedPlayer()} uses the non-targeting player bound by a combat-damage trigger (Crosis,
 * the Purger).
 */
public record DiscardAllCardsOfChosenColorEffect(boolean targetsPlayer)
        implements CombatDamageTriggerContextEffect {

    public DiscardAllCardsOfChosenColorEffect() {
        this(true);
    }

    public static DiscardAllCardsOfChosenColorEffect damagedPlayer() {
        return new DiscardAllCardsOfChosenColorEffect(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return targetsPlayer ? null : TriggerContext.DAMAGED_PLAYER;
    }
}
