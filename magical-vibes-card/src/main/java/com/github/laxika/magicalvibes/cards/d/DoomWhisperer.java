package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "69")
public class DoomWhisperer extends Card {

    public DoomWhisperer() {
        // Flying and trample are auto-loaded from Scryfall.

        // Pay 2 life: Surveil 2.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(2), new SurveilEffect(2)),
                "Pay 2 life: Surveil 2."));
    }
}
