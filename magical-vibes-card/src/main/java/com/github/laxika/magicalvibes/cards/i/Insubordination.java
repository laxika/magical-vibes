package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedCreatureDidntAttack;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MMQ", collectorNumber = "141")
public class Insubordination extends Card {

    public Insubordination() {
        // At the beginning of the end step of enchanted creature's controller, this Aura deals
        // 2 damage to that player unless that creature attacked this turn.
        target(TargetFilters.creature()).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED,
                ConditionalEffect.unless(new EnchantedCreatureDidntAttack(),
                        new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PERMANENT_CONTROLLER)));
    }
}
