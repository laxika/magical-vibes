package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

public record DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype subtype, boolean gainLife) implements CardEffect {

    public DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype subtype) {
        this(subtype, false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
