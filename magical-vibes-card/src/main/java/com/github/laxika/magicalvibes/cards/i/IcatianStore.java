package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersForManaEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "419")
public class IcatianStore extends Card {

    public IcatianStore() {
        // This land enters tapped, and its controller may choose not to untap it.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // At the beginning of your upkeep, if this land is tapped, put a storage counter on it.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new SourceIsTapped(), new PutCountersOnSelfEffect(CounterType.STORAGE)));

        // {T}, Remove any number of storage counters from this land: Add {W} for each removed.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new RemoveCountersForManaEffect(ManaColor.WHITE, CounterType.STORAGE)),
                "{T}, Remove any number of storage counters from this land: Add {W} for each storage counter removed this way."
        ));
    }
}
