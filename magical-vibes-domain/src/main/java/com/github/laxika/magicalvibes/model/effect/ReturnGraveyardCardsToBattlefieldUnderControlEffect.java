package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * When resolved, returns the listed cards to the battlefield simultaneously under the ability
 * controller's control. A card whose id has a recorded graveyard entry version returns only while it
 * is still in the graveyard it was remembered in (a card that left and re-entered is a new object);
 * a card without one was remembered in exile and returns only while it is still exiled.
 *
 * <p>Pushed as the delayed upkeep trigger of Rescue from the Underworld.</p>
 */
public record ReturnGraveyardCardsToBattlefieldUnderControlEffect(
        List<UUID> cardIds,
        Map<UUID, Long> graveyardEntryVersions
) implements CardEffect {

    public ReturnGraveyardCardsToBattlefieldUnderControlEffect {
        cardIds = List.copyOf(cardIds);
        graveyardEntryVersions = Map.copyOf(graveyardEntryVersions);
    }
}
