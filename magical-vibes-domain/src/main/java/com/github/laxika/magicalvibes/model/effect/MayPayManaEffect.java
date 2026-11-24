package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.UUID;

/**
 * Like {@link MayEffect}, but the player must pay a mana cost to get the effect.
 * Used for "you may pay {X}. If you do, [effect]" patterns (e.g. Spellbomb cycle).
 *
 * <p>{@link MayPayPayer#ANY_PLAYER} offers the payment to each player in APNAP order and applies
 * the wrapped effect if at least one player pays.
 *
 * <p>{@code payer} redirects the "may pay" prompt away from the ability's controller:
 * {@link MayPayPayer#ENCHANTED_CONTROLLER} prompts the enchanted permanent's controller — the
 * player carried on the stack entry's {@code targetId} by an
 * {@code Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)} trigger (Paralyze) — and
 * {@link MayPayPayer#DEFENDING_PLAYER} prompts the player being attacked, read from an
 * {@code Triggers.attacks()} ability's {@code attackedTargetId} (Mtenda Lion).
 *
 * <p>{@code elseEffect} models the "If you don't, [effect]" half of a punisher-style choice — it
 * resolves instead of {@code wrapped} when the payment is declined or cannot be made (Preferred
 * Selection). Leave it {@code null} for the plain "if you do" shape, where declining does nothing.
 *
 * <p>{@code lifeCost} is life paid <em>in addition to</em> the mana ("you may pay {4} and 2 life" —
 * Purgatory). Both halves must be payable or the whole payment fails; leave it {@code 0} for the
 * mana-only shape. This is not the "pay {M} or N life" choice — that is
 * {@link TargetPlayerMayPayManaOrLifeEffect}.
 *
 * <p>For "you may pay {X}. When you do, [targeted effect]" wrap the effect in a
 * {@link QueueReflexiveAbilityEffect}: paying puts a separate reflexive triggered ability on the
 * stack, which chooses its targets then (CR 603.12). A card-level target group bound to this
 * effect supplies the reflexive ability's target restriction and count.
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
 * {@code loop} is the resolution's progress through the players, {@code null} in a card definition.
 */
public record MayPayManaEffect(String manaCost, DynamicAmount dynamicManaCost, CardEffect wrapped, String prompt,
                               MayPayPayer payer, CardEffect elseEffect, int lifeCost, CardEffect payment,
                               PayerLoop loop)
        implements CardEffect {

    /**
     * The players still to be asked to pay a non-mana {@code payment}, in turn order, those who chose to, and the
     * permanents they chose to sacrifice.
     *
     * @param remainingPayerIds   the players not yet asked, the one being asked first
     * @param acceptedPayerIds    the players who chose to pay, in the order they were asked
     * @param chosenPermanentIds  for a sacrifice payment, the permanents the accepting players chose, sacrificed
     *                            together once everyone has answered
     */
    public record PayerLoop(List<UUID> remainingPayerIds, List<UUID> acceptedPayerIds,
                            List<UUID> chosenPermanentIds) {
        public PayerLoop {
            remainingPayerIds = List.copyOf(remainingPayerIds);
            acceptedPayerIds = List.copyOf(acceptedPayerIds);
            chosenPermanentIds = List.copyOf(chosenPermanentIds);
        }
    }

    public MayPayManaEffect {
        if (payment != null && (manaCost != null || dynamicManaCost != null || lifeCost != 0
                || (payer != MayPayPayer.ANY_PLAYER && payer != MayPayPayer.ANY_OPPONENT
                && payer != MayPayPayer.ANY_OTHER_PLAYER))) {
            throw new IllegalArgumentException("A non-mana payment is offered to any player or any opponent alone");
        }
    }

    public MayPayManaEffect(String manaCost, DynamicAmount dynamicManaCost, CardEffect wrapped, String prompt,
                            MayPayPayer payer, CardEffect elseEffect, int lifeCost) {
        this(manaCost, dynamicManaCost, wrapped, prompt, payer, elseEffect, lifeCost, null, null);
    }

    /**
     * "[Any player | any opponent] may [payment]. If a player does, [ifAPlayerDoes]. If no one does, [ifNoOneDoes]";
     * "[ifNoOneDoes] unless any player [payment]" (CR 118.12a). Either outcome may be {@code null}.
     *
     * @param payers  {@link MayPayPayer#ANY_PLAYER}, {@link MayPayPayer#ANY_OPPONENT} or
     *                {@link MayPayPayer#ANY_OTHER_PLAYER}
     * @param payment the cost, a step over {@link Subjects#acceptedPlayers()}, made once for every player who accepted
     *                (or over another subject: "has you draw a card" is {@code DrawCardEffect(1, you())}, made once)
     */
    public static MayPayManaEffect anyPlayerMay(MayPayPayer payers, CardEffect payment, String prompt,
                                                CardEffect ifAPlayerDoes, CardEffect ifNoOneDoes) {
        return new MayPayManaEffect(null, null, ifAPlayerDoes, prompt, payers, ifNoOneDoes, 0, payment, null);
    }

    /** This payment as the resolution goes through the players ({@link PayerLoop}). */
    public MayPayManaEffect asking(List<UUID> remainingPayerIds, List<UUID> acceptedPayerIds,
                                   List<UUID> chosenPermanentIds) {
        return new MayPayManaEffect(manaCost, dynamicManaCost, wrapped, prompt, payer, elseEffect, lifeCost, payment,
                new PayerLoop(remainingPayerIds, acceptedPayerIds, chosenPermanentIds));
    }

    /** A payment whose generic mana amount is evaluated when the ability resolves. */
    public static MayPayManaEffect dynamic(DynamicAmount amount, CardEffect wrapped, String prompt,
                                           MayPayPayer payer, CardEffect elseEffect) {
        return new MayPayManaEffect(null, amount, wrapped, prompt, payer, elseEffect, 0);
    }

    /**
     * A payment whose {@code {X}} generic component is evaluated when the ability resolves while
     * retaining any fixed components in {@code manaCost}, such as {@code {X}{R}}.
     */
    public static MayPayManaEffect dynamic(String manaCost, DynamicAmount amount, CardEffect wrapped,
                                           String prompt) {
        return new MayPayManaEffect(manaCost, amount, wrapped, prompt, MayPayPayer.CONTROLLER,
                null, 0);
    }

    /**
     * Delegates to the wrapped effect, like {@link MayEffect}: the target of "you may pay {X}. If
     * you do, [targeted effect]" is chosen when the ability goes on the stack (CR 603.3d), while
     * the payment choice happens at resolution (CR 603.5). When {@code wrapped} is null (pay-to-
     * avoid / punisher shape) or itself non-targeting, falls back to {@code elseEffect}'s spec —
     * "you may pay {X}. If you don't, destroy target …" (Knight of the Mists).
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


    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt) {
        this(manaCost, null, wrapped, prompt, MayPayPayer.CONTROLLER, null, 0);
    }

    public MayPayManaEffect(String manaCost, int lifeCost, CardEffect wrapped, String prompt) {
        this(manaCost, null, wrapped, prompt, MayPayPayer.CONTROLLER, null, lifeCost);
    }

    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt, MayPayPayer payer) {
        this(manaCost, null, wrapped, prompt, payer, null, 0);
    }

    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt, CardEffect elseEffect) {
        this(manaCost, null, wrapped, prompt, MayPayPayer.CONTROLLER, elseEffect, 0);
    }

    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt, MayPayPayer payer,
                            CardEffect elseEffect, int lifeCost) {
        this(manaCost, null, wrapped, prompt, payer, elseEffect, lifeCost);
    }
}
