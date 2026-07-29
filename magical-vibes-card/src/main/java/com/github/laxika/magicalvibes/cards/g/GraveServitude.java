package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "126")
public class GraveServitude extends Card {

    public GraveServitude() {
        // Enchant creature; the Mirage flash clause lets it be cast at instant speed at the cost of
        // being sacrificed at the next cleanup step. "Is black" replaces the creature's colors
        // (CR 105.3), hence the overriding GrantColorEffect.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new FlashCastWithCleanupSacrificeEffect())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, -1, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantColorEffect(CardColor.BLACK, GrantScope.ENCHANTED_CREATURE, true));
    }
}
