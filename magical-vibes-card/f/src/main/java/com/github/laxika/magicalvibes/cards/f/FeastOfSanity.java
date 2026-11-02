package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "84")
public class FeastOfSanity extends Card {

    public FeastOfSanity() {
        addTrigger(Triggers.youDiscardCard(),
                SequenceEffect.of(new DealDamageToAnyTargetEffect(1), new GainLifeEffect(1)));
    }
}
