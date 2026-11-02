package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2363")
public class PiratedCopy extends Card {

    public PiratedCopy() {
        addStaticEffect(new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(),
                "creature",
                Set.of(CardSubtype.PIRATE),
                List.of(
                        new TriggeredAbility(Triggers.creatureWithSameNameDealsCombatDamageToPlayer(),
                                new AllyCombatDamageTriggerEffect(
                                new PermanentHasSameNameAsSourcePredicate(),
                                new DrawCardEffect(1))))));
    }
}
