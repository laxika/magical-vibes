package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ALA", collectorNumber = "166")
public class EmpyrialArchangel extends Card {

    public EmpyrialArchangel() {
        // Flying and Shroud are auto-loaded keywords. "All damage that would be dealt to you is
        // dealt to this creature instead."
        addEffect(EffectSlot.STATIC, new RedirectPlayerDamageToSelfEffect());
    }
}
