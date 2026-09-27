package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsNotSharingCardTypeWithTriggeringCardEffect;

@CardRegistration(set = "YDSK", collectorNumber = "12")
public class AnguishedRecollection extends Card {

    public AnguishedRecollection() {
        addEffect(EffectSlot.SPELL, new DiscardCardThenEffect(
                null,
                new SeekCardsNotSharingCardTypeWithTriggeringCardEffect(2),
                "a card"));
    }
}
