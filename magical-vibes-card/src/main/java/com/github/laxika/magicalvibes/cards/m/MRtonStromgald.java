package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "199")
public class MRtonStromgald extends Card {

    public MRtonStromgald() {
        // Whenever Marton Stromgald attacks, other attacking creatures get +1/+1
        // until end of turn for each attacking creature other than Marton Stromgald.
        PermanentCount otherAttackers = new PermanentCount(
                new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER, true);
        addEffect(EffectSlot.ON_ATTACK, new BoostAllCreaturesEffect(
                otherAttackers,
                otherAttackers,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                ))));

        // Whenever Marton Stromgald blocks, other blocking creatures get +1/+1
        // until end of turn for each blocking creature other than Marton Stromgald.
        PermanentCount otherBlockers = new PermanentCount(
                new PermanentIsBlockingPredicate(), CountScope.ANY_PLAYER, true);
        addEffect(EffectSlot.ON_BLOCK, new BoostAllCreaturesEffect(
                otherBlockers,
                otherBlockers,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsBlockingPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                ))));
    }
}
