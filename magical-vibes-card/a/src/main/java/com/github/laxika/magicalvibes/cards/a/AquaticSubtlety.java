package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAlternateCastEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "4")
public class AquaticSubtlety extends Card {

    public AquaticSubtlety() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect.yours(2, HandToLibraryPlacement.BOTTOM));
        addSpellEffect(PerpetuallyGrantAlternateCastEffect.evoke(
                Subjects.cardsInYourHand(new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.BLUE),
                        new CardTypePredicate(CardType.CREATURE)))),
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.BLUE), "blue")));
    }
}
