package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "182")
@CardRegistration(set = "AKR", collectorNumber = "213")
@CardRegistration(set = "MP2", collectorNumber = "28")
@CardRegistration(set = "FDC", collectorNumber = "223")
public class RhonasTheIndomitable extends Card {

    public RhonasTheIndomitable() {
        // Deathtouch, indestructible — auto-loaded from Scryfall.

        // Rhonas can't attack or block unless you control another creature with power 4 or greater.
        addStaticEffect(CantAttackOrBlockUnlessEffect.attackOrBlock(
                new ControlsOtherPermanentCount(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4)
                ))),
                "you control another creature with power 4 or greater"
        ));

        // {2}{G}: Another target creature gets +2/+0 and gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{G}",
                List.of(
                        BoostEffect.target(2, 0),
                        new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target())
                ),
                "{2}{G}: Another target creature gets +2/+0 and gains trample until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another creature"
                )
        ));
    }
}
