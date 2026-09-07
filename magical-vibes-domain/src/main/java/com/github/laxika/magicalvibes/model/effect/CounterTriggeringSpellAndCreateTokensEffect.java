package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Counters the spell that caused a spell-cast trigger, then creates the supplied tokens for that
 * spell's controller. The caster and the spell's mana value are carried independently by the
 * triggered stack entry so token creation still happens if the spell cannot be countered or has
 * already left the stack.
 */
public record CounterTriggeringSpellAndCreateTokensEffect(CreateTokenEffect token)
        implements TokenCreatingEffect, TriggeringSpellManaValueEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return token.amount();
    }

    @Override
    public CardType tokenType() {
        return token.primaryType();
    }

    @Override
    public int tokenPower() {
        return token.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return token.tokenToughness();
    }
}
