package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSuspectedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMKM", collectorNumber = "12")
public class SnarlfangVermin extends Card {

    public SnarlfangVermin() {
        addTrigger(Triggers.dealsCombatDamageToCreature(),
                new SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect());
        addTrigger(new TriggerCondition(Triggers.creatureDies(
                                new PermanentIsSuspectedPredicate(),
                                ObjectRelation.OPPONENT_CONTROLS),
                        AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new ConditionalEffect(new SourceCardInGraveyard(),
                        new LoseLifeEffect(1, LoseLifeRecipient.DYING_CREATURE_CONTROLLER)));
    }
}
