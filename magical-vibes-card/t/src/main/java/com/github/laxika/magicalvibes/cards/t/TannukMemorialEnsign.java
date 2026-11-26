package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "233")
public class TannukMemorialEnsign extends Card {

    public TannukMemorialEnsign() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                DealDamageEffect.to(Subjects.eachOpponent(), 1),
                new ConditionalEffect(new NthAbilityResolutionThisTurn(2), new DrawCardEffect(1)));
    }
}
