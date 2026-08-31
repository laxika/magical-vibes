package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "117")
public class SupremeInquisitor extends Card {

    public SupremeInquisitor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(5, new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
                        new SearchTargetLibraryEffect(5, null, LibrarySearchDestination.EXILE, true)
                ),
                "Tap five untapped Wizards you control: Search target player's library for up to five cards and exile them. Then that player shuffles.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
