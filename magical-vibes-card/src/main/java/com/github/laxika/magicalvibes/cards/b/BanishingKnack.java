package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "17")
public class BanishingKnack extends Card {

    public BanishingKnack() {
        // Until end of turn, target creature gains
        // "{T}: Return target nonland permanent to its owner's hand."
        ActivatedAbility grantedAbility = new ActivatedAbility(
                true,
                null,
                List.of(ReturnToHandEffect.target()),
                "{T}: Return target nonland permanent to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        "Target must be a nonland permanent"
                )
        );

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                grantedAbility,
                GrantScope.TARGET,
                null,
                EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
