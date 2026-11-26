package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "C17", collectorNumber = "15")
public class BoneyardScourge extends Card {

    public BoneyardScourge() {
        addTrigger(new TriggerCondition(Triggers.creatureDies(
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                                ObjectRelation.YOU_CONTROL),
                        AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                UnlessPaysEffect.youMayPay("{1}{B}", new PutCardsOntoBattlefieldEffect(Subjects.sourceObjectCard()))
                        .withPrompt("Pay {1}{B} to return Boneyard Scourge from your graveyard to the battlefield?"));
    }
}
