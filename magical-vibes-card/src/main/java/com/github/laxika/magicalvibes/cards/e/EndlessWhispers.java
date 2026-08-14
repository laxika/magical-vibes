package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;

@CardRegistration(set = "5DN", collectorNumber = "49")
public class EndlessWhispers extends Card {

    public EndlessWhispers() {
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_DEATH,
                RegisterDelayedReturnDyingCreatureUnderControlEffect.forOpponent(),
                GrantScope.ALL_CREATURES));
    }
}
