package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "39")
public class TyrantsFamiliar extends Card {

    public TyrantsFamiliar() {
        ControlledCommanderAsCast lieutenant = new ControlledCommanderAsCast();

        addEffect(EffectSlot.STATIC, new ConditionalEffect(lieutenant,
                new StaticBoostEffect(2, 2, GrantScope.SELF)));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate())),
                "Target must be a creature defending player controls"))
                .addEffect(EffectSlot.ON_ATTACK,
                        new ConditionalEffect(lieutenant, new DealDamageToTargetCreatureEffect(7)));
    }
}
