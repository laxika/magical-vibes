package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

public class JayemdaeTome extends Card {

    public JayemdaeTome() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new DrawCardEffect()),
                false,
                "{4}, {T}: Draw a card."
        ));
    }
}
