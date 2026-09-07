package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "WAR", collectorNumber = "157")
public class ChallengerTroll extends Card {

    public ChallengerTroll() {
        addEffect(EffectSlot.STATIC, new EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(
                1, new PermanentPowerAtLeastPredicate(4)));
    }
}
