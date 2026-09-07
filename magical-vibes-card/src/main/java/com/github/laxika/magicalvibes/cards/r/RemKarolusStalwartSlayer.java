package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventSpellDamageToControllerAndPermanentsEffect;

@CardRegistration(set = "MID", collectorNumber = "235")
public class RemKarolusStalwartSlayer extends Card {

    public RemKarolusStalwartSlayer() {
        addEffect(EffectSlot.STATIC, new PreventSpellDamageToControllerAndPermanentsEffect());
        addEffect(EffectSlot.STATIC, new AdditionalDamageToOpponentsAndTheirPermanentsEffect(1));
    }
}
