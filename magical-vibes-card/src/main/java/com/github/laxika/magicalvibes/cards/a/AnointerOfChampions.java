package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "3")
public class AnointerOfChampions extends Card {

    public AnointerOfChampions() {
        // {T}: Target attacking creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "{T}: Target attacking creature gets +1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsAttackingPredicate(),
                        "Target must be an attacking creature"
                )));
    }
}
