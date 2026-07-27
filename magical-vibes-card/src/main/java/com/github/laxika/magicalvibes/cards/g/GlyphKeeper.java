package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;


@CardRegistration(set = "AKH", collectorNumber = "55")
public class GlyphKeeper extends Card {

    public GlyphKeeper() {
        // Flying is auto-loaded from Scryfall.

        // Whenever this creature becomes the target of a spell or ability for the first time each turn,
        // counter that spell or ability. The "first time each turn" gating and targeting the triggering
        // object are handled in TriggerCollectionService for any counterspelling effect in this slot.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, new CounterSpellEffect());

        // Embalm {5}{U}{U} ({5}{U}{U}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Sphinx with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{5}{U}{U}", "Sphinx");
    }
}
