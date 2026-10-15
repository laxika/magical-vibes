package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "58")
public class SphinxsHerald extends Card {

    public SphinxsHerald() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(creatureOfColor(CardColor.WHITE), creatureOfColor(CardColor.BLUE),
                                        creatureOfColor(CardColor.BLACK)),
                                List.of("a white creature", "a blue creature", "a black creature")),
                        new SearchLibraryEffect(
                                new CardNamedPredicate("Sphinx Sovereign"), LibrarySearchDestination.BATTLEFIELD)
                ),
                "{2}{U}, {T}, Sacrifice a white creature, a blue creature, and a black creature: "
                        + "Search your library for a card named Sphinx Sovereign, put it onto the battlefield, then shuffle."
        ));
    }

    private static PermanentPredicate creatureOfColor(CardColor color) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(color))));
    }
}
