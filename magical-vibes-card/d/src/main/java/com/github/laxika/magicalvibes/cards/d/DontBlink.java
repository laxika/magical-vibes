package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleCreaturesEnteringFromExileEffect;

@CardRegistration(set = "WHO", collectorNumber = "40")
@CardRegistration(set = "WHO", collectorNumber = "645")
public class DontBlink extends Card {

    public DontBlink() {
        addSpellEffect(new ShuffleCreaturesEnteringFromExileEffect());
        addCycling("{2}");
    }
}
