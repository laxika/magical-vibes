package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "136")
public class DeathMatch extends Card {

    public DeathMatch() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerOptions.DEFAULT.withEventCondition(
                new SourceIsCreature()), new MayEffect(new BoostTargetCreatureEffect(-3, -3),
                "Have target creature get -3/-3 until end of turn?"));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                MayEffect.byTriggeringPermanentControllerOfTheirChoice(
                        new BoostTargetCreatureEffect(-3, -3),
                        "Have target creature get -3/-3 until end of turn?"));
    }
}
