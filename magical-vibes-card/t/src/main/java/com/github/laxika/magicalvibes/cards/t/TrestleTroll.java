package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "205")
public class TrestleTroll extends Card {

    public TrestleTroll() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{G}",
                List.of(new RegenerateEffect()),
                "{1}{B}{G}: Regenerate Trestle Troll."
        ));
    }
}
