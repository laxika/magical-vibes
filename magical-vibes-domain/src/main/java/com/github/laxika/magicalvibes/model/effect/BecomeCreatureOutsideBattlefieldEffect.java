package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/** Static effect for a self-referential creature characteristic outside the battlefield. */
public record BecomeCreatureOutsideBattlefieldEffect(
        int power,
        int toughness,
        List<CardSubtype> grantedSubtypes
) implements SelfOutsideBattlefieldCreatureEffect {

    public BecomeCreatureOutsideBattlefieldEffect {
        grantedSubtypes = List.copyOf(grantedSubtypes);
    }
}
