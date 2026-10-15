package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CantorOfTheRefrain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardInGraveyardEffect;

@CardRegistration(set = "YEOE", collectorNumber = "13")
public class VoidcalledDevotee extends Card {

    public VoidcalledDevotee() {
        addEffect(EffectSlot.ON_ATTACK, new ConjureCardInGraveyardEffect(CantorOfTheRefrain::new));
    }
}
