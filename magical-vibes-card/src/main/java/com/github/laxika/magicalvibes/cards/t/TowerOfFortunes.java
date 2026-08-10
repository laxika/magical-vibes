package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "267")
public class TowerOfFortunes extends Card {

    public TowerOfFortunes() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{8}",
                List.of(new DrawCardEffect(4)),
                "{8}, {T}: Draw four cards."
        ));
    }
}
