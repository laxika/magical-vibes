package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ExileUntilNonlandCardMayCastIfManaValueLessThanMountainsEffect;

@CardRegistration(set = "TLA", collectorNumber = "153")
public class SolsticeRevelations extends Card {

    public SolsticeRevelations() {
        addCastingOption(new FlashbackCast("{6}{R}"));
        addSpellEffect(new ExileUntilNonlandCardMayCastIfManaValueLessThanMountainsEffect());
    }
}
