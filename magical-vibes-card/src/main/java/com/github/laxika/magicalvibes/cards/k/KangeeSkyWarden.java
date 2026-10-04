package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

import java.util.List;

@CardRegistration(set = "SCD", collectorNumber = "234")
public class KangeeSkyWarden extends Card {

    public KangeeSkyWarden() {
        addEffect(EffectSlot.ON_ATTACK, new BoostAllCreaturesEffect(2, 0,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)
                ))));
        addEffect(EffectSlot.ON_BLOCK, new BoostAllCreaturesEffect(0, 2,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsBlockingPredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)
                ))));
    }
}
