package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Trigger-condition record for {@code Triggers.youGainLife()}: "Whenever a [color] instant or sorcery spell causes you
 * to gain life, this deals {@code damage} damage to target creature or player" (Firesong and Sunspeaker). Never resolved
 * directly: the life-gain collector judges the life gain's source and puts {@link #triggeredEffect()} on the stack with
 * its target chosen as the ability triggers.
 */
public record DealDamageOnSpellLifeGainEffect(int damage, CardColor triggeringColor) implements CardEffect {

    /** The damage that goes on the stack: "target creature or player", never a planeswalker or a battle. */
    public CardEffect triggeredEffect() {
        return new DealDamageToTargetCreatureOrPlayerEffect(damage);
    }
}
