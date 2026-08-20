package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "225")
public class RipApart extends Card {

    public RipApart() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Rip Apart deals 3 damage to target creature or planeswalker",
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(3),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate()
                                )),
                                "Target must be a creature or planeswalker."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact or enchantment",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsEnchantmentPredicate()
                                )),
                                "Target must be an artifact or enchantment."
                        )
                )
        )));
    }
}
