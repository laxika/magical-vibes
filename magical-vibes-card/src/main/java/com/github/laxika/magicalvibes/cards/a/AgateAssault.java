package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "122")
public class AgateAssault extends Card {

    public AgateAssault() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Agate Assault deals 4 damage to target creature. If that creature would die this turn, exile it instead",
                        new DealDamageToAnyTargetEffect(new Fixed(4), false, true),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target artifact",
                        new ExileTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsArtifactPredicate(),
                                "Target must be an artifact."
                        )
                )
        )));
    }
}
