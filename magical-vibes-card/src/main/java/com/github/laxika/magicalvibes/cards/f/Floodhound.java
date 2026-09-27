package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "42")
public class Floodhound extends Card {

    public Floodhound() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(CreateTokenEffect.ofClueToken(1)),
                "{3}, {T}: Investigate."
        ));
    }
}
