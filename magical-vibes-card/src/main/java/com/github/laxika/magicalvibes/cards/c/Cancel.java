package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "71")
@CardRegistration(set = "M10", collectorNumber = "44")
@CardRegistration(set = "M11", collectorNumber = "48")
public class Cancel extends Card {

    public Cancel() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
