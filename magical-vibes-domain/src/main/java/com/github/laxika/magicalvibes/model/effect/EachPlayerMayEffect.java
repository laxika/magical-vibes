package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * "Each player may [do X]" (Kwain, Itinerant Meddler; The Second Doctor): every player {@code deciders} names is asked
 * in APNAP order, knowing the earlier answers, and nothing happens while they decide; then X happens once, together,
 * for the players who said yes (CR 101.4), as the next step of the same resolution. The players who said yes are
 * the entry's accepted players ({@link Subjects#acceptedPlayers()}), which is what {@link #forAcceptors()} is written
 * over; "then each player who drew a card this way" after it reads that step ({@code playersThisWay(DRAW)}).
 *
 * <p>A card writes it with {@link #of(PlayerSubject, String, Function)}, X once as a function of the players doing it.
 *
 * @param deciders          who is asked; never a declared target
 * @param prompt            the question each of them is asked
 * @param forAcceptors      X over {@link Subjects#acceptedPlayers()}
 * @param remainingPlayerIds players still to decide, in APNAP order; {@code null} before the first is asked
 * @param acceptedPlayerIds players who said yes so far, in APNAP order
 */
public record EachPlayerMayEffect(PlayerSubject deciders, String prompt, CardEffect forAcceptors,
                                  List<UUID> remainingPlayerIds, List<UUID> acceptedPlayerIds)
        implements CardEffect {

    public EachPlayerMayEffect {
        Objects.requireNonNull(deciders, "deciders");
        Objects.requireNonNull(prompt, "prompt");
        Objects.requireNonNull(forAcceptors, "forAcceptors");
        if (deciders.declaresTarget()) {
            throw new IllegalArgumentException("\"Each player may\" asks players who are no target: " + deciders);
        }
        if (remainingPlayerIds != null) {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
        }
        acceptedPlayerIds = acceptedPlayerIds == null ? List.of() : List.copyOf(acceptedPlayerIds);
    }

    /**
     * "[Deciders] may [payload]": {@code payload.apply(players)} is X done by {@code players} ("draw a card" is
     * {@code players -> new DrawCardEffect(1, players)}).
     */
    public static EachPlayerMayEffect of(PlayerSubject deciders, String prompt,
                                         Function<PlayerSubject, CardEffect> payload) {
        return new EachPlayerMayEffect(deciders, prompt, payload.apply(Subjects.acceptedPlayers()), null, List.of());
    }

    /** This question with the players still to decide and those who said yes so far. */
    public EachPlayerMayEffect deciding(List<UUID> remaining, List<UUID> accepted) {
        return new EachPlayerMayEffect(deciders, prompt, forAcceptors, remaining, accepted);
    }
}
