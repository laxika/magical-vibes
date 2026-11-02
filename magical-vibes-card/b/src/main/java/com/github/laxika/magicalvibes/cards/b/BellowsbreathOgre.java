package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YNEO", collectorNumber = "21")
public class BellowsbreathOgre extends Card {

    public BellowsbreathOgre() {
        setStartingIntensity(1);

        // Whenever this creature attacks, it deals damage equal to its intensity to any target.
        // Then this creature intensifies by 1.
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new DealDamageToAnyTargetEffect(new SourceIntensity()),
                new IntensifySourceCardEffect(1)));
    }
}
