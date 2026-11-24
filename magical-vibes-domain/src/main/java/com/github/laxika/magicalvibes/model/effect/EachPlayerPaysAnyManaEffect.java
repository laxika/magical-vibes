package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StepKind;

import java.util.Objects;

/**
 * "[Each player | you] may pay any amount of [mana | {E} | life]", "pay {X}" as the ability resolves: each payer in
 * turn is asked once how much to pay, any number from zero (CR 107.1c) up to what they can pay in full (CR 118.3).
 * Several players are asked starting with the controller (join forces: "starting with you") or with the active player
 * (APNAP, CR 101.4). The step records the payments ({@link Resource#stepKind()}) after the last answer: "that many",
 * "the amount of {E} paid this way" is {@code AmountThisWay.manaPaid()} / {@code energyPaid()} / {@code lifePaid()},
 * "the amount of mana they paid this way" {@code AmountThisWay.manaPaidByYou()}; "if you paid one or more" is
 * {@code HappenedThisWay(kind)}.
 *
 * <p>"You may pay {X}. If you do, ..." is {@code MayEffect(SequenceEffect.of(this, ...))}: once the player chooses to
 * pay, the payment is made and the rest follows, whatever X is (a paid {0} is still paying, CR 118.12); "when you do"
 * is a {@code QueueReflexiveAbilityEffect} after it. The X of a spell's own cost is chosen as it is cast (CR 107.3a),
 * not by this step.
 *
 * <p>A mana payment opens a window for mana abilities (CR 605.3a, 608.2g) and accepts mana that only can't be spent
 * to cast spells (Powerstone); {@link Resource#X_MANA} is a cost containing {X}, so mana spendable only on such costs
 * pays it too. Energy is paid by removing energy counters (CR 107.14). Life can be paid only up to the life total
 * and not at all while the player can't lose life (CR 119.4, 119.8), and paying it is losing it.
 *
 * @param startingWithYou whether the controller is asked first, else the active player
 * @param payers          who pays; never a target
 * @param resource        what is paid
 */
public record EachPlayerPaysAnyManaEffect(boolean startingWithYou, PlayerSubject payers, Resource resource)
        implements CardEffect {

    /** What a payer pays, and the step kind the payment records. */
    public enum Resource {
        /** "Pay any amount of mana". */
        MANA(StepKind.PAY_MANA),
        /** "Pay {X}": a mana cost containing {X}. */
        X_MANA(StepKind.PAY_MANA),
        /** "Pay any amount of {E}", "pay one or more {E}". */
        ENERGY(StepKind.PAY_ENERGY),
        /** "Pay any amount of life". */
        LIFE(StepKind.PAY_LIFE);

        private final StepKind stepKind;

        Resource(StepKind stepKind) {
            this.stepKind = stepKind;
        }

        /** The step kind a payment of this resource records. */
        public StepKind stepKind() {
            return stepKind;
        }

        /** Whether this is a mana payment, during which mana abilities may be activated. */
        public boolean isMana() {
            return this == MANA || this == X_MANA;
        }
    }

    public EachPlayerPaysAnyManaEffect {
        Objects.requireNonNull(payers, "payers");
        Objects.requireNonNull(resource, "resource");
        if (payers.declaresTarget()) {
            throw new IllegalArgumentException("The payers of a payment are not targeted: " + payers);
        }
    }

    public EachPlayerPaysAnyManaEffect(boolean startingWithYou) {
        this(startingWithYou, Subjects.eachPlayer(), Resource.MANA);
    }

    /** "Starting with you, each player may pay any amount of mana." */
    public static EachPlayerPaysAnyManaEffect joinForces() {
        return new EachPlayerPaysAnyManaEffect(true);
    }

    /** "Each player may pay any amount of mana." */
    public static EachPlayerPaysAnyManaEffect inApnapOrder() {
        return new EachPlayerPaysAnyManaEffect(false);
    }

    /** "[You may] pay any amount of {E}", "pay {X}", "pay any amount of life": the controller pays. */
    public static EachPlayerPaysAnyManaEffect byYou(Resource resource) {
        return new EachPlayerPaysAnyManaEffect(true, Subjects.you(), resource);
    }
}
