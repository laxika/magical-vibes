package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "110")
public class WingnutBatOnTheBelfry extends Card {

    public WingnutBatOnTheBelfry() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new GrantChosenKeywordEffect(
                        List.of(Keyword.FLYING, Keyword.MENACE, Keyword.HASTE), GrantScope.SELF));

        addEffect(EffectSlot.ON_ATTACK, new BoostAllCreaturesEffect(1, 0,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                ))));
    }
}
