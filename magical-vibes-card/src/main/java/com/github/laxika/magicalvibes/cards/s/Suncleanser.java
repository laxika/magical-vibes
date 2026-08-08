package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAndLockPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAndLockPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "39")
public class Suncleanser extends Card {

    public Suncleanser() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Remove all counters from target creature. It can't have counters put on it for as long as this creature remains on the battlefield.",
                        new RemoveAllCountersAndLockPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature"
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent loses all counters. That player can't get counters for as long as this creature remains on the battlefield.",
                        new RemoveAllCountersAndLockPlayerEffect(),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"
                        )
                )
        )));
    }
}
