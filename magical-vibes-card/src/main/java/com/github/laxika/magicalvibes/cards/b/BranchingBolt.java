package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "158")
public class BranchingBolt extends Card {

    public BranchingBolt() {
        PermanentPredicateTargetFilter creatureWithFlying = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)
                )),
                "Target must be a creature with flying."
        );
        PermanentPredicateTargetFilter creatureWithoutFlying = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                )),
                "Target must be a creature without flying."
        );

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Branching Bolt deals 3 damage to target creature with flying",
                        new DealDamageToTargetCreatureEffect(3),
                        creatureWithFlying
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Branching Bolt deals 3 damage to target creature without flying",
                        new DealDamageToTargetCreatureEffect(3),
                        creatureWithoutFlying
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Branching Bolt deals 3 damage to target creature with flying and target creature without flying",
                        List.<CardEffect>of(
                                new DealDamageToTargetCreatureEffect(3),
                                new DealDamageToTargetCreatureEffect(3)
                        ),
                        List.of(creatureWithFlying, creatureWithoutFlying)
                )
        )));
    }
}
