package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "AVR", collectorNumber = "66")
public class MassAppeal extends Card {

    public MassAppeal() {
        // Draw a card for each Human you control.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN), CountScope.CONTROLLER)));
    }
}
