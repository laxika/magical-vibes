package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "75")
public class BloodCurdle extends Card {

    public BloodCurdle() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnChosenOwnPermanentEffect(
                                CounterType.MENACE, 1, new PermanentIsCreaturePredicate()));
    }
}
