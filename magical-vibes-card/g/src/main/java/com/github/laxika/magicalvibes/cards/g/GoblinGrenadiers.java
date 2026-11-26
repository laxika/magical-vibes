package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WTH", collectorNumber = "104")
public class GoblinGrenadiers extends Card {

    public GoblinGrenadiers() {
        setAllowSharedTargets(true);
        // Two positional target groups, both chosen as the trigger goes on the stack.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature"
        ));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Second target must be a land"
        ));

        // Whenever this creature attacks and isn't blocked, you may sacrifice it. If you do, destroy
        // target creature and target land. The destroy effect is unbound to a group so it destroys
        // every chosen target; the ConditionalEffect after the sacrifice is the "if you do" gate.
        addTrigger(Triggers.attacksUnblocked(), new MayEffect(
                SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), DestroyTargetPermanentEffect.eachTarget())),
                "Sacrifice Goblin Grenadiers to destroy target creature and target land?"
        ));
    }
}
