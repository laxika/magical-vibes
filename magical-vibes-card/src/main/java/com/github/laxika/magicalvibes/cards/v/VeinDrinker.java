package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "91")
public class VeinDrinker extends Card {

    public VeinDrinker() {
        // {R}, {T}: This creature deals damage equal to its power to target creature.
        // That creature deals damage equal to its power to this creature.
        addActivatedAbility(new ActivatedAbility(
                true, "{R}",
                List.of(new SourceFightsTargetCreatureEffect()),
                "{R}, {T}: This creature fights target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // Whenever a creature dealt damage by this creature this turn dies, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
