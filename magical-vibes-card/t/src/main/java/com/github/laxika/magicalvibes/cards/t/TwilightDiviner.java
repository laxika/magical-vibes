package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredOrCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "122")
@CardRegistration(set = "ECL", collectorNumber = "315")
public class TwilightDiviner extends Card {

    public TwilightDiviner() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(2));
        addTrigger(new TriggerCondition(Triggers.enters(
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.CREATURE,
                                        new PermanentEnteredOrCastFromZonePredicate(Zone.GRAVEYARD))),
                                ObjectRelation.ANOTHER_YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent());
    }
}
