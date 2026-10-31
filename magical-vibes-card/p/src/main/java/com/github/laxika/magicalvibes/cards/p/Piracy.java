package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect;

@CardRegistration(set = "P02", collectorNumber = "42")
@CardRegistration(set = "S99", collectorNumber = "45")
public class Piracy extends Card {

    public Piracy() {
        addSpellEffect(new MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect());
    }
}
