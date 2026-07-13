package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "POR", collectorNumber = "40")
@CardRegistration(set = "7ED", collectorNumber = "59")
public class AncestralMemories extends Card {

    public AncestralMemories() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(7, 2));
    }
}
