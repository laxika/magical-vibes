package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "132")
@CardRegistration(set = "DBL", collectorNumber = "132")
public class BurnTheAccursed extends Card {

    public BurnTheAccursed() {
        // Burn the Accursed deals 5 damage to target creature and 2 damage to that creature's
        // controller. If that creature would die this turn, exile it instead.
        DealDamageToTargetCreatureEffect creatureDamage = new DealDamageToTargetCreatureEffect(5);
        DealDamageToPlayersEffect controllerDamage =
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PERMANENT_CONTROLLER);
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .bindEffect(creatureDamage)
                .bindEffect(controllerDamage);
        addSimultaneousEffects(EffectSlot.SPELL, creatureDamage, controllerDamage);
    }
}
