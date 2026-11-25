package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Asks the player who may pay. A non-mana payment offered to any player ({@code MayPayManaEffect.anyPlayerMay}) asks
 * one player per run, in turn order from the active player, skipping a player who couldn't pay it in full; the answer
 * ({@link #answerPayment}) puts this effect, moved on to the next player, back at this index, a sacrifice's or tap's
 * pick ({@link #completePaymentPick}) first. Nothing changes while the players decide; once nobody is left, the payment
 * for everyone who accepted and the "if a player does" effect, or the "if no one does" effect, are put next. Paid as
 * each accepts ({@code paysAsEachAccepts}), an accepting player's payment, "if a player does" after the first one, and
 * this effect for the players still to be asked are put next instead.
 */
@Component
@RequiredArgsConstructor
public class MayPayManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;
    private final StepDepartureSupport stepDepartureSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        askNextPayer(gameData, entry, (MayPayManaEffect) effect);
    }

    /**
     * Asks the next player who could pay {@code e}'s payment, or, once every player has answered, puts next the payment
     * for those who chose to pay and "if a player does" (CR 118.12), or "if no one does". Paid as each accepts, the
     * player who just accepted pays first.
     */
    private void askNextPayer(GameData gameData, StackEntry entry, MayPayManaEffect e) {
        List<UUID> remaining = e.loop() == null
                ? payerOrder(gameData, entry, e.payer())
                : new ArrayList<>(e.loop().remainingPayerIds());
        List<UUID> accepted = e.loop() == null ? List.of() : e.loop().acceptedPayerIds();
        List<UUID> chosen = e.loop() == null ? List.of() : e.loop().chosenPermanentIds();
        boolean anyPaid = e.loop() != null && e.loop().anyPaid();
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (e.paysAsEachAccepts() && !accepted.isEmpty()) {
            List<CardEffect> next = new ArrayList<>();
            if (!anyPaid && e.wrapped() != null) {
                next.add(e.wrapped());
            }
            next.add(e.asking(remaining, List.of(), List.of(), true));
            putPaymentNext(entry, e, accepted, chosen, next);
            return;
        }
        while (!remaining.isEmpty() && !canPay(gameData, entry, e.payment(), remaining.getFirst())) {
            remaining.removeFirst();
        }
        if (remaining.isEmpty()) {
            List<CardEffect> next = new ArrayList<>();
            if (!accepted.isEmpty()) {
                if (e.wrapped() != null) {
                    next.add(e.wrapped());
                }
                putPaymentNext(entry, e, accepted, chosen, next);
                return;
            }
            if (e.elseEffect() != null && !anyPaid) {
                next.add(e.elseEffect());
            }
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, next);
            return;
        }
        MayPayManaEffect asking = e.asking(remaining, accepted, chosen);
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), remaining.getFirst(), List.of(asking), entry.getCard().getName() + " - " + e.prompt(),
                entry.getTargetId(), null, entry.getSourcePermanentId(), null, 0, 0,
                entry.getAttackedTargetId(), entry.getActivePlayerId(), null,
                entry.getSourcePermanentSnapshot(), entry.getControllerId(), null, 0));
    }

    /**
     * Puts next the payment of the {@code accepted} players, then {@code next}: the picked permanents sacrificed as one
     * departure step, or tapped as one tap step, else the payment step itself over {@link Subjects#acceptedPlayers()}.
     */
    private void putPaymentNext(StackEntry entry, MayPayManaEffect e, List<UUID> accepted, List<UUID> chosen,
                                List<CardEffect> next) {
        entry.setAcceptedPlayerIds(accepted);
        if (e.payment() instanceof SacrificePermanentsEffect) {
            stepDepartureSupport.departAsNextStep(entry, StepDepartureSupport.sacrifices(chosen), next);
            return;
        }
        if (e.payment() instanceof TapPermanentsEffect) {
            entry.recordStep(StepKind.CHOOSE, StepOutcome.ofObjects(chosen, accepted));
            next.addFirst(new TapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)));
        } else {
            next.addFirst(e.payment());
        }
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, next);
    }

    /**
     * Notes {@code playerId}'s answer to {@code mayPay}, the effect the parked resolution resumes with: the payment
     * moves on to the next player. A player accepting a sacrifice picks the permanents now, unasked when only that
     * many could be sacrificed (CR 608.2d); then {@code true} is returned while the pick is asked.
     */
    public boolean answerPayment(GameData gameData, UUID playerId, boolean accepted, MayPayManaEffect mayPay) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null || mayPay.loop() == null) {
            return false;
        }
        int index = gameData.pendingEffectResolutionIndex;
        List<UUID> remaining = new ArrayList<>(mayPay.loop().remainingPayerIds());
        remaining.remove(playerId);
        List<UUID> acceptedPayers = new ArrayList<>(mayPay.loop().acceptedPayerIds());
        List<UUID> chosen = new ArrayList<>(mayPay.loop().chosenPermanentIds());
        List<UUID> candidates = List.of();
        int count = 0;
        String verb = "sacrifice";
        if (accepted) {
            acceptedPayers.add(playerId);
            if (mayPay.payment() instanceof SacrificePermanentsEffect sacrifice) {
                candidates = sacrificePermanentsEffectHandler.sacrificeCandidates(gameData, entry, sacrifice, playerId);
                count = sacrificePermanentsEffectHandler.sacrificeCount(gameData, entry, sacrifice, playerId);
            } else if (mayPay.payment() instanceof TapPermanentsEffect tap) {
                candidates = tapCandidates(gameData, entry, tap, playerId);
                count = tap.chosenCount();
                verb = "tap";
            }
            if (candidates.size() <= count) {
                chosen.addAll(candidates);
                candidates = List.of();
            }
        }
        entry.replaceEffectToResolve(index, mayPay.asking(remaining, acceptedPayers, chosen));
        if (candidates.isEmpty()) {
            return false;
        }
        if (count == 1) {
            playerInputService.beginPermanentChoice(gameData, playerId, candidates,
                    new PermanentChoiceContext.AnyPlayerPaymentPick(playerId), "Choose a permanent to " + verb + ".");
        } else {
            playerInputService.beginMultiPermanentChoice(gameData, playerId, candidates, count,
                    new MultiPermanentChoiceContext.AnyPlayerPaymentPick(playerId),
                    "Choose " + count + " permanents to " + verb + ".");
        }
        return true;
    }

    /**
     * Adds the permanents an accepting player picked to the payment the parked resolution resumes with; they are
     * sacrificed or tapped together with the others' once every player has answered, or at once when paid as each
     * accepts.
     */
    public void completePaymentPick(GameData gameData, List<UUID> permanentIds) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }
        int index = gameData.pendingEffectResolutionIndex;
        if (!(entry.getEffectsToResolve().get(index) instanceof MayPayManaEffect mayPay) || mayPay.loop() == null) {
            return;
        }
        List<UUID> chosen = new ArrayList<>(mayPay.loop().chosenPermanentIds());
        chosen.addAll(permanentIds);
        entry.replaceEffectToResolve(index, mayPay.asking(mayPay.loop().remainingPayerIds(),
                mayPay.loop().acceptedPayerIds(), chosen));
    }

    /**
     * Whether {@code playerId} could pay {@code payment} in full now (CR 118.3): a sacrifice needs enough permanents
     * they could sacrifice (CR 608.2d), a tap enough untapped permanents that can become tapped, a discard that many
     * cards in hand (Phantasmagorian ruling), a life payment that much life while they can lose life and pay life
     * (CR 119.4, 119.8); being dealt damage or having a card drawn can always be chosen.
     */
    public boolean canPay(GameData gameData, StackEntry entry, CardEffect payment, UUID playerId) {
        return switch (payment) {
            case SacrificePermanentsEffect sacrifice ->
                    sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(gameData, entry, sacrifice, playerId);
            case TapPermanentsEffect tap -> tapCandidates(gameData, entry, tap, playerId).size() >= tap.chosenCount();
            case DiscardEffect discard -> gameData.playerHands.getOrDefault(playerId, List.of()).size()
                    >= amountFor(gameData, entry, discard.amount(), playerId);
            case EachPlayerPaysAnyManaEffect pay when pay.exactly() != null -> {
                // Paying 0 life is always possible; more needs that much life (CR 119.4).
                int amount = amountFor(gameData, entry, pay.exactly(), playerId);
                yield amount <= 0 || gameQueryService.canPayLifeForCosts(gameData, playerId)
                        && gameData.getLife(playerId) >= amount;
            }
            case SequenceEffect sequence -> sequence.steps().stream()
                    .allMatch(step -> canPay(gameData, entry, step, playerId));
            default -> true;
        };
    }

    /** {@code amount} evaluated for {@code playerId} as the payer ("half their life"). */
    private int amountFor(GameData gameData, StackEntry entry, DynamicAmount amount, UUID playerId) {
        return amountEvaluationService.evaluate(gameData, amount,
                AmountContext.forStackEntry(entry, null).withControllerId(playerId));
    }

    /**
     * The permanents {@code playerId} could tap for a tap payment over {@code controlledBy(acceptedPlayers(), filter)}:
     * theirs, matching the filter, untapped and able to become tapped, and choosable.
     */
    private List<UUID> tapCandidates(GameData gameData, StackEntry entry, TapPermanentsEffect tap, UUID playerId) {
        if (!(tap.subject() instanceof PermanentSubject.ControlledBy controlledBy) || tap.chosenCount() <= 0) {
            throw new IllegalArgumentException("A tap payment picks from controlledBy(acceptedPlayers(), filter): "
                    + tap);
        }
        if (!gameData.playerIds.contains(playerId)) {
            return List.of();
        }
        FilterContext filterContext = sacrificePermanentsEffectHandler.filterContextFor(gameData, entry);
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(p -> !p.isTapped())
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(p, controlledBy.filter(),
                        filterContext))
                .filter(p -> !gameQueryService.cantBeChosen(gameData, p))
                .map(Permanent::getId)
                .toList();
    }

    /** The players {@code payer} names, in turn order starting with the active player (CR 101.4). */
    private List<UUID> payerOrder(GameData gameData, StackEntry entry, MayPayPayer payer) {
        List<UUID> order = apnapOrder(gameData);
        if (payer == MayPayPayer.ANY_OPPONENT) {
            order.removeIf(entry.getControllerId()::equals);
        }
        return order;
    }

    /** Opponents of {@code controllerId} in turn order starting with the active player (APNAP, CR 101.4); mutable. */
    public static List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> opponents = apnapOrder(gameData);
        opponents.removeIf(controllerId::equals);
        return opponents;
    }

    private static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
