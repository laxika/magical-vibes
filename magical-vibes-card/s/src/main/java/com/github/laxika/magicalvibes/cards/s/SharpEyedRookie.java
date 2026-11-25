package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "176")
@CardRegistration(set = "MKM", collectorNumber = "353")
public class SharpEyedRookie extends Card {

    public SharpEyedRookie() {
        // "Whenever a creature you control enters": this creature entering never has greater power or toughness
        // than itself. The intervening "if" is checked once as the ability resolves, before the counter and the Clue.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TriggeringPermanentHasGreaterPowerOrToughnessThanSource()),
                SequenceEffect.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE), CreateTokenEffect.ofClueToken(1)));
    }
}
