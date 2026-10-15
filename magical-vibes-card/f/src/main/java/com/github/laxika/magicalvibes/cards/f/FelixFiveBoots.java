package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;

@CardRegistration(set = "OTC", collectorNumber = "6")
@CardRegistration(set = "OTC", collectorNumber = "42")
public class FelixFiveBoots extends Card {

    public FelixFiveBoots() {
        addEffect(EffectSlot.STATIC, AdditionalTriggeredAbilityEffect.forAllyCreatureCombatDamageToPlayer());
    }
}
