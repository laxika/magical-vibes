package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;

@CardRegistration(set = "KTK", collectorNumber = "152")
public class SultaiFlayer extends Card {

    private static final TriggeringPermanentConditionalEffect DEATH_TRIGGER =
            new TriggeringPermanentConditionalEffect(
                    new PermanentToughnessAtLeastPredicate(4), new GainLifeEffect(4));

    public SultaiFlayer() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, DEATH_TRIGGER);
        addEffect(EffectSlot.ON_DEATH, DEATH_TRIGGER);
    }
}
