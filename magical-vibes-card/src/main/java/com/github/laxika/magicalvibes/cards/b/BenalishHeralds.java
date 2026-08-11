package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "6")
public class BenalishHeralds extends Card {

    public BenalishHeralds() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}",
                List.of(new DrawCardEffect()),
                "{3}{U}, {T}: Draw a card."
        ));
    }
}
