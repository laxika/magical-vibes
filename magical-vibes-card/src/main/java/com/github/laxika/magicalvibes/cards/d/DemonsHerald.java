package com.github.laxika.magicalvibes.cards.d;

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

@CardRegistration(set = "ALA", collectorNumber = "72")
public class DemonsHerald extends Card {

    public DemonsHerald() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(creatureOfColor(CardColor.BLUE), creatureOfColor(CardColor.BLACK),
                                        creatureOfColor(CardColor.RED)),
                                List.of("a blue creature", "a black creature", "a red creature")),
                        new SearchLibraryEffect(
                                new CardNamedPredicate("Prince of Thralls"), LibrarySearchDestination.BATTLEFIELD)
                ),
                "{2}{B}, {T}, Sacrifice a blue creature, a black creature, and a red creature: "
                        + "Search your library for a card named Prince of Thralls, put it onto the battlefield, then shuffle."
        ));
    }

    private static PermanentPredicate creatureOfColor(CardColor color) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(color))));
    }
}
