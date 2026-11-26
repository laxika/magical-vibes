package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "71")
@CardRegistration(set = "M10", collectorNumber = "58")
@CardRegistration(set = "M11", collectorNumber = "58")
@CardRegistration(set = "DD2", collectorNumber = "1")
@CardRegistration(set = "JVC", collectorNumber = "1")
@CardRegistration(set = "SS1", collectorNumber = "1")
@CardRegistration(set = "SLD", collectorNumber = "746")
@CardRegistration(set = "SLD", collectorNumber = "1454")
@CardRegistration(set = "MB2", collectorNumber = "29")
public class JaceBeleren extends Card {

    public JaceBeleren() {
        // +2: Each player draws a card.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DrawCardEffect(1, Subjects.eachPlayer())),
                "+2: Each player draws a card."
        ));

        // −1: Target player draws a card.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new DrawCardEffect(1, Subjects.targetPlayer())),
                "\u22121: Target player draws a card."
        ));

        // −10: Target player mills twenty cards.
        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new MillEffect(20, Subjects.targetPlayer())),
                "\u221210: Target player mills twenty cards."
        ));
    }
}
