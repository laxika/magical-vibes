package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "123")
public class GyreSage extends Card {

    public GyreSage() {
        // Evolve is keyword-driven; the engine creates the trigger from Keyword.EVOLVE.

        // {T}: Add {G} for each +1/+1 counter on Gyre Sage.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE))),
                "{T}: Add {G} for each +1/+1 counter on Gyre Sage."
        ));
    }
}
