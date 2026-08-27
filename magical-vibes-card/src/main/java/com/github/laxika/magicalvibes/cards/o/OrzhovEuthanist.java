package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "54")
public class OrzhovEuthanist extends Card {

    public OrzhovEuthanist() {
        target(damagedCreatureTarget())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_DEATH, new HauntEffect());
        target(damagedCreatureTarget())
                .addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES, new DestroyTargetPermanentEffect());
    }

    private static PermanentPredicateTargetFilter damagedCreatureTarget() {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentDealtDamageThisTurnPredicate()
                )),
                "Target must be a creature that was dealt damage this turn");
    }
}
