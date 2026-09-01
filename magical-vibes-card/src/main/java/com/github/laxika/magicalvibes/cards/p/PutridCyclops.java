package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.TopCardOfLibraryManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "FUT", collectorNumber = "75")
public class PutridCyclops extends Card {

    public PutridCyclops() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostSelfEffect(
                new Scaled(new TopCardOfLibraryManaValue(), -1),
                new Scaled(new TopCardOfLibraryManaValue(), -1)));
    }
}
