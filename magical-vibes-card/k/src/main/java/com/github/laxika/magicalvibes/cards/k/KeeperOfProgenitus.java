package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddProducedManaWhenLandOfSubtypeTappedEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "135")
public class KeeperOfProgenitus extends Card {

    public KeeperOfProgenitus() {
        // Whenever a player taps a Mountain, Forest, or Plains for mana,
        // that player adds one mana of any type that land produced.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddProducedManaWhenLandOfSubtypeTappedEffect(
                        List.of(CardSubtype.MOUNTAIN, CardSubtype.FOREST, CardSubtype.PLAINS)));
    }
}
