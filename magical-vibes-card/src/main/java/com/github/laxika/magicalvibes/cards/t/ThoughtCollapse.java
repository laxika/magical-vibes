package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "RNA", collectorNumber = "57")
public class ThoughtCollapse extends Card {

    public ThoughtCollapse() {
        addEffect(EffectSlot.SPELL, new MillEffect(3, MillRecipient.TARGET_SPELL_CONTROLLER));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
