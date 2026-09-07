package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

public class DreadOsseosaur extends Card {

    public DreadOsseosaur() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new MillEffect(2, MillRecipient.CONTROLLER), "Mill two cards?"));
        addEffect(EffectSlot.ON_ATTACK,
                new MayEffect(new MillEffect(2, MillRecipient.CONTROLLER), "Mill two cards?"));
    }
}
