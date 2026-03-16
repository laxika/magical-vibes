package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static ability that functions while the card is in its owner's graveyard:
 * creatures with the specified {@code subtype} controlled by the card's owner
 * enter the battlefield with additional +1/+1 counters.
 * <p>
 * This is a replacement effect (MTG Rule 614.1c) — it modifies how the creature
 * enters the battlefield rather than triggering after the fact.
 * <p>
 * Used by cards like Dearly Departed ("As long as Dearly Departed is in your graveyard,
 * each Human creature you control enters the battlefield with an additional +1/+1 counter on it.").
 */
public record GraveyardEnterWithAdditionalCountersEffect(
        CardSubtype subtype,
        int count
) implements CardEffect {
}
