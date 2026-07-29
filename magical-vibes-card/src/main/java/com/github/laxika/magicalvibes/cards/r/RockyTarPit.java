package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "329")
public class RockyTarPit extends Card {

    public RockyTarPit() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}, Sacrifice this land: Search your library for a Swamp or Mountain card, put it onto
        // the battlefield, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.SWAMP),
                                        new CardSubtypePredicate(CardSubtype.MOUNTAIN))),
                                LibrarySearchDestination.BATTLEFIELD)
                ),
                "{T}, Sacrifice Rocky Tar Pit: Search your library for a Swamp or Mountain card, put it onto the battlefield, then shuffle."
        ));
    }
}
