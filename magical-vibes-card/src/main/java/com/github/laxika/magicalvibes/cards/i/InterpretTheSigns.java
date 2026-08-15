package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardDrawManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "JOU", collectorNumber = "43")
public class InterpretTheSigns extends Card {

    public InterpretTheSigns() {
        addEffect(EffectSlot.SPELL, new ScryEffect(3));
        addEffect(EffectSlot.SPELL, new RevealTopCardDrawManaValueEffect());
    }
}
