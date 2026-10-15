package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetPlayerControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMoreLandsThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

@CardRegistration(set = "EXO", collectorNumber = "11")
public class OathOfLieges extends Card {

    public OathOfLieges() {
        // At the beginning of each player's upkeep, that player chooses target player who controls
        // more lands than they do and is their opponent. The first player may search their library
        // for a basic land card and put it onto the battlefield, then shuffle.
        target(new PlayerPredicateTargetFilter(
                new PlayerControlsMoreLandsThanControllerPredicate(),
                "Target player must be an opponent who controls more lands than you"
        )).addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MayEffect(
                new SearchLibraryEffect(
                        new FixedIfTargetPlayerControlsMoreLands(1, 1),
                        CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.BATTLEFIELD,
                        LibrarySearchPlayer.ACTIVE_PLAYER),
                "Search your library for a basic land card?",
                null,
                MayChoicePlayer.ACTIVE_PLAYER));
    }
}
