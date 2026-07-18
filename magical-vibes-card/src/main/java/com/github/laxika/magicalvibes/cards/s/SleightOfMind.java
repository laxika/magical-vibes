package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;

@CardRegistration(set = "5ED", collectorNumber = "124")
@CardRegistration(set = "4ED", collectorNumber = "102")
public class SleightOfMind extends Card {

    public SleightOfMind() {
        // Change the text of target spell or permanent by replacing all instances of one color word
        // with another (color words only — no basic land types; targets a spell or a permanent).
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect(true, false, true));
    }
}
