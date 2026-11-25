package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "109")
public class NightfireGiant extends Card {

    public NightfireGiant() {
        addStaticEffect(new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)), new StaticBoostEffect(1, 1, Subjects.source())));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(DealDamageEffect.toAnyTarget(2)),
                "{4}{R}: Nightfire Giant deals 2 damage to any target."
        ));
    }
}
