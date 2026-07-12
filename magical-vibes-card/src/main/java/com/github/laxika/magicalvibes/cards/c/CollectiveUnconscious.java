package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "8ED", collectorNumber = "238")
public class CollectiveUnconscious extends Card {

    public CollectiveUnconscious() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
    }
}
