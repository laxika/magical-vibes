package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "84")
public class BrandedBrawlers extends Card {

    private static final PermanentAllOfPredicate UNTAPPED_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentNotPredicate(new PermanentIsTappedPredicate())
    ));

    public BrandedBrawlers() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new NotCondition(new DefendingPlayerControlsPermanent(UNTAPPED_LAND)),
                "defending player controls no untapped land"
        ));
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new NotCondition(new ControlsPermanentCount(1, UNTAPPED_LAND)),
                "you control no untapped land"
        ));
    }
}
