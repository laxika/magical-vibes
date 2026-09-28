package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HBG", collectorNumber = "36")
public class SeekNewKnowledge extends Card {

    public SeekNewKnowledge() {
        addEffect(EffectSlot.SPELL, new SeekCardsToHandEffect(
                new Fixed(2), new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addEffect(EffectSlot.SPELL, new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(
                0, 1, HandToLibraryPlacement.BOTTOM));
    }
}
