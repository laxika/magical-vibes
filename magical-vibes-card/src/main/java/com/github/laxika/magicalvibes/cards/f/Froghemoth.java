package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileUpToCombatDamageCardsFromGraveyardEffect;

@CardRegistration(set = "AFR", collectorNumber = "184")
public class Froghemoth extends Card {

    public Froghemoth() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileUpToCombatDamageCardsFromGraveyardEffect());
    }
}
