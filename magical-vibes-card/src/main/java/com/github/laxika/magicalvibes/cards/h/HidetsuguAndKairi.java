package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MOM", collectorNumber = "228")
public class HidetsuguAndKairi extends Card {

    public HidetsuguAndKairi() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(
                        3, 2, HandToLibraryPlacement.TOP));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_DEATH,
                new ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect());
    }
}
