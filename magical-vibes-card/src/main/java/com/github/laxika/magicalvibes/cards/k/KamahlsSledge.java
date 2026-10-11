package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TOR", collectorNumber = "102")
public class KamahlsSledge extends Card {

    public KamahlsSledge() {
        DealDamageToTargetCreatureEffect creatureDamage = new DealDamageToTargetCreatureEffect(4);
        ConditionalEffect thresholdControllerDamage = new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new DealDamageToPlayersEffect(4, DamageRecipient.TARGET_PERMANENT_CONTROLLER));
        target(TargetFilters.creature())
                .bindEffect(creatureDamage)
                .bindEffect(thresholdControllerDamage);
        addSimultaneousEffects(EffectSlot.SPELL, creatureDamage, thresholdControllerDamage);
    }
}
