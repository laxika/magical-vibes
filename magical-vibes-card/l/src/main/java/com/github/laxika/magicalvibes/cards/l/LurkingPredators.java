package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "190")
@CardRegistration(set = "C16", collectorNumber = "156")
public class LurkingPredators extends Card {

    public LurkingPredators() {
        addTrigger(Triggers.opponentCastsSpell(), new RevealTopCardCreatureToBattlefieldOrMayBottomEffect());
    }
}
