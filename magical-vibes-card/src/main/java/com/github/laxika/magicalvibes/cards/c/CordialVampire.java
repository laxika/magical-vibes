package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MH1", collectorNumber = "83")
public class CordialVampire extends Card {

    public CordialVampire() {
        var vampires = new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE);
        var trigger = new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, vampires);

        // Whenever this creature or another creature dies, put a +1/+1 counter on each Vampire you control.
        addEffect(EffectSlot.ON_DEATH, trigger);
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, trigger);
    }
}
