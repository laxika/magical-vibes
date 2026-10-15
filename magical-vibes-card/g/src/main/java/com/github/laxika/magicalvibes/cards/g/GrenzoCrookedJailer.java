package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.HeistTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "22")
public class GrenzoCrookedJailer extends Card {

    public GrenzoCrookedJailer() {
        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");

        target(opponentFilter)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new HeistTargetLibraryEffect())
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new HeistTargetLibraryEffect());

        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect(
                "{0}",
                new CardAllOfPredicate(List.of(
                        new CardControllerDoesNotOwnPredicate(),
                        new CardMaxManaValuePredicate(3))),
                null,
                true));
    }
}
