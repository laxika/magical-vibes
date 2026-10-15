package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "DKA", collectorNumber = "32")
public class ChillOfForeboding extends Card {

    public ChillOfForeboding() {
        addEffect(EffectSlot.SPELL, new MillEffect(5, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new MillEffect(5, MillRecipient.EACH_OPPONENT));
        addCastingOption(new FlashbackCast("{7}{U}"));
    }
}
