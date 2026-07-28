package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "361")
public class TimberlineRidge extends Card {

    public TimberlineRidge() {
        // This land doesn't untap during your untap step if it has a depletion counter on it.
        addEffect(EffectSlot.STATIC, new DoesntUntapWithCounterEffect(CounterType.DEPLETION));

        // At the beginning of your upkeep, remove a depletion counter from this land.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new RemoveCounterFromSourceEffect(CounterType.DEPLETION, 1));

        // {T}: Add {R} or {G}. Put a depletion counter on this land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED), new PutCountersOnSelfEffect(CounterType.DEPLETION)),
                "{T}: Add {R}. Put a depletion counter on Timberline Ridge."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN), new PutCountersOnSelfEffect(CounterType.DEPLETION)),
                "{T}: Add {G}. Put a depletion counter on Timberline Ridge."
        ));
    }
}
