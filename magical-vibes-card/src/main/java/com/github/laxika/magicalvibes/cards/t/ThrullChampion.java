package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "44")
public class ThrullChampion extends Card {

    public ThrullChampion() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1,
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasSubtypePredicate(CardSubtype.THRULL)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD)),
                "{T}: Gain control of target Thrull for as long as you control this creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.THRULL),
                        "Target must be a Thrull")));
    }
}
