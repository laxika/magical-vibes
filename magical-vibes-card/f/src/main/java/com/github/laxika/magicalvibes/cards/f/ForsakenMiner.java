package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "88")
public class ForsakenMiner extends Card {

    public ForsakenMiner() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(new TriggerCondition(Triggers.youCommitCrime(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayPayManaEffect("{B}",
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        "Pay {B} to return Forsaken Miner from your graveyard to the battlefield?"));
    }
}
