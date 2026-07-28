package com.github.laxika.magicalvibes.cards.l;

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

@CardRegistration(set = "ICE", collectorNumber = "357")
public class LandCap extends Card {

    public LandCap() {
        // This land doesn't untap during your untap step if it has a depletion counter on it.
        addEffect(EffectSlot.STATIC, new DoesntUntapWithCounterEffect(CounterType.DEPLETION));

        // At the beginning of your upkeep, remove a depletion counter from this land.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new RemoveCounterFromSourceEffect(CounterType.DEPLETION, 1));

        // {T}: Add {W} or {U}. Put a depletion counter on this land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE), new PutCountersOnSelfEffect(CounterType.DEPLETION)),
                "{T}: Add {W}. Put a depletion counter on Land Cap."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE), new PutCountersOnSelfEffect(CounterType.DEPLETION)),
                "{T}: Add {U}. Put a depletion counter on Land Cap."
        ));
    }
}
