package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToSpellsThisTurnEffect;

@CardRegistration(set = "SLD", collectorNumber = "2228")
public class BorneUponAWind extends Card {

    public BorneUponAWind() {
        addEffect(EffectSlot.SPELL, new GrantFlashToSpellsThisTurnEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
