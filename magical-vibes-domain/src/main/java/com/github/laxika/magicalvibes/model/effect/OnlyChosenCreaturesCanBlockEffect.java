package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/**
 * "Only creatures in the chosen piles can block this turn" (Stand or Fall), as a floating effect on every player: a
 * creature outside {@code permanentIds} can't block while it lasts (CR 509.1b), including one that entered after the
 * choice. An empty choice bars every creature; several such effects each bar the creatures outside their own set.
 *
 * @param permanentIds the creatures in the chosen pile
 */
public record OnlyChosenCreaturesCanBlockEffect(Set<UUID> permanentIds) implements CardEffect {

    public OnlyChosenCreaturesCanBlockEffect {
        permanentIds = Set.copyOf(permanentIds);
    }
}
