package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "SHM", collectorNumber = "224")
public class BarkshellBlessing extends Card {

    public BarkshellBlessing() {
        // Target creature gets +2/+2 until end of turn.
        // (Conspire is handled entirely by the casting flow via the Scryfall keyword.)
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
    }
}
