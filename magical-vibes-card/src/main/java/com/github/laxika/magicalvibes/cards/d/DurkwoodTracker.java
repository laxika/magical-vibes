package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "194")
public class DurkwoodTracker extends Card {

    public DurkwoodTracker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(new ConditionalEffect(new SourceIsOnBattlefield(), new SourceFightsTargetCreatureEffect())),
                "{1}{G}, {T}: If this creature is on the battlefield, it deals damage equal to its power to target attacking creature. "
                        + "That creature deals damage equal to its power to this creature.",
                TargetFilters.attackingCreature()
        ));
    }
}
