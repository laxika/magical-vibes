package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect;

@CardRegistration(set = "TOR", collectorNumber = "55")
public class CarrionWurm extends Card {

    public CarrionWurm() {
        addEffect(EffectSlot.ON_ATTACK, new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(3));
        addEffect(EffectSlot.ON_BLOCK, new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(3));
    }
}
