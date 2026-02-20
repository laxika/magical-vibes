package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ECL", collectorNumber = "6")
public class BarkOfDoran extends Card {

    public BarkOfDoran() {
        addEffect(EffectSlot.STATIC, new BoostEquippedCreatureEffect(0, 1));
        addEffect(EffectSlot.STATIC, new AssignCombatDamageWithToughnessEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new EquipEffect()),
                true,
                false,
                "Equip {1}",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                ),
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
