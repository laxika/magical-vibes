package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MKM", collectorNumber = "38")
public class AgencyOutfitter extends Card {

    public AgencyOutfitter() {
        // When this creature enters, you may search your graveyard, hand, and/or library for a card
        // named Magnifying Glass and/or a card named Thinking Cap and put them onto the battlefield.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                SequenceEffect.of(
                        new SearchZonesForCardNamedToBattlefieldEffect("Magnifying Glass"),
                        new SearchZonesForCardNamedToBattlefieldEffect("Thinking Cap")),
                "Search your graveyard, hand, and/or library for a card named Magnifying Glass and/or "
                        + "a card named Thinking Cap?"));
    }
}
