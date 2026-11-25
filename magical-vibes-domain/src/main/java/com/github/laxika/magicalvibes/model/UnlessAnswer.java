package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CostEffect;
import java.util.List;
import java.util.UUID;

/**
 * Where the payers of an "unless" effect ({@code UnlessPaysEffect}, CR 118.12a) stand, kept on the resolving stack entry
 * so that another prompt answered before the resolution resumes (a trigger's or replacement's question during a
 * payment's choice) can't take it. Every player the payer subject names is asked in turn, in APNAP order, every one
 * even after another paid (CR 101.4); the branch is chosen once nobody is left to ask.
 *
 * @param effectIndex     the index of the effect in the entry's effects to resolve
 * @param remainingPayers the players still to be asked, the one being asked first
 * @param anyPaid         whether a player chose to pay (CR 118.12: the choice, not what happened)
 * @param alternative     the any-of ransom's alternative the first remaining player is asked, by its place in Oracle
 *                        order (every earlier one declined or unpayable)
 * @param payingPlayerId  the player paying the parts of an all-of ransom still to pay, or {@code null}
 * @param partsToPay      the parts of an all-of ransom still to pay once the part being paid (which asked) is done
 */
public record UnlessAnswer(int effectIndex, List<UUID> remainingPayers, boolean anyPaid, int alternative,
                           UUID payingPlayerId, List<CostEffect> partsToPay) {

    public UnlessAnswer {
        remainingPayers = List.copyOf(remainingPayers);
        partsToPay = List.copyOf(partsToPay);
    }

    /** The state before anyone was asked: {@code payers} to ask, in APNAP order. */
    public static UnlessAnswer asking(int effectIndex, List<UUID> payers) {
        return new UnlessAnswer(effectIndex, payers, false, 0, null, List.of());
    }

    /** The player being asked, or {@code null} once nobody is left. */
    public UUID currentPayer() {
        return remainingPayers.isEmpty() ? null : remainingPayers.getFirst();
    }

    /** The first remaining player answered: they leave the list, and {@code paid} counts towards "if a player does". */
    public UnlessAnswer answered(boolean paid) {
        return new UnlessAnswer(effectIndex, remainingPayers.isEmpty() ? List.of()
                : remainingPayers.subList(1, remainingPayers.size()), anyPaid || paid, 0, payingPlayerId, partsToPay);
    }

    /** The first remaining player is asked the any-of ransom's {@code next} alternative. */
    public UnlessAnswer askingAlternative(int next) {
        return new UnlessAnswer(effectIndex, remainingPayers, anyPaid, next, payingPlayerId, partsToPay);
    }

    /** {@code payerId} still has {@code parts} of an all-of ransom to pay ({@code List.of()} once paid). */
    public UnlessAnswer paying(UUID payerId, List<CostEffect> parts) {
        return new UnlessAnswer(effectIndex, remainingPayers, anyPaid, alternative,
                parts.isEmpty() ? null : payerId, parts);
    }
}
