package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "69")
public class HondenOfSeeingWinds extends Card {

    public HondenOfSeeingWinds() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DrawCardEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER)));
    }
}
