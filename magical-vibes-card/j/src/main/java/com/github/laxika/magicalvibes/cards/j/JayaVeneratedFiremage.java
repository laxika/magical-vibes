package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "135")
@CardRegistration(set = "SLD", collectorNumber = "513")
public class JayaVeneratedFiremage extends Card {

    public JayaVeneratedFiremage() {
        addStaticEffect(new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(1).anotherSource()
                .fromSources(new PermanentColorInPredicate(Set.of(CardColor.RED))).toAnyPermanentOrPlayer());

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(DealDamageEffect.toAnyTarget(2)),
                "−2: Jaya deals 2 damage to any target."
        ));
    }
}
