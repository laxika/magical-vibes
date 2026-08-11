package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "178")
public class BlazingSalvo extends Card {

    public BlazingSalvo() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new MayEffect(
                new DealDamageToPlayersEffect(5, DamageRecipient.TARGET_PERMANENT_CONTROLLER),
                "Have Blazing Salvo deal 5 damage to you?",
                new DealDamageToTargetCreatureEffect(3),
                MayChoicePlayer.TARGET_PERMANENT_CONTROLLER));
    }
}
