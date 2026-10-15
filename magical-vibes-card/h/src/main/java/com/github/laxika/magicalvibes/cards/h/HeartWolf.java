package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSacrificeSourceWhenTargetLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "75")
public class HeartWolf extends Card {

    public HeartWolf() {
        // {T}: Target Dwarf creature gets +2/+0 and gains first strike until end of turn. When that
        // creature leaves the battlefield this turn, sacrifice this creature. Activate only during combat.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new BoostTargetCreatureEffect(2, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET),
                        new RegisterDelayedSacrificeSourceWhenTargetLeavesEffect()
                ),
                "{T}: Target Dwarf creature gets +2/+0 and gains first strike until end of turn. When that "
                        + "creature leaves the battlefield this turn, sacrifice this creature. "
                        + "Activate only during combat.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.DWARF)
                        )),
                        "Target must be a Dwarf creature"
                ),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_COMBAT
        ));
    }
}
