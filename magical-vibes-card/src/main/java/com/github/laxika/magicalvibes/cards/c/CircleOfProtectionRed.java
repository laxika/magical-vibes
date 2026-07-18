package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenSourceMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "17")
@CardRegistration(set = "9ED", collectorNumber = "11")
@CardRegistration(set = "6ED", collectorNumber = "11")
@CardRegistration(set = "8ED", collectorNumber = "13")
@CardRegistration(set = "7ED", collectorNumber = "9")
@CardRegistration(set = "5ED", collectorNumber = "20")
public class CircleOfProtectionRed extends Card {

    public CircleOfProtectionRed() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new PreventNextDamageFromChosenSourceMatchingEffect(new PermanentColorInPredicate(Set.of(CardColor.RED)), "red")),
                "The next time a red source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
