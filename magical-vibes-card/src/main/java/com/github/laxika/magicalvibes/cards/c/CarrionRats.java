package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect;

@CardRegistration(set = "TOR", collectorNumber = "54")
public class CarrionRats extends Card {

    public CarrionRats() {
        addEffect(EffectSlot.ON_ATTACK, new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect());
        addEffect(EffectSlot.ON_BLOCK, new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect());
    }
}
