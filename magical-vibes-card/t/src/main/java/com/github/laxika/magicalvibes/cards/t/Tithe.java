package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetPlayerControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "VIS", collectorNumber = "23")
public class Tithe extends Card {

    public Tithe() {
        // Search your library for a Plains card. If target opponent controls more lands than you,
        // you may search your library for an additional Plains card. Reveal those cards, put them
        // into your hand, then shuffle. Land count is checked on resolution.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new FixedIfTargetPlayerControlsMoreLands(2, 1),
                new CardSubtypePredicate(CardSubtype.PLAINS),
                LibrarySearchDestination.HAND));
    }
}
