package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "91")
@CardRegistration(set = "M15", collectorNumber = "140")
@CardRegistration(set = "SIS", collectorNumber = "41")
public class ForgeDevil extends Card {

    public ForgeDevil() {
        // When this creature enters, it deals 1 damage to target creature...
        DealDamageToTargetCreatureEffect creatureDamage = new DealDamageToTargetCreatureEffect(1);
        target(TargetFilters.creature()).bindEffect(creatureDamage);

        // ...and 1 damage to you.
        addSimultaneousEffects(EffectSlot.ON_ENTER_BATTLEFIELD,
                creatureDamage,
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
