package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenColoredSourceEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "11")
@CardRegistration(set = "8ED", collectorNumber = "13")
@CardRegistration(set = "7ED", collectorNumber = "9")
public class CircleOfProtectionRed extends Card {

    public CircleOfProtectionRed() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new PreventNextDamageFromChosenColoredSourceEffect(CardColor.RED)),
                "The next time a red source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
