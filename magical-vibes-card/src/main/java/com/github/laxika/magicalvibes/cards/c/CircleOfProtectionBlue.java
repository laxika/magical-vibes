package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenColoredSourceEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "11")
@CardRegistration(set = "7ED", collectorNumber = "7")
@CardRegistration(set = "6ED", collectorNumber = "9")
public class CircleOfProtectionBlue extends Card {

    public CircleOfProtectionBlue() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new PreventNextDamageFromChosenColoredSourceEffect(CardColor.BLUE)),
                "The next time a blue source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
