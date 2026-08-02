package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "14")
public class HondenOfCleansingFire extends Card {

    public HondenOfCleansingFire() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new GainLifeEffect(new Scaled(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER), 2)));
    }
}
