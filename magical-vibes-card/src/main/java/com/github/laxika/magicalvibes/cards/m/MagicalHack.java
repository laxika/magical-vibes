package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;

@CardRegistration(set = "5ED", collectorNumber = "101")
public class MagicalHack extends Card {

    public MagicalHack() {
        // Change the text of target spell or permanent by replacing all instances of one basic land
        // type with another (basic land types only — no color words; targets a spell or a permanent).
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect(false, true, true));
    }
}
