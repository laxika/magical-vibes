package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "69")
public class ConsignToThePit extends Card {

    public ConsignToThePit() {
        // Destroy target creature. Consign to the Pit deals 2 damage to that creature's controller.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER),
                ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET));
    }
}
