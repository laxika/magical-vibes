package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "203")
public class JorKadeenFirstGoldwarden extends Card {

    public JorKadeenFirstGoldwarden() {
        PermanentAllOfPredicate equippedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsEquippedPredicate()
        ));
        PermanentCount equippedCreatures = new PermanentCount(equippedCreature, CountScope.CONTROLLER);

        addTrigger(Triggers.attacks(), new BoostSelfEffect(equippedCreatures, equippedCreatures));
        addTrigger(Triggers.attacks(), ConditionalEffect.unless(
                new SourcePowerAtLeast(4),
                new DrawCardEffect(1)));
    }
}
