package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetMatches;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;

@CardRegistration(set = "MOM", collectorNumber = "172")
public class WarTrainedSlasher extends Card {

    public WarTrainedSlasher() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new AttackedTargetMatches(new PermanentIsBattlePredicate()),
                new BoostSelfEffect(new SourcePower(), new Fixed(0))));
    }
}
