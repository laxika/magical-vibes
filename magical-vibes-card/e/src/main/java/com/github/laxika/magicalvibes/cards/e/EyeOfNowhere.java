package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "CHK", collectorNumber = "59")
public class EyeOfNowhere extends Card {

    public EyeOfNowhere() {
        addSpellEffect(ReturnToHandEffect.target());
    }
}
