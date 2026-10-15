package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;

@CardRegistration(set = "EMN", collectorNumber = "176")
public class UlvenwaldObserver extends Card {

    private static final TriggeringPermanentConditionalEffect DEATH_TRIGGER =
            new TriggeringPermanentConditionalEffect(
                    new PermanentToughnessAtLeastPredicate(4), new DrawCardEffect(1));

    public UlvenwaldObserver() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, DEATH_TRIGGER);
        addEffect(EffectSlot.ON_DEATH, DEATH_TRIGGER);
    }
}
