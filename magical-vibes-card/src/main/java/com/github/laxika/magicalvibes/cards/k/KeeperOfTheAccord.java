package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsMoreCreaturesThanController;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsMoreLandsThanController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "164")
@CardRegistration(set = "PIP", collectorNumber = "451")
@CardRegistration(set = "PIP", collectorNumber = "692")
@CardRegistration(set = "PIP", collectorNumber = "979")
public class KeeperOfTheAccord extends Card {

    public KeeperOfTheAccord() {
        // At the beginning of each opponent's end step, if that player controls more creatures
        // than you, create a 1/1 white Soldier creature token.
        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED, new ConditionalEffect(
                new TargetPlayerControlsMoreCreaturesThanController(),
                CreateTokenEffect.whiteSoldier(1)));

        // At the beginning of each opponent's end step, if that player controls more lands than
        // you, you may search your library for a basic Plains card and put it onto the battlefield
        // tapped, then shuffle.
        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED, new ConditionalEffect(
                new TargetPlayerControlsMoreLandsThanController(),
                new MayEffect(
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        CardPredicateUtils.basicLand(),
                                        new CardSubtypePredicate(CardSubtype.PLAINS))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a basic Plains card?")));
    }
}
