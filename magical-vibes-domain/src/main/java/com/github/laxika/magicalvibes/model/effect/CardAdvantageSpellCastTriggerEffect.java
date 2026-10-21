package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Map;
import java.util.UUID;

/**
 * Internal marker for a spell-cast trigger whose condition is evaluated after the spell resolves.
 * The prepared form retains the source and the card-resource counts. The counts are first captured when the
 * spell is cast and recaptured when the spell begins to resolve, since card advantage compares the board
 * with the state before that spell resolved. {@code handSpellOwnerId} is the owner of a spell cast from
 * hand, whose card still counts as part of the "before" total while it is on the stack.
 */
public record CardAdvantageSpellCastTriggerEffect(
        Card sourceCard,
        UUID controllerId,
        UUID sourcePermanentId,
        UUID spellCardId,
        Map<UUID, Integer> cardCountsBefore,
        UUID handSpellOwnerId
) implements CardEffect {

    public CardAdvantageSpellCastTriggerEffect() {
        this(null, null, null, null, Map.of(), null);
    }

    public CardAdvantageSpellCastTriggerEffect {
        cardCountsBefore = cardCountsBefore == null ? Map.of() : Map.copyOf(cardCountsBefore);
    }

    public CardAdvantageSpellCastTriggerEffect withSnapshot(Card sourceCard, UUID controllerId,
                                                              UUID sourcePermanentId, UUID spellCardId,
                                                              Map<UUID, Integer> cardCountsBefore,
                                                              UUID handSpellOwnerId) {
        return new CardAdvantageSpellCastTriggerEffect(
                sourceCard, controllerId, sourcePermanentId, spellCardId, cardCountsBefore, handSpellOwnerId);
    }

    public CardAdvantageSpellCastTriggerEffect withCardCountsBefore(Map<UUID, Integer> cardCountsBefore) {
        return new CardAdvantageSpellCastTriggerEffect(
                sourceCard, controllerId, sourcePermanentId, spellCardId, cardCountsBefore, handSpellOwnerId);
    }

    public boolean isPrepared() {
        return sourceCard != null && controllerId != null && sourcePermanentId != null
                && spellCardId != null;
    }
}
