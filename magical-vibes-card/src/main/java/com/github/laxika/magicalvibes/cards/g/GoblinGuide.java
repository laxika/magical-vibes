package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfDefendingPlayerLibraryMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ZEN", collectorNumber = "126")
public class GoblinGuide extends Card {

    public GoblinGuide() {
        addEffect(EffectSlot.ON_ATTACK,
                new RevealTopCardOfDefendingPlayerLibraryMatchingToHandEffect(
                        new CardTypePredicate(CardType.LAND)));
    }
}
