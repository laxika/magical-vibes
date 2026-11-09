package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Exiles the damaged player's top card, grants its exiler's controller permission to play it
 * until end of turn, and optionally records perpetual characteristics for nonland permanents.
 */
public record ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionWithPerpetualCharacteristicsEffect(
        PerpetuallyGrantCardCharacteristicsEffect characteristics
) implements CardEffect {

    public ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionWithPerpetualCharacteristicsEffect {
        Objects.requireNonNull(characteristics, "characteristics");
    }
}
