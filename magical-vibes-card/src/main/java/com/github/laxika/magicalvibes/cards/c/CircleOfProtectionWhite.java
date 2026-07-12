package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenColoredSourceEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "14")
public class CircleOfProtectionWhite extends Card {

    public CircleOfProtectionWhite() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new PreventNextDamageFromChosenColoredSourceEffect(CardColor.WHITE)),
                "The next time a white source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
