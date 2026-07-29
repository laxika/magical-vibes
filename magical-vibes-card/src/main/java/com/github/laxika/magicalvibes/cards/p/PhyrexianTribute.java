package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "134")
public class PhyrexianTribute extends Card {

    public PhyrexianTribute() {
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.SPELL, new SacrificeMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
