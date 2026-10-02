package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect;

@CardRegistration(set = "YBRO", collectorNumber = "23")
public class RichlauHeadmaster extends Card {

    public RichlauHeadmaster() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                MayPayManaEffect.reflexiveTarget("{1}",
                        new PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect(),
                        "Pay {1} to perpetually reduce target artifact card's cost and put it second from the top of your library?"));
    }
}
