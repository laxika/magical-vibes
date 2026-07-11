package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Karplusan Yeti — 3/3 Yeti.
 * {T}: This creature deals damage equal to its power to target creature.
 * That creature deals damage equal to its power to this creature. (fight mechanic)
 */
@CardRegistration(set = "9ED", collectorNumber = "198")
public class KarplusanYeti extends Card {

    public KarplusanYeti() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new SourceFightsTargetCreatureEffect()),
                "{T}: This creature deals damage equal to its power to target creature. "
                        + "That creature deals damage equal to its power to this creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
