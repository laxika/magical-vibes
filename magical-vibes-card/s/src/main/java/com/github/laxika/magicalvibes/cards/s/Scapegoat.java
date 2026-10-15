package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STH", collectorNumber = "14")
public class Scapegoat extends Card {

    public Scapegoat() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        target(TargetFilters.creatureYouControl(), 0, 99)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
