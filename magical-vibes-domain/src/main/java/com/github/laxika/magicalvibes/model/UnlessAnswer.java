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
 * @param paidPlayerIds   the players who chose to pay, in the order they were asked (CR 118.12: the choice, not what
 *                        happened); the branch for those who didn't reads them ({@code UnlessPaysEffect.eachUnless})
 * @param alternative     the any-of ransom's alternative the first remaining player is asked, by its place in Oracle
 *                        order (every earlier one declined or unpayable)
 * @param payingPlayerId  the player paying the parts of an all-of ransom still to pay, or {@code null}
 * @param partsToPay      the parts of an all-of ransom still to pay once the part being paid (which asked) is done
 * @param choices         for a ransom paid together once everyone answered ({@code UnlessPaysEffect.payingTogether()}),
 *                        what each player who chose to pay chose to pay with, in the order they were asked
 * @param remainingObjects for "for each [object] … unless" ({@code UnlessPaysEffect.forEach}), the objects still to be
 *                        asked about, the one being asked about first; {@code remainingPayers} and {@code paidPlayerIds}
 *                        are then that object's
 * @param unpaidObjectIds for "for each [object] … unless", the objects already asked about that nobody paid for
 */
public record UnlessAnswer(int effectIndex, List<UUID> remainingPayers, List<UUID> paidPlayerIds, int alternative,
                           UUID payingPlayerId, List<CostEffect> partsToPay, List<Choice> choices,
                           List<UUID> remainingObjects, List<UUID> unpaidObjectIds) {

    /**
     * One player's choice to pay a ransom paid together: the permanents they chose (to sacrifice or tap), none for a
     * payment with nothing to choose (damage, a draw).
     *
     * @param payerId   the player who chose to pay
     * @param objectIds the permanents they chose
     */
    public record Choice(UUID payerId, List<UUID> objectIds) {
        public Choice {
            objectIds = List.copyOf(objectIds);
        }
    }

    public UnlessAnswer {
        remainingPayers = List.copyOf(remainingPayers);
        paidPlayerIds = List.copyOf(paidPlayerIds);
        partsToPay = List.copyOf(partsToPay);
        choices = List.copyOf(choices);
        remainingObjects = List.copyOf(remainingObjects);
        unpaidObjectIds = List.copyOf(unpaidObjectIds);
    }

    public UnlessAnswer(int effectIndex, List<UUID> remainingPayers, List<UUID> paidPlayerIds, int alternative,
                        UUID payingPlayerId, List<CostEffect> partsToPay, List<Choice> choices) {
        this(effectIndex, remainingPayers, paidPlayerIds, alternative, payingPlayerId, partsToPay, choices, List.of(),
                List.of());
    }

    /** The state before anyone was asked: {@code payers} to ask, in APNAP order. */
    public static UnlessAnswer asking(int effectIndex, List<UUID> payers) {
        return new UnlessAnswer(effectIndex, payers, List.of(), 0, null, List.of(), List.of());
    }

    /**
     * The state of "for each [object] … unless" before anyone was asked: {@code objects} to ask about in order, the
     * first one's {@code payers} to ask, in APNAP order.
     */
    public static UnlessAnswer askingAbout(int effectIndex, List<UUID> objects, List<UUID> payers) {
        return new UnlessAnswer(effectIndex, payers, List.of(), 0, null, List.of(), List.of(), objects, List.of());
    }

    /** The object being asked about ("for each [object] … unless"), or {@code null}. */
    public UUID currentObject() {
        return remainingObjects.isEmpty() ? null : remainingObjects.getFirst();
    }

    /**
     * Every payer answered for the object being asked about: it counts as unpaid unless one of them chose to pay
     * ({@code skipped}: it was gone, nothing is done to it), and the next object's {@code payers} are asked.
     */
    public UnlessAnswer nextObject(boolean skipped, List<UUID> payers) {
        List<UUID> unpaid = unpaidObjectIds;
        if (!skipped && !anyPaid() && currentObject() != null) {
            unpaid = new java.util.ArrayList<>(unpaidObjectIds);
            unpaid.add(currentObject());
        }
        List<UUID> rest = remainingObjects.isEmpty() ? List.of() : remainingObjects.subList(1, remainingObjects.size());
        return new UnlessAnswer(effectIndex, payers, List.of(), 0, null, List.of(), List.of(), rest, unpaid);
    }

    /** Whether a player chose to pay ("if a player does", CR 118.12). */
    public boolean anyPaid() {
        return !paidPlayerIds.isEmpty();
    }

    private List<UUID> withPaid(UUID payerId) {
        if (payerId == null || paidPlayerIds.contains(payerId)) {
            return paidPlayerIds;
        }
        List<UUID> next = new java.util.ArrayList<>(paidPlayerIds);
        next.add(payerId);
        return next;
    }

    /** The player being asked, or {@code null} once nobody is left. */
    public UUID currentPayer() {
        return remainingPayers.isEmpty() ? null : remainingPayers.getFirst();
    }

    /** The first remaining player answered: they leave the list, and {@code paid} counts towards "if a player does". */
    public UnlessAnswer answered(boolean paid) {
        return new UnlessAnswer(effectIndex, remainingPayers.isEmpty() ? List.of()
                : remainingPayers.subList(1, remainingPayers.size()), paid ? withPaid(currentPayer()) : paidPlayerIds,
                0, payingPlayerId, partsToPay,
                choices, remainingObjects, unpaidObjectIds);
    }

    /** The first remaining player is asked the any-of ransom's {@code next} alternative. */
    public UnlessAnswer askingAlternative(int next) {
        return new UnlessAnswer(effectIndex, remainingPayers, paidPlayerIds, next, payingPlayerId, partsToPay, choices,
                remainingObjects, unpaidObjectIds);
    }

    /** {@code payerId} still has {@code parts} of an all-of ransom to pay ({@code List.of()} once paid). */
    public UnlessAnswer paying(UUID payerId, List<CostEffect> parts) {
        return new UnlessAnswer(effectIndex, remainingPayers, paidPlayerIds, alternative,
                parts.isEmpty() ? null : payerId, parts, choices, remainingObjects, unpaidObjectIds);
    }

    /**
     * {@code payerId} chose to pay with {@code objectIds} (the permanents a pick chose, or none): kept until everyone
     * answered, then paid together with the others' choices.
     */
    public UnlessAnswer withChoice(UUID payerId, List<UUID> objectIds) {
        List<Choice> next = new java.util.ArrayList<>(choices);
        next.add(new Choice(payerId, objectIds));
        return new UnlessAnswer(effectIndex, remainingPayers, withPaid(payerId), alternative, payingPlayerId, partsToPay,
                next, remainingObjects, unpaidObjectIds);
    }
}
