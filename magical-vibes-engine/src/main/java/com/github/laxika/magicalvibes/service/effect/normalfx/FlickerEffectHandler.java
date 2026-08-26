package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
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
 * {@link FlickerEffect}, dispatching on {@link FlickerEffect#timing()} then {@link FlickerEffect#scope()}.
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
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GrantKeywordEffectHandler grantKeywordEffectHandler;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlickerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FlickerEffect) effect;
        if (e.scope() == com.github.laxika.magicalvibes.model.effect.FlickerScope.CONTROLLERS_PERMANENTS
                && e.chooseAnyNumber()) {
            beginControllersPermanentsChoice(gameData, entry, e,
                    e.timing() == ReturnTiming.IMMEDIATE ? entry.getXValue() + 1 : 1);
            return;
        }
        if (e.timing() == ReturnTiming.IMMEDIATE) {
            resolveImmediate(gameData, entry, e);
            return;
        }
        switch (e.scope()) {
            case TARGET -> resolveTargetAtStep(gameData, entry, e);
            case SELF -> resolveSelfAtStep(gameData, entry, e);
            case TARGET_PLAYERS_PERMANENTS -> resolvePlayersPermanentsAtStep(gameData, entry, e);
            case CONTROLLERS_PERMANENTS -> resolveControllersPermanentsAtStep(gameData, entry, e);
            case ENCHANTED_CREATURE_AND_AURAS -> resolveEnchantedCreatureAndAurasAtStep(gameData, entry, e);
        }
    }

    private void resolveEnchantedCreatureAndAurasAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        UUID creatureId = entry.getTargetId();
        Permanent creature = creatureId == null ? null : gameQueryService.findPermanentById(gameData, creatureId);
        if (creature == null) {
            return;
        }

        List<Permanent> attachedAuras = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (creatureId.equals(permanent.getAttachedTo()) && permanent.getCard().isAura()) {
                attachedAuras.add(permanent);
            }
        });

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
        gameData.queueDelayedAction(PendingExileReturn.withCardsAttachedToPrimary(
                primaryCard, ownerId, additionalCards, cardsToAttach));

        gameLogService.append(gameData, GameLog.cardThen(primaryCard,
                " is exiled with its attached Auras. It will return at the beginning of the next "
                        + e.returnStep().getDisplayName().toLowerCase() + "."));
        log.info("Game {} - {} exiles {} and its attached Auras; will return at next {}",
                gameData.id, entry.getCard().getName(), primaryCard.getName(), e.returnStep());
    }

    private void resolveTargetAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty()) {
            return;
        }

        Map<UUID, List<Card>> cardsByOwner = new LinkedHashMap<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
            UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);
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

        for (Map.Entry<UUID, List<Card>> group : cardsByOwner.entrySet()) {
            List<Card> cards = group.getValue();
            gameData.queueDelayedAction(new PendingExileReturn(
                    cards.getFirst(), group.getKey(), e.returnTapped(), false, e.returnStep(),
                    e.plusOnePlusOneCountersOnReturn(), cards.subList(1, cards.size()),
                    e.returnAtOwnerNextEndStep(), false, false, false, null, null, false,
                    e.plusOnePlusOneCountersOnlyOnCreatures(),
                    e.loyaltyCountersOnPlaneswalkersOnReturn()));
        }
    }

    private void resolveSelfAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        UUID returnControllerId = e.returnUnderController()
                ? entry.getControllerId()
                : source.getCard().getOwnerId() != null ? source.getCard().getOwnerId() : entry.getControllerId();
        exileSupport.exileAndScheduleReturn(gameData, entry, source, returnControllerId, e.returnTapped(), e.returnStep(),
                e.plusOnePlusOneCountersOnReturn());
    }

    private void resolvePlayersPermanentsAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> toExile = battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                .toList();

        for (Permanent permanent : toExile) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            UUID ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), controllerId);
            exileSupport.exileAndScheduleReturn(
                    gameData, entry, permanent, ownerId, e.returnTapped(), e.returnStep(),
                    e.plusOnePlusOneCountersOnReturn());
        }
    }

    /**
     * Exiles every filtered permanent the ability's controller controls and queues a single delayed
     * return per owner, so the whole group re-enters simultaneously at the requested step
     * (Legion's Initiative).
     */
    private void resolveControllersPermanentsAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> toExile = battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                .toList();
        exileControllersPermanentsAtStep(gameData, entry, e, toExile);
    }

    private void resolveControllersPermanentsAtStep(
            GameData gameData, StackEntry entry, FlickerEffect e, List<UUID> permanentIds) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> toExile = permanentIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(p -> p != null
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                .toList();
        exileControllersPermanentsAtStep(gameData, entry, e, toExile);
    }

    private void exileControllersPermanentsAtStep(
            GameData gameData, StackEntry entry, FlickerEffect e, List<Permanent> toExile) {
        UUID controllerId = entry.getControllerId();
        if (toExile.isEmpty()) {
            return;
        }

        Map<UUID, List<Card>> cardsByOwner = new LinkedHashMap<>();
        for (Permanent permanent : toExile) {
            UUID ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), controllerId);
            List<Card> cards = permanent.cardsLeavingBattlefield();
            permanentRemovalService.removePermanentToExile(gameData, permanent);
            cardsByOwner.computeIfAbsent(ownerId, id -> new ArrayList<>()).addAll(cards);
            gameLogService.append(gameData, GameLog.cardThen(cards.getFirst(),
                    " is exiled. It will return at the beginning of the next "
                            + e.returnStep().getDisplayName().toLowerCase() + "."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        for (Map.Entry<UUID, List<Card>> group : cardsByOwner.entrySet()) {
            List<Card> cards = group.getValue();
            gameData.queueDelayedAction(new PendingExileReturn(
                    cards.getFirst(), group.getKey(), e.returnTapped(), false, e.returnStep(),
                    e.plusOnePlusOneCountersOnReturn(), cards.subList(1, cards.size()),
                    e.returnAtControllerNextStep(), e.grantHaste(), false, false,
                    e.returnAtControllerNextStep() ? controllerId : null, null, false,
                    e.plusOnePlusOneCountersOnlyOnCreatures(), e.loyaltyCountersOnPlaneswalkersOnReturn()));
        }
        log.info("Game {} - {} exiles {} permanents; they return at next {}",
                gameData.id, entry.getCard().getName(), toExile.size(), e.returnStep());
    }

    private void resolveImmediate(GameData gameData, StackEntry entry, FlickerEffect e) {
        List<UUID> permanentIds = immediateFlickerTargets(entry, e);

        // Every chosen permanent leaves before any of them comes back, so a multi-target flicker
        // (Ghostly Flicker) returns them simultaneously and their ETB triggers see each other.
        List<FlickeredPermanent> exiled = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                exiled.add(exileForImmediateReturn(gameData, entry, e, target));
            }
        }
        if (exiled.isEmpty()) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        for (FlickeredPermanent flickered : exiled) {
            returnAfterImmediateExile(gameData, entry, e, flickered);
        }
    }

    public void flickerPermanentsUnderOwnersControl(
            GameData gameData, StackEntry entry, List<UUID> permanentIds) {
        List<FlickeredPermanent> exiled = new ArrayList<>();
        FlickerEffect effect = FlickerEffect.flickerTarget();
        for (UUID permanentId : permanentIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                exiled.add(exileForImmediateReturn(gameData, entry, effect, target));
            }
        }
        if (exiled.isEmpty()) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        for (FlickeredPermanent flickered : exiled) {
            returnAfterImmediateExile(gameData, entry, effect, flickered);
        }
    }

    private void beginControllersPermanentsChoice(
            GameData gameData, StackEntry entry, FlickerEffect effect, int remainingIterations) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;
        List<UUID> validIds = battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, effect.filter()))
                .map(Permanent::getId)
                .toList();
        if (validIds.isEmpty()) return;
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validIds, validIds.size(),
                new MultiPermanentChoiceContext.FlickerAnyNumber(entry, effect, remainingIterations),
                entry.getCard().getName() + " — Choose any number of permanents you control to exile.");
    }

    public boolean completeAnyNumberChoice(GameData gameData, List<UUID> permanentIds,
                                            MultiPermanentChoiceContext.FlickerAnyNumber context) {
        if (context.effect().timing() == ReturnTiming.AT_STEP) {
            resolveControllersPermanentsAtStep(
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
     * The permanents an immediate flicker touches: the source for SELF, otherwise the chosen target
     * group — which is a single {@code targetId} for the usual one-target flicker and a real list for
     * a card that binds the effect to a multi-target group.
     */
    private List<UUID> immediateFlickerTargets(StackEntry entry, FlickerEffect e) {
        if (e.scope() == com.github.laxika.magicalvibes.model.effect.FlickerScope.SELF) {
            return List.of(entry.getSourcePermanentId());
        }
        List<UUID> group = entry.targetsForEffect(e);
        if (!group.isEmpty()) {
            return group;
        }
        return entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of();
    }

    /** A permanent that has already been exiled by an immediate flicker, with the state its return needs. */
    private record FlickeredPermanent(Card card, UUID ownerId, UUID returnControllerId, boolean hadBonusSubtype) {
    }

    private FlickeredPermanent exileForImmediateReturn(
            GameData gameData, StackEntry entry, FlickerEffect e, Permanent target) {
        UUID previousControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), previousControllerId);
        UUID returnControllerId = e.returnUnderController() ? entry.getControllerId() : ownerId;

        Card card = target.getOriginalCard();
        boolean hadBonusSubtype = e.bonusSubtype() != null
                && card.getSubtypes().contains(e.bonusSubtype());

        permanentRemovalService.removePermanentToExile(gameData, target);
        return new FlickeredPermanent(card, ownerId, returnControllerId, hadBonusSubtype);
    }

    private void returnAfterImmediateExile(
            GameData gameData, StackEntry entry, FlickerEffect e, FlickeredPermanent flickered) {
        Card card = flickered.card();
        UUID ownerId = flickered.ownerId();
        UUID returnControllerId = flickered.returnControllerId();
        boolean hadBonusSubtype = flickered.hadBonusSubtype();

        // Immediately return from exile as a new permanent
        gameData.removeFromExile(card.getId());
        Permanent returned = new Permanent(card);
        boolean applyReturnCounters = e.plusOnePlusOneCountersOnReturn() > 0
                && (e.bonusSubtype() == null || hadBonusSubtype);
        if (applyReturnCounters
                && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, returned, returnControllerId)) {
            int returnCounters = gameQueryService.doublePlusOnePlusOneCounters(
                    gameData, returned, returnControllerId, e.plusOnePlusOneCountersOnReturn());
            if (returnCounters > 0) {
                returned.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, returnCounters);
            }
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, returnControllerId, returned);
        if (e.returnUnderController() && !returnControllerId.equals(ownerId)) {
            graveyardReturnSupport.trackStolenCreature(gameData, returned.getId(), returnControllerId, ownerId);
        }

        gameLogService.append(gameData, GameLog.builder().card(card).text(" is exiled by ").card(entry.getCard()).text(" and returns to the battlefield under " + gameData.playerIdToName.get(returnControllerId) + "'s control.").build());
        log.info("Game {} - {} flickers {} (immediate return)", gameData.id, entry.getCard().getName(), card.getName());

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, returnControllerId, card, null, false);

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

        // Apply bonus if the exiled permanent had the required subtype
        if (hadBonusSubtype && e.bonusEffect() instanceof DrawCardEffect drawEffect) {
            int drawAmount = amountEvaluationService.evaluate(gameData, drawEffect.amount(),
                    AmountContext.forStackEntry(entry, null));
            for (int i = 0; i < drawAmount; i++) {
                drawService.resolveDrawCard(gameData, entry.getControllerId());
            }
            gameLogService.append(gameData, GameLog.builder().text(gameData.playerIdToName.get(entry.getControllerId()) + " draws a card (").card(card).text(" was a " + e.bonusSubtype().getDisplayName() + ").").build());
        }
    }
}
