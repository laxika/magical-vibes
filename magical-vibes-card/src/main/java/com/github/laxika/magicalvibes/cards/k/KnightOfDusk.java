package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "152")
public class KnightOfDusk extends Card {

    public KnightOfDusk() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(new DestroyCreatureBlockingThisEffect()),
                true,
                "{B}{B}: Destroy target creature blocking Knight of Dusk."
        ));
    }
}
