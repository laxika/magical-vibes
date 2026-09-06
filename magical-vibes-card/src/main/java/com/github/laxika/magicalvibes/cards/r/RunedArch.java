package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "334")
public class RunedArch extends Card {

    public RunedArch() {
        // This artifact enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {X}, {T}, Sacrifice this artifact: X target creatures with power 2 or less can't be
        // blocked this turn. The target count scales with the paid X (withXScaledTargets); the
        // unblockable handler fans over the whole chosen target group.
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new SacrificeSelfCost(), new MakeCreatureUnblockableEffect()),
                "{X}, {T}, Sacrifice this artifact: X target creatures with power 2 or less can't be blocked this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtMostPredicate(2)
                        )),
                        "Targets must be creatures with power 2 or less"
                ),
                null, null, null, List.of(), 100, 100)
                .withXScaledTargets());
    }
}
