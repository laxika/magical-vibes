package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OHOP", collectorNumber = "27")
public class Naya extends Card {

    public Naya() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(Integer.MAX_VALUE - 1));

        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.RED, CardColor.GREEN, CardColor.WHITE))));
        target(new ControlledPermanentPredicateTargetFilter(
                targetFilter,
                "Target must be a red, green, or white creature you control"
        )).addEffect(EffectSlot.CHAOS_TRIGGERED, new BoostTargetCreatureEffect(
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER),
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER),
                targetFilter));
    }
}
