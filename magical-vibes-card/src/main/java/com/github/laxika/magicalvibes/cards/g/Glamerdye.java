package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;

@CardRegistration(set = "EVE", collectorNumber = "21")
public class Glamerdye extends Card {

    public Glamerdye() {
        // Change the text of target spell or permanent by replacing all instances of one color word
        // with another (color words only — no basic land types; targets a spell or a permanent).
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect(true, false, true));
        addCastingOption(new Retrace());
    }
}
