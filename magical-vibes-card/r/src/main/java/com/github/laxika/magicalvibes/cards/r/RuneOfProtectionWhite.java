package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "41")
public class RuneOfProtectionWhite extends Card {

    public RuneOfProtectionWhite() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYou(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)), "white")),
                "The next time a white source of your choice would deal damage to you this turn, prevent that damage."
        ));
        addCycling("{2}");
    }
}
