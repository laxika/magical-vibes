package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandCardFromGraveyardAndSearchSameNameEffect;

@CardRegistration(set = "AER", collectorNumber = "71")
public class SecretSalvage extends Card {

    public SecretSalvage() {
        addSpellEffect(new ExileTargetNonlandCardFromGraveyardAndSearchSameNameEffect());
    }
}
