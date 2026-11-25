package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * "Any player / any opponent may [a payment other than mana]. If a player does, … If no one does, …" and "[effect]
 * unless any player [payment]": what is left of the "may pay" record after step 10 runs U3a and U3b' moved every mana,
 * life and cost payment onto {@link UnlessPaysEffect}; the non-mana {@link #anyPlayerMay} payments wait for run U3c.
 *
 * <p>{@code payment} is a cost other than mana, paid by the players who accept as the ability resolves ("any opponent
 * may sacrifice a creature of their choice", "any player may have this deal 4 damage to them"; CR 118.12), made
 * through {@link #anyPlayerMay}: a step written over {@link Subjects#acceptedPlayers()}. Every player the payer names
 * who could pay it in full (CR 118.3) is asked in turn order starting with the active player, knowing the earlier
 * answers, the remaining players still asked after one accepts; a player accepting a {@link SacrificePermanentsEffect}
 * payment chooses which permanents right then. Once everyone has answered, the payment is made for all who accepted as
 * one event (CR 101.4; the punisher rulings: every chosen permanent is sacrificed at the same time), followed by
 * {@code wrapped} ("if a player does"), whatever the payment then did, or, when nobody accepted, by {@code elseEffect}
 * ("if no one does", "unless"; CR 118.12a).
 * {@code paysAsEachAccepts} ({@link #payingAsEachAccepts()}) instead has each accepting player pay at once, before the
 * next player is asked, "if a player does" following the first payment only: the PLC "any player may … If a player
 * does, counter [this spell]" cards, whose rulings say that as soon as any player performs the action the spell is
 * countered, but the remaining players still get the option (Phantasmagorian, Dash Hopes, Brain Gorgers).
 * {@code loop} is the resolution's progress through the players, {@code null} in a card definition.
 */
public record MayPayManaEffect(CardEffect wrapped, String prompt, MayPayPayer payer, CardEffect elseEffect,
                               CardEffect payment, boolean paysAsEachAccepts, PayerLoop loop)
        implements CardEffect {

    /**
     * The players still to be asked to pay a non-mana {@code payment}, in turn order, those who chose to, and the
     * permanents they chose to sacrifice.
     *
     * @param remainingPayerIds   the players not yet asked, the one being asked first
     * @param acceptedPayerIds    the players who chose to pay, in the order they were asked
     * @param chosenPermanentIds  for a sacrifice or tap payment, the permanents the accepting players chose, sacrificed
     *                            or tapped together once everyone has answered
     * @param anyPaid             with {@code paysAsEachAccepts}, whether a player has already paid, so "if a player
     *                            does" has followed and "if no one does" won't
     */
    public record PayerLoop(List<UUID> remainingPayerIds, List<UUID> acceptedPayerIds,
                            List<UUID> chosenPermanentIds, boolean anyPaid) {
        public PayerLoop {
            remainingPayerIds = List.copyOf(remainingPayerIds);
            acceptedPayerIds = List.copyOf(acceptedPayerIds);
            chosenPermanentIds = List.copyOf(chosenPermanentIds);
        }
    }

    public MayPayManaEffect {
        Objects.requireNonNull(payment, "payment");
        Objects.requireNonNull(payer, "payer");
    }

    /**
     * "[Any player | any opponent] may [payment]. If a player does, [ifAPlayerDoes]. If no one does, [ifNoOneDoes]";
     * "[ifNoOneDoes] unless any player [payment]" (CR 118.12a). Either outcome may be {@code null}.
     *
     * @param payers  {@link MayPayPayer#ANY_PLAYER} or {@link MayPayPayer#ANY_OPPONENT}
     * @param payment the cost, a step over {@link Subjects#acceptedPlayers()}, made once for every player who accepted
     *                (or over another subject: "has you draw a card" is {@code DrawCardEffect(1, you())}, made once)
     */
    public static MayPayManaEffect anyPlayerMay(MayPayPayer payers, CardEffect payment, String prompt,
                                                CardEffect ifAPlayerDoes, CardEffect ifNoOneDoes) {
        return new MayPayManaEffect(ifAPlayerDoes, prompt, payers, ifNoOneDoes, payment, false, null);
    }

    /** This payment made by each accepting player at once, "if a player does" after the first ({@code paysAsEachAccepts}). */
    public MayPayManaEffect payingAsEachAccepts() {
        return new MayPayManaEffect(wrapped, prompt, payer, elseEffect, payment, true, loop);
    }

    /** This payment as the resolution goes through the players ({@link PayerLoop}). */
    public MayPayManaEffect asking(List<UUID> remainingPayerIds, List<UUID> acceptedPayerIds,
                                   List<UUID> chosenPermanentIds) {
        return asking(remainingPayerIds, acceptedPayerIds, chosenPermanentIds, loop != null && loop.anyPaid());
    }

    /** {@link #asking(List, List, List)}, noting whether a player has already paid. */
    public MayPayManaEffect asking(List<UUID> remainingPayerIds, List<UUID> acceptedPayerIds,
                                   List<UUID> chosenPermanentIds, boolean anyPaid) {
        return new MayPayManaEffect(wrapped, prompt, payer, elseEffect, payment, paysAsEachAccepts,
                new PayerLoop(remainingPayerIds, acceptedPayerIds, chosenPermanentIds, anyPaid));
    }

    /**
     * Delegates to the wrapped effect, like {@link MayEffect}: the target of "if a player does, [targeted effect]" is
     * chosen when the ability goes on the stack (CR 603.3d), while the payment choice happens at resolution (CR 603.5);
     * else {@code elseEffect}'s.
     */
    @Override
    public TargetSpec targetSpec() {
        if (wrapped != null) {
            TargetSpec wrappedSpec = wrapped.targetSpec();
            if (wrappedSpec != TargetSpec.NONE) {
                return wrappedSpec;
            }
        }
        if (elseEffect != null) {
            return elseEffect.targetSpec();
        }
        return TargetSpec.NONE;
    }

    @Override
    public boolean targetsChosenByReflexiveAbility() {
        return wrapped != null && wrapped.targetsChosenByReflexiveAbility();
    }
}
