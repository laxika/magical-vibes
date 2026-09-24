package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;

@CardRegistration(set = "SLD", collectorNumber = "821")
public class EchoOfEons extends Card {

    public EchoOfEons() {
        addEffect(EffectSlot.SPELL, new EachPlayerShufflesZonesIntoLibraryEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));
        addCastingOption(new FlashbackCast("{2}{U}"));
    }
}
