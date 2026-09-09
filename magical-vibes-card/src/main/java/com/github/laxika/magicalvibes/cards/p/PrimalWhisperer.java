package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "135")
public class PrimalWhisperer extends Card {

    public PrimalWhisperer() {
        PermanentAllOfPredicate faceDownCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsFaceDownPredicate()));
        PermanentCount faceDownCreatureCount = new PermanentCount(faceDownCreature, CountScope.ANY_PLAYER);
        Scaled boost = new Scaled(faceDownCreatureCount, 2);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(boost, boost, GrantScope.SELF));
    }
}
