package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "152")
public class AkkiCoalflinger extends Card {

    public AkkiCoalflinger() {
        // "Attacking creatures" is not limited to those you control, so the grant sweeps the
        // whole battlefield filtered to attackers.
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ALL_CREATURES,
                        new PermanentIsAttackingPredicate())),
                "{R}, {T}: Attacking creatures gain first strike until end of turn."));
    }
}
