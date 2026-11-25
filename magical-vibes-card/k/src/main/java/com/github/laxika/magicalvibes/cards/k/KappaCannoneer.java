package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "108")
@CardRegistration(set = "NEC", collectorNumber = "14")
@CardRegistration(set = "NEC", collectorNumber = "50")
@CardRegistration(set = "EOC", collectorNumber = "74")
@CardRegistration(set = "FDC", collectorNumber = "64")
public class KappaCannoneer extends Card {

    public KappaCannoneer() {
        // Ward {4}
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), UnlessPaysEffect.counterUnlessPays(4));
        // Whenever this creature or another artifact you control enters, put a +1/+1 counter on
        // this creature. It can't be blocked this turn.
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        new MakeCreatureUnblockableEffect(true, false))))));
    }
}
