package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "115")
@CardRegistration(set = "AFC", collectorNumber = "156")
public class DecreeOfSavagery extends Card {

    private static final String PROMPT = "Put four +1/+1 counters on target creature?";

    public DecreeOfSavagery() {
        addSpellEffect(PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 4, new PermanentIsCreaturePredicate()));

        addTrigger(Triggers.youCycleThisCard(), new MayEffect(
                PutCountersEffect.withTargetRestriction(
                        CounterType.PLUS_ONE_PLUS_ONE, 4, new PermanentIsCreaturePredicate()),
                PROMPT));
        addCycling("{4}{G}{G}");
    }
}
