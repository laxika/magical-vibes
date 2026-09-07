package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DIS", collectorNumber = "102")
public class AnthemOfRakdos extends Card {

    public AnthemOfRakdos() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, SequenceEffect.of(
                new BoostTargetCreatureEffect(2, 0),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHandEmpty(), new DoubleControllerDamageEffect(null, true)));
    }
}
