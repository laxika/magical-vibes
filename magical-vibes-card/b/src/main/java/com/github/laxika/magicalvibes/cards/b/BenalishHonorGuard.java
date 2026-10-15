package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "5")
public class BenalishHonorGuard extends Card {

    public BenalishHonorGuard() {
        // This creature gets +1/+0 for each legendary creature you control.
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        new PermanentIsCreaturePredicate()
                )), CountScope.CONTROLLER),
                new Fixed(0)));
    }
}
