package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/** Drains game transitions between completed mutation scopes, under the session lock. */
@Service
@RequiredArgsConstructor
public class SubgameService {
    private final GameRegistry registry;
    private final GameMutationCoordinator mutations;
    private final GameSetupService setup;
    private final GameLogService logs;
    private final EffectResolutionService effects;
    private final InputCompletionService completion;
    private final InteractionHandlerRegistry interactions;
    private final GameOutcomeService outcomes;
    private final com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService removal;

    public void advance(GameSession session) {
        if (session.transitioning) return;
        session.transitioning = true;
        boolean success = false;
        try {
            while (true) {
                GameData active = session.active();
                if (!active.pendingAncestorTransfers.isEmpty()) transferAncestors(active);
                else if (active.subgameRequested) start(session, active);
                else if (session.depth() > 0 && active.gameResult != null) finish(session, active);
                else break;
            }
            success = true;
        } finally {
            if (success) session.transitioning = false;
            else session.transitionFailed = true;
        }
    }

    private void transferAncestors(GameData child) {
        for (PendingAncestorTransfer transfer : List.copyOf(child.pendingAncestorTransfers)) {
            GameData source = child.session.frames().stream().filter(frame -> frame.id.equals(transfer.sourceGameId()))
                    .findFirst().orElseThrow();
            mutations.mutate(source, () -> {
                Set<StackEntry> existing = Collections.newSetFromMap(new IdentityHashMap<>());
                existing.addAll(source.stack);
                UUID id = transfer.cardId();
                for (var zone : source.playerBattlefields.values()) {
                    for (Permanent permanent : List.copyOf(zone)) {
                        if (permanent.cardsLeavingBattlefield().stream().anyMatch(card -> card.getId().equals(id))) {
                            removal.removePermanentToOutsideGame(source, permanent);
                        }
                    }
                }
                source.phasedOutPermanents.values().forEach(zone -> zone.removeIf(permanent ->
                        permanent.cardsLeavingBattlefield().stream().anyMatch(card -> card.getId().equals(id))));
                removal.removeCardFromGraveyardById(source, id);
                source.playerHands.values().forEach(zone -> zone.removeIf(card -> card.getId().equals(id)));
                source.playerDecks.values().forEach(zone -> zone.removeIf(card -> card.getId().equals(id)));
                source.removeFromExile(id);
                source.stack.removeIf(entry -> !entry.isCopy() && entry.getPhysicalCard() != null && entry.getPhysicalCard().getId().equals(id)
                        && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                        && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY);
                StackEntry resolving = source.pendingEffectResolutionEntry;
                if (resolving != null && !resolving.isCopy() && resolving.getPhysicalCard() != null && resolving.getPhysicalCard().getId().equals(id))
                    resolving.setSpellDispositionHandled(true);
                List<StackEntry> deferred = source.stack.stream().filter(entry -> !existing.contains(entry)).toList();
                source.suspendedZoneTriggers.addAll(deferred);
                source.stack.removeAll(deferred);
            });
            mutations.mutate(child, () -> child.pendingAncestorTransfers.remove(transfer));
        }
    }

    private void start(GameSession session, GameData parent) {
        if (parent.pendingEffectResolutionEntry == null) {
            throw new IllegalStateException("A subgame must retain the creating resolution");
        }
        GameData child = new GameData(UUID.randomUUID(), parent.gameName, parent.createdByUserId, parent.createdByUsername);
        child.simulation = parent.simulation;
        child.format = parent.format;
        child.playerIds.addAll(parent.playerIds);
        child.orderedPlayerIds.addAll(parent.orderedPlayerIds);
        child.playerNames.addAll(parent.playerNames);
        child.playerIdToName.putAll(parent.playerIdToName);
        child.playerDeckChoices.putAll(parent.playerDeckChoices);
        child.aiPlayerIds.addAll(parent.aiPlayerIds);
        child.alwaysOfferPriorityWindows = parent.alwaysOfferPriorityWindows;
        Map<UUID, List<Card>> decks = new LinkedHashMap<>();
        mutations.mutate(parent, () -> {
            for (UUID player : parent.orderedPlayerIds) {
                decks.put(player, new ArrayList<>(parent.playerDecks.get(player)));
                parent.playerDecks.get(player).clear();
                child.playerSideboards.put(player, new ArrayList<>(parent.playerSideboards.getOrDefault(player, List.of())));
                parent.playerSideboards.computeIfAbsent(player, ignored -> new ArrayList<>()).clear();
                child.playerAutoStopSteps.put(player, new HashSet<>(parent.playerAutoStopSteps.getOrDefault(player, Set.of())));
            }
            if (parent.planechase != null) {
                child.planechase = new PlanechaseState();
                child.planechase.deck.addAll(parent.planechase.deck);
                parent.planechase.deck.clear();
                Collections.shuffle(child.planechase.deck);
            }
            parent.subgameRequested = false;
            logs.append(parent, GameLog.text("A subgame begins."));
            mutations.emit(parent, new GameEventFact.SubgameStarted(parent.id, child.id));
        });
        session.push(child);
        if (!child.simulation) registry.register(child);
        mutations.mutate(child, () -> {
            setup.initializeSubgame(child, decks);
            emitActive(child);
        });
    }

    private void finish(GameSession session, GameData child) {
        GameData parent = session.frames().get(session.depth() - 1);
        if (child.gameResult == GameEventFact.GameResult.ABANDONED) {
            GameData root = session.root();
            mutations.mutate(root, () -> outcomes.abandon(root));
            for (GameData frame : session.frames()) if (!frame.simulation) registry.remove(frame.id);
            while (session.depth() > 0) session.pop();
            return;
        }
        Map<UUID, Card> cards = collectCards(child);
        Map<UUID, List<Card>> outside = new HashMap<>();
        List<Card> planar = new ArrayList<>();
        mutations.mutate(child, () -> {
            child.playerSideboards.forEach((owner, pool) -> outside.put(owner, new ArrayList<>(pool)));
            child.playerSideboards.clear();
            if (child.planechase != null) {
                planar.addAll(child.planechase.deck);
                child.planechase.faceUp.forEach(object -> planar.add(object.getCard()));
                child.planechase.deck.clear();
                child.planechase.faceUp.clear();
            }
        });
        session.pop();
        mutations.mutate(parent, () -> {
            for (Card card : cards.values()) {
                UUID owner = card.getOwnerId();
                if (owner == null) owner = findOwner(child, card);
                parent.playerDecks.get(owner).add(card);
            }
            parent.playerDecks.values().forEach(Collections::shuffle);
            outside.forEach((owner, pool) -> parent.playerSideboards.computeIfAbsent(owner, ignored -> new ArrayList<>()).addAll(pool));
            if (parent.planechase != null) {
                parent.planechase.deck.addAll(planar);
                Collections.shuffle(parent.planechase.deck);
            }
            parent.waitingForSubgame = false;
            StackEntry entry = parent.pendingEffectResolutionEntry;
            entry.setSubgameResult(new SubgameResult(child.id,
                    child.winnerPlayerId == null ? Set.of() : Set.of(child.winnerPlayerId)));
            logs.append(parent, GameLog.text(child.winnerPlayerId == null ? "The subgame ended in a draw."
                    : parent.playerIdToName.get(child.winnerPlayerId) + " won the subgame."));
            emitActive(parent);
            // Ancestor departures can request trigger choices. They belong after the creating spell.
            List<PendingMayAbility> ancestorMayAbilities = new ArrayList<>(parent.pendingMayAbilities);
            parent.pendingMayAbilities.clear();
            PendingInteraction ancestorChoice = parent.interaction.activeInteraction();
            if (ancestorChoice != null) parent.interaction.clearAwaitingInput();
            effects.resolveEffectsFrom(parent, entry, parent.pendingEffectResolutionIndex);
            parent.stack.addAll(parent.suspendedZoneTriggers);
            parent.suspendedZoneTriggers.clear();
            parent.pendingMayAbilities.addAll(ancestorMayAbilities);
            if (parent.status == GameStatus.RUNNING && !parent.waitingForSubgame) {
                if (ancestorChoice != null) {
                    if (parent.interaction.isAwaitingInput()) parent.queueInteraction(ancestorChoice);
                    else interactions.begin(parent, ancestorChoice);
                }
                completion.processMayAbilitiesThenAutoPass(parent);
            }
        });
        if (!child.simulation) registry.remove(child.id);
    }

    private void emitActive(GameData game) {
        GameContext context = game.session.context();
        mutations.emit(game, new GameEventFact.ActiveGameChanged(context.sessionId(), game.id,
                context.activationEpoch(), game.session.depth()), GameEventAudience.allPlayers());
        mutations.invalidateAllPlayerViews(game);
    }

    /** Enumerates physical cards, including spells retained by asynchronous resolution. */
    public Map<UUID, Card> collectCards(GameData game) {
        Map<UUID, Card> cards = new LinkedHashMap<>();
        java.util.function.Consumer<Card> add = card -> {
            if (card != null && !card.isToken()) cards.putIfAbsent(card.getId(), card);
        };
        game.subgameCards.values().forEach(add);
        game.playerDecks.values().forEach(zone -> zone.forEach(add));
        game.playerHands.values().forEach(zone -> zone.forEach(add));
        game.playerGraveyards.values().forEach(zone -> zone.forEach(add));
        game.playerIds.forEach(owner -> game.getPlayerExiledCards(owner).forEach(add));
        game.playerBattlefields.values().forEach(zone -> zone.forEach(permanent -> permanent.cardsLeavingBattlefield().forEach(add)));
        game.phasedOutPermanents.values().forEach(zone -> zone.forEach(permanent -> permanent.cardsLeavingBattlefield().forEach(add)));
        game.stack.forEach(entry -> addSpell(entry, add));
        if (game.pendingEffectResolutionEntry != null) addSpell(game.pendingEffectResolutionEntry, add);
        game.playerSideboards.values().forEach(zone -> zone.forEach(card -> cards.remove(card.getId())));
        game.playerCommandZones.values().forEach(zone -> zone.forEach(card -> cards.remove(card.getId())));
        return cards;
    }

    private void addSpell(StackEntry entry, java.util.function.Consumer<Card> add) {
        if (!entry.isCopy() && !entry.isSpellDispositionHandled()
                && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY) add.accept(entry.getPhysicalCard());
    }

    private UUID findOwner(GameData game, Card card) {
        for (UUID player : game.orderedPlayerIds) {
            if (game.playerDecks.getOrDefault(player, List.of()).contains(card)
                    || game.playerHands.getOrDefault(player, List.of()).contains(card)
                    || game.playerGraveyards.getOrDefault(player, List.of()).contains(card)
                    || game.getPlayerExiledCards(player).contains(card)) return player;
        }
        for (var zone : java.util.stream.Stream.concat(game.playerBattlefields.entrySet().stream(),
                game.phasedOutPermanents.entrySet().stream()).toList()) {
            for (Permanent permanent : zone.getValue()) {
                if (permanent.cardsLeavingBattlefield().contains(card)) return game.stolenCreatures.getOrDefault(permanent.getId(), zone.getKey());
            }
        }
        for (StackEntry entry : game.stack) if (entry.getPhysicalCard() == card) return entry.getOwnerId();
        if (game.pendingEffectResolutionEntry != null && game.pendingEffectResolutionEntry.getPhysicalCard() == card)
            return game.pendingEffectResolutionEntry.getOwnerId();
        throw new IllegalStateException("Missing owner for returned subgame card");
    }
}
