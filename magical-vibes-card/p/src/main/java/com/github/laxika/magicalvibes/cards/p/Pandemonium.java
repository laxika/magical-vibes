package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "93")
@CardRegistration(set = "TPR", collectorNumber = "149")
@CardRegistration(set = "TSB", collectorNumber = "68")
public class Pandemonium extends Card {

    public Pandemonium() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                new MayEffect(DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower()),
                        "Have it deal damage to any target?", null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
