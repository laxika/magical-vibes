package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "26")
@CardRegistration(set = "DSC", collectorNumber = "55")
public class BarbflareGremlin extends Card {

    public BarbflareGremlin() {
        // Whenever a player taps a land for mana, if this creature is tapped, that player adds
        // one mana of any type that land produced. Then that land deals 1 damage to that player.
        addTrigger(Triggers.playerTapsLandForMana(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                SequenceEffect.of(
                        new AddOneOfEachManaTypeProducedByLandEffect(false),
                        new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER)
                                .dealtByTriggeringPermanent()));
    }
}
