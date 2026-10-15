package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MSC", collectorNumber = "687")
public class ElektraFemmeFatale extends Card {

    public ElektraFemmeFatale() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                SequenceEffect.of(
                        new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER),
                        new QueueReflexiveAbilityEffect(new DealDamageToTargetCreatureEffect(4))),
                "Have Elektra deal 2 damage to you?"));
    }
}
