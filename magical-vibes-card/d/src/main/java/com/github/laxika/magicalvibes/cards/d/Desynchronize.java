package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "BRO", collectorNumber = "46")
public class Desynchronize extends Card {

    public Desynchronize() {
        addSpellEffect(new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0));
        addSpellEffect(new ScryEffect(2));
    }
}
