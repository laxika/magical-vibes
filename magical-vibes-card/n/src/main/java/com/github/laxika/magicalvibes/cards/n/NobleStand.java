package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

@CardRegistration(set = "NEM", collectorNumber = "14")
public class NobleStand extends Card {

    public NobleStand() {
        addEffect(EffectSlot.ON_ANY_CREATURE_BLOCKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentControlledBySourceControllerPredicate(),
                        new GainLifeEffect(2)));
    }
}
