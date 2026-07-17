package com.github.laxika.magicalvibes.cards.a;

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

@CardRegistration(set = "ALA", collectorNumber = "2")
public class AngelsHerald extends Card {

    public AngelsHerald() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(creatureOfColor(CardColor.GREEN), creatureOfColor(CardColor.WHITE),
                                        creatureOfColor(CardColor.BLUE)),
                                List.of("a green creature", "a white creature", "a blue creature")),
                        new SearchLibraryEffect(
                                new CardNamedPredicate("Empyrial Archangel"), LibrarySearchDestination.BATTLEFIELD)
                ),
                "{2}{W}, {T}, Sacrifice a green creature, a white creature, and a blue creature: "
                        + "Search your library for a card named Empyrial Archangel, put it onto the battlefield, then shuffle."
        ));
    }

    private static PermanentPredicate creatureOfColor(CardColor color) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(color))));
    }
}
