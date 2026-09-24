package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "27")
public class TsaganRaiderWarlord extends Card {

    private static final PermanentPredicate CREATURE_WITH_FIRST_OR_DOUBLE_STRIKE =
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentAnyOfPredicate(List.of(
                            new PermanentHasKeywordPredicate(Keyword.FIRST_STRIKE),
                            new PermanentHasKeywordPredicate(Keyword.DOUBLE_STRIKE)
                    ))
            ));

    public TsaganRaiderWarlord() {
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                new PermanentCount(CREATURE_WITH_FIRST_OR_DOUBLE_STRIKE, CountScope.CONTROLLER),
                new Fixed(0)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new MaxSpeed(),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new MaxSpeed(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES)));
    }
}
