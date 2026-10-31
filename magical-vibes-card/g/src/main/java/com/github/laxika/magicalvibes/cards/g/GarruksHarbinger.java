package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "185")
@CardRegistration(set = "M21", collectorNumber = "307")
public class GarruksHarbinger extends Card {

    public GarruksHarbinger() {
        // Scryfall reports "Hexproof" keyword, but this card only has "hexproof from black"
        removeKeyword(Keyword.HEXPROOF);

        // Hexproof from black
        addStaticEffect(TargetingRestrictionEffect.hexproofFromColors(Set.of(CardColor.BLACK)));

        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                new LookAtTopCardsEffect(
                        new EventValue(),
                        new Fixed(1),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.PLANESWALKER),
                                        new CardSubtypePredicate(CardSubtype.GARRUK))))),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                        false,
                        LibrarySearchDestination.HAND,
                        true));
    }
}
