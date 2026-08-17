package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ORI", collectorNumber = "188")
@CardRegistration(set = "M20", collectorNumber = "182")
@CardRegistration(set = "ROE", collectorNumber = "196")
public class MightOfTheMasses extends Card {

    public MightOfTheMasses() {
        // Target creature gets +1/+1 until end of turn for each creature you control.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
    }
}
