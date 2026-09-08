package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "92")
public class RoguesGallery extends Card {

    public RoguesGallery() {
        addEffect(EffectSlot.SPELL, new ReturnUpToOneOfEachFilterFromGraveyardToHandEffect(List.of(
                creatureOfColor(CardColor.WHITE),
                creatureOfColor(CardColor.BLUE),
                creatureOfColor(CardColor.BLACK),
                creatureOfColor(CardColor.RED),
                creatureOfColor(CardColor.GREEN))));
    }

    private CardPredicate creatureOfColor(CardColor color) {
        return new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardColorPredicate(color)));
    }
}
