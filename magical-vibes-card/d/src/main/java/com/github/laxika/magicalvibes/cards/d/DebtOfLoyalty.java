package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Regenerate target creature. You gain control of that creature if it regenerates this way.
 *
 * <p>The control gain rides on the regeneration shield, so it only happens when the shield is
 * actually spent — a creature that never would have been destroyed stays with its controller.
 */
@CardRegistration(set = "WTH", collectorNumber = "11")
public class DebtOfLoyalty extends Card {

    public DebtOfLoyalty() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, RegenerateEffect.withGainControlOnRegenerate());
    }
}
