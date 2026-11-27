package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

import com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardGrantFreePlayUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.FlashforwardCast;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithHandsRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.WebSlingingEffect;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.DelayedTriggerPaymentView;
import com.github.laxika.magicalvibes.networking.model.GameLogEntryView;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.GameLogViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.effect.GrantedAbilityViewFactory;
import com.github.laxika.magicalvibes.service.effect.GrantedEffectAttribution;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Pure player-specific projection of authoritative game state into immutable networking views.
 *
 * <p>This factory performs no delivery, serialization, or domain mutation. The same methods serve
 * event-driven human output and explicit join/reconnect projection.
 */
@Component
@RequiredArgsConstructor
public class GameViewProjectionFactory {
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.planar.PlanechaseViewService planarViews;

    private final CardViewFactory cardViewFactory;
    private final GameLogViewFactory gameLogViewFactory;
    private final PermanentViewFactory permanentViewFactory;
    private final StackEntryViewFactory stackEntryViewFactory;
    private final GameQueryService gameQueryService;
    private final ValidTargetService validTargetService;
    private final CastingCostService castingCostService;
    private final CastingPermissionService castingPermissionService;
    private final GrantedAbilityViewFactory grantedAbilityViewFactory;
    private final GameActionAvailabilityService actionAvailabilityService;
    private final SecretMissionService secretMissionService;

    public Map<UUID, GameStateMessage> createGameStateMessages(
            GameData gameData,
            List<GameLogEntryView> newLogEntries,
            Collection<UUID> recipientIds) {
        return gameQueryService.withQueryScope(gameData,
                () -> createGameStateMessagesWithinQueryScope(
                        gameData, newLogEntries, recipientIds));
    }

    private Map<UUID, GameStateMessage> createGameStateMessagesWithinQueryScope(
            GameData gameData,
            List<GameLogEntryView> newLogEntries,
            Collection<UUID> recipientIds) {
        List<List<PermanentView>> battlefields = getBattlefields(gameData);
        Map<UUID, FaceDownReveal> faceDownReveals = collectFaceDownReveals(gameData);
        List<StackEntryView> stack = getStackViews(gameData);
        List<Integer> deckSizes = getDeckSizes(gameData);
        List<Integer> handSizes = getHandSizes(gameData);
        List<Integer> lifeTotals = getLifeTotals(gameData);
        List<Integer> poisonCounters = getPoisonCounters(gameData);
        List<Integer> energyCounters = getEnergyCounters(gameData);
        List<Integer> radCounters = getRadCounters(gameData);
        List<Integer> speeds = getSpeeds(gameData);
        UUID priorityPlayerId = gameData.interaction.isAwaitingInput() ? null : gameQueryService.getPriorityPlayerId(gameData);

        Map<UUID, GameStateMessage> messages = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!recipientIds.contains(playerId)) {
                continue;
            }
            List<List<CardView>> graveyards = getGraveyardViews(gameData, playerId);
            List<CardSubtype> playerGranted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(gameData, playerId);
            List<CardView> hand = gameData.playerHands.getOrDefault(playerId, List.of())
                    .stream().map(c -> createHandCardView(gameData, playerId, c, playerGranted)).toList();
            List<CardView> opponentHand = getRevealedOpponentHand(gameData, playerId);
            int mulliganCount = gameData.mulliganCounts.getOrDefault(playerId, 0);
            Map<String, Integer> manaPool = getManaPool(gameData, playerId);
            List<TurnStep> autoStopSteps = gameData.playerAutoStopSteps.containsKey(playerId)
                    ? new ArrayList<>(gameData.playerAutoStopSteps.get(playerId))
                    : List.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
            List<Integer> playableCardIndices = actionAvailabilityService.getPlayableCardIndices(gameData, playerId);
            List<Integer> playableForetellIndices =
                    actionAvailabilityService.getPlayableForetellIndices(gameData, playerId);
            List<Integer> potentialPlayableCardIndices =
                    actionAvailabilityService.getPotentialPlayableCardIndices(
                            gameData, playerId, playableCardIndices);
            int potentialManaTotal = actionAvailabilityService.getPotentialManaTotal(gameData, playerId);
            Map<UUID, List<Integer>> potentialPayableAbilityIndices =
                    actionAvailabilityService.getPotentialPayableAbilityIndices(gameData, playerId);
            List<Integer> playableGraveyardLandIndices =
                    actionAvailabilityService.getPlayableGraveyardLandIndices(gameData, playerId);
            List<CardView> playableExileCards = getPlayableExileCards(gameData, playerId);
            List<Integer> playableFlashbackIndices =
                    actionAvailabilityService.getPlayableFlashbackIndices(gameData, playerId);
            List<List<CardView>> revealedLibraryTopCards = getRevealedLibraryTopCards(gameData, playerId);
            List<CardView> playableLibraryTopCards = getPlayableLibraryTopCards(gameData, playerId);
            int searchTaxCost = getSearchTaxCost(gameData, playerId);

            // Mindslaver: controller sees the controlled player's hand and playable indices
            if (gameData.mindControllerPlayerId() != null && playerId.equals(gameData.mindControllerPlayerId())) {
                    UUID controlledId = gameData.mindControlledPlayerId();
                if (controlledId != null) {
                    List<CardSubtype> controlledGranted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(gameData, controlledId);
                    opponentHand = gameData.playerHands.getOrDefault(controlledId, List.of())
                            .stream().map(c -> createHandCardView(gameData, controlledId, c, controlledGranted)).toList();
                    playableCardIndices =
                            actionAvailabilityService.getPlayableCardIndices(gameData, controlledId);
                    playableForetellIndices =
                            actionAvailabilityService.getPlayableForetellIndices(gameData, controlledId);
                    potentialPlayableCardIndices =
                            actionAvailabilityService.getPotentialPlayableCardIndices(
                                    gameData, controlledId, playableCardIndices);
                    potentialManaTotal =
                            actionAvailabilityService.getPotentialManaTotal(gameData, controlledId);
                    potentialPayableAbilityIndices =
                            actionAvailabilityService.getPotentialPayableAbilityIndices(
                                    gameData, controlledId);
                    playableGraveyardLandIndices =
                            actionAvailabilityService.getPlayableGraveyardLandIndices(
                                    gameData, controlledId);
                    playableExileCards = getPlayableExileCards(gameData, controlledId);
                    playableFlashbackIndices =
                            actionAvailabilityService.getPlayableFlashbackIndices(
                                    gameData, controlledId);
                    playableLibraryTopCards = getPlayableLibraryTopCards(gameData, controlledId);
                }
            }

            List<Integer> playableOpponentHandIndices = getPlayableOpponentHandIndices(gameData, playerId);

            Map<UUID, CardView> permanentReveals = collectFaceDownPermanentReveals(gameData, playerId);
            messages.put(playerId, new GameStateMessage(
                    MessageType.GAME_STATE, gameData.status, gameData.activePlayerId, gameData.turnNumber,
                    gameData.currentStep, priorityPlayerId,
                    applyFaceDownPermanentReveals(
                            applyFaceDownReveals(battlefields, faceDownReveals, playerId),
                            permanentReveals),
                    getStackViewsFor(gameData, stack, playerId, permanentReveals.keySet()), graveyards, deckSizes, handSizes, lifeTotals, poisonCounters, energyCounters,
                    radCounters,
                    hand, opponentHand, mulliganCount, manaPool, autoStopSteps, playableCardIndices,
                    playableForetellIndices,
                    playableGraveyardLandIndices, playableExileCards, newLogEntries, searchTaxCost,
                    gameData.mindControlledPlayerId(), revealedLibraryTopCards, playableFlashbackIndices,
                    playableLibraryTopCards, potentialPlayableCardIndices, potentialManaTotal,
                    potentialPayableAbilityIndices, speeds, gameData.dayNight,
                    gameData.planechase == null ? null : planarViews.create(gameData, playerId),
                    gameData.monarchPlayerId, commanderView(gameData, playerId),
                    gameData.exiledCards.stream()
                            .filter(entry -> FaceDownVisibility.mayLookAtExiledCard(gameData, entry.card().getId(), playerId))
                            .map(entry -> cardViewFactory.create(entry.card()))
                            .toList(),
                    List.of(),
                    getDelayedTriggerPayments(gameData, playerId),
                    playableOpponentHandIndices
            ));
        }
        return Collections.unmodifiableMap(messages);
    }

    /**
     * Indices of the revealed opponent hand the viewer may play from right now (Sen Triplets). The revealed hand
     * is the first opponent's, so the indices are only offered when that opponent is the one whose hand the viewer
     * may play from, and not while the viewer acts for a Mindslaver-controlled player.
     */
    private List<Integer> getPlayableOpponentHandIndices(GameData gameData, UUID playerId) {
        UUID handOwnerId = gameData.handPlayableBy(playerId);
        if (handOwnerId == null
                || playerId.equals(gameData.mindControllerPlayerId())
                || !handOwnerId.equals(gameData.orderedPlayerIds.stream()
                        .filter(id -> !id.equals(playerId)).findFirst().orElse(null))) {
            return List.of();
        }
        return actionAvailabilityService.getPlayableIndicesFromHandOf(gameData, playerId, handOwnerId);
    }

    /** The stack as everybody may see it, with no face-down card's identity (CR 708.2, 708.4). */
    List<StackEntryView> getStackViews(GameData gameData) {
        return gameData.stack.stream().map(entry -> {
            List<CardSubtype> granted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(
                    gameData, entry.getControllerId());
            if (FaceDownVisibility.maySeeOnStack(gameData, entry, null)) {
                return stackEntryViewFactory.create(entry, granted);
            }
            return entry.isCastFaceDown()
                    ? stackEntryViewFactory.createFaceDownSpell(entry, granted)
                    : stackEntryViewFactory.createFaceDownAbility(entry,
                            FaceDownVisibility.faceDownSource(gameData, entry),
                            FaceDownVisibility.descriptionShownTo(gameData, entry, null));
        }).toList();
    }

    /**
     * {@code publicViews} with the face-down cards {@code viewerId} may look at shown to them (CR 708.5).
     *
     * @param revealedPermanentIds the face-down permanents an effect lets the viewer look at
     */
    List<StackEntryView> getStackViewsFor(GameData gameData, List<StackEntryView> publicViews, UUID viewerId,
                                          Set<UUID> revealedPermanentIds) {
        List<StackEntryView> views = new ArrayList<>(publicViews);
        for (int i = 0; i < views.size(); i++) {
            StackEntry entry = gameData.stack.get(i);
            if (FaceDownVisibility.maySeeOnStack(gameData, entry, null)) {
                continue;
            }
            Permanent source = FaceDownVisibility.faceDownSource(gameData, entry);
            if (FaceDownVisibility.maySeeOnStack(gameData, entry, viewerId)
                    || source != null && revealedPermanentIds.contains(source.getId())) {
                views.set(i, stackEntryViewFactory.create(entry,
                        gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(
                                gameData, entry.getControllerId())));
            }
        }
        return List.copyOf(views);
    }

    List<List<PermanentView>> getBattlefields(GameData data) {
        List<List<PermanentView>> battlefields = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            if (bf == null) {
                battlefields.add(new ArrayList<>());
            } else {
                List<PermanentView> views = new ArrayList<>();
                for (Permanent p : bf) {
                    GameQueryService.ExplainedBonus explained = gameQueryService.explainStaticBonus(data, p);
                    GameQueryService.StaticBonus bonus = explained.bonus();
                    // Compute adjusted bonus P/T to account for static base P/T overrides (e.g. Deep Freeze)
                    int adjustedBonusPower = gameQueryService.getEffectivePower(p, bonus) - p.getEffectivePower();
                    int adjustedBonusToughness = gameQueryService.getEffectiveToughness(p, bonus) - p.getEffectiveToughness();
                    List<ActivatedAbility> allGrantedAbilities = new ArrayList<>(bonus.grantedActivatedAbilities());
                    // A basic land type's mana ability (CR 305.6) comes last, as in the engine's ability list.
                    List<ActivatedAbility> present = new ArrayList<>(p.getCard().getActivatedAbilities(Zone.BATTLEFIELD));
                    present.addAll(allGrantedAbilities);
                    allGrantedAbilities.addAll(gameQueryService.missingIntrinsicManaAbilities(data, p, present));
                    List<Card> faceUpExiledWith = new ArrayList<>();
                    int faceDownExiledCount = 0;
                    for (ExiledCardEntry exiledWith : data.getExiledWithPermanentEntries(p.getId(), p.getCard().getId())) {
                        if (exiledWith.faceDown()) {
                            faceDownExiledCount++;
                        } else {
                            faceUpExiledWith.add(exiledWith.card());
                        }
                    }
                    Set<CardSubtype> removedSubtypes = new HashSet<>(p.getCard().getSubtypes());
                    removedSubtypes.removeAll(gameQueryService.getEffectiveSubtypes(data, p));
                    PermanentView view = permanentViewFactory.create(p, adjustedBonusPower, adjustedBonusToughness, bonus.keywords(), bonus.animatedCreature(), allGrantedAbilities, bonus.grantedColors(), bonus.grantedSubtypes(), bonus.grantedCardTypes(), bonus.colorOverriding(), bonus.subtypeOverriding(), bonus.landSubtypeOverriding(), bonus.cardTypeOverriding(), bonus.removedKeywords(), bonus.losesAllAbilities(), bonus.losesAllNonManaAbilities(), bonus.grantedSupertypes(), explained.lines(), faceUpExiledWith, faceDownExiledCount, bonus.name(), removedSubtypes, data.regenerationShieldCount(p.getId()), data.lostActivatedAbilities(p.getId()), data.textReplacements(p.getId()));
                    List<GrantedEffectAttribution> attributions =
                            new ArrayList<>(explained.grantedEffectAttributions());
                    attributions.addAll(gameQueryService.attachedRestrictionAttributions(data, p));
                    views.add(view.withGrantedAbilities(grantedAbilityViewFactory.create(
                            data, p, bonus, attributions,
                            data.hasFloatingEffectOnPermanent(p.getId(), CantBeBlockedEffect.class))));
                }
                battlefields.add(views);
            }
        }
        return battlefields;
    }

    /** Face-down exiled cards of one permanent, revealed only to the viewer controlling it. */
    record FaceDownReveal(UUID viewerId, List<CardView> cards,
                          Map<UUID, List<CardView>> cardsByViewer) {
        FaceDownReveal(UUID viewerId, List<CardView> cards) {
            this(viewerId, cards, Map.of(viewerId, cards));
        }

        List<CardView> cardsFor(UUID viewerId) {
            return cardsByViewer.getOrDefault(viewerId, List.of());
        }
    }

    /**
     * Face-down exiled cards (hideaway lands, Grimoire Thief, ...) keyed by their permanent's id, shown to the
     * players {@link FaceDownVisibility#exileViewers} names. The shared battlefield views carry only a card-back
     * count for these; the actual cards are swapped into each viewer's copy by {@link #applyFaceDownReveals}.
     */
    Map<UUID, FaceDownReveal> collectFaceDownReveals(GameData data) {
        Map<UUID, FaceDownReveal> reveals = new HashMap<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                Map<UUID, List<CardView>> cardsByViewer = new LinkedHashMap<>();
                for (ExiledCardEntry entry : data.getExiledWithPermanentEntries(p.getId(), p.getCard().getId())) {
                    if (!entry.faceDown()) continue;
                    for (UUID viewer : FaceDownVisibility.exileViewers(data, p, pid, entry)) {
                        cardsByViewer.computeIfAbsent(viewer, ignored -> new ArrayList<>())
                                .add(cardViewFactory.create(entry.card()));
                    }
                }
                if (!cardsByViewer.isEmpty()) {
                    UUID legacyViewerId = cardsByViewer.size() == 1
                            ? cardsByViewer.keySet().iterator().next() : null;
                    List<CardView> legacyCards = legacyViewerId == null
                            ? List.of() : cardsByViewer.get(legacyViewerId);
                    reveals.put(p.getId(), new FaceDownReveal(legacyViewerId, legacyCards,
                            cardsByViewer.entrySet().stream()
                                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                                            Map.Entry::getKey, e -> List.copyOf(e.getValue())))));
                }
            }
        }
        return reveals;
    }

    /** The battlefields as seen by {@code viewerId}: permanents they control get their face-down
     *  exiled cards revealed; everything else is shared untouched. */
    List<List<PermanentView>> applyFaceDownReveals(List<List<PermanentView>> battlefields,
                                                   Map<UUID, FaceDownReveal> reveals, UUID viewerId) {
        if (reveals.isEmpty()) {
            return battlefields;
        }
        List<List<PermanentView>> result = new ArrayList<>(battlefields.size());
        for (List<PermanentView> side : battlefields) {
            List<PermanentView> viewerSide = new ArrayList<>(side.size());
            for (PermanentView view : side) {
                FaceDownReveal reveal = reveals.get(view.id());
                List<CardView> revealedCards = reveal == null ? List.of() : reveal.cardsFor(viewerId);
                viewerSide.add(revealedCards.isEmpty()
                        ? view
                        : view.withFaceDownRevealed(revealedCards,
                        Math.max(0, view.faceDownExiledCount() - revealedCards.size())));
            }
            result.add(viewerSide);
        }
        return result;
    }

    /** Face-down battlefield creatures revealed to a player by a static or turn-scoped permission. */
    Map<UUID, CardView> collectFaceDownPermanentReveals(GameData data, UUID viewerId) {
        if (!gameQueryService.mayLookAtOpposingFaceDownCreatures(data, viewerId)) {
            return Map.of();
        }

        Map<UUID, CardView> reveals = new HashMap<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> battlefield = data.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (!permanent.isFaceDown()
                        || !gameQueryService.isCreature(data, permanent)
                        || viewerId.equals(gameQueryService.findPermanentController(data, permanent.getId()))) {
                    continue;
                }
                reveals.put(permanent.getId(), cardViewFactory.create(permanent.getCard()));
            }
        }
        return reveals;
    }

    List<List<PermanentView>> applyFaceDownPermanentReveals(
            List<List<PermanentView>> battlefields, Map<UUID, CardView> reveals) {
        if (reveals.isEmpty()) {
            return battlefields;
        }
        return battlefields.stream()
                .map(side -> side.stream()
                        .map(view -> reveals.containsKey(view.id())
                                ? view.withCard(reveals.get(view.id()))
                                : view)
                        .toList())
                .toList();
    }

    List<List<CardView>> getGraveyardViews(GameData data) {
        return getGraveyardViews(data, null);
    }

    List<List<CardView>> getGraveyardViews(GameData data, UUID viewerId) {
        List<List<CardView>> graveyards = new ArrayList<>();
        boolean graveyardAbilitiesLost = gameQueryService.graveyardCardsHaveLostAllAbilities(data);
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> gy = data.playerGraveyards.get(pid);
            List<CardSubtype> granted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(data, pid);
            graveyards.add(gy != null
                    ? gy.stream().map(c -> {
                        List<CardSubtype> cardGranted = new ArrayList<>(granted);
                        cardGranted.addAll(gameQueryService.computeGrantedGraveyardSubtypesForOwnedCreatureCard(
                                data, pid, c));
                        var filteredPermission = viewerId != null && viewerId.equals(pid)
                                ? castingPermissionService.findFilteredGraveyardPermission(data, pid, c)
                                : Optional.<CastingPermissionService.FilteredGraveyardPermission>empty();
                        var targetedPermission = viewerId != null && viewerId.equals(pid)
                                ? data.graveyardCardCastPermission(c.getId(), pid).orElse(null)
                                : null;
                        int graveyardCastExileCount = targetedPermission != null
                                ? targetedPermission.additionalGraveyardExileCount()
                                : filteredPermission.map(permission -> permission.permission().additionalGraveyardExileCount())
                                        .orElse(0);
                        String graveyardCastExileLabel = targetedPermission != null
                                ? targetedPermission.additionalGraveyardExileCount() > 0 ? "other cards" : null
                                : filteredPermission.map(permission -> permission.permission().additionalGraveyardExileLabel())
                                        .orElse(null);
                        return cardViewFactory.createForGraveyard(c, cardGranted,
                                gameQueryService.computeGrantedGraveyardAbilitiesForOwnedCard(data, pid, c),
                                graveyardAbilitiesLost,
                                graveyardCastExileCount, graveyardCastExileLabel);
                    }).toList()
                    : new ArrayList<>());
        }
        return graveyards;
    }

    List<Integer> getHandSizes(GameData data) {
        List<Integer> sizes = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> hand = data.playerHands.get(pid);
            sizes.add(hand != null ? hand.size() : 0);
        }
        return sizes;
    }

    List<CardView> getRevealedOpponentHand(GameData gameData, UUID playerId) {
        // Mindslaver: controller always sees the controlled player's hand
        // (handled separately while building each player-specific state projection)

        // Sen Triplets: the targeted player plays with their hand revealed this turn.
        boolean fullHandRevealed = gameData.handRevealedToSenTripletsController(playerId);
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (!opponentId.equals(playerId)) {
                List<CardSubtype> granted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(gameData, opponentId);
                List<Card> opponentHand = gameData.playerHands.getOrDefault(opponentId, List.of());
                boolean revealEntireOpponentHand = fullHandRevealed || playsWithHandRevealed(gameData, opponentId);
                return opponentHand.stream()
                        .filter(card -> revealEntireOpponentHand
                                || gameData.isCardRevealedInHand(card.getId(), opponentId))
                        .map(c -> cardViewFactory.create(c, granted, List.of(),
                                gameQueryService.computeGrantedHandAbilitiesForOwnedCard(gameData, opponentId, c)))
                        .toList();
            }
        }
        return List.of();
    }

    /**
     * Whether {@code playerId} plays with their hand revealed, to every player (CR 701.20a): an active static
     * {@link PlayWithHandsRevealedEffect} names them ("your opponents play with their hands revealed", Telepathy) (none of a face-down or ability-less permanent's, CR 708.2a, 613.1f), or one is floated on them
     * (Truth or Dare, Stromgald Spy).
     */
    private boolean playsWithHandRevealed(GameData gameData, UUID playerId) {
        if (gameData.hasFloatingEffectOnPlayer(playerId, PlayWithHandsRevealedEffect.class)) {
            return true;
        }
        for (var active : gameQueryService.activeStaticEffects(gameData, PlayWithHandsRevealedEffect.class)) {
            if (SubjectResolutionService.namesPlayer(gameData, active.source(), active.controllerId(), playerId,
                    active.effect().players())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the player is deciding on a pending "you may exile it face down" for the top card of
     * their own library that they are allowed to look at (Urianger Augurelt's Draw Arcanum), so the
     * card must be shown to them before they choose.
     */
    private boolean isChoosingWhetherToExileLookedAtTopCard(GameData data, UUID playerId) {
        if (data.pendingMayAbilities.isEmpty()) {
            return false;
        }
        PendingMayAbility pending = data.pendingMayAbilities.getFirst();
        UUID choosingPlayerId = pending.choicePlayerId() != null
                ? pending.choicePlayerId() : pending.controllerId();
        return playerId.equals(choosingPlayerId) && pending.effects().stream().anyMatch(effect ->
                effect instanceof com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect exile
                        && exile.faceDown() && exile.exilerMayLookWhileExiled()
                        && exile.player() instanceof com.github.laxika.magicalvibes.model.effect.PlayerSubject.You);
    }

    List<List<CardView>> getRevealedLibraryTopCards(GameData data, UUID viewerId) {
        // Determine which players have their top card visible to the viewer.
        // PlayWithTopCardRevealedEffect = publicly revealed to all players.
        // LookAtTopCardOfOwnLibraryEffect / AllowCastFromTopOfLibraryEffect and temporary
        // top-library permissions = private, only visible to the controller.
        Set<UUID> revealedPlayerIds = new HashSet<>();
        if (data.planechase != null) {
            for (var planar : data.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof PlayWithTopCardRevealedEffect topCardRevealed
                            && topCardRevealed.allPlayers()) {
                        revealedPlayerIds.addAll(data.orderedPlayerIds);
                    }
                }
            }
        }
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> deck = data.playerDecks.get(pid);
            if (pid.equals(viewerId) && castingPermissionService.mayLookAtOwnLibraryTop(data, pid)) {
                revealedPlayerIds.add(pid);
            }
            // A card whose own static ability lets it be cast from the top of its owner's library also lets its
            // owner look at it there (Chittering Illuminator, Pearl Lake Warden; one ability, CR 113.6b).
            if (pid.equals(viewerId) && deck != null && !deck.isEmpty()
                    && deck.getFirst().getStaticEffects().stream()
                    .anyMatch(AllowCastSourceCardFromTopOfLibraryEffect.class::isInstance)) {
                revealedPlayerIds.add(pid);
            }
            if (pid.equals(viewerId) && isChoosingWhetherToExileLookedAtTopCard(data, pid)) {
                revealedPlayerIds.add(pid);
            }
            if (deck != null && !deck.isEmpty() && data.floatingEffectsOnPlayer(pid,
                    RevealTopCardGrantFreePlayUntilEndOfTurnEffect.class).stream().anyMatch(fe ->
                    deck.getFirst().getId().equals(
                            ((RevealTopCardGrantFreePlayUntilEndOfTurnEffect) fe.effect()).cardId()))) {
                revealedPlayerIds.add(pid);
            }
            if (castingPermissionService.hasLibraryTopPermission(data, viewerId, pid)
                    && deck != null && !deck.isEmpty()) {
                revealedPlayerIds.add(pid);
            }
            if (pid.equals(viewerId) && castingPermissionService.canPlayLandsFromTopOfLibrary(data, pid)) {
                revealedPlayerIds.add(pid);
            }

            for (GameQueryService.ActiveStaticEffect<CardEffect> active
                    : gameQueryService.activeStaticEffects(data, pid, CardEffect.class)) {
                Permanent perm = active.source();
                if (perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(data, perm)) continue;
                CardEffect effect = active.effect();
                if (effect instanceof PlayWithTopCardRevealedEffect topCardRevealed) {
                    // Public: visible to all
                    if (topCardRevealed.allPlayers()) {
                        revealedPlayerIds.addAll(data.orderedPlayerIds);
                    } else {
                        revealedPlayerIds.add(pid);
                    }
                } else if (pid.equals(viewerId) && effect instanceof AllowCastFromTopOfLibraryEffect allow
                        && allow.filter() != null && deck != null && !deck.isEmpty()
                        && !castingPermissionService.canCastFromTopOfLibrary(data, pid, deck.getFirst())
                        && !castingPermissionService.canCastFromTopOfLibraryAsAdventure(data, pid, deck.getFirst())) {
                    // A filtered permission only exposes a top card it lets the player cast.
                    continue;
                } else if (pid.equals(viewerId) &&
                        (effect instanceof LookAtTopCardOfOwnLibraryEffect
                                || effect instanceof AllowCastFromTopOfLibraryEffect)) {
                    // Private: only visible to the controller
                    revealedPlayerIds.add(pid);
                }
            }
        }

        List<List<CardView>> result = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            if (revealedPlayerIds.contains(pid)) {
                List<Card> deck = data.playerDecks.get(pid);
                if (deck != null && !deck.isEmpty()) {
                    result.add(List.of(cardViewFactory.create(deck.getFirst())));
                } else {
                    result.add(List.of());
                }
            } else {
                result.add(List.of());
            }
        }
        return result;
    }

    List<Integer> getDeckSizes(GameData data) {
        List<Integer> sizes = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> deck = data.playerDecks.get(pid);
            sizes.add(deck != null ? deck.size() : 0);
        }
        return sizes;
    }

    Map<String, Integer> getManaPool(GameData data, UUID playerId) {
        if (playerId == null) {
            return new ManaPool().toMap();
        }
        ManaPool pool = data.playerManaPools.get(playerId);
        return pool != null ? pool.toMap() : new ManaPool().toMap();
    }

    List<Integer> getLifeTotals(GameData gameData) {
        List<Integer> totals = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            totals.add(gameData.getLife(pid));
        }
        return totals;
    }

    List<Integer> getPoisonCounters(GameData gameData) {
        List<Integer> counters = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            counters.add(gameData.playerPoisonCounters.getOrDefault(pid, 0));
        }
        return counters;
    }

    List<Integer> getEnergyCounters(GameData gameData) {
        List<Integer> counters = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            counters.add(gameData.playerEnergyCounters.getOrDefault(pid, 0));
        }
        return counters;
    }

    List<Integer> getRadCounters(GameData gameData) {
        List<Integer> counters = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            counters.add(gameData.playerRadCounters.getOrDefault(pid, 0));
        }
        return counters;
    }

    List<Integer> getSpeeds(GameData gameData) {
        List<Integer> speeds = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            speeds.add(gameData.playerSpeeds.getOrDefault(pid, 0));
        }
        return speeds;
    }

    List<CardView> getPlayableExileCards(GameData gameData, UUID playerId) {
        List<CardView> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        boolean cantCastDueToAttackExile = castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId);
        Set<CardType> restrictedSpellTypes = castingPermissionService.getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = castingPermissionService.getForbiddenCardNames(gameData, playerId);

        // Collect card IDs playable via static exile permissions
        Set<UUID> castableFromExileWithSource = castingPermissionService.getCastableExiledCardIds(gameData, playerId);
        Set<UUID> anyManaTypeIds = castingPermissionService.getAnyManaTypeExiledCardIds(gameData, playerId);
        Set<UUID> snowManaAsAnyColorIds = gameData.exiledCards.stream()
                .filter(entry -> castingPermissionService.hasSnowManaAsAnyColorPermission(
                        gameData, playerId, entry.card().getId()))
                .map(entry -> entry.card().getId())
                .collect(Collectors.toSet());

        // Include player's own exiled cards plus cards from any exile zone castable via source effect
        List<Card> exiledCards = new ArrayList<>(gameData.getPlayerExiledCards(playerId));
        Set<UUID> alreadyIncluded = new HashSet<>();
        for (Card c : exiledCards) alreadyIncluded.add(c.getId());
        for (Card card : com.github.laxika.magicalvibes.service.OutsideGameCards.view(gameData, playerId)) {
            if (gameData.mayPlayFromOutsideGame(card.getId())
                    && alreadyIncluded.add(card.getId())) {
                exiledCards.add(card);
            }
        }
        for (UUID cardId : castableFromExileWithSource) {
            if (!alreadyIncluded.contains(cardId)) {
                ExiledCardEntry entry = gameData.findExiledCard(cardId);
                if (entry != null) {
                    exiledCards.add(entry.card());
                    alreadyIncluded.add(cardId);
                }
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (!alreadyIncluded.contains(entry.card().getId())
                    && castingPermissionService.hasExilePlayPermission(gameData, playerId, entry.card().getId())) {
                exiledCards.add(entry.card());
                alreadyIncluded.add(entry.card().getId());
            }
        }
        if (exiledCards.isEmpty()) {
            return playable;
        }

        ManaPool pool = gameData.playerManaPools.get(playerId);

        for (Card card : exiledCards) {
            ManaPool cardPool = pool;
            if (pool.getExiledSpellOnlyManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteExiledSpellOnlyMana();
            }
            if (snowManaAsAnyColorIds.contains(card.getId())) {
                cardPool = new ManaPool(cardPool);
                cardPool.setSnowManaSpendableAsAnyColor(true);
            }
            if (!card.hasType(CardType.CREATURE) && cardPool.getNoncreatureSpellOnlyManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteNoncreatureSpellOnlyMana();
            }
            if (card.hasType(CardType.CREATURE) && card.getCardText() == null && card.getKeywords().isEmpty()
                    && cardPool.getCreatureSpellWithoutAbilitiesOnlyManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteCreatureSpellWithoutAbilitiesOnlyMana();
            }
            if (card.hasType(CardType.CREATURE)
                    && gameQueryService.getEffectiveCardColors(gameData, card).contains(CardColor.BLUE)
                    && cardPool.getBlueCreatureSpellOnlyManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteBlueCreatureSpellOnlyMana();
            }
            if (gameQueryService.getEffectiveCardColors(gameData, card).size() == 3
                    && cardPool.getExactlyThreeColorSpellOnlyManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteExactlyThreeColorSpellOnlyMana();
            }
            if ((card.hasType(CardType.CREATURE) || card.hasType(CardType.ENCHANTMENT))
                    && cardPool.getCreatureOrEnchantmentSpellOnlyManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteCreatureOrEnchantmentSpellOnlyMana();
            }
            if (card.hasType(CardType.ENCHANTMENT)
                    && cardPool.getEnchantmentOrRoomUnlockOrTurnFaceUpManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteEnchantmentOrRoomUnlockOrTurnFaceUpMana();
            }
            if (card.hasType(CardType.ENCHANTMENT)
                    && card.getSubtypes().contains(CardSubtype.ROOM)
                    && cardPool.getRoomSpellsOrUnlocksManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteRoomSpellsOrUnlocksMana();
            }
            ExiledCardEntry exiledEntry = gameData.findExiledCard(card.getId());
            boolean fromOutsideGame = exiledEntry == null
                    && gameData.mayPlayFromOutsideGame(card.getId());
            ForetellCast foretellCast = card.getCastingOption(ForetellCast.class).orElse(null);
            ManaCost foretoldCost = gameData.foretoldCardCosts.get(card.getId());
            if (foretoldCost == null && foretellCast != null && foretellCast.manaCostString() != null) {
                foretoldCost = new ManaCost(foretellCast.manaCostString());
            }
            boolean foretellPermission = foretoldCost != null
                    && gameData.foretoldCardIds.contains(card.getId())
                    && exiledEntry != null
                    && playerId.equals(exiledEntry.exilerId())
                    && exiledEntry.exiledTurnNumber() < gameData.turnNumber;
            boolean hasSuspendedExileAbility = gameData.isSuspended(card.getId())
                    && !card.getActivatedAbilities(Zone.EXILE).isEmpty();
            if (hasSuspendedExileAbility) {
                playable.add(cardViewFactory.create(card));
                continue;
            }

            boolean hasPermission = fromOutsideGame || castingPermissionService.hasExilePlayPermission(gameData, playerId, card.getId())
                    || castableFromExileWithSource.contains(card.getId())
                    || foretellPermission;
            FlashforwardCast flashforwardCast = !fromOutsideGame
                    ? card.getCastingOption(FlashforwardCast.class).orElse(null) : null;
            boolean hasFlashforward = flashforwardCast != null;
            boolean hasExileCast = card.getCastingOption(ExileCast.class).isPresent() || hasFlashforward;
            boolean hasExileAbility = !card.getActivatedAbilities(Zone.EXILE).isEmpty();
            if (hasExileAbility && !hasPermission && !hasExileCast) {
                if (canActivateExileAbilityNow(gameData, playerId, card, pool, isActivePlayer, isMainPhase, stackEmpty)) {
                    playable.add(exileCardView(gameData, playerId, card));
                }
                continue;
            }
            if (!hasPermission && !hasExileCast) {
                continue;
            }

            if (card.hasType(CardType.LAND)) {
                if (isActivePlayer && isMainPhase && landsPlayed < gameQueryService.getMaxLandsThisTurn(gameData, playerId) && stackEmpty
                        && !gameData.isRestricted(playerId, PlayerRestriction.Kind.PLAY_LANDS)
                        && !castingPermissionService.isLandPlayRestricted(gameData, playerId)
                        && !castingPermissionService.isLandPlayForbiddenByChosenName(gameData, card)) {
                    playable.add(exileCardView(gameData, playerId, card));
                }
                continue;
            }

            if (card.getManaCost() == null
                    || castingPermissionService.isSpellLimitReached(gameData, playerId, card)
                    || cantCastDueToAttackExile) continue;
            if (!fromOutsideGame && !gameQueryService.canCastSpellFromZone(gameData, card, Zone.EXILE, playerId)) continue;
            if (fromOutsideGame && !castingPermissionService.isSpellCastingAllowed(gameData, playerId, card)) continue;
            if (castingPermissionService.isSpellRestricted(gameData, playerId, card, restrictedSpellTypes, forbiddenCardNames)) continue;
            if (castingPermissionService.isNoncreatureSpellCastRestricted(gameData, playerId, card)) continue;
            if (castingPermissionService.isSpellRestrictedByResolvedFilter(gameData, playerId, card)) continue;
            if (castingPermissionService.isAdditionalNonartifactSpellRestricted(gameData, playerId, card)) continue;

            boolean canAffordManaValueLifeAlternative =
                    castingPermissionService.hasExilePlayForLifeEqualToManaValuePermission(
                            gameData, playerId, card.getId())
                            && gameData.getLife(playerId) >= card.getManaValue()
                            && gameQueryService.canPlayerLoseLife(gameData, playerId)
                            && gameQueryService.canPayLifeForCosts(gameData);

            if (castingPermissionService.canCastWithTiming(gameData, playerId, card, isActivePlayer, isMainPhase, stackEmpty)) {
                boolean canPayManaValueLifeAlternative = !foretellPermission
                        && castingPermissionService.hasManaValueLifeAlternativeFromExiledWithSource(
                        gameData, playerId, card.getId())
                        && gameData.getLife(playerId) >= card.getManaValue()
                        && gameQueryService.canPlayerLoseLife(gameData, playerId)
                        && gameQueryService.canPayLifeForCosts(gameData);
                if (canPayManaValueLifeAlternative) {
                    playable.add(exileCardView(gameData, playerId, card));
                    continue;
                }
                if (!foretellPermission
                        && (castingPermissionService.hasFreeCastFromExiledWithSource(gameData, playerId, card.getId())
                        || castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, card))) {
                    playable.add(exileCardView(gameData, playerId, card));
                } else {
                    boolean playWithoutPaying = !foretellPermission
                            && gameData.exilePlayWithoutPayingManaCost(card.getId());
                    ManaCost baseCost = foretellPermission
                            ? foretoldCost
                            : hasFlashforward
                            ? new ManaCost(flashforwardCast.getCost(ManaCastingCost.class)
                            .map(ManaCastingCost::manaCost)
                            .orElseThrow())
                            : card.getParsedManaCost();
                    ManaCost cost = castingCostService.applyColoredManaCostReductions(
                            gameData, playerId, card, baseCost);
                    boolean canAfford;
                    if (anyManaTypeIds.contains(card.getId())) {
                        int additionalCost = castingCostService.getCastCostModifier(
                                gameData, playerId, card, 0, Zone.EXILE);
                        canAfford = cost.canPayWithSpending(cardPool, additionalCost,
                                castingPermissionService.anyManaSpending(gameData, playerId, card.getId()));
                    } else {
                        int additionalCost = castingCostService.getCastCostModifier(
                                gameData, playerId, card, 0, Zone.EXILE);
                        boolean isArtifact = card.hasType(CardType.ARTIFACT);
                        boolean powerstoneContext = isArtifact && cardPool.getPowerstoneOnlyColorless() > 0;
                        boolean isMyr = gameQueryService.cardHasSubtype(card, CardSubtype.MYR, gameData, playerId);
                        boolean hasRestrictedRedContext = isArtifact
                                || card.hasType(CardType.CREATURE);
                        canAfford = (isArtifact || isMyr || hasRestrictedRedContext || powerstoneContext)
                                ? cost.canPay(cardPool, additionalCost, isArtifact, isMyr, hasRestrictedRedContext,
                                false, false, null, null, false, false, false, false, Set.of(), powerstoneContext)
                                : cost.canPay(cardPool, additionalCost);
                        // Check non-zero alternative cost from battlefield (e.g. Jodah)
                        if (!foretellPermission && !canAfford) {
                            canAfford = castingCostService.canAffordAlternativeCostFromBattlefield(
                                    gameData, playerId, card, pool, additionalCost, Zone.EXILE);
                        }
                        if (!canAfford
                                && (card.getKeywords().contains(Keyword.CONVOKE)
                                || gameQueryService.hasSpellCastingAbilityGrant(
                                gameData, playerId, card, Keyword.CONVOKE, Zone.EXILE))) {
                            int untappedCreatureCount = gameData.playerBattlefields
                                    .getOrDefault(playerId, List.of())
                                    .stream()
                                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                                    .filter(permanent -> !permanent.isTapped())
                                    .mapToInt(ignored -> 1)
                                    .sum();
                            canAfford = pool.getTotal() + untappedCreatureCount
                                    >= cost.getManaValue() + additionalCost;
                        }
                    }
                    if (playWithoutPaying || canAfford || canAffordManaValueLifeAlternative) {
                        playable.add(cardViewFactory.create(card));
                    } else if (castingPermissionService.hasWaterbendCastFromExiledWithSourcePermission(
                            gameData, playerId, card.getId())
                            && canPayWaterbendFromExile(gameData, playerId, card.getManaValue())) {
                        playable.add(cardViewFactory.create(card));
                    }
                }
            }
        }

        return playable;
    }

    private boolean canActivateExileAbilityNow(GameData gameData, UUID playerId, Card card, ManaPool pool,
                                                boolean isActivePlayer, boolean isMainPhase, boolean stackEmpty) {
        for (ActivatedAbility ability : card.getActivatedAbilities(Zone.EXILE)) {
            if (ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED
                    && (!isActivePlayer || !isMainPhase || !stackEmpty)) {
                continue;
            }
            if (ability.getManaCost() == null) {
                return true;
            }
            ManaCost abilityCost = new ManaCost(ability.getManaCost());
            if (gameQueryService.canPayBlackManaWithLife(gameData, playerId)) {
                abilityCost = abilityCost.withBlackManaAsPhyrexian();
            }
            if (pool != null && abilityCost.canPay(pool, 0)) {
                return true;
            }
        }
        return false;
    }

    private boolean canPayWaterbendFromExile(GameData gameData, UUID playerId, int amount) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int availableMana = pool == null ? 0 : pool.getTotal();
        if (availableMana >= amount) {
            return true;
        }
        int untappedEligible = (int) gameData.playerBattlefields
                .getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> !permanent.isTapped())
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent)
                        || gameQueryService.isCreature(gameData, permanent))
                .count();
        return untappedEligible >= amount - availableMana;
    }

    private CardView exileCardView(GameData gameData, UUID playerId, Card card) {
        CardView view = cardViewFactory.create(card);
        int counterCost = castingPermissionService.findAdditionalCounterCostFromSource(
                gameData, playerId, card.getId()).orElse(0);
        return counterCost == 0 ? view : view.toBuilder().exileCastCounterCost(counterCost).build();
    }

    /**
     * Returns the top card of each library open to the player (see
     * {@link CastingPermissionService#findLibraryTopOwners}) as a playable CardView, tagged with that library's
     * owner, if:
     * - the player has a permanent or temporary permission to cast from the top of that library
     * - the top card matches one of the castable types
     * - the player can afford and is allowed to cast it
     */
    List<CardView> getPlayableLibraryTopCards(GameData gameData, UUID playerId) {
        List<CardView> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        for (UUID libraryOwnerId : castingPermissionService.findLibraryTopOwners(gameData, playerId)) {
            for (CardView view : getPlayableLibraryTopCards(gameData, playerId, libraryOwnerId)) {
                playable.add(view.toBuilder().libraryOwnerId(libraryOwnerId).build());
            }
        }
        return playable;
    }

    private List<CardView> getPlayableLibraryTopCards(GameData gameData, UUID playerId, UUID libraryOwnerId) {
        List<CardView> playable = new ArrayList<>();
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        if (deck == null || deck.isEmpty()) {
            return playable;
        }

        Card topCard = deck.getFirst();
        Card physicalTopCard = topCard;
        boolean castFaceDown = false;
        if (topCard.getMorphCost() != null) {
            Card faceDown = CastingPermissionService.faceDownSpellCharacteristics(topCard);
            int faceDownCost = castingCostService.getCastCostModifierForFaceDownSpell(
                    gameData, playerId, faceDown, Zone.LIBRARY);
            ManaPool faceDownPool = new ManaPool(gameData.playerManaPools.get(playerId));
            faceDownPool.promoteFaceDownSpellsOrTurnFaceUpMana();
            if (castingPermissionService.canCastFromTopOfLibraryNormally(gameData, playerId, faceDown)
                    && new ManaCost("{3}").canPay(faceDownPool, faceDownCost)) {
                topCard = faceDown;
                castFaceDown = true;
            }
        }
        boolean freeTopPlay = castingPermissionService.hasLibraryTopCardFreePlayPermission(gameData, playerId, topCard);

        if (topCard.hasType(CardType.LAND)) {
            boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
            boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                    || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
            if ((freeTopPlay || castingPermissionService.canPlayLandFromTopOfLibrary(
                    gameData, playerId, topCard))
                    && isActivePlayer
                    && isMainPhase
                    && castingPermissionService.canPlayLandNow(gameData, playerId, topCard)) {
                playable.add(cardViewFactory.create(topCard));
            }
            return playable;
        }

        CardView frontView = playableLibraryTopSpellView(gameData, playerId, topCard,
                cardViewFactory.create(physicalTopCard), castFaceDown);
        if (frontView != null) {
            playable.add(frontView);
        }
        // CR 716.3a: cast as an Adventure, only the Adventure's characteristics decide castability.
        Card adventure = CastingPermissionService.adventureSpellCharacteristics(physicalTopCard);
        if (adventure != null) {
            CardView adventureView = playableLibraryTopSpellView(gameData, playerId, adventure,
                    cardViewFactory.create(adventure).toBuilder().castAsAdventure(true).build(), false);
            if (adventureView != null) {
                playable.add(adventureView);
            }
        }

        return playable;
    }

    /**
     * Returns {@code view} when the nonland {@code topCard} characteristics can be cast from the top
     * of the library right now, or {@code null} when they can't.
     */
    private CardView playableLibraryTopSpellView(GameData gameData, UUID playerId, Card topCard,
                                                 CardView view, boolean castFaceDown) {
        boolean freeTopPlay = castingPermissionService.hasLibraryTopCardFreePlayPermission(gameData, playerId, topCard);
        boolean lifeTopPlay = castingPermissionService
                .canCastFromTopOfLibraryByPayingLifeEqualToManaValue(gameData, playerId, topCard);
        if (!castingPermissionService.canCastFromTopOfLibrary(gameData, playerId, topCard)
                || (!castFaceDown && !freeTopPlay && !lifeTopPlay && topCard.getManaCost() == null)) {
            return null;
        }

        if (!gameQueryService.canPlayerCastSpellsFromZone(gameData, playerId, Zone.LIBRARY)) {
            return null;
        }
        if (!gameQueryService.canCastSpellFromZone(gameData, topCard, Zone.LIBRARY, playerId)) {
            return null;
        }

        // The from-library-top cast path rejects cards with additional cast costs (no wire for
        // the payment selections) — never advertise them as playable.
        if (castingCostService.hasAdditionalSpellCosts(gameData, playerId, topCard)) {
            return null;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        boolean cantCastDueToAttack = castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId);
        Set<CardType> restrictedSpellTypes = castingPermissionService.getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = castingPermissionService.getForbiddenCardNames(gameData, playerId);

        if (castingPermissionService.isSpellLimitReached(gameData, playerId, topCard)
                || cantCastDueToAttack) return null;
        if (castingPermissionService.isSpellRestricted(gameData, playerId, topCard, restrictedSpellTypes, forbiddenCardNames)) return null;
        if (castingPermissionService.isNoncreatureSpellCastRestricted(gameData, playerId, topCard)) return null;
        if (castingPermissionService.isSpellRestrictedByResolvedFilter(gameData, playerId, topCard)) return null;
        if (castingPermissionService.isAdditionalNonartifactSpellRestricted(gameData, playerId, topCard)) return null;

        if (!castingPermissionService.canCastWithTimingFromLibraryTop(
                gameData, playerId, topCard, isActivePlayer, isMainPhase, stackEmpty)) return null;

        // Check if spell requires a legal target (MTG rule 601.2c)
        if (EffectResolution.needsSpellCastTarget(topCard) && !validTargetService.hasValidTargetsForSpell(gameData, topCard, playerId)) {
            return null;
        }

        boolean canPayLifeAlternative = !castFaceDown && castingPermissionService
                .canCastFromTopOfLibraryByPayingLifeEqualToManaValue(gameData, playerId, topCard)
                && gameData.getLife(playerId) >= topCard.getManaValue()
                && gameQueryService.canPlayerLoseLife(gameData, playerId)
                && gameQueryService.canPayLifeForCosts(gameData);
        boolean alternativeZeroCost = !castFaceDown && castingCostService.hasAlternativeZeroCostFromBattlefield(
                gameData, playerId, topCard, Zone.LIBRARY);
        CardView topCardView = view;
        if (!freeTopPlay && !canPayLifeAlternative && !alternativeZeroCost) {
            int counterCost = castingPermissionService.findAdditionalCounterCostFromTopOfLibrary(
                    gameData, playerId, topCard).orElse(0);
            if (counterCost > 0) {
                topCardView = topCardView.toBuilder().exileCastCounterCost(counterCost).build();
            }
        }
        if (freeTopPlay || canPayLifeAlternative || alternativeZeroCost) {
            return topCardView;
        } else {
            ManaCost cost = castingCostService.applyColoredManaCostReductions(
                    gameData, playerId, topCard, castFaceDown ? new ManaCost("{3}") : topCard.getParsedManaCost());
            ManaPool pool = gameData.playerManaPools.get(playerId);
            ManaPool cardPool = pool;
            if (castFaceDown) {
                cardPool = new ManaPool(pool);
                cardPool.promoteFaceDownSpellsOrTurnFaceUpMana();
            }
            if (!topCard.hasType(CardType.CREATURE) && pool.getNoncreatureSpellOnlyManaTotal() > 0) {
                cardPool = new ManaPool(pool);
                cardPool.promoteNoncreatureSpellOnlyMana();
            }
            if (topCard.hasType(CardType.CREATURE) && topCard.getCardText() == null
                    && topCard.getKeywords().isEmpty()
                    && cardPool.getCreatureSpellWithoutAbilitiesOnlyManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteCreatureSpellWithoutAbilitiesOnlyMana();
            }
            if (topCard.hasType(CardType.CREATURE)
                    && gameQueryService.getEffectiveCardColors(gameData, topCard).contains(CardColor.BLUE)
                    && cardPool.getBlueCreatureSpellOnlyManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteBlueCreatureSpellOnlyMana();
            }
            if (gameQueryService.getEffectiveCardColors(gameData, topCard).size() == 3
                    && cardPool.getExactlyThreeColorSpellOnlyManaTotal() > 0) {
                cardPool = new ManaPool(cardPool);
                cardPool.promoteExactlyThreeColorSpellOnlyMana();
            }
            int additionalCost = castFaceDown
                    ? castingCostService.getCastCostModifierForFaceDownSpell(gameData, playerId, topCard, Zone.LIBRARY)
                    : castingCostService.getCastCostModifier(gameData, playerId, topCard, 0, Zone.LIBRARY);
            boolean canAfford = cost.canPay(cardPool, additionalCost);
            if (!canAfford && castingPermissionService.canSpendAnyManaTypeToCast(gameData, playerId, topCard)) {
                canAfford = cost.canPayAsGeneric(cardPool, 0, additionalCost);
            }
            if (!canAfford && (castingPermissionService.canSpendManaAsAnyColorToCastFromBattlefield(
                    gameData, playerId, topCard)
                    || castingPermissionService.hasLibraryTopPermissionForCard(gameData, playerId, topCard))) {
                ManaPool anyColorPool = new ManaPool(cardPool);
                anyColorPool.setAllManaSpendableAsAnyColor(true);
                canAfford = cost.canPay(anyColorPool, additionalCost);
            }
            if (!canAfford && !castFaceDown) {
                canAfford = castingCostService.canAffordAlternativeCostFromBattlefield(
                        gameData, playerId, topCard, pool, additionalCost, Zone.LIBRARY);
            }
            if (canAfford) {
                return topCardView;
            }
        }
        return null;
    }

    private CardView createHandCardView(GameData gameData, UUID playerId, Card card, List<CardSubtype> grantedSubtypes) {
        CardView view = cardViewFactory.create(card, grantedSubtypes, List.of(),
                gameQueryService.computeGrantedHandAbilitiesForOwnedCard(gameData, playerId, card));
        if (view.hasAlternateCastingCost()) {
            return view;
        }
        var grantedBlitz = gameQueryService.findGrantedBlitzAlternateCast(gameData, playerId, card);
        if (grantedBlitz.isPresent()) {
            return view.toBuilder()
                    .hasAlternateCastingCost(true)
                    .alternateCostManaCost(grantedBlitz.get().getCost(ManaCastingCost.class)
                            .map(ManaCastingCost::manaCost)
                            .orElse(null))
                    .build();
        }
        var granted = gameQueryService.findGrantedAlternateCast(gameData, playerId, card);
        if (granted.isPresent()) {
            var exileHandCost = granted.get().cast().getCost(ExileCardsFromHandCastingCost.class);
            return view.toBuilder()
                    .hasAlternateCastingCost(true)
                    .alternateCostManaCost(granted.get().cast().getCost(ManaCastingCost.class)
                            .map(ManaCastingCost::manaCost)
                            .orElse(null))
                    .alternateCostExileHandCount(exileHandCost.map(ExileCardsFromHandCastingCost::count).orElse(0))
                    .alternateCostExileHandLabel(exileHandCost.map(ExileCardsFromHandCastingCost::label).orElse(null))
                    .build();
        }
        WebSlingingEffect webSlinging = castingCostService.findWebSlingingEffectFromBattlefield(
                gameData, playerId, card);
        if (webSlinging != null) {
            return view.toBuilder()
                    .hasAlternateCastingCost(true)
                    .alternateCostReturnCount(1)
                    .alternateCostManaCost(webSlinging.manaCost())
                    .build();
        }
        if (!castingCostService.canPaySharedColorDiscardAlternativeCostFromBattlefield(gameData, playerId, card)) {
            return view;
        }
        return view.toBuilder()
                .hasAlternateCastingCost(true)
                .alternateCostExileHandCount(1)
                .alternateCostExileHandLabel("a card that shares a color")
                .alternateCostDiscardsHandCard(true)
                .build();
    }

    public JoinGame getJoinGame(GameData data, UUID playerId) {
        return gameQueryService.withQueryScope(data,
                () -> getJoinGameWithinQueryScope(data, playerId));
    }

    private JoinGame getJoinGameWithinQueryScope(GameData data, UUID playerId) {
        List<CardSubtype> playerGranted = playerId != null
                ? gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(data, playerId)
                : List.of();
        List<CardView> hand = playerId != null
                ? data.playerHands.getOrDefault(playerId, List.of()).stream()
                .map(c -> createHandCardView(data, playerId, c, playerGranted)).toList()
                : List.of();
        int mulliganCount = playerId != null ? data.mulliganCounts.getOrDefault(playerId, 0) : 0;
        Map<String, Integer> manaPool = getManaPool(data, playerId);
        List<TurnStep> autoStopSteps = playerId != null && data.playerAutoStopSteps.containsKey(playerId)
                ? new ArrayList<>(data.playerAutoStopSteps.get(playerId))
                : List.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
        return new JoinGame(
                data.id,
                data.gameName,
                data.status,
                new ArrayList<>(data.playerNames),
                new ArrayList<>(data.orderedPlayerIds),
                gameLogViewFactory.createAll(data.gameLog),
                data.currentStep,
                data.activePlayerId,
                data.turnNumber,
                gameQueryService.getPriorityPlayerId(data),
                hand,
                mulliganCount,
                getDeckSizes(data),
                getHandSizes(data),
                getBattlefields(data),
                manaPool,
                autoStopSteps,
                getLifeTotals(data),
                getPoisonCounters(data),
                getEnergyCounters(data),
                getRadCounters(data),
                getStackViewsFor(data, getStackViews(data), playerId, playerId == null
                        ? Set.of() : collectFaceDownPermanentReveals(data, playerId).keySet()),
                getGraveyardViews(data, playerId),
                getSpeeds(data),
                data.dayNight,
                data.planechase == null ? null : planarViews.create(data, playerId),
                data.monarchPlayerId, commanderView(data, playerId)
        );
    }

    private com.github.laxika.magicalvibes.networking.model.CommanderView commanderView(GameData game, UUID playerId) {
        Map<UUID, List<CardView>> zones = new java.util.LinkedHashMap<>();
        Map<UUID, List<UUID>> commanders = new java.util.LinkedHashMap<>();
        game.playerCommandZones.forEach((id, cards) -> zones.put(id, cards.stream().map(cardViewFactory::create).toList()));
        game.playerCommanders.forEach((id, cards) -> commanders.put(id, cards.stream().map(Card::getId).toList()));
        Map<UUID, Map<UUID, Integer>> damage = new java.util.LinkedHashMap<>();
        game.commanderDamageReceived.forEach((id, amounts) -> damage.put(id, Map.copyOf(amounts)));
        UUID actingPlayerId = playerId != null && playerId.equals(game.mindControllerPlayerId())
                ? game.mindControlledPlayerId() : playerId;
        boolean controlledViewer = playerId != null && playerId.equals(game.mindControlledPlayerId());
        return new com.github.laxika.magicalvibes.networking.model.CommanderView(game.format, zones, commanders,
                Map.copyOf(game.commanderTaxByCardId), damage,
                actingPlayerId == null || controlledViewer ? List.of()
                        : actionAvailabilityService.getPlayableCommanders(game, actingPlayerId).stream().map(Card::getId).toList(),
                actingPlayerId == null || controlledViewer ? List.of()
                        : secretMissionService.revealableMissionCardIds(game, actingPlayerId));
    }

    /**
     * The delayed-trigger payments the viewer can make now. A Mindslaver controller acts for the
     * controlled player, so they see that player's payments and the controlled player sees none.
     * Identical obligations (one Nafs Asp damaging the player twice) collapse into one entry, which
     * stays offered until each has been paid one request at a time.
     */
    List<DelayedTriggerPaymentView> getDelayedTriggerPayments(GameData gameData, UUID playerId) {
        if (playerId.equals(gameData.mindControlledPlayerId())) return List.of();
        UUID actingPlayerId = playerId.equals(gameData.mindControllerPlayerId()) && gameData.mindControlledPlayerId() != null
                ? gameData.mindControlledPlayerId() : playerId;
        return actionAvailabilityService.getAvailableDelayedTriggerPayments(gameData, actingPlayerId).stream()
                .map(payment -> new DelayedTriggerPaymentView(payment.sourceCard().getId(),
                        payment.sourceCard().getName(), payment.affectedId(),
                        affectedName(gameData, payment.affectedId()), payment.manaCost()))
                .distinct()
                .toList();
    }

    private String affectedName(GameData gameData, UUID affectedId) {
        String playerName = gameData.playerIdToName.get(affectedId);
        if (playerName != null) return playerName;
        Permanent permanent = gameQueryService.findPermanentById(gameData, affectedId);
        return permanent == null ? null : FaceDownVisibility.publicName(gameData, permanent);
    }

    int getSearchTaxCost(GameData gameData, UUID playerId) {
        int unpaidCount = 0;
        Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds.get(playerId);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    if (effect instanceof CantSearchLibrariesEffect restriction && restriction.payableToIgnore()) {
                        if (paidSet == null || !paidSet.contains(perm.getId())) {
                            unpaidCount++;
                        }
                    }
                }
            }
        }
        return unpaidCount * 2;
    }

}
