package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * "Each opponent may [payload]. For each opponent who does, [payload for you]" (tempting offer, Tempting Contract):
 * each opponent decides in turn order, knowing the earlier decisions, before anything happens; then the payload
 * happens once for all the opponents who accepted, then once more for you for each of them (the C13 tempting offer
 * rulings; CR 101.4). The accepting opponents are recorded on the stack entry ({@link Subjects#acceptedPlayers()}),
 * which is what {@link #forAcceptors()} is written over.
 *
 * <p>A card writes it with {@link #of(String, Function)}, the payload once as a function of the players doing it;
 * the text before "each opponent may" ("Put a +1/+1 counter on each creature you control.") is the payload over
 * {@link Subjects#you()} as its own step before this one.
 *
 * @param prompt               the question each opponent is asked
 * @param forAcceptors         the payload over {@link Subjects#acceptedPlayers()}
 * @param forYou               the payload over {@link Subjects#you()}, repeated once for each opponent who accepted
 * @param remainingOpponentIds opponents still to decide, in turn order; {@code null} before the first is asked
 * @param acceptedOpponentIds  opponents who accepted so far, in turn order
 */
public record TemptingOfferEffect(
        String prompt,
        CardEffect forAcceptors,
        CardEffect forYou,
        List<UUID> remainingOpponentIds,
        List<UUID> acceptedOpponentIds
) implements CardEffect {

    public TemptingOfferEffect {
        Objects.requireNonNull(prompt, "prompt");
        Objects.requireNonNull(forAcceptors, "forAcceptors");
        Objects.requireNonNull(forYou, "forYou");
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
        acceptedOpponentIds = acceptedOpponentIds == null ? List.of() : List.copyOf(acceptedOpponentIds);
    }

    /**
     * The offer of {@code payload}: {@code payload.apply(players)} is the payload done by, or for, {@code players}
     * ("[they] create X 1/1 red Elemental creature tokens with haste" is
     * {@code players -> elementals.forController(players)}).
     */
    public static TemptingOfferEffect of(String prompt, Function<PlayerSubject, CardEffect> payload) {
        return new TemptingOfferEffect(prompt, payload.apply(Subjects.acceptedPlayers()),
                payload.apply(Subjects.you()), null, List.of());
    }

    /** This offer with the opponents still to decide and those who accepted so far. */
    public TemptingOfferEffect deciding(List<UUID> remaining, List<UUID> accepted) {
        return new TemptingOfferEffect(prompt, forAcceptors, forYou, remaining, accepted);
    }

    /**
     * The steps that follow once every opponent has decided: the payload for the {@code accepted} opponents, then
     * the payload for you once for each of them; none when nobody accepted.
     */
    public List<CardEffect> stepsFor(List<UUID> accepted) {
        return accepted.isEmpty() ? List.of()
                : List.of(forAcceptors, new RepeatEffect(new Fixed(accepted.size()), forYou));
    }
}
