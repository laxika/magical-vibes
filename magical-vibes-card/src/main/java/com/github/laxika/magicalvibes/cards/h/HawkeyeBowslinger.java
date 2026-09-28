package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "596")
public class HawkeyeBowslinger extends Card {

    public HawkeyeBowslinger() {
        // Whenever you cast a spell that targets a creature, put a +1/+1 counter on Hawkeye. Scry 1.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new PutCountersOnSourceEffect(1, 1, 1), new ScryEffect(1)),
                new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate())
        ));
    }
}
