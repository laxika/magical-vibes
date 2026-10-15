package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndReturnMilledCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasAdventurePredicate;

public class SeekThrills extends Card {

    public SeekThrills() {
        addEffect(EffectSlot.SPELL, new MillControllerAndReturnMilledCardsToHandEffect(
                7, new CardHasAdventurePredicate()));
    }
}
