package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChannelHarmEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "7")
public class ChannelHarm extends Card {

    public ChannelHarm() {
        target(TargetFilters.creature()).addSpellEffect(new ChannelHarmEffect());
    }
}
