package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "68")
public class TatteredDrake extends Card {

    public TatteredDrake() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                "{B}: Regenerate this creature."));
    }
}
