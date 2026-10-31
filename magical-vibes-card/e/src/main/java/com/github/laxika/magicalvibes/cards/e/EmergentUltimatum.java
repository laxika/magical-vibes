package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EmergentUltimatumEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "IKO", collectorNumber = "185")
@CardRegistration(set = "SPG", collectorNumber = "105")
public class EmergentUltimatum extends Card {

    public EmergentUltimatum() {
        addSpellEffect(new EmergentUltimatumEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
