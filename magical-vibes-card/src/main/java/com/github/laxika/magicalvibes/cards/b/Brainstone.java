package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "223")
public class Brainstone extends Card {

    public Brainstone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(
                                3, 2, HandToLibraryPlacement.TOP)
                ),
                "{2}, {T}, Sacrifice this artifact: Draw three cards, then put two cards from your hand on top of your library in any order."
        ));
    }
}
