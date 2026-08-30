package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToChosenCreatureOrPlaneswalkerThisTurnEffect;

@CardRegistration(set = "WAR", collectorNumber = "14")
public class GideonsSacrifice extends Card {

    public GideonsSacrifice() {
        addEffect(EffectSlot.SPELL, new RedirectYourDamageToChosenCreatureOrPlaneswalkerThisTurnEffect());
    }
}
