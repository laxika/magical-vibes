package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromGraveyardAndConjureDuplicateIntoHandEffect;

@CardRegistration(set = "YOTJ", collectorNumber = "24")
public class NashiIllusionGadgeteer extends Card {

    public NashiIllusionGadgeteer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardFromGraveyardAndConjureDuplicateIntoHandEffect());
    }
}
