package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;

@CardRegistration(set = "HOC", collectorNumber = "17")
@CardRegistration(set = "HOC", collectorNumber = "57")
@CardRegistration(set = "MAR", collectorNumber = "5")
@CardRegistration(set = "SOA", collectorNumber = "9")
@CardRegistration(set = "OMB", collectorNumber = "5")
public class Reprieve extends Card {

    public Reprieve() {
        addEffect(EffectSlot.SPELL, new ReturnTargetSpellToHandEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
