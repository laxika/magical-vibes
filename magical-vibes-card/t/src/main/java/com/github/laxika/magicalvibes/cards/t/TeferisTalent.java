package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowLoyaltyActivationAtInstantSpeedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "74")
@CardRegistration(set = "MOC", collectorNumber = "82")
public class TeferisTalent extends Card {

    private static final String EMBLEM_TEXT =
            "You may activate loyalty abilities of planeswalkers you control on any player's turn any time you could cast an instant.";

    public TeferisTalent() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"
        ));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        -12,
                        List.of(new CreateEmblemEffect(
                                List.of(new AllowLoyaltyActivationAtInstantSpeedEffect()),
                                EMBLEM_TEXT)),
                        "−12: You get an emblem with \"" + EMBLEM_TEXT + "\"."
                ),
                GrantScope.ENCHANTED_PERMANENT
        ));

        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new PutCounterOnTargetPermanentEffect(CounterType.LOYALTY));
    }
}
