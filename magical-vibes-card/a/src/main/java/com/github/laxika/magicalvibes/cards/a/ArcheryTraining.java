package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnGrantingPermanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "2")
public class ArcheryTraining extends Card {

    public ArcheryTraining() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // At the beginning of your upkeep, you may put an arrow counter on this Aura.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.ARROW),
                "Put an arrow counter on Archery Training?"
        ));

        // Enchanted creature has "{T}: This creature deals X damage to target attacking or blocking
        // creature, where X is the number of arrow counters on Archery Training." The counter count
        // reads the Aura (the granting permanent), not the creature — bound at activation in
        // ActivatedAbilityExecutionService.snapshotEffects. The damage source is the creature.
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new DealDamageToTargetCreatureEffect(
                                new CountersOnGrantingPermanent(CounterType.ARROW))),
                        "{T}: This creature deals X damage to target attacking or blocking creature, "
                                + "where X is the number of arrow counters on Archery Training.",
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsAttackingPredicate(),
                                                new PermanentIsBlockingPredicate())))),
                                "Target must be an attacking or blocking creature"
                        )
                ),
                GrantScope.ENCHANTED_CREATURE
        ));
    }
}
