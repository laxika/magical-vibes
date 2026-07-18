package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeForColorlessManaUntilEndOfTurnEffect;

@CardRegistration(set = "4ED", collectorNumber = "236")
public class Channel extends Card {

    public Channel() {
        addEffect(EffectSlot.SPELL, new MayPayLifeForColorlessManaUntilEndOfTurnEffect());
    }
}
