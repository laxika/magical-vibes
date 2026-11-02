package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "133")
@CardRegistration(set = "SPM", collectorNumber = "273")
@CardRegistration(set = "OM1", collectorNumber = "127")
public class KravenTheHunter extends Card {

    public KravenTheHunter() {
        addTrigger(Triggers.creatureDies(
                        new PermanentHasGreatestPowerAmongControllerCreaturesPredicate(),
                        ObjectRelation.OPPONENT_CONTROLS),
                SequenceEffect.of(new DrawCardEffect(1), new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
