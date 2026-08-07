package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "WTH", collectorNumber = "104")
public class GoblinGrenadiers extends Card {

    public GoblinGrenadiers() {
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
        // every chosen target; SacrificeSelfThenEffect supplies the "if you do" gate.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new MayEffect(
                new SacrificeSelfThenEffect(new DestroyEachTargetPermanentEffect()),
                "Sacrifice Goblin Grenadiers to destroy target creature and target land?"
        ));
    }
}
