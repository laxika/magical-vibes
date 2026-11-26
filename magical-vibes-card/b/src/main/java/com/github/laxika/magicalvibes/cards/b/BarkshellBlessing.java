package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "SHM", collectorNumber = "224")
public class BarkshellBlessing extends Card {

    public BarkshellBlessing() {
        // Target creature gets +2/+2 until end of turn.
        // (Conspire is handled entirely by the casting flow via the Scryfall keyword.)
        addSpellEffect(BoostEffect.target(2, 2));
    }
}
