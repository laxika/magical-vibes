package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;

@CardRegistration(set = "SPM", collectorNumber = "133")
@CardRegistration(set = "SPM", collectorNumber = "273")
public class KravenTheHunter extends Card {

    public KravenTheHunter() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentHasGreatestPowerAmongControllerCreaturesPredicate(),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new PutCountersOnSourceEffect(1, 1, 1))));
    }
}
