package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "5DN", collectorNumber = "84")
public class ChannelTheSuns extends Card {

    public ChannelTheSuns() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.WHITE));
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.BLUE));
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.BLACK));
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.RED));
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.GREEN));
    }
}
