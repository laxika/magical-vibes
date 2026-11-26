package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TSP", collectorNumber = "120")
@CardRegistration(set = "TSR", collectorNumber = "127")
@CardRegistration(set = "SOC", collectorNumber = "220")
public class NetherTraitor extends Card {

    public NetherTraitor() {
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                UnlessPaysEffect.youMayPay("{B}", new PutCardsOntoBattlefieldEffect(Subjects.sourceObjectCard()))
                        .withPrompt("Pay {B} to return Nether Traitor from your graveyard to the battlefield?"));
    }
}
