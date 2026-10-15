package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;

@CardRegistration(set = "BNG", collectorNumber = "80")
public class PainSeer extends Card {

    public PainSeer() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new RevealTopCardPutIntoHandAndChangeLifeEffect(false));
    }
}
