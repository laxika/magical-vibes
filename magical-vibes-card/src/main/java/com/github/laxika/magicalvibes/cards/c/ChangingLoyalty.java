package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "23")
@CardRegistration(set = "SOC", collectorNumber = "73")
public class ChangingLoyalty extends Card {

    public ChangingLoyalty() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{2}")));
        target(TargetFilters.creature());

        // When enchanted creature dies, return that card to the battlefield under your control.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true));

        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{2}", true));
    }
}
