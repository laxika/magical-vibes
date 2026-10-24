package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.DepartureIntent;

import java.util.List;
import java.util.Objects;

/**
 * Internal resolution step: the permanents an effect already chose (a player's pick, a random pick, the rest of
 * what was kept) leave the battlefield together as one event. A handler or choice completion that has changed the
 * game before the removal (put counters, flipped coins, picked at random) inserts it as the next step of its stack
 * entry, so the removal is a step of its own and can end on its CR 616.1 questions before anything leaves. Never
 * put on a card.
 *
 * @param intents the departures, all performed as one batch
 */
public record DepartPermanentsEffect(List<DepartureIntent> intents) implements CardEffect {

    public DepartPermanentsEffect {
        intents = List.copyOf(Objects.requireNonNull(intents, "intents"));
    }
}
