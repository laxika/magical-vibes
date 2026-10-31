package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardCardsEffect;

@CardRegistration(set = "ROE", collectorNumber = "128")
@CardRegistration(set = "M3C", collectorNumber = "205")
@CardRegistration(set = "C21", collectorNumber = "155")
public class SufferThePast extends Card {

    public SufferThePast() {
        addSpellEffect(new ExileTargetPlayerGraveyardCardsEffect(1, 1));
    }
}
