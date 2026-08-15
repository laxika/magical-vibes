package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "KLD", collectorNumber = "148")
public class CommencementOfFestivities extends Card {

    public CommencementOfFestivities() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatToPlayers());
    }
}
