package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "41")
public class PilgrimOfVirtue extends Card {

    public PilgrimOfVirtue() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        PreventDamageFromChosenSourceEffect.nextDamageToYou(
                                new PermanentColorInPredicate(Set.of(CardColor.BLACK)), "black")),
                "Sacrifice this creature: The next time a black source of your choice would deal damage this turn, prevent that damage."
        ));
    }
}
