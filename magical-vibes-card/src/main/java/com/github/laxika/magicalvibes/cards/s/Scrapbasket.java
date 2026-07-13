package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeAllColorsUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "262")
public class Scrapbasket extends Card {

    public Scrapbasket() {
        // {1}: This creature becomes all colors until end of turn (self-scoped layer-5 color set).
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new BecomeAllColorsUntilEndOfTurnEffect()),
                "{1}: This creature becomes all colors until end of turn."));
    }
}
