package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "8")
public class InTheTrenches extends Card {

    public InTheTrenches() {
        // Creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));

        // {5}{W}: Exile target nonland permanent you don't control until this enchantment leaves
        // the battlefield. Activate only as a sorcery and only once.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}",
                List.of(new ExileTargetPermanentUntilSourceLeavesEffect()),
                "{5}{W}: Exile target nonland permanent you don't control until this enchantment "
                        + "leaves the battlefield. Activate only as a sorcery and only once.",
                TargetFilters.nonlandPermanentAnOpponentControls(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withMaxActivationsPerGame(1));
    }
}
