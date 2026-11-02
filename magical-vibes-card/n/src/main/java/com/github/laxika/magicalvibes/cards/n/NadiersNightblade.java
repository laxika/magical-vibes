package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "175")
@CardRegistration(set = "CMM", collectorNumber = "641")
@CardRegistration(set = "BLC", collectorNumber = "184")
public class NadiersNightblade extends Card {

    public NadiersNightblade() {
        CardEffect drainOpponentAndGainLife = SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(1));
        // "A token you control leaves the battlefield" includes a token copy of this creature: a
        // leaves-the-battlefield ability looks back in time (CR 603.10a).
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.TOKEN, ObjectRelation.ANOTHER_YOU_CONTROL)),
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.TOKEN, ObjectRelation.THIS))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(drainOpponentAndGainLife))));
    }
}
