package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Internal resolution step: a "then" handler's rider, resolved after the handler's own change as a step of its own
 * on the same stack entry, against the player and values that handler already determined (CR 608.2c, CR 608.2h).
 * Its handler resolves {@link #rider()} on a stack entry derived from the resolving one, whose controller is
 * {@link #controllerId()} (the destroyed permanent's controller for "its controller loses 2 life"), so the rider's
 * ordinary {@code CONTROLLER}-style recipient lands on the right player. As a step of its own, a rider that asks a
 * CR 616.1 question before its first change (Lich's Duel Mastery's sacrifice for the life loss) can be stopped and
 * resolved again without redoing the handler's change. Never put on a card.
 *
 * @param rider                       the rider, possibly a {@link ConditionalEffect} checked as the step resolves
 * @param controllerId                the player the rider resolves for
 * @param targetId                    the rider's target (the resolving entry's target, or a player it is aimed at)
 * @param eventValue                  the last-known value the rider reads through {@code EventValue}
 * @param removedPermanentControllers the controllers of permanents the handler removed, as last known
 * @param targetCardIds               the cards the rider refers to (the destroyed permanent's card), or empty
 */
public record ResolveRiderForPlayerEffect(
        CardEffect rider,
        UUID controllerId,
        UUID targetId,
        int eventValue,
        Map<UUID, UUID> removedPermanentControllers,
        List<UUID> targetCardIds
) implements CardEffect {

    public ResolveRiderForPlayerEffect {
        Objects.requireNonNull(rider, "rider");
        Objects.requireNonNull(controllerId, "controllerId");
        removedPermanentControllers = removedPermanentControllers == null
                ? Map.of() : Map.copyOf(removedPermanentControllers);
        targetCardIds = targetCardIds == null ? List.of() : List.copyOf(targetCardIds);
    }
}
