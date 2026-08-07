package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "ORI", collectorNumber = "49")
public class ClashOfWills extends Card {

    public ClashOfWills() {
        // Counter target spell unless its controller pays {X}.
        //
        // useX makes the cost the X paid for this spell; targeting is auto-derived from the effect.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, false));
    }
}
