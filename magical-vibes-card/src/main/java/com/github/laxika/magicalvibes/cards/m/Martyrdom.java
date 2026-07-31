package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToAnyTargetToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "10a")
@CardRegistration(set = "ALL", collectorNumber = "10b")
public class Martyrdom extends Card {

    public Martyrdom() {
        // Until end of turn, target creature you control gains "{0}: The next 1 damage that would be
        // dealt to target creature, planeswalker, or player this turn is dealt to this creature
        // instead." Only you may activate this ability — the grant lasts only until end of turn and
        // goes on a creature you control, whose controller is the only legal activator by default.
        ActivatedAbility grantedAbility = new ActivatedAbility(
                false,
                "{0}",
                List.of(new RedirectNextDamageToAnyTargetToSourceEffect(1)),
                "{0}: The next 1 damage that would be dealt to target creature, planeswalker, or player this turn is dealt to this creature instead."
        );

        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                grantedAbility,
                GrantScope.TARGET,
                null,
                EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
