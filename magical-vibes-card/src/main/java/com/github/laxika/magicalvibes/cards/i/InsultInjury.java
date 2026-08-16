package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageThisTurnEffect;

/**
 * Insult // Injury — front half (Insult).
 * Sorcery — Damage can't be prevented this turn. If a source you control would deal damage this
 * turn, it deals double that damage instead.
 * Back half (Injury) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "213")
@CardRegistration(set = "AKR", collectorNumber = "162")
public class InsultInjury extends Card {

    public InsultInjury() {
        setBackFaceCard(new Injury());

        // Damage can't be prevented this turn.
        addEffect(EffectSlot.SPELL, new DamageCantBePreventedThisTurnEffect());
        // If a source you control would deal damage this turn, it deals double that damage instead.
        addEffect(EffectSlot.SPELL, new DoubleControllerDamageThisTurnEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Injury";
    }
}
