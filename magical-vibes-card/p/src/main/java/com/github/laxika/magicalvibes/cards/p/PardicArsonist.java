package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "105")
public class PardicArsonist extends Card {

    public PardicArsonist() {
        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                        DealDamageEffect.toAnyTarget(3)),
                        Subjects.source())));
    }
}
