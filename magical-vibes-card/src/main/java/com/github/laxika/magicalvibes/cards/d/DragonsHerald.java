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

@CardRegistration(set = "ALA", collectorNumber = "98")
public class DragonsHerald extends Card {

    public DragonsHerald() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(creatureOfColor(CardColor.BLACK), creatureOfColor(CardColor.RED),
                                        creatureOfColor(CardColor.GREEN)),
                                List.of("a black creature", "a red creature", "a green creature")),
                        new SearchLibraryEffect(
                                new CardNamedPredicate("Hellkite Overlord"), LibrarySearchDestination.BATTLEFIELD)
                ),
                "{2}{R}, {T}, Sacrifice a black creature, a red creature, and a green creature: "
                        + "Search your library for a card named Hellkite Overlord, put it onto the battlefield, then shuffle."
        ));
    }

    private static PermanentPredicate creatureOfColor(CardColor color) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(color))));
    }
}
