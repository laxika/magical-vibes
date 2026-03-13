package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;

@CardRegistration(set = "M10", collectorNumber = "190")
public class LurkingPredators extends Card {

    public LurkingPredators() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new RevealTopCardCreatureToBattlefieldOrMayBottomEffect());
    }
}
