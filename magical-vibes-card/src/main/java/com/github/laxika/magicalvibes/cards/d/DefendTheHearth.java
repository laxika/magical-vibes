package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "THS", collectorNumber = "156")
public class DefendTheHearth extends Card {

    public DefendTheHearth() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatToPlayers());
    }
}
