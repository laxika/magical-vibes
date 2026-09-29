package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantEvokeToMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "4")
public class AquaticSubtlety extends Card {

    public AquaticSubtlety() {
        addEffect(EffectSlot.SPELL,
                new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(
                        2, 2, HandToLibraryPlacement.BOTTOM));
        addEffect(EffectSlot.SPELL, new PerpetuallyGrantEvokeToMatchingHandCardsEffect(
                new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.BLUE),
                        new CardTypePredicate(CardType.CREATURE))),
                new CardColorPredicate(CardColor.BLUE), "blue"));
    }
}
