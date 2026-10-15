package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileUntilNonlandCardMayCastIfManaValueLessThanMountainsEffect;

@CardRegistration(set = "TLA", collectorNumber = "153")
public class SolsticeRevelations extends Card {

    public SolsticeRevelations() {
        addEffect(EffectSlot.SPELL, new ExileUntilNonlandCardMayCastIfManaValueLessThanMountainsEffect());
    }
}
