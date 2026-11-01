package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2447")
public class LucyMacLeanPositivelyArmed extends Card {

    public LucyMacLeanPositivelyArmed() {
        var ability = OncePerTurnTriggerEffect.markOnAcceptance(
                new CreateTokenCopyOfEnteringTokenForTargetPlayerEffect());
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.DEFAULT, ability);
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.TOKEN, ObjectRelation.OPPONENT_CONTROLS)),
                TriggerOptions.DEFAULT, ability);
    }
}
