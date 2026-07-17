package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "215")
public class CavePeople extends Card {

    public CavePeople() {
        // Whenever this creature attacks, it gets +1/-2 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(1, -2));

        // {1}{R}{R}, {T}: Target creature gains mountainwalk until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}{R}",
                List.of(new GrantKeywordEffect(Keyword.MOUNTAINWALK, GrantScope.TARGET)),
                "{1}{R}{R}, {T}: Target creature gains mountainwalk until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
