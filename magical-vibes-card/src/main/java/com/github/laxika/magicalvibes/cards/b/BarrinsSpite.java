package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.SacrificeOneOfTwoTargetCreaturesThenReturnOtherToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "235")
public class BarrinsSpite extends Card {

    public BarrinsSpite() {
        // Choose two target creatures controlled by the same player. Their controller chooses and
        // sacrifices one of them. Return the other to its owner's hand.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new SacrificeOneOfTwoTargetCreaturesThenReturnOtherToHandEffect());
        target(TargetFilters.creature());
        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);
    }
}
