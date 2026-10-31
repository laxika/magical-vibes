package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EcologicalAppreciationEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "STX", collectorNumber = "128")
public class EcologicalAppreciation extends Card {

    public EcologicalAppreciation() {
        addSpellEffect(new EcologicalAppreciationEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
