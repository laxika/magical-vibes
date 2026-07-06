package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "145")
public class ContagionEngine extends Card {

    public ContagionEngine() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.MINUS_ONE_MINUS_ONE, 1,
                        new PermanentIsCreaturePredicate(), EachPermanentScope.TARGET_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new ProliferateEffect(), new ProliferateEffect()),
                "{4}, {T}: Proliferate, then proliferate again."
        ));
    }
}
