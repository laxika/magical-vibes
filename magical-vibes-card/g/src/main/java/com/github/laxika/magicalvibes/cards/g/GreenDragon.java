package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;

@CardRegistration(set = "AFR", collectorNumber = "186")
public class GreenDragon extends Card {

    public GreenDragon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                        EffectSlot.ON_OPPONENT_CREATURE_DEALT_DAMAGE,
                        new DestroyTargetPermanentEffect()));
    }
}
