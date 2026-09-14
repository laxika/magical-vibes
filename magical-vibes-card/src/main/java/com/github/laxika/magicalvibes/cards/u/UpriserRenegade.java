package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "170")
public class UpriserRenegade extends Card {

    public UpriserRenegade() {
        PermanentCount otherModifiedCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsModifiedPredicate())),
                CountScope.CONTROLLER, true);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new Scaled(otherModifiedCreatures, 2), new Fixed(0)));
    }
}
