package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "42")
public class MantaRay extends Card {

    public MantaRay() {
        // "This creature can't attack unless defending player controls an Island."
        addStaticEffect(new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"
        ));

        // "This creature can't be blocked except by blue creatures."
        addStaticEffect(new CanBeBlockedOnlyByFilterEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLUE)), "blue creatures"));

        // When you control no Islands, sacrifice this creature.
        addTrigger(Triggers.sourceState(new PermanentNotPredicate(new PermanentControllerControlsPermanentPredicate(
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND)))),
                new SacrificeSelfEffect());
    }
}
