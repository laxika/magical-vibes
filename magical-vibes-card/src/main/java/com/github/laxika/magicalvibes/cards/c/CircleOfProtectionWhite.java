package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "8ED", collectorNumber = "14")
@CardRegistration(set = "5ED", collectorNumber = "21")
@CardRegistration(set = "7ED", collectorNumber = "10")
@CardRegistration(set = "6ED", collectorNumber = "12")
@CardRegistration(set = "4ED", collectorNumber = "18")
@CardRegistration(set = "ICE", collectorNumber = "16")
public class CircleOfProtectionWhite extends Card {

    public CircleOfProtectionWhite() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYou(new PermanentColorInPredicate(Set.of(CardColor.WHITE)), "white")),
                "The next time a white source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
