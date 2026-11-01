package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "120")
@CardRegistration(set = "TSR", collectorNumber = "127")
@CardRegistration(set = "SOC", collectorNumber = "220")
public class NetherTraitor extends Card {

    public NetherTraitor() {
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayPayManaEffect("{B}",
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        "Pay {B} to return Nether Traitor from your graveyard to the battlefield?"));
    }
}
