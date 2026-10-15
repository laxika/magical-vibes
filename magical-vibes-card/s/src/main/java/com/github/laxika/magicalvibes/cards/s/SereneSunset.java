package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JUD", collectorNumber = "131")
public class SereneSunset extends Card {

    public SereneSunset() {
        targetExactlyX(TargetFilters.creature(), 100)
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatByTargetCreatures());
    }
}
