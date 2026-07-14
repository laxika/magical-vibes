package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "24")
public class IndigoFaerie extends Card {

    public IndigoFaerie() {
        // Flying is auto-loaded from Scryfall.
        // {U}: Target permanent becomes blue in addition to its other colors until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{U}",
                List.of(new GrantColorUntilEndOfTurnEffect(CardColor.BLUE, true)),
                "{U}: Target permanent becomes blue in addition to its other colors until end of turn."
        ));
    }
}
