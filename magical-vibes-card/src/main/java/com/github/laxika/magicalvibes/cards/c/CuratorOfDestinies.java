package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "FDN", collectorNumber = "34")
public class CuratorOfDestinies extends Card {

    public CuratorOfDestinies() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealTopCardsAndSeparateEffect(5, CardPileDisposition.HAND_WITH_FACE_DOWN_PILE, true));
    }
}
