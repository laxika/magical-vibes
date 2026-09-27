package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsMoreCreaturesThanController;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsMoreLandsThanController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "191")
public class KeeperOfTheAccord extends Card {

    public KeeperOfTheAccord() {
        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED, new ConditionalEffect(
                new TargetPlayerControlsMoreCreaturesThanController(),
                new CreateTokenEffect("Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of())));

        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED, new ConditionalEffect(
                new TargetPlayerControlsMoreLandsThanController(),
                new MayEffect(
                        new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                                new CardSupertypePredicate(CardSupertype.BASIC),
                                new CardSubtypePredicate(CardSubtype.PLAINS))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a basic Plains card?")));
    }
}
