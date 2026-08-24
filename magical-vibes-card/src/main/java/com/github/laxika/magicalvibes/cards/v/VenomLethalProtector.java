package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDrawAndMayPutPermanentEffect;

public class VenomLethalProtector extends Card {

    public VenomLethalProtector() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificeAnotherCreatureDrawAndMayPutPermanentEffect(),
                "You may sacrifice another creature."));
    }
}
