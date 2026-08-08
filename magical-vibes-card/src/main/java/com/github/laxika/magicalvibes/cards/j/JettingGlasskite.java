package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;


@CardRegistration(set = "BOK", collectorNumber = "38")
public class JettingGlasskite extends Card {

    public JettingGlasskite() {
        // Flying is auto-loaded from Scryfall.

        // Whenever this creature becomes the target of a spell or ability for the first time each turn,
        // counter that spell or ability. The "first time each turn" gating and targeting the triggering
        // object are handled in TriggerCollectionService for any counterspelling effect in this slot.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, new CounterSpellEffect());
    }
}
