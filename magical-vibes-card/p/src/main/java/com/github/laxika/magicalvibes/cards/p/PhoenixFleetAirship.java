package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

@CardRegistration(set = "TLA", collectorNumber = "114")
public class PhoenixFleetAirship extends Card {

    public PhoenixFleetAirship() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new ControllerSacrificedPermanentThisTurn(), new CreateTokenCopyOfSourceEffect()));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(8, new PermanentNamedPredicate("Phoenix Fleet Airship")),
                AnimatePermanentsEffect.crew()));
    }
}
