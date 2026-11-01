package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "175")
@CardRegistration(set = "CMM", collectorNumber = "641")
@CardRegistration(set = "BLC", collectorNumber = "184")
public class NadiersNightblade extends Card {

    public NadiersNightblade() {
        CardEffect drainOpponentAndGainLife = SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(1));
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsTokenPredicate(), drainOpponentAndGainLife));
        // "a token you control leaves the battlefield" includes this token itself: a leaves-the-battlefield
        // ability looks back in time (CR 603.10a).
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                ConditionalEffect.atTriggerTime(new SourceIsToken(), drainOpponentAndGainLife));
    }
}
