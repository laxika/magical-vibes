package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Map;
import java.util.UUID;

/**
 * Internal marker for a spell-cast trigger whose condition is evaluated after the spell resolves.
 * The prepared form retains the source and the card-resource counts captured when the spell was cast.
 */
public record CardAdvantageSpellCastTriggerEffect(
        Card sourceCard,
        UUID controllerId,
        UUID sourcePermanentId,
        UUID spellCardId,
        Map<UUID, Integer> cardCountsBefore
) implements CardEffect {

    public CardAdvantageSpellCastTriggerEffect() {
        this(null, null, null, null, Map.of());
    }

    public CardAdvantageSpellCastTriggerEffect {
        cardCountsBefore = cardCountsBefore == null ? Map.of() : Map.copyOf(cardCountsBefore);
    }

    public CardAdvantageSpellCastTriggerEffect withSnapshot(Card sourceCard, UUID controllerId,
                                                              UUID sourcePermanentId, UUID spellCardId,
                                                              Map<UUID, Integer> cardCountsBefore) {
        return new CardAdvantageSpellCastTriggerEffect(
                sourceCard, controllerId, sourcePermanentId, spellCardId, cardCountsBefore);
    }

    public boolean isPrepared() {
        return sourceCard != null && controllerId != null && sourcePermanentId != null
                && spellCardId != null;
    }
}
