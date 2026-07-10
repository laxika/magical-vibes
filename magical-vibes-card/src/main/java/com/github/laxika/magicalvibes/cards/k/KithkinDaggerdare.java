package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "223")
public class KithkinDaggerdare extends Card {

    public KithkinDaggerdare() {
        addActivatedAbility(new ActivatedAbility(true, "{G}",
                List.of(new BoostTargetCreatureEffect(2, 2)),
                "{G}, {T}: Target attacking creature gets +2/+2 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsAttackingPredicate(),
                        "Target must be an attacking creature"
                )));
    }
}
