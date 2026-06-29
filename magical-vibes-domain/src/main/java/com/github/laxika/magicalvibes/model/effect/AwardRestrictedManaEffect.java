package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;

import java.util.Set;

public record AwardRestrictedManaEffect(ManaColor color, int amount, Set<CardType> allowedSpellTypes) implements ManaProducingEffect {

    public void applyTo(ManaPool pool) {
        if (allowedSpellTypes.contains(CardType.INSTANT)
                && allowedSpellTypes.contains(CardType.SORCERY)
                && !allowedSpellTypes.contains(CardType.CREATURE)
                && !allowedSpellTypes.contains(CardType.ARTIFACT)) {
            if (color == ManaColor.COLORLESS) {
                pool.addInstantSorceryOnlyColorless(amount);
            } else {
                pool.addInstantSorceryOnlyColored(color, amount);
            }
        } else if (color == ManaColor.RED
                && allowedSpellTypes.contains(CardType.CREATURE)
                && allowedSpellTypes.contains(CardType.ARTIFACT)) {
            pool.addRestrictedRed(amount);
        } else {
            pool.add(color, amount);
        }
    }
}
