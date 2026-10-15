package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "39")
public class RuneOfProtectionLands extends Card {

    public RuneOfProtectionLands() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYou(
                        new PermanentIsLandPredicate(), "land")),
                "The next time a land source of your choice would deal damage to you this turn, prevent that damage."
        ));
        addCycling("{2}");
    }
}
