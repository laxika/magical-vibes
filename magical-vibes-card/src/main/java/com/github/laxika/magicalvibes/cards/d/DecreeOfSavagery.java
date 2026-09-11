package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SCG", collectorNumber = "115")
public class DecreeOfSavagery extends Card {

    private static final String PROMPT = "Put four +1/+1 counters on target creature?";

    public DecreeOfSavagery() {
        addEffect(EffectSlot.SPELL, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 4, new PermanentIsCreaturePredicate()));

        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.PLUS_ONE_PLUS_ONE, 4, new PermanentIsCreaturePredicate()),
                PROMPT));
        addCycling("{4}{G}{G}");
    }
}
