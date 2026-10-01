package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WHO", collectorNumber = "604")
public class UNITHeadquarters extends Card {

    public UNITHeadquarters() {
        CreateTokenEffect soldier = CreateTokenEffect.whiteSoldier(1).withTapped(true);
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, soldier);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, soldier);
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
