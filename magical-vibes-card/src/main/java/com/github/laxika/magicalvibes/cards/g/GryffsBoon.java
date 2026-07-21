package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromGraveyardAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "25")
public class GryffsBoon extends Card {

    public GryffsBoon() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        // Enchanted creature gets +1/+0 and has flying.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));

        // {3}{W}: Return this card from your graveyard to the battlefield attached to target
        // creature. Activate only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new ReturnSourceFromGraveyardAttachedToTargetEffect()),
                "{3}{W}: Return this card from your graveyard to the battlefield attached to target "
                        + "creature. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
