package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EmergentUltimatumEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "IKO", collectorNumber = "185")
public class EmergentUltimatum extends Card {

    public EmergentUltimatum() {
        addEffect(EffectSlot.SPELL, new EmergentUltimatumEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
