package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeForColorlessManaUntilEndOfTurnEffect;

@CardRegistration(set = "4ED", collectorNumber = "236")
@CardRegistration(set = "SUM", collectorNumber = "188")
@CardRegistration(set = "3ED", collectorNumber = "188")
@CardRegistration(set = "V09", collectorNumber = "3")
@CardRegistration(set = "VMA", collectorNumber = "200")
public class Channel extends Card {

    public Channel() {
        addEffect(EffectSlot.SPELL, new MayPayLifeForColorlessManaUntilEndOfTurnEffect());
    }
}
