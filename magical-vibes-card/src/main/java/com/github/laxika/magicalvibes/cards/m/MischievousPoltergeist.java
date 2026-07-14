package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "144")
public class MischievousPoltergeist extends Card {

    public MischievousPoltergeist() {
        // Pay 1 life: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), new RegenerateEffect()),
                "Pay 1 life: Regenerate Mischievous Poltergeist."));
    }
}
