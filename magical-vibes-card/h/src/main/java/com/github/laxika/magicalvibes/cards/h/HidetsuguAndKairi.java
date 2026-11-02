package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "228")
@CardRegistration(set = "MOM", collectorNumber = "309")
public class HidetsuguAndKairi extends Card {

    public HidetsuguAndKairi() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(
                        3, 2, HandToLibraryPlacement.TOP));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.dies(ObjectRelation.THIS),
                new ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect());
    }
}
