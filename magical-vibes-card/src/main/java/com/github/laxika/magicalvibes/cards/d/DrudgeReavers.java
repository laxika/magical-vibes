package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "105")
public class DrudgeReavers extends Card {

    public DrudgeReavers() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                "{B}: Regenerate Drudge Reavers."));
    }
}
