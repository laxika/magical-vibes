package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "43")
public class BringerOfTheBlackDawn extends Card {

    public BringerOfTheBlackDawn() {
        // You may pay {W}{U}{B}{R}{G} rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}{U}{B}{R}{G}"))));

        // At the beginning of your upkeep, you may pay 2 life. If you do, search your library for a
        // card, then shuffle and put that card on top.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayPayManaEffect(
                "{0}",
                2,
                new SearchLibraryEffect(null, LibrarySearchDestination.TOP_OF_LIBRARY),
                "Pay 2 life to search your library for a card?"
        ));
    }
}
