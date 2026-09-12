package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtCombatDamageToPlayerThisCombatPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "16")
public class GlenElendra extends Card {

    public GlenElendra() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        PermanentAllOfPredicate damagedCreature = new PermanentAllOfPredicate(List.of(
                creature, new PermanentDealtCombatDamageToPlayerThisCombatPredicate()));

        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_PLAYER_DAMAGED_BY_FIRST_TARGET_THIS_COMBAT);
        target(new ControlledPermanentPredicateTargetFilter(
                damagedCreature, "First target must be a creature you control that dealt combat damage to a player this combat"));
        target(TargetFilters.creature()).addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED,
                new MayEffect(new ExchangeControlOfTargetPermanentsEffect(
                        creature, false, false, true, false, false, false, false, false,
                        damagedCreature, false), "Exchange control of the two target creatures?"));
        target(new OwnedPermanentPredicateTargetFilter(creature, "Target must be a creature you own"))
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT));
    }
}
