package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAnyNumberOfPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOK", collectorNumber = "94")
public class BarrelDownSokenzan extends Card {

    public BarrelDownSokenzan() {
        // Sweep — Return any number of Mountains you control to their owner's hand.
        target(TargetFilters.creature());
        addEffect(EffectSlot.SPELL,
                new ReturnAnyNumberOfPermanentsToHandEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)));

        // Barrel Down Sokenzan deals damage to target creature equal to twice the number of
        // Mountains returned this way.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(
                new Scaled(new EventValue(), 2)));
    }
}
