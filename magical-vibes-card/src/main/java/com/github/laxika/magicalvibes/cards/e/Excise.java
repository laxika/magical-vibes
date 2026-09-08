package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUnlessControllerPaysEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "8")
public class Excise extends Card {

    public Excise() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetPermanentUnlessControllerPaysEffect(new XValue()));
    }
}
