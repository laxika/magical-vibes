package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExchangeGraveyardAndLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "STX", collectorNumber = "191")
public class HarnessInfinity extends Card {

    public HarnessInfinity() {
        addSpellEffect(new ExchangeGraveyardAndLibraryEffect(Zone.HAND, Zone.GRAVEYARD));
        addSpellEffect(new ExileSpellEffect());
    }
}
