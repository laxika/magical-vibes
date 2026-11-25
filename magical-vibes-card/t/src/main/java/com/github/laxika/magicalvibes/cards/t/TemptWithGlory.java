package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "C13", collectorNumber = "24")
@CardRegistration(set = "CMA", collectorNumber = "26")
public class TemptWithGlory extends Card {

    public TemptWithGlory() {
        addSpellEffect(counters(Subjects.you()));
        addSpellEffect(TemptingOfferEffect.of(
                "Put a +1/+1 counter on each creature you control? If you do, its controller puts another +1/+1 "
                        + "counter on each creature they control.",
                TemptWithGlory::counters));
    }

    /** "[players] put a +1/+1 counter on each creature they control". */
    private static CardEffect counters(PlayerSubject players) {
        return new PutCountersEffect(
                Subjects.controlledBy(players, new PermanentIsCreaturePredicate()), CounterType.PLUS_ONE_PLUS_ONE)
                .placedByEachController();
    }
}
