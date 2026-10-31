package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "5DN", collectorNumber = "84")
public class ChannelTheSuns extends Card {

    public ChannelTheSuns() {
        addSpellEffect(new AwardManaEffect(ManaColor.WHITE));
        addSpellEffect(new AwardManaEffect(ManaColor.BLUE));
        addSpellEffect(new AwardManaEffect(ManaColor.BLACK));
        addSpellEffect(new AwardManaEffect(ManaColor.RED));
        addSpellEffect(new AwardManaEffect(ManaColor.GREEN));
    }
}
