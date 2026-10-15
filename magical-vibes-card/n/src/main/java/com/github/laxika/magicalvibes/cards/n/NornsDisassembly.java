package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "2")
public class NornsDisassembly extends Card {

    public NornsDisassembly() {
        // Sacrifice a historic permanent: Seek a historic card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsHistoricPredicate(), "Sacrifice a historic permanent"),
                        new SeekLibraryEffect(1, new CardIsHistoricPredicate(), LibrarySearchDestination.HAND)
                ),
                "{1}{W}, Sacrifice a historic permanent: Seek a historic card."
        ));
    }
}
