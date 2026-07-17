package com.github.laxika.magicalvibes.cards.b;

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

@CardRegistration(set = "ALA", collectorNumber = "124")
public class BehemothsHerald extends Card {

    public BehemothsHerald() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(creatureOfColor(CardColor.RED), creatureOfColor(CardColor.GREEN),
                                        creatureOfColor(CardColor.WHITE)),
                                List.of("a red creature", "a green creature", "a white creature")),
                        new SearchLibraryEffect(
                                new CardNamedPredicate("Godsire"), LibrarySearchDestination.BATTLEFIELD)
                ),
                "{2}{G}, {T}, Sacrifice a red creature, a green creature, and a white creature: "
                        + "Search your library for a card named Godsire, put it onto the battlefield, then shuffle."
        ));
    }

    private static PermanentPredicate creatureOfColor(CardColor color) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(color))));
    }
}
