package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "CHK", collectorNumber = "245")
public class StrengthOfCedars extends Card {

    public StrengthOfCedars() {
        // Target creature gets +X/+X until end of turn, where X is the number of lands you control.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER),
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
