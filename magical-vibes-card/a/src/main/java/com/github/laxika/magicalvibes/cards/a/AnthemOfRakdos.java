package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "102")
public class AnthemOfRakdos extends Card {

    public AnthemOfRakdos() {
        addTrigger(Triggers.creatureYouControlAttacks(), SequenceEffect.of(
                new BoostTargetCreatureEffect(2, 0),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)));
        addStaticEffect(new ConditionalEffect(
                new ControllerHandEmpty(), new DoubleControllerDamageEffect(null, true)));
    }
}
