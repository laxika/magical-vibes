package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import java.util.Set;

/**
 * "Put a counter over that type and draw a card. If all [the tracked types] have counters over them, sacrifice it and
 * draw one more card" (Bucket List): marks each of {@code trackedTypes} the triggering spell has on the source's tracker
 * list, draws once if that marked a new type, then sacrifices a completed tracker and draws again. The spell's types are
 * the stack entry's triggering spell's, as it was when the ability triggered; the source is the live permanent, else
 * its snapshot taken as the ability triggered, so the draw happens even if the source left the battlefield.
 */
public record TrackCastSpellCardTypesEffect(Set<CardType> trackedTypes)
        implements CardEffect {
    public TrackCastSpellCardTypesEffect {
        trackedTypes = Set.copyOf(trackedTypes);
    }
}
