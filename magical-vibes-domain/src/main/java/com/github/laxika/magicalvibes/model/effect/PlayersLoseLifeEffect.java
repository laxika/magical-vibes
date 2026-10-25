package com.github.laxika.magicalvibes.model.effect;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Internal resolution step: the players an earlier step already determined lose the amounts it already determined
 * (CR 608.2h), as the life losses of one effect. A handler or choice completion that changes the game before its
 * life loss (exiles, sacrifices) inserts it after itself, so the loss is a step of its own: the Lich's Duel Mastery
 * sacrifices that replace it can then ask their CR 616.1 questions before any life is lost, and the stopped step is
 * resolved again without redoing the earlier change. Never put on a card.
 *
 * @param losses the life each player loses, in the order the losses happen
 */
public record PlayersLoseLifeEffect(Map<UUID, Integer> losses) implements CardEffect {

    public PlayersLoseLifeEffect {
        losses = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(losses, "losses")));
    }
}
