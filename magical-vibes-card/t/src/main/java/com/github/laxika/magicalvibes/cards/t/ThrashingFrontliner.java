package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetMatches;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;

@CardRegistration(set = "MOM", collectorNumber = "167")
public class ThrashingFrontliner extends Card {

    public ThrashingFrontliner() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new AttackedTargetMatches(new PermanentIsBattlePredicate()),
                new BoostSelfEffect(1, 1)));
    }
}
