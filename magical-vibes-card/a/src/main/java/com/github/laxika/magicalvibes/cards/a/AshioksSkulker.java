package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "40")
public class AshioksSkulker extends Card {

    public AshioksSkulker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{3}{U}: This creature can't be blocked this turn."
        ));
    }
}
