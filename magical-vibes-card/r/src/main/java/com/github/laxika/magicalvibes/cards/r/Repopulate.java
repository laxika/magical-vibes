package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ULG", collectorNumber = "111")
public class Repopulate extends Card {

    public Repopulate() {
        addEffect(EffectSlot.SPELL,
                new ShuffleGraveyardIntoLibraryEffect(true, new CardTypePredicate(CardType.CREATURE)));
        addCycling("{2}");
    }
}
