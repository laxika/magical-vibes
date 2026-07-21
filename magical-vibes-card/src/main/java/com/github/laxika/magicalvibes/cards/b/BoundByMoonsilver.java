package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantTransformEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "13")
public class BoundByMoonsilver extends Card {

    public BoundByMoonsilver() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // Enchanted creature can't attack, block, or transform.
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantTransformEffect());

        // Sacrifice another permanent: Attach this Aura to target creature.
        // Activate only as a sorcery and only once each turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentTruePredicate(), "Sacrifice another permanent"),
                        new AttachSourceAuraToTargetCreatureEffect()
                ),
                "Sacrifice another permanent: Attach this Aura to target creature. Activate only as a sorcery and only once each turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                ),
                null,
                1,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
