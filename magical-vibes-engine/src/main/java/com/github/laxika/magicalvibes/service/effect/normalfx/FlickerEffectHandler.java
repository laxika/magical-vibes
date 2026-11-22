package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PendingExileReturns;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves every "exile permanent(s), return under owner's (or controller's) control" flicker via
 * {@link FlickerEffect}, dispatching on {@link FlickerEffect#timing()} then the shape of {@link FlickerEffect#subject()},
 * whose permanents {@link SubjectResolutionService} names.
 *
 * <p>{@code AT_STEP} scopes delegate to {@link ExileSupport#exileAndScheduleReturn} (a delayed trigger
 * that survives the source leaving the battlefield). {@code IMMEDIATE} exiles and re-creates the
 * permanent inline, optionally with returned +1/+1 counters or a subtype-conditional bonus effect. An
 * immediate TARGET flicker bound to a multi-target group (Ghostly Flicker's two targets) exiles every
 * chosen permanent before returning any of them.
 * When {@link FlickerEffect#returnUnderController()} is true, the permanent returns under the effect
 * controller and is tracked as stolen if the owner differs (Restoration Angel).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlickerEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GrantKeywordEffectHandler grantKeywordEffectHandler;
    private final PlayerInputService playerInputService;
    private final CloneService cloneService;
    private final com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final StateTriggerService stateTriggerService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlickerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FlickerEffect) effect;
        if (e.chooseAnyNumber()) {
            beginControllersPermanentsChoice(gameData, entry, e,
                    e.timing() == ReturnTiming.IMMEDIATE ? entry.getXValue() + 1 : 1);
            return;
        }
        if (e.timing() == ReturnTiming.NONE) {
            exileTargetOnly(gameData, entry, e);
            return;
        }
        if (e.subject() instanceof PermanentSubject.ExiledThisResolution) {
            returnEarlierExile(gameData, entry, e);
            return;
        }
        if (e.timing() == ReturnTiming.IMMEDIATE) {
            resolveImmediate(gameData, entry, e);
            return;
        }
        if (e.exilesHostWithAuras()) {
            resolveHostWithAurasAtStep(gameData, entry, e);
        } else if (e.subject() instanceof PermanentSubject.Target) {
            resolveTargetAtStep(gameData, entry, e);
        } else if (e.subject() instanceof PermanentSubject.OneOf oneOf) {
            resolveOneOfAtStep(gameData, entry, e, oneOf);
        } else if (e.subject().isSet() || e.subject() instanceof PermanentSubject.ReturnedThisResolution) {
            resolveSetAtStep(gameData, entry, e);
        } else {
            resolvePermanentAtStep(gameData, entry, e,
                    subjectResolutionService.permanent(gameData, entry, e, e.subject()));
        }
    }

    /**
     * Exiles the targeted permanent and returns nothing, remembering its last known card and
     * controller so a later effect of the same resolution can return it from exile.
     */
    private void exileTargetOnly(GameData gameData, StackEntry entry, FlickerEffect e) {
        Permanent target = subjectResolutionService.permanent(gameData, entry, e, e.subject());
        if (target == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(),
                target.getCard().getOwnerId() != null ? target.getCard().getOwnerId() : controllerId);
        entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
        entry.rememberRemovedPermanentController(target.getId(), ownerId);
        permanentRemovalService.removePermanentToExile(gameData, target);
        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
    }

    /**
     * Returns the card an earlier {@link ReturnTiming#NONE} flicker of this resolution exiled, either
     * right away or at the next step, under its owner's control. Does nothing if the card is no
     * longer in exile.
     */
    private void returnEarlierExile(GameData gameData, StackEntry entry, FlickerEffect e) {
        List<UUID> exiledIds = subjectResolutionService.permanentIds(gameData, entry, e, e.subject());
        UUID targetId = exiledIds.isEmpty() ? null : exiledIds.getFirst();
        Card card = targetId == null ? null : entry.lastKnownPermanentCard(targetId);
        if (card == null || gameData.findExiledCard(card.getId()) == null) {
            return;
        }
        UUID ownerId = entry.lastKnownPermanentController(targetId);
        if (e.timing() == ReturnTiming.IMMEDIATE) {
            returnImmediateBatch(gameData, entry, e, List.of(new FlickeredPermanent(card, ownerId, ownerId, false)));
            return;
        }
        PendingExileReturns.schedule(gameData,new PendingExileReturn(
                card, ownerId, e.returnTapped(), false, e.returnStep(),
                e.plusOnePlusOneCountersOnReturn(), List.of(),
                false, false, false, false, null, null, false,
                e.plusOnePlusOneCountersOnlyOnCreatures(), e.loyaltyCountersOnPlaneswalkersOnReturn(),
                Set.of(), e.counterTypeOnReturn(), e.counterAmountOnReturn(),
                e.countersOnReturn(), e.returnLandsTapped())
                .withTriggerSource(entry.getCard(), entry.getControllerId()));
        gameLogService.append(gameData, GameLog.cardThen(card,
                " will return at the beginning of the next "
                        + e.returnStep().getDisplayName().toLowerCase() + "."));
    }

    /**
     * Exiles the host and every Aura attached to it and schedules one return: the host first, then the Auras attached
     * to it (Flickerform). The host is the subject's first part and the Auras its {@code attachmentsOf} part.
     */
    private void resolveHostWithAurasAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        List<PermanentSubject> parts = ((PermanentSubject.AllOf) e.subject()).subjects();
        Permanent creature = subjectResolutionService.permanent(gameData, entry, e, parts.getFirst());
        if (creature == null) {
            return;
        }

        List<Permanent> attachedAuras = subjectResolutionService.permanents(gameData, entry, e, parts.get(1));

        List<Card> creatureCards = creature.cardsLeavingBattlefield();
        if (creatureCards.isEmpty()) {
            return;
        }

        List<Card> additionalCards = new ArrayList<>(creatureCards.subList(1, creatureCards.size()));
        Set<UUID> cardsToAttach = new LinkedHashSet<>();
        for (Permanent aura : attachedAuras) {
            List<Card> auraCards = aura.cardsLeavingBattlefield();
            if (auraCards.isEmpty() || !permanentRemovalService.removePermanentToExile(gameData, aura)) {
                continue;
            }
            additionalCards.addAll(auraCards);
            auraCards.stream().map(Card::getId).forEach(cardsToAttach::add);
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(creature.getId(), controllerId);
        if (ownerId == null || !permanentRemovalService.removePermanentToExile(gameData, creature)) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        Card primaryCard = creatureCards.getFirst();
        PendingExileReturns.schedule(gameData,PendingExileReturn.withCardsAttachedToPrimary(
                primaryCard, ownerId, additionalCards, cardsToAttach)
                .withTriggerSource(entry.getCard(), entry.getControllerId()));

        gameLogService.append(gameData, GameLog.cardThen(primaryCard,
                " is exiled with its attached Auras. It will return at the beginning of the next "
                        + e.returnStep().getDisplayName().toLowerCase() + "."));
        log.info("Game {} - {} exiles {} and its attached Auras; will return at next {}",
                gameData.id, entry.getCard().getName(), primaryCard.getName(), e.returnStep());
    }

    private void resolveTargetAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        resolveTargetsAtStep(gameData, entry, e, subjectResolutionService.permanents(gameData, entry, e, e.subject()));
    }

    private void resolveTargetsAtStep(GameData gameData, StackEntry entry, FlickerEffect e, List<Permanent> targets) {
        if (targets.isEmpty()) {
            return;
        }

        Map<UUID, List<Card>> cardsByOwner = new LinkedHashMap<>();
        for (Permanent target : targets) {

            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
            UUID ownerId = e.returnUnderController()
                    ? entry.getControllerId()
                    : gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);
            entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
            List<Card> cards = target.cardsLeavingBattlefield();
            permanentRemovalService.removePermanentToExile(gameData, target);
            cardsByOwner.computeIfAbsent(ownerId, id -> new ArrayList<>()).addAll(cards);

            gameLogService.append(gameData, GameLog.cardThen(cards.getFirst(),
                    " is exiled. It will return at the beginning of the next "
                            + e.returnStep().getDisplayName().toLowerCase() + "."));
            log.info("Game {} - {} exiles {}; will return at next {}",
                    gameData.id, entry.getCard().getName(), cards.getFirst().getName(), e.returnStep());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        if (cardsByOwner.isEmpty()) {
            return;
        }
        UUID primaryOwnerId = cardsByOwner.keySet().iterator().next();
        List<Card> cards = new ArrayList<>();
        Map<UUID, UUID> returnControllerByCardId = new HashMap<>();
        for (Map.Entry<UUID, List<Card>> group : cardsByOwner.entrySet()) {
            for (Card card : group.getValue()) {
                cards.add(card);
                if (!group.getKey().equals(primaryOwnerId)) {
                    returnControllerByCardId.put(card.getId(), group.getKey());
                }
            }
        }
        PendingExileReturns.schedule(gameData,new PendingExileReturn(
                cards.getFirst(), primaryOwnerId, e.returnTapped(), false, e.returnStep(),
                e.plusOnePlusOneCountersOnReturn(), cards.subList(1, cards.size()),
                e.returnAtOwnerNextEndStep() || e.returnAtControllerNextStep(), false, false, false,
                e.returnAtControllerNextStep() ? entry.getControllerId() : null, null, false,
                e.plusOnePlusOneCountersOnlyOnCreatures(), e.loyaltyCountersOnPlaneswalkersOnReturn(),
                Set.of(), e.counterTypeOnReturn(), e.counterAmountOnReturn(),
                e.countersOnReturn(), e.returnLandsTapped())
                .withReturnControllers(returnControllerByCardId)
                .withTriggerSource(entry.getCard(), entry.getControllerId()));
    }

    private void resolvePermanentAtStep(GameData gameData, StackEntry entry, FlickerEffect e, Permanent source) {
        if (source == null) {
            return;
        }

        UUID returnControllerId = e.returnUnderController()
                ? entry.getControllerId()
                : gameData.stolenCreatures.getOrDefault(source.getId(),
                        source.getCard().getOwnerId() != null ? source.getCard().getOwnerId() : entry.getControllerId());
        exileSupport.exileAndScheduleReturn(gameData, entry, source, returnControllerId, e.returnTapped(), e.returnStep(),
                e.plusOnePlusOneCountersOnReturn(), e.returnAtControllerNextStep(),
                e.returnAtControllerNextStep() ? entry.getControllerId() : null);
    }

    /**
     * "Exile this permanent or up to one target creature": with no target chosen, the first option without a target
     * (the source); with both on the battlefield the controller picks one as the ability resolves; else whichever is
     * still there.
     */
    private void resolveOneOfAtStep(GameData gameData, StackEntry entry, FlickerEffect e,
                                    PermanentSubject.OneOf oneOf) {
        PermanentSubject targetOption = oneOf.options().stream()
                .filter(PermanentSubject.Target.class::isInstance).findFirst().orElse(null);
        PermanentSubject otherOption = oneOf.options().stream()
                .filter(option -> option != targetOption).findFirst().orElseThrow();
        List<UUID> targetIds = targetOption == null ? List.of()
                : subjectResolutionService.permanentIds(gameData, entry, e, targetOption);
        Permanent other = subjectResolutionService.permanent(gameData, entry, e, otherOption);
        if (targetIds.isEmpty()) {
            resolvePermanentAtStep(gameData, entry, e, other);
            return;
        }
        // "Exile this permanent or up to one target creature": only the creature is targeted, so the
        // controller picks between the source and the target as the ability resolves.
        List<Permanent> targets = subjectResolutionService.permanents(gameData, entry, e, targetOption);
        Permanent target = targets.isEmpty() ? null : targets.getFirst();
        if (other == null || target == null) {
            resolveTargetsAtStep(gameData, entry, e, targets);
            return;
        }
        playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect(List.of(
                        new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption(
                                "Exile " + FaceDownVisibility.publicName(gameData, other), e.withSubject(otherOption)),
                        new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption(
                                "Exile " + FaceDownVisibility.publicName(gameData, target), e.withSubject(targetOption)))),
                entry.getSourcePermanentId());
    }

    /**
     * Exiles every permanent of a set subject ("each creature target player controls", "nontoken creatures you
     * control", the permanents an earlier step returned) and queues a single delayed return, so the whole group
     * re-enters simultaneously at the requested step, each card under its owner's control.
     */
    private void resolveSetAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        List<Permanent> toExile = subjectResolutionService.permanents(gameData, entry, e, e.subject());
        exileSetAtStep(gameData, entry, e, toExile);
        if (e.subject() instanceof PermanentSubject.ReturnedThisResolution) {
            entry.clearReturnedPermanentIds();
        }
    }

    private void resolveChosenPermanentsAtStep(
            GameData gameData, StackEntry entry, FlickerEffect e, List<UUID> permanentIds) {
        List<UUID> stillMatching = subjectResolutionService.permanentIds(gameData, entry, e, e.subject());
        List<Permanent> toExile = permanentIds.stream()
                .filter(stillMatching::contains)
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .toList();
        exileSetAtStep(gameData, entry, e, toExile);
    }

    private void exileSetAtStep(GameData gameData, StackEntry entry, FlickerEffect e, List<Permanent> toExile) {
        if (toExile.isEmpty()) {
            return;
        }

        Map<UUID, List<Card>> cardsByOwner = new LinkedHashMap<>();
        for (Permanent permanent : toExile) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            UUID ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), controllerId);
            List<Card> cards = permanent.cardsLeavingBattlefield();
            permanentRemovalService.removePermanentToExile(gameData, permanent);
            cardsByOwner.computeIfAbsent(ownerId, id -> new ArrayList<>()).addAll(cards);
            gameLogService.append(gameData, GameLog.cardThen(cards.getFirst(),
                    " is exiled. It will return at the beginning of the next "
                            + e.returnStep().getDisplayName().toLowerCase() + "."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        // One delayed triggered ability returns every card, each under its owner's control (Ghostway).
        List<Card> cards = cardsByOwner.values().stream().flatMap(List::stream).toList();
        PendingExileReturns.schedule(gameData,new PendingExileReturn(
                cards.getFirst(), cardsByOwner.keySet().iterator().next(), e.returnTapped(), false, e.returnStep(),
                e.plusOnePlusOneCountersOnReturn(), cards.subList(1, cards.size()),
                e.returnAtControllerNextStep(), e.grantHaste(), false, false,
                e.returnAtControllerNextStep() ? entry.getControllerId() : null, null, false,
                e.plusOnePlusOneCountersOnlyOnCreatures(), e.loyaltyCountersOnPlaneswalkersOnReturn(),
                Set.of(), e.counterTypeOnReturn(), e.counterAmountOnReturn(),
                e.countersOnReturn(), e.returnLandsTapped())
                .withTriggerSource(entry.getCard(), entry.getControllerId())
                .withReturnControllers(returnControllersByCardId(cardsByOwner)));
        log.info("Game {} - {} exiles {} permanents; they return at next {}",
                gameData.id, entry.getCard().getName(), toExile.size(), e.returnStep());
    }

    private static Map<UUID, UUID> returnControllersByCardId(Map<UUID, List<Card>> cardsByOwner) {
        Map<UUID, UUID> controllerByCardId = new LinkedHashMap<>();
        cardsByOwner.forEach((ownerId, cards) -> cards.forEach(card -> controllerByCardId.put(card.getId(), ownerId)));
        return controllerByCardId;
    }

    private void resolveImmediate(GameData gameData, StackEntry entry, FlickerEffect e) {
        List<UUID> permanentIds = immediateFlickerTargets(gameData, entry, e);

        // Every chosen permanent leaves before any of them comes back, so a multi-target flicker
        // (Ghostly Flicker) returns them simultaneously and their ETB triggers see each other.
        List<FlickeredPermanent> exiled = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                exiled.addAll(exileForImmediateReturn(gameData, entry, e, target));
            }
        }
        if (exiled.isEmpty()) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        // CR 603.8: a state trigger triggers as soon as its condition is true, even when the state
        // holds only while the exiled permanents are away.
        stateTriggerService.checkStateTriggers(gameData);
        returnImmediateBatch(gameData, entry, e, exiled);

        if (e.addAdditionalEndStepIfFirst()
                && gameData.currentStep == TurnStep.END_STEP
                && gameData.endStepsThisTurn == 1) {
            gameData.additionalEndStepsPending++;
            gameLogService.append(gameData, GameLog.text("There is an additional end step after this step."));
        }
    }

    public void flickerSelfUnderControl(GameData gameData, StackEntry entry, UUID returnControllerId) {
        FlickerEffect effect = FlickerEffect.flickerSelfUnderYourControl();
        Permanent source = subjectResolutionService.permanent(gameData, entry, effect, effect.subject());
        if (source == null) {
            return;
        }
        List<FlickeredPermanent> flickered = exileForImmediateReturn(gameData, entry, effect, source, returnControllerId);
        permanentRemovalService.removeOrphanedAuras(gameData);
        if (flickered.size() == 1) {
            returnAfterImmediateExile(gameData, entry, effect, flickered.getFirst());
        } else {
            returnImmediateBatch(gameData, entry, effect, flickered);
        }
    }

    public void flickerPermanentsUnderOwnersControl(
            GameData gameData, StackEntry entry, List<UUID> permanentIds) {
        List<FlickeredPermanent> exiled = new ArrayList<>();
        FlickerEffect effect = FlickerEffect.flickerTarget();
        for (UUID permanentId : permanentIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                exiled.addAll(exileForImmediateReturn(gameData, entry, effect, target));
            }
        }
        if (exiled.isEmpty()) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        returnImmediateBatch(gameData, entry, effect, exiled);
    }

    private void beginControllersPermanentsChoice(
            GameData gameData, StackEntry entry, FlickerEffect effect, int remainingIterations) {
        UUID controllerId = entry.getControllerId();
        List<UUID> validIds = subjectResolutionService.permanentIds(gameData, entry, effect, effect.subject());
        if (validIds.isEmpty()) return;
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validIds, validIds.size(),
                new MultiPermanentChoiceContext.FlickerAnyNumber(entry, effect, remainingIterations),
                entry.getCard().getName() + " — Choose any number of permanents you control to exile.");
    }

    /** The permanents an "any number of permanents you control" flicker may still exile, as its subject names them. */
    public List<UUID> eligibleChoices(GameData gameData, MultiPermanentChoiceContext.FlickerAnyNumber context) {
        return subjectResolutionService.permanentIds(gameData, context.resolvingEntry(), context.effect(),
                context.effect().subject());
    }

    public boolean completeAnyNumberChoice(GameData gameData, List<UUID> permanentIds,
                                            MultiPermanentChoiceContext.FlickerAnyNumber context) {
        if (context.effect().timing() == ReturnTiming.AT_STEP) {
            resolveChosenPermanentsAtStep(
                    gameData, context.resolvingEntry(), context.effect(), permanentIds);
        } else {
            flickerPermanentsUnderOwnersControl(gameData, context.resolvingEntry(), permanentIds);
        }
        int remainingIterations = context.remainingIterations() - 1;
        if (remainingIterations <= 0) return true;
        beginControllersPermanentsChoice(
                gameData, context.resolvingEntry(), context.effect(), remainingIterations);
        return !gameData.interaction.isAwaitingInput();
    }

    /**
     * The permanents an immediate flicker touches: those the subject names, where a target is the chosen target
     * group — a single {@code targetId} for the usual one-target flicker and a real list for a card that binds the
     * effect to a multi-target group — and a choice offers its target when one was chosen, else its other option.
     */
    private List<UUID> immediateFlickerTargets(GameData gameData, StackEntry entry, FlickerEffect e) {
        if (e.subject() instanceof PermanentSubject.OneOf oneOf) {
            List<UUID> chosen = List.of();
            for (PermanentSubject option : oneOf.options().stream()
                    .sorted(java.util.Comparator.comparing(option -> !(option instanceof PermanentSubject.Target)))
                    .toList()) {
                chosen = subjectResolutionService.permanentIds(gameData, entry, e, option);
                if (!chosen.isEmpty()) {
                    break;
                }
            }
            return chosen;
        }
        if (e.subject() instanceof PermanentSubject.Target) {
            List<UUID> boundGroups = targetsOfEveryBoundGroup(entry, e);
            if (boundGroups != null) {
                return boundGroups;
            }
        }
        return subjectResolutionService.permanentIds(gameData, entry, e, e.subject());
    }

    /**
     * The distinct targets of every group the effect instance is bound to, or {@code null} when it is
     * bound to at most one group or none of those groups holds a target. One flicker bound to several groups (Don and Leo) must exile all of
     * its targets before any of them returns, so they enter the battlefield simultaneously.
     */
    private List<UUID> targetsOfEveryBoundGroup(StackEntry entry, FlickerEffect e) {
        Card targeting = entry.getTargetingCard();
        if (targeting == null || targeting.getEffectTargetIndex(e, 1) < 0) {
            return null;
        }
        Set<UUID> targets = new LinkedHashSet<>();
        for (int occurrence = 0; targeting.getEffectTargetIndex(e, occurrence) >= 0; occurrence++) {
            targets.addAll(entry.targetsForGroup(targeting.getEffectTargetIndex(e, occurrence)));
        }
        return targets.isEmpty() ? null : List.copyOf(targets);
    }

    /** A permanent that has already been exiled by an immediate flicker, with the state its return needs. */
    private record FlickeredPermanent(Card card, UUID ownerId, UUID returnControllerId, boolean hadBonusSubtype) {
    }

    private List<FlickeredPermanent> exileForImmediateReturn(
            GameData gameData, StackEntry entry, FlickerEffect e, Permanent target) {
        return exileForImmediateReturn(gameData, entry, e, target, null);
    }

    /**
     * Exiles the permanent and describes each card that returns for it. A meld or merged permanent
     * is made of several cards, which all leave together and return separately.
     */
    private List<FlickeredPermanent> exileForImmediateReturn(
            GameData gameData, StackEntry entry, FlickerEffect e, Permanent target,
            UUID returnControllerOverride) {
        UUID previousControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(),
                target.getCard().getOwnerId() != null ? target.getCard().getOwnerId() : previousControllerId);
        UUID returnControllerId = returnControllerOverride != null
                ? returnControllerOverride
                : e.returnUnderController() ? entry.getControllerId() : ownerId;

        List<Card> cards = target.cardsLeavingBattlefield();
        Set<CardSubtype> battlefieldSubtypes = gameQueryService.effectiveCreatureSubtypes(gameData, target);
        boolean hadBonusSubtype = e.bonusSubtype() != null
                ? battlefieldSubtypes.contains(e.bonusSubtype())
                : !e.bonusSubtypes().isEmpty() && battlefieldSubtypes.stream().anyMatch(e.bonusSubtypes()::contains);

        permanentRemovalService.removePermanentToExile(gameData, target);
        return cards.stream()
                .map(card -> new FlickeredPermanent(card, ownerId, returnControllerId, hadBonusSubtype))
                .toList();
    }

    private void returnAfterImmediateExile(
            GameData gameData, StackEntry entry, FlickerEffect e, FlickeredPermanent flickered) {
        if ((e.equals(FlickerEffect.flickerTarget()) || e.equals(FlickerEffect.flickerTargetUnderYourControl()))
                && (flickered.card().isAura()
                || flickered.card().getStaticEffects()
                .stream().anyMatch(com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect.class::isInstance))) {
            returnAuraBatch(gameData, List.of(flickered));
            return;
        }
        Runnable afterEntry = placeAfterImmediateExile(gameData, entry, e, flickered,
                new Permanent(flickered.card()), battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of());
        afterEntry.run();
    }

    private void returnImmediateBatch(GameData gameData, StackEntry entry, FlickerEffect effect,
                                      List<FlickeredPermanent> exiled) {
        List<FlickeredPermanent> returning = exiled.stream()
                .filter(flickered -> !flickered.card().isToken()
                        && (flickered.card().getType().isPermanentType()
                        || flickered.card().getAdditionalTypes().stream().anyMatch(CardType::isPermanentType))
                        && gameData.findExiledCard(flickered.card().getId()) != null)
                .toList();
        if ((effect.equals(FlickerEffect.flickerTarget())
                || effect.equals(FlickerEffect.flickerTargetUnderYourControl()))
                && returning.stream().anyMatch(flickered -> flickered.card().isAura()
                || flickered.card().getStaticEffects()
                .stream().anyMatch(com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect.class::isInstance))) {
            returnAuraBatch(gameData, returning);
            return;
        }
        List<Permanent> simultaneous = returning.stream().map(flickered -> new Permanent(flickered.card())).toList();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Runnable> afterEntry = new ArrayList<>();
        // The returns are one event, so Don't Blink shuffles each owner's library once for all of them.
        Set<UUID> shuffleOwners = new LinkedHashSet<>();
        gameData.exileEntryShuffleBatchOwners = shuffleOwners;
        try {
            for (int i = 0; i < returning.size(); i++) {
                afterEntry.add(placeAfterImmediateExile(gameData, entry, effect, returning.get(i),
                        simultaneous.get(i), enterTappedTypes, simultaneous));
            }
        } finally {
            gameData.exileEntryShuffleBatchOwners = null;
        }
        shuffleOwners.forEach(ownerId -> LibraryShuffleHelper.shuffleLibrary(gameData, ownerId));
        afterEntry.forEach(Runnable::run);
        exiled.stream()
                .filter(flickered -> !returning.contains(flickered) && flickered.hadBonusSubtype()
                        && effect.bonusSubtypes().isEmpty())
                .forEach(flickered -> applyImmediateBonus(gameData, entry, effect, flickered.card(), true));
    }

    private void returnAuraBatch(GameData gameData, List<FlickeredPermanent> returning) {
        List<com.github.laxika.magicalvibes.model.BattlefieldEntryCard> cards = returning.stream()
                .filter(flickered -> !flickered.card().isToken())
                .map(flickered -> new com.github.laxika.magicalvibes.model.BattlefieldEntryCard(
                        flickered.returnControllerId(), flickered.ownerId(), flickered.card(),
                        com.github.laxika.magicalvibes.model.Zone.EXILE, null))
                .toList();
        battlefieldEntryBatchSupport.begin(gameData, cards);
    }

    private Runnable placeAfterImmediateExile(
            GameData gameData, StackEntry entry, FlickerEffect e, FlickeredPermanent flickered,
            Permanent returned, Set<CardType> enterTappedTypes, List<Permanent> simultaneous) {
        Card card = flickered.card();
        UUID ownerId = flickered.ownerId();
        UUID returnControllerId = flickered.returnControllerId();
        boolean hadBonusSubtype = flickered.hadBonusSubtype();

        if (card.isToken() || (!card.getType().isPermanentType()
                && card.getAdditionalTypes().stream().noneMatch(CardType::isPermanentType))
                || gameData.findExiledCard(card.getId()) == null) {
            return () -> {};
        }

        // Immediately return from exile as a new permanent
        gameData.removeFromExile(card.getId());
        // A copy-on-enter card returning alone gets its copy choice however it was flickered, and the
        // flicker's own tapped return still applies to the copy.
        boolean soleReturn = simultaneous.size() == 1;
        if ((e.subject() instanceof PermanentSubject.Source || soleReturn)
                && cloneService.prepareCloneReplacementEffect(
                        gameData, returnControllerId, card, null, 0)) {
            gameData.cloneOperation.entersTapped = e.returnTapped() || e.tapOnImmediateReturn();
            return () -> {};
        }
        returned.setEnteredFromExile(true);
        if (e.tapOnImmediateReturn()) {
            returned.tap();
        }
        if (e.returnAttacking()) {
            UUID attackTargetId = entry.getAttackedTargetId();
            if (attackTargetId == null && entry.getSourcePermanentId() != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (source != null) {
                    attackTargetId = source.getAttackTarget();
                }
            }
            returned.setAttackTarget(attackTargetId);
            // Put onto the battlefield attacking without being declared: attacking, but never "attacked" (CR 508.4).
            returned.enterAttacking(true);
        }
        if (e.returnFaceDown()) {
            returned.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        }
        boolean applyReturnCounters = e.plusOnePlusOneCountersOnReturn() > 0
                && (e.bonusSubtype() == null || hadBonusSubtype);
        boolean counterAfterEntering = applyReturnCounters && e.bonusSubtype() != null;
        if (applyReturnCounters && !counterAfterEntering
                && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, returned, returnControllerId)) {
            int returnCounters = gameQueryService.doublePlusOnePlusOneCounters(
                    gameData, returned, returnControllerId, e.plusOnePlusOneCountersOnReturn());
            if (returnCounters > 0) {
                returned.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, returnCounters);
            }
        }
        if (e.returnTapped()) {
            returned.tap();
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, returnControllerId, returned,
                enterTappedTypes, simultaneous);
        if (e.returnUnderController() && !returnControllerId.equals(ownerId)) {
            graveyardReturnSupport.trackStolenCreature(gameData, returned.getId(), returnControllerId, ownerId);
        }

        gameLogService.append(gameData, GameLog.builder().card(card).text(" is exiled by ").card(entry.getCard()).text(" and returns to the battlefield under " + gameData.playerIdToName.get(returnControllerId) + "'s control.").build());
        log.info("Game {} - {} flickers {} (immediate return)", gameData.id, entry.getCard().getName(), card.getName());

        return () -> {
            if (e.returnFaceDown()) {
                battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, returnControllerId, card);
            } else {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, returnControllerId, card, null, false);
            }

            if (counterAfterEntering
                    && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, returned, returnControllerId)) {
                int returnCounters = gameQueryService.doublePlusOnePlusOneCounters(
                        gameData, returned, returnControllerId, e.plusOnePlusOneCountersOnReturn());
                if (returnCounters > 0) {
                    returned.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, returnCounters);
                }
            }

            if (e.addCounterIfReturnedUnderControllerOtherwiseTap()) {
                if (returnControllerId.equals(entry.getControllerId())) {
                    if (!gameQueryService.cantHavePlusOnePlusOneCounters(gameData, returned, returnControllerId)) {
                        int returnCounters = gameQueryService.doublePlusOnePlusOneCounters(
                                gameData, returned, returnControllerId, 1);
                        if (returnCounters > 0) {
                            returned.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                                    returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + returnCounters);
                        }
                    }
                } else {
                    returned.tap();
                }
            }
            if (!e.grantedKeywordsOnReturn().isEmpty()) {
                grantKeywordEffectHandler.grantToPermanent(gameData, entry, returned, e.grantedKeywordsOnReturn());
            }

            // A bonus keyed to a set of subtypes ("if that creature is a Bird, Frog, Otter, or Rat")
            // looks at the permanent as it exists after returning, not as it was when exiled.
            boolean bonusApplies = e.bonusSubtypes().isEmpty()
                    ? hadBonusSubtype
                    : gameQueryService.effectiveCreatureSubtypes(gameData, returned).stream()
                            .anyMatch(e.bonusSubtypes()::contains);
            applyImmediateBonus(gameData, entry, e, card, bonusApplies);
        };
    }

    /**
     * Applies the flicker's bonus once the required subtype has been established. A single-subtype
     * bonus ("if a Pirate was exiled this way") still counts a token that ceased to exist; a
     * subtype-set bonus is only applied for a permanent that returned.
     */
    private void applyImmediateBonus(GameData gameData, StackEntry entry, FlickerEffect e, Card card,
                                     boolean hadBonusSubtype) {
        if (hadBonusSubtype && e.bonusEffect() instanceof DrawCardEffect drawEffect) {
            int drawAmount = amountEvaluationService.evaluate(gameData, drawEffect.amount(),
                    AmountContext.forStackEntry(entry, null));
            for (int i = 0; i < drawAmount; i++) {
                drawService.resolveDrawCard(gameData, entry.getControllerId());
            }
            String matchingSubtype = e.bonusSubtype() != null
                    ? e.bonusSubtype().getDisplayName()
                    : card.getSubtypes().stream().filter(e.bonusSubtypes()::contains)
                            .findFirst().map(CardSubtype::getDisplayName).orElse("matching subtype");
            gameLogService.append(gameData, GameLog.builder().text(gameData.playerIdToName.get(entry.getControllerId()) + " draws a card (").card(card).text(" was a " + matchingSubtype + ").").build());
        }
    }
}
