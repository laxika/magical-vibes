package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectAllCreatureDamageToControllerEffect;

@CardRegistration(set = "CHR", collectorNumber = "4")
@CardRegistration(set = "DRK", collectorNumber = "2")
public class BloodOfTheMartyr extends Card {

    public BloodOfTheMartyr() {
        addEffect(EffectSlot.SPELL, new RedirectAllCreatureDamageToControllerEffect());
    }
}
