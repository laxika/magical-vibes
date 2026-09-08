package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;

@CardRegistration(set = "TOR", collectorNumber = "22")
public class AlterReality extends Card {

    public AlterReality() {
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect(true, false, true));
        addCastingOption(new FlashbackCast("{1}{U}"));
    }
}
