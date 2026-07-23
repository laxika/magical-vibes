package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

@CardRegistration(set = "5ED", collectorNumber = "110")
@CardRegistration(set = "ICE", collectorNumber = "90")
public class Portent extends Card {

    public Portent() {
        addEffect(EffectSlot.SPELL, new ReorderTopCardsOfTargetLibraryEffect(3));
        addEffect(EffectSlot.SPELL, new MayEffect(new ShuffleLibraryEffect(true), "You may have that player shuffle their library."));
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect(1));
    }
}
