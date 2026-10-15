package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "227")
public class ArcaneEncyclopedia extends Card {

    public ArcaneEncyclopedia() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new DrawCardEffect(1)),
                "{3}, {T}: Draw a card."
        ));
    }
}
