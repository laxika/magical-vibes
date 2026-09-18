package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "85")
@CardRegistration(set = "TLE", collectorNumber = "175")
public class SukiKyoshiCaptain extends Card {

    public SukiKyoshiCaptain() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)));

        PermanentPredicate attackingWarrior = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.ALL_OWN_CREATURES,
                        attackingWarrior)),
                "{3}{W}: Attacking Warriors you control gain double strike until end of turn."
        ));
    }
}
