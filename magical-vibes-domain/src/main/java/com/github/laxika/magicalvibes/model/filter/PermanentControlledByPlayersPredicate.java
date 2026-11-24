package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.PlayerSubject;

import java.util.Objects;

/**
 * Matches a permanent controlled by one of the players {@code players} names: "creatures that player controls" after
 * "choose a player" ({@code playersThisWay(CHOOSE)}, The Black Gate). The subject needs the resolving entry, so a
 * handler binds the predicate to those players as the effect applies ({@code SubjectResolutionService.bindChosenValues},
 * CR 608.2h); the bound rule then covers whatever those players control whenever it is checked (CR 611.2c). Unbound it
 * matches nothing.
 */
public record PermanentControlledByPlayersPredicate(PlayerSubject players) implements PermanentPredicate {

    public PermanentControlledByPlayersPredicate {
        Objects.requireNonNull(players, "players");
    }
}
