package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureSkywriterDjinnSpellbookEffect;

@CardRegistration(set = "YDMU", collectorNumber = "5")
public class SkywriterDjinn extends Card {

    public SkywriterDjinn() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConjureSkywriterDjinnSpellbookEffect());
    }
}
