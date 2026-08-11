package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "202")
public class SentryOfTheUnderworld extends Card {

    public SentryOfTheUnderworld() {
        // Flying and vigilance are auto-loaded from Scryfall keywords.

        // {W}{B}, Pay 3 life: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{B}",
                List.of(new PayLifeCost(3), new RegenerateEffect()),
                "{W}{B}, Pay 3 life: Regenerate this creature."
        ));
    }
}
