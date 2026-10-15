package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;

@CardRegistration(set = "5ED", collectorNumber = "197")
@CardRegistration(set = "CHR", collectorNumber = "39")
@CardRegistration(set = "LEG", collectorNumber = "121")
@CardRegistration(set = "ME3", collectorNumber = "78")
public class TheWretched extends Card {

    public TheWretched() {
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(new SourceIsAttacking(),
                        new GainControlOfAllPermanentsMatchingEffect(new PermanentBlockingSourcePredicate(),
                                ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD), false));
    }
}
