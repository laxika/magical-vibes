package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "58")
@CardRegistration(set = "M11", collectorNumber = "58")
public class JaceBeleren extends Card {

    public JaceBeleren() {
        // +2: Each player draws a card.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new EachPlayerDrawsCardEffect(1)),
                "+2: Each player draws a card."
        ));

        // −1: Target player draws a card.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new DrawCardForTargetPlayerEffect(1, false, true)),
                "\u22121: Target player draws a card."
        ));

        // −10: Target player mills twenty cards.
        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new MillTargetPlayerEffect(20)),
                "\u221210: Target player mills twenty cards."
        ));
    }
}
