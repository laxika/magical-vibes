package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "53")
public class SwordDancer extends Card {

    public SwordDancer() {
        addActivatedAbility(new ActivatedAbility(false, "{W}{W}",
                List.of(new BoostTargetCreatureEffect(-1, 0)),
                "{W}{W}: Target attacking creature gets -1/-0 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsAttackingPredicate(),
                        "Target must be an attacking creature"
                )));
    }
}
