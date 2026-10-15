package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "33")
public class ScepterOfInsight extends Card {

    public ScepterOfInsight() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}",
                List.of(new DrawCardEffect()),
                "{3}{U}, {T}: Draw a card."
        ));
    }
}
