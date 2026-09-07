package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

@CardRegistration(set = "GRN", collectorNumber = "47")
@CardRegistration(set = "FUT", collectorNumber = "54")
public class Narcomoeba extends Card {

    public Narcomoeba() {
        addEffect(EffectSlot.ON_SELF_MILLED, new MayEffect(
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                "Put Narcomoeba onto the battlefield?"));
    }
}
