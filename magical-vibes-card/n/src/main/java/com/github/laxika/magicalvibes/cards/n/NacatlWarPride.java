package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "147")
public class NacatlWarPride extends Card {

    public NacatlWarPride() {
        addStaticEffect(new MustBeBlockedIfAbleEffect());
        addStaticEffect(new CanBeBlockedByAtMostNCreaturesEffect(1));
        PermanentCount defendingPlayerCreatures =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.DEFENDING_PLAYER);
        addTrigger(Triggers.attacks(),
                CreateTokenCopyOfSourceEffect.tappedAndAttacking(defendingPlayerCreatures, true));
    }
}
