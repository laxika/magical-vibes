package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "STH", collectorNumber = "49")
public class WalkingDream extends Card {

    public WalkingDream() {
        addStaticEffect(new CantBeBlockedEffect());
        addStaticEffect(new ConditionalEffect(
                new OpponentControlsPermanentCount(2, new PermanentIsCreaturePredicate()),
                DoesntUntapEffect.self()));
    }
}
