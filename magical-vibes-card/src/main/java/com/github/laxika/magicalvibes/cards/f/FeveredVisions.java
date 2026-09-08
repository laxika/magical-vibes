package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "244")
public class FeveredVisions extends Card {

    public FeveredVisions() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, SequenceEffect.of(
                new DrawCardForTargetPlayerEffect(1),
                new ConditionalEffect(
                        new AllConditions(List.of(new NotControllerTurn(), new ActivePlayerHandAtLeast(4))),
                        new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER))));
    }
}
