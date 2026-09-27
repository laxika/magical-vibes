package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;

@CardRegistration(set = "CMM", collectorNumber = "316")
@CardRegistration(set = "CMM", collectorNumber = "569")
@CardRegistration(set = "CMM", collectorNumber = "651")
public class RegalBehemoth extends Card {

    public RegalBehemoth() {
        // Trample is auto-loaded from Scryfall.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());

        // While its controller is the monarch, tapping a land adds one additional mana of a type
        // that land produced.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddOneOfEachManaTypeProducedByLandEffect(true, null, true));
    }
}
