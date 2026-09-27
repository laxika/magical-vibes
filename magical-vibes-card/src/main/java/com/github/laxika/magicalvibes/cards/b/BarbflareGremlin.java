package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DSC", collectorNumber = "26")
@CardRegistration(set = "DSC", collectorNumber = "55")
public class BarbflareGremlin extends Card {

    public BarbflareGremlin() {
        // Whenever a player taps a land for mana, if this creature is tapped, that player adds
        // one mana of any type that land produced. Then that land deals 1 damage to that player.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new ConditionalEffect(new SourceIsTapped(), SequenceEffect.of(
                        new AddOneOfEachManaTypeProducedByLandEffect(false),
                        new DealDamageOnLandTapEffect(1))));
    }
}
