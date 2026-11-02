package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBRO", collectorNumber = "23")
public class RichlauHeadmaster extends Card {

    public RichlauHeadmaster() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayPayManaEffect("{1}",
                        new QueueReflexiveAbilityEffect(
                                new PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect()),
                        "Pay {1} to perpetually reduce target artifact card's cost and put it second from the top of your library?"));
    }
}
