package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "KTK", collectorNumber = "65")
public class BitterRevelation extends Card {

    public BitterRevelation() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(4, 2));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2));
    }
}
