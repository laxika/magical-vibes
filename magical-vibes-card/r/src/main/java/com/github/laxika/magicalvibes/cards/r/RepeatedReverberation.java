package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextLoyaltyAbilityThisTurnEffect;

@CardRegistration(set = "M20", collectorNumber = "156")
public class RepeatedReverberation extends Card {

    public RepeatedReverberation() {
        addEffect(EffectSlot.SPELL, new CopyNextInstantOrSorceryCastThisTurnEffect());
        addEffect(EffectSlot.SPELL, new CopyNextInstantOrSorceryCastThisTurnEffect());
        addEffect(EffectSlot.SPELL, new CopyNextLoyaltyAbilityThisTurnEffect());
        addEffect(EffectSlot.SPELL, new CopyNextLoyaltyAbilityThisTurnEffect());
    }
}
