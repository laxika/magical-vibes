package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "105")
@CardRegistration(set = "GK2", collectorNumber = "89")
public class BurningTreeShaman extends Card {

    public BurningTreeShaman() {
        addTrigger(Triggers.youActivateNonManaAbility(),
                DealDamageEffect.to(Subjects.you(), 1));
        addTrigger(Triggers.opponentActivatesNonManaAbility(),
                DealDamageEffect.to(Subjects.targetPlayer(), 1));
    }
}
