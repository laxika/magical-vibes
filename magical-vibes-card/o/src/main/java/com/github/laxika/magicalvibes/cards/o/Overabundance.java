package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "259")
public class Overabundance extends Card {

    public Overabundance() {
        // Whenever a player taps a land for mana, that player adds one mana of any type that land produced,
        // and Overabundance deals 1 damage to them.
        addTrigger(Triggers.playerTapsLandForMana(), SequenceEffect.of(
                new AddOneOfEachManaTypeProducedByLandEffect(),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER)));
    }
}
