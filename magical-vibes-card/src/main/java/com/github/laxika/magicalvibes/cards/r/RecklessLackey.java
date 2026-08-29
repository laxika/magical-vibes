package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "140")
public class RecklessLackey extends Card {

    public RecklessLackey() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1), CreateTokenEffect.ofTreasureToken(1)),
                "{2}{R}, Sacrifice this creature: Draw a card and create a Treasure token."
        ));
    }
}
