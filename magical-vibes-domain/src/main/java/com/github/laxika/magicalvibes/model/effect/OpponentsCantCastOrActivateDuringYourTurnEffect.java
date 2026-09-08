package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: during the source controller's turn, their opponents can't cast spells and,
 * when enabled, can't activate abilities of artifact, creature, or enchantment permanents.
 * Used by Grand Abolisher (M12) and Voice of Victory (TDM).
 */
public record OpponentsCantCastOrActivateDuringYourTurnEffect(boolean restrictsActivatedAbilities) implements CardEffect {

    public OpponentsCantCastOrActivateDuringYourTurnEffect() {
        this(true);
    }
}
