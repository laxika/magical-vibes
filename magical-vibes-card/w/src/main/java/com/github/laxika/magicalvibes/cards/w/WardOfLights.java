package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "47")
public class WardOfLights extends Card {

    public WardOfLights() {
        // Enchant creature; as it enters choose a color, and the enchanted creature has protection
        // from it (the grant never detaches this Aura). The Mirage flash clause lets it be cast at
        // instant speed at the cost of being sacrificed at the next cleanup step.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new FlashCastWithCleanupSacrificeEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect())
                .addEffect(EffectSlot.STATIC,
                        new ProtectionFromChosenColorEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
