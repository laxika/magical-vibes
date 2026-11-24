package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Asks the player who may pay. A non-mana payment offered to any player ({@code MayPayManaEffect.anyPlayerMay}) asks
 * one player per run, in turn order from the active player, skipping a player who couldn't pay it in full; the answer
 * ({@link #answerPayment}) puts this effect, moved on to the next player, back at this index, a sacrifice's pick
 * ({@link #completeSacrificePick}) first. Nothing changes while the players decide; once nobody is left, the payment for
 * everyone who accepted and the "if a player does" effect, or the "if no one does" effect, are put next.
 */
@Component
@RequiredArgsConstructor
public class MayPayManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;
    private final StepDepartureSupport stepDepartureSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MayPayManaEffect) effect;

        if (e.payment() != null) {
            askNextPayer(gameData, entry, e);
            return;
        }
        if (e.payer() == MayPayPayer.ANY_PLAYER || e.payer() == MayPayPayer.ANY_OTHER_PLAYER
                || e.payer() == MayPayPayer.ANY_OPPONENT) {
            List<UUID> order = payerOrder(gameData, entry, e.payer());
            if (order.isEmpty()) {
                return;
            }
            UUID first = order.getFirst();
            gameData.anyPlayerMayPayManaRemainingPlayers.clear();
            gameData.anyPlayerMayPayManaRemainingPlayers.addAll(order.subList(1, order.size()));
            gameData.resolvingMayEffectFromStack = true;
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), first, List.of(e), entry.getCard().getName() + " - " + e.prompt(),
                    entry.getTargetId(), e.manaCost(), entry.getSourcePermanentId(), null, 0, e.lifeCost(),
                    entry.getAttackedTargetId(), entry.getActivePlayerId(), null,
                    entry.getSourcePermanentSnapshot(), entry.getControllerId(), null, 0));
            return;
        }

        // CR 603.5 — "you may pay" choice happens at resolution time.
        // For "that player may pay" triggers (Paralyze) the payer is the enchanted permanent's
        // controller, carried on the stack entry's targetId, not the Aura's controller. For
        // "defending player may pay" attack triggers (Mtenda Lion) it is the attacked player.
        UUID payer = switch (e.payer()) {
            case CONTROLLER -> entry.getControllerId();
            case ENCHANTED_CONTROLLER -> entry.getTargetId();
            case DEFENDING_PLAYER -> defendingPlayer(gameData, entry);
            case TARGET_PERMANENT_CONTROLLER -> targetPermanentController(gameData, entry);
            case TARGET_PLAYER_OR_PERMANENT_CONTROLLER -> targetPlayerOrPermanentController(gameData,
                    entry.getTargetId());
            // "That player": the player the entry names, or the event's player once the entry is aimed at a spell.
            case TRIGGERING_PLAYER -> entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                    ? entry.getTargetId() : entry.getTriggeringPlayerId();
            // "That spell's controller": the controller of the spell the event was about, as it was then.
            case TRIGGERING_SPELL_CONTROLLER -> entry.getTriggeringSpellSnapshot() != null
                    ? entry.getTriggeringSpellSnapshot().getControllerId() : entry.getTargetId();
            case ACTIVE_PLAYER -> entry.getActivePlayerId() != null
                    ? entry.getActivePlayerId() : gameData.activePlayerId;
            case ANY_PLAYER, ANY_OTHER_PLAYER, ANY_OPPONENT -> null;
        };
        if (payer == null) {
            return;
        }
        if (e.wrapped() instanceof DiscardAndDrawCardEffect
                && gameData.playerHands.getOrDefault(payer, List.of()).isEmpty()) {
            return;
        }
        if (e.wrapped() instanceof DiscardCardThenEffect discard && discard.filter() == null
                && !discard.thenWithoutDiscard() && discard.player() instanceof PlayerSubject.You
                && gameData.playerHands.getOrDefault(payer, List.of()).isEmpty()) {
            // "you may pay {M} and discard a card": discarding is part of the cost, so with no card
            // to discard the payment cannot be made.
            return;
        }

        if (e.wrapped() instanceof ExileDyingCreatureCardAndCreateTokenEffect && entry.getTriggeringCardId() != null
                && gameQueryService.findCardInGraveyardById(gameData, entry.getTriggeringCardId()) == null) {
            // "you may pay {1} and exile it": exiling the card is part of the payment, so a card that
            // already left the graveyard makes the payment impossible.
            return;
        }

        if (e.wrapped() instanceof ConditionalEffect conditional
                && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forStackEntry(entry), entry.getEventValue())) {
            return;
        }

        String manaCost = e.manaCost();
        if (e.dynamicManaCost() != null) {
            Permanent source = entry.getSourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            int amount = amountEvaluationService.evaluate(gameData, e.dynamicManaCost(),
                    AmountContext.forStackEntry(entry, source).withControllerId(payer));
            String dynamicGenericCost = "{" + amount + "}";
            manaCost = e.manaCost() == null
                    ? dynamicGenericCost
                    : e.manaCost().replace("{X}", dynamicGenericCost);
        }

        entry.setLastManaPaymentPlayerId(payer);
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                payer,
                e.wrapped() == null ? List.of() : List.of(e.wrapped()),
                entry.getCard().getName() + " - " + e.prompt(),
                entry.getTargetId(),
                manaCost,
                entry.getSourcePermanentId(),
                null,
                0,
                e.lifeCost(),
                entry.getAttackedTargetId(),
                entry.getActivePlayerId(),
                null,
                entry.getSourcePermanentSnapshot(),
                entry.getControllerId(),
                entry.getTriggeringCardId(),
                entry.getEventValue(),
                entry.getTriggeringPermanentId(),
                null,
                null,
                null,
                entry.getTriggeringCardGraveyardEntryVersion()
        ).withSourceGraveyardEntryVersion(entry.getSourceGraveyardEntryVersion()));

    }

    /**
     * Asks the next player who could pay {@code e}'s payment, or, once every player has answered, puts next the payment
     * for those who chose to pay and "if a player does" (CR 118.12), or "if no one does".
     */
    private void askNextPayer(GameData gameData, StackEntry entry, MayPayManaEffect e) {
        List<UUID> remaining = e.loop() == null
                ? payerOrder(gameData, entry, e.payer())
                : new ArrayList<>(e.loop().remainingPayerIds());
        List<UUID> accepted = e.loop() == null ? List.of() : e.loop().acceptedPayerIds();
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        while (!remaining.isEmpty() && !canPay(gameData, entry, e.payment(), remaining.getFirst())) {
            remaining.removeFirst();
        }
        if (remaining.isEmpty()) {
            List<CardEffect> next = new ArrayList<>();
            if (!accepted.isEmpty()) {
                entry.setAcceptedPlayerIds(accepted);
                if (e.wrapped() != null) {
                    next.add(e.wrapped());
                }
                if (e.payment() instanceof SacrificePermanentsEffect) {
                    stepDepartureSupport.departAsNextStep(entry,
                            StepDepartureSupport.sacrifices(e.loop().chosenPermanentIds()), next);
                    return;
                }
                next.addFirst(e.payment());
            } else if (e.elseEffect() != null) {
                next.add(e.elseEffect());
            }
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, next);
            return;
        }
        MayPayManaEffect asking = e.asking(remaining, accepted,
                e.loop() == null ? List.of() : e.loop().chosenPermanentIds());
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), remaining.getFirst(), List.of(asking), entry.getCard().getName() + " - " + e.prompt(),
                entry.getTargetId(), null, entry.getSourcePermanentId(), null, 0, 0,
                entry.getAttackedTargetId(), entry.getActivePlayerId(), null,
                entry.getSourcePermanentSnapshot(), entry.getControllerId(), null, 0));
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
        if (accepted) {
            acceptedPayers.add(playerId);
            if (mayPay.payment() instanceof SacrificePermanentsEffect sacrifice) {
                candidates = sacrificePermanentsEffectHandler.sacrificeCandidates(gameData, entry, sacrifice, playerId);
                count = sacrificePermanentsEffectHandler.sacrificeCount(gameData, entry, sacrifice, playerId);
                if (candidates.size() <= count) {
                    chosen.addAll(candidates);
                    candidates = List.of();
                }
            }
        }
        entry.replaceEffectToResolve(index, mayPay.asking(remaining, acceptedPayers, chosen));
        if (candidates.isEmpty()) {
            return false;
        }
        if (count == 1) {
            playerInputService.beginPermanentChoice(gameData, playerId, candidates,
                    new PermanentChoiceContext.AnyPlayerPaymentSacrifice(playerId), "Choose a permanent to sacrifice.");
        } else {
            playerInputService.beginMultiPermanentChoice(gameData, playerId, candidates, count,
                    new MultiPermanentChoiceContext.AnyPlayerPaymentSacrifice(playerId),
                    "Choose " + count + " permanents to sacrifice.");
        }
        return true;
    }

    /**
     * Adds the permanents an accepting player picked to the payment the parked resolution resumes with; they are
     * sacrificed together with the others' once every player has answered.
     */
    public void completeSacrificePick(GameData gameData, List<UUID> permanentIds) {
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
     * they could sacrifice (CR 608.2d); being dealt damage or having a card drawn can always be chosen.
     */
    public boolean canPay(GameData gameData, StackEntry entry, CardEffect payment, UUID playerId) {
        return switch (payment) {
            case SacrificePermanentsEffect sacrifice ->
                    sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(gameData, entry, sacrifice, playerId);
            case SequenceEffect sequence -> sequence.steps().stream()
                    .allMatch(step -> canPay(gameData, entry, step, playerId));
            default -> true;
        };
    }

    /** The players {@code payer} names, in turn order starting with the active player (CR 101.4). */
    private List<UUID> payerOrder(GameData gameData, StackEntry entry, MayPayPayer payer) {
        List<UUID> order = apnapOrder(gameData);
        if (payer == MayPayPayer.ANY_OPPONENT) {
            order.removeIf(entry.getControllerId()::equals);
        }
        if (payer == MayPayPayer.ANY_OTHER_PLAYER && entry.getTriggeringSpellSnapshot() != null) {
            order.removeIf(entry.getTriggeringSpellSnapshot().getControllerId()::equals);
        }
        return order;
    }

    private UUID targetPermanentController(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return null;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
        if (controllerId == null) {
            // An earlier step of this spell moved the target away (String of Disappearances): its
            // controller is the one it had as it left the battlefield (CR 608.2h).
            controllerId = entry.getRemovedPermanentControllers().get(targetId);
        }
        if (controllerId == null && targetId.equals(entry.getTriggeringPermanentId())) {
            controllerId = entry.getTriggeringPermanentControllerId();
        }
        return controllerId;
    }

    private UUID targetPlayerOrPermanentController(GameData gameData, UUID targetId) {
        if (targetId == null) {
            return null;
        }
        return gameData.playerIds.contains(targetId)
                ? targetId
                : gameQueryService.findPermanentController(gameData, targetId);
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

    /** The attacked player, or the controller of the attacked planeswalker, of an ON_ATTACK trigger. */
    private UUID defendingPlayer(GameData gameData, StackEntry entry) {
        if (entry.getDefendingPlayerId() != null) {
            return entry.getDefendingPlayerId();
        }
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        if (gameData.playerIds.contains(attackedTargetId)) {
            return attackedTargetId;
        }
        // CR 310.9d: for a battle being attacked, "defending player" refers to its protector.
        Permanent attacked = gameQueryService.findPermanentById(gameData, attackedTargetId);
        if (attacked != null && gameQueryService.isBattle(gameData, attacked)) {
            return attacked.getProtectorPlayerId();
        }
        return gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}
