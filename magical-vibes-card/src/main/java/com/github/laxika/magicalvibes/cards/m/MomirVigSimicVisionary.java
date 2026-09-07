package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "118")
public class MomirVigSimicVisionary extends Card {

    public MomirVigSimicVisionary() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAllOfPredicate(List.of(
                                new CardColorPredicate(CardColor.GREEN),
                                new CardTypePredicate(CardType.CREATURE))),
                        List.of(new MayEffect(
                                new SearchLibraryEffect(new CardTypePredicate(CardType.CREATURE),
                                        LibrarySearchDestination.TOP_OF_LIBRARY),
                                "Search your library for a creature card?"))));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAllOfPredicate(List.of(
                                new CardColorPredicate(CardColor.BLUE),
                                new CardTypePredicate(CardType.CREATURE))),
                        List.of(new RevealTopCardMatchingToHandEffect(
                                new CardTypePredicate(CardType.CREATURE)))));
    }
}
