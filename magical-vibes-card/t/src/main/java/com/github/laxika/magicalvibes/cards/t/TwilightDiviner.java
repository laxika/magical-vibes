package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFromGraveyardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "122")
@CardRegistration(set = "ECL", collectorNumber = "315")
public class TwilightDiviner extends Card {

    public TwilightDiviner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(2));
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE,
                new EnteringCreatureFromGraveyardConditionalEffect(
                        new OncePerTurnTriggerEffect(new CreateTokenCopyOfTargetPermanentEffect())));
    }
}
