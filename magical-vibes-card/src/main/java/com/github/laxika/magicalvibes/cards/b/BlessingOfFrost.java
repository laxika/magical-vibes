package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "161")
public class BlessingOfFrost extends Card {

    public BlessingOfFrost() {
        addEffect(EffectSlot.SPELL, new DistributeCountersAmongControlledCreaturesEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new SnowManaSpentToCast()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4))),
                CountScope.CONTROLLER)));
    }
}
