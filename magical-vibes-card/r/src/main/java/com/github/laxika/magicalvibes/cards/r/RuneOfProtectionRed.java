package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "40")
public class RuneOfProtectionRed extends Card {

    public RuneOfProtectionRed() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYou(
                        new PermanentColorInPredicate(Set.of(CardColor.RED)), "red")),
                "The next time a red source of your choice would deal damage to you this turn, prevent that damage."
        ));
        addCycling("{2}");
    }
}
