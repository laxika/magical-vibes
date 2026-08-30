package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;

@CardRegistration(set = "KHM", collectorNumber = "44")
public class AuguryRaven extends Card {

    public AuguryRaven() {
        addCastingOption(new ForetellCast("{1}{U}"));
    }
}
