package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "111")
public class SlingbowTrap extends Card {

    public SlingbowTrap() {
        PermanentPredicate attackingCreatureWithFlying = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
        PermanentPredicate blackFlyingAttacker = new PermanentAllOfPredicate(List.of(
                attackingCreatureWithFlying,
                new PermanentColorInPredicate(Set.of(CardColor.BLACK))));

        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{G}")),
                new AnyPlayerControlsPermanentCount(1, blackFlyingAttacker),
                false));

        target(new PermanentPredicateTargetFilter(
                attackingCreatureWithFlying,
                "Target must be an attacking creature with flying"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
