package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCombatOpponentToHandAtEndOfCombatEffect;

@CardRegistration(set = "PLC", collectorNumber = "93")
public class AetherMembrane extends Card {

    public AetherMembrane() {
        addEffect(EffectSlot.ON_BLOCK, new ReturnCombatOpponentToHandAtEndOfCombatEffect());
    }
}
