package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "15")
@CardRegistration(set = "5ED", collectorNumber = "18")
@CardRegistration(set = "8ED", collectorNumber = "11")
@CardRegistration(set = "7ED", collectorNumber = "7")
@CardRegistration(set = "6ED", collectorNumber = "9")
@CardRegistration(set = "ICE", collectorNumber = "13")
public class CircleOfProtectionBlue extends Card {

    public CircleOfProtectionBlue() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYou(new PermanentColorInPredicate(Set.of(CardColor.BLUE)), "blue")),
                "The next time a blue source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
