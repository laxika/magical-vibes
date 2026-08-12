package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "19")
public class EnchantmentAlteration extends Card {

    public EnchantmentAlteration() {
        PermanentPredicate auraAttachedToCreatureOrLand = new PermanentAnyOfPredicate(List.of(
                new PermanentIsAuraAttachedToCreaturePredicate(),
                new PermanentIsAuraAttachedToLandPredicate()));
        PermanentPredicate creatureOrLand = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsLandPredicate()));

        target(new PermanentPredicateTargetFilter(
                auraAttachedToCreatureOrLand,
                "Target must be an Aura attached to a creature or land"))
                .addEffect(EffectSlot.SPELL, new AttachTargetAuraToTargetCreatureEffect());
        target(new PermanentPredicateTargetFilter(creatureOrLand,
                "Target must be another permanent of that type"));
        setMultiTargetConstraint(MultiTargetConstraint.SAME_CREATURE_OR_LAND_TYPE_AS_FIRST_AURA_HOST);
    }
}
