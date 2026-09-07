package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ICE", collectorNumber = "291")
public class Fumarole extends Card {

    public Fumarole() {
        setAllowSharedTargets(true);

        // As an additional cost to cast this spell, pay 3 life.
        addEffect(EffectSlot.SPELL, new PayLifeCost(3));

        // Destroy target creature and target land — two positional target groups, both required.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature"
        ));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Second target must be a land"
        ));

        // Unbound to a group: destroys every chosen target, so both the creature and the land go.
        addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
