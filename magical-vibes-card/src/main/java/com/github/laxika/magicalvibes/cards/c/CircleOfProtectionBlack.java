package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenColoredSourceEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "8")
@CardRegistration(set = "7ED", collectorNumber = "6")
@CardRegistration(set = "8ED", collectorNumber = "10")
@CardRegistration(set = "9ED", collectorNumber = "10")
public class CircleOfProtectionBlack extends Card {

    public CircleOfProtectionBlack() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new PreventNextDamageFromChosenColoredSourceEffect(CardColor.BLACK)),
                "The next time a black source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
