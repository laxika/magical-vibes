package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseSameNameToHandEffect;

@CardRegistration(set = "HBG", collectorNumber = "48")
public class StrokeOfLuck extends Card {

    public StrokeOfLuck() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsChooseSameNameToHandEffect(4));
    }
}
