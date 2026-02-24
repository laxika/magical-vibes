package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "145")
public class ContagionEngine extends Card {

    public ContagionEngine() {
        setTargetFilter(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new ProliferateEffect(), new ProliferateEffect()),
                "{4}, {T}: Proliferate, then proliferate again."
        ));
    }
}
