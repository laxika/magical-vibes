package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "6")
@CardRegistration(set = "OPCA", collectorNumber = "6")
@CardRegistration(set = "MOC", collectorNumber = "155")
public class RealityShaping extends Card {

    public RealityShaping() {
        addTrigger(Triggers.encounterThis(), new EachPlayerMayPutCardFromHandToBattlefieldEffect(
                new CardIsPermanentPredicate(), "permanent", false, false, true));
    }
}
