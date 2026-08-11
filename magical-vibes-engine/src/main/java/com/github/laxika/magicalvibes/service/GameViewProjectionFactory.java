package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesForCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PubliclyRevealedHandEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;
import com.github.laxika.magicalvibes.networking.model.GameLogEntryView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.GameLogViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import com.github.laxika.magicalvibes.service.effect.GrantedAbilityViewFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Pure player-specific projection of authoritative game state into immutable networking views.
 *
 * <p>This factory performs no delivery, serialization, or domain mutation. The same methods serve
 * event-driven human output and explicit join/reconnect projection.
 */
@Component
@RequiredArgsConstructor
public class GameViewProjectionFactory {

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

    public Map<UUID, GameStateMessage> createGameStateMessages(
            GameData gameData,
            List<GameLogEntryView> newLogEntries,
            Collection<UUID> recipientIds) {
        List<List<PermanentView>> battlefields = getBattlefields(gameData);
        Map<UUID, FaceDownReveal> faceDownReveals = collectFaceDownReveals(gameData);
        List<StackEntryView> stack = getStackViews(gameData);
        List<List<CardView>> graveyards = getGraveyardViews(gameData);
        List<Integer> deckSizes = getDeckSizes(gameData);
        List<Integer> handSizes = getHandSizes(gameData);
        List<Integer> lifeTotals = getLifeTotals(gameData);
        List<Integer> poisonCounters = getPoisonCounters(gameData);
        UUID priorityPlayerId = gameData.interaction.isAwaitingInput() ? null : gameQueryService.getPriorityPlayerId(gameData);

        Map<UUID, GameStateMessage> messages = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!recipientIds.contains(playerId)) {
                continue;
            }
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
            if (gameData.mindControllerPlayerId != null && playerId.equals(gameData.mindControllerPlayerId)) {
                    UUID controlledId = gameData.mindControlledPlayerId;
                if (controlledId != null) {
                    List<CardSubtype> controlledGranted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(gameData, controlledId);
                    opponentHand = gameData.playerHands.getOrDefault(controlledId, List.of())
                            .stream().map(c -> createHandCardView(gameData, controlledId, c, controlledGranted)).toList();
                    playableCardIndices =
                            actionAvailabilityService.getPlayableCardIndices(gameData, controlledId);
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

            messages.put(playerId, new GameStateMessage(
                    gameData.status, gameData.activePlayerId, gameData.turnNumber,
                    gameData.currentStep, priorityPlayerId,
                    applyFaceDownReveals(battlefields, faceDownReveals, playerId),
                    stack, graveyards, deckSizes, handSizes, lifeTotals, poisonCounters,
                    hand, opponentHand, mulliganCount, manaPool, autoStopSteps, playableCardIndices,
                    playableGraveyardLandIndices, playableExileCards, newLogEntries, searchTaxCost,
                    gameData.mindControlledPlayerId, revealedLibraryTopCards, playableFlashbackIndices,
                    playableLibraryTopCards, potentialPlayableCardIndices, potentialManaTotal,
                    potentialPayableAbilityIndices
            ));
        }
        return Collections.unmodifiableMap(messages);
    }

    List<StackEntryView> getStackViews(GameData gameData) {
        return gameData.stack.stream().map(entry -> {
            List<CardSubtype> granted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(
                    gameData, entry.getControllerId());
            return stackEntryViewFactory.create(entry, granted);
        }).toList();
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
                    allGrantedAbilities.addAll(p.getPersistentGrantedActivatedAbilities());
                    allGrantedAbilities.addAll(p.getTemporaryActivatedAbilities());
                    allGrantedAbilities.addAll(p.getUntilNextTurnActivatedAbilities());
                    List<Card> faceUpExiledWith = new ArrayList<>();
                    int faceDownExiledCount = 0;
                    for (ExiledCardEntry exiledWith : data.getExiledWithPermanentEntries(p.getId(), p.getCard().getId())) {
                        if (exiledWith.faceDown()) {
                            faceDownExiledCount++;
                        } else {
                            faceUpExiledWith.add(exiledWith.card());
                        }
                    }
                    PermanentView view = permanentViewFactory.create(p, adjustedBonusPower, adjustedBonusToughness, bonus.keywords(), bonus.animatedCreature(), allGrantedAbilities, bonus.grantedColors(), bonus.grantedSubtypes(), bonus.grantedCardTypes(), bonus.colorOverriding(), bonus.subtypeOverriding(), bonus.landSubtypeOverriding(), bonus.cardTypeOverriding(), bonus.removedKeywords(), bonus.losesAllAbilities() || p.isLosesAllAbilitiesUntilEndOfTurn(), bonus.grantedSupertypes(), explained.lines(), faceUpExiledWith, faceDownExiledCount);
                    views.add(view.withGrantedAbilities(grantedAbilityViewFactory.create(
                            p, bonus, explained.grantedEffectAttributions())));
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
     * Face-down exiled cards (hideaway lands, Grimoire Thief, ...) keyed by their permanent's id.
     * The shared battlefield views carry only a card-back count for these; the actual cards are
     * swapped into the controller's copy by {@link #applyFaceDownReveals}.
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
                    UUID viewerId = entry.exilerId() != null ? entry.exilerId() : pid;
                    cardsByViewer.computeIfAbsent(viewerId, ignored -> new ArrayList<>())
                            .add(cardViewFactory.create(entry.card()));
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

    List<List<CardView>> getGraveyardViews(GameData data) {
        List<List<CardView>> graveyards = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> gy = data.playerGraveyards.get(pid);
            List<CardSubtype> granted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(data, pid);
            graveyards.add(gy != null
                    ? gy.stream().map(c -> cardViewFactory.createForGraveyard(c, granted,
                            gameQueryService.computeGrantedGraveyardAbilitiesForOwnedCreatureCard(data, pid, c))).toList()
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

        boolean allHandsRevealed = false;
        boolean opponentRevealsOwnHand = false;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof PubliclyRevealedHandEffect reveal) {
                        if (!reveal.controllerOnly()) {
                            allHandsRevealed = true;
                        } else if (!pid.equals(playerId)) {
                            // Opponent plays with their hand revealed (Enduring Renewal).
                            opponentRevealsOwnHand = true;
                        }
                    }
                }
            }
        }

        boolean reveals = allHandsRevealed || opponentRevealsOwnHand || opponentHandRevealedBySource(gameData, playerId);
        if (!reveals) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) return List.of();
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof RevealOpponentHandsEffect) {
                        reveals = true;
                        break;
                    }
                }
                if (reveals) break;
            }
        }
        if (!reveals) return List.of();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (!opponentId.equals(playerId)) {
                List<CardSubtype> granted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(gameData, opponentId);
                return gameData.playerHands.getOrDefault(opponentId, List.of())
                        .stream().map(c -> cardViewFactory.create(c, granted)).toList();
            }
        }
        return List.of();
    }

    /**
     * {@code true} if an opponent of {@code playerId} was made to play with their hand revealed by a
     * source permanent (Stromgald Spy) that is still on the battlefield.
     */
    private boolean opponentHandRevealedBySource(GameData gameData, UUID playerId) {
        for (Map.Entry<UUID, Set<UUID>> entry : gameData.handsRevealedWhileSourceOnBattlefield.entrySet()) {
            if (gameQueryService.findPermanentById(gameData, entry.getKey()) == null) {
                continue;
            }
            for (UUID revealedPlayerId : entry.getValue()) {
                if (!revealedPlayerId.equals(playerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    List<List<CardView>> getRevealedLibraryTopCards(GameData data, UUID viewerId) {
        // Determine which players have their top card visible to the viewer.
        // PlayWithTopCardRevealedEffect = publicly revealed to all players.
        // LookAtTopCardOfOwnLibraryEffect / AllowCastFromTopOfLibraryEffect = private,
        //   only visible to the controller.
        Set<UUID> revealedPlayerIds = new HashSet<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof PlayWithTopCardRevealedEffect) {
                        // Public: visible to all
                        revealedPlayerIds.add(pid);
                    } else if (pid.equals(viewerId) &&
                            (effect instanceof LookAtTopCardOfOwnLibraryEffect
                                    || effect instanceof AllowCastFromTopOfLibraryEffect)) {
                        // Private: only visible to the controller
                        revealedPlayerIds.add(pid);
                    }
                }
                if (revealedPlayerIds.contains(pid)) break;
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
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = castingPermissionService.getMaxSpellsPerTurn(gameData, playerId);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttackExile = castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId);
        Set<CardType> restrictedSpellTypes = castingPermissionService.getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = castingPermissionService.getForbiddenCardNames(gameData, playerId);

        // Collect card IDs castable via AllowCastFromCardsExiledWithSourceEffect
        Set<UUID> castableFromExileWithSource = castingPermissionService.getCastableExiledCardIds(gameData, playerId);
        Set<UUID> anyManaTypeIds = castingPermissionService.getAnyManaTypeExiledCardIds(gameData, playerId);

        // Include player's own exiled cards plus cards from any exile zone castable via source effect
        List<Card> exiledCards = new ArrayList<>(gameData.getPlayerExiledCards(playerId));
        Set<UUID> alreadyIncluded = new HashSet<>();
        for (Card c : exiledCards) alreadyIncluded.add(c.getId());
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
                    && playerId.equals(gameData.exilePlayPermissions.get(entry.card().getId()))) {
                exiledCards.add(entry.card());
                alreadyIncluded.add(entry.card().getId());
            }
        }
        if (exiledCards.isEmpty()) {
            return playable;
        }

        ManaPool pool = gameData.playerManaPools.get(playerId);

        for (Card card : exiledCards) {
            UUID permittedPlayer = gameData.exilePlayPermissions.get(card.getId());
            boolean hasPermission = (permittedPlayer != null && permittedPlayer.equals(playerId))
                    || castableFromExileWithSource.contains(card.getId());
            boolean hasExileCast = card.getCastingOption(ExileCast.class).isPresent();
            if (!hasPermission && !hasExileCast) {
                continue;
            }

            if (card.hasType(CardType.LAND)) {
                if (isActivePlayer && isMainPhase && landsPlayed < gameData.getMaxLandsThisTurn(playerId) && stackEmpty
                        && !gameData.playersCantPlayLandsThisTurn.contains(playerId)
                        && !castingPermissionService.isLandPlayRestricted(gameData, playerId)
                        && !castingPermissionService.isLandPlayForbiddenByChosenName(gameData, card)) {
                    playable.add(cardViewFactory.create(card));
                }
                continue;
            }

            if (card.getManaCost() == null || spellLimitReached || cantCastDueToAttackExile) continue;
            if (castingPermissionService.isSpellRestricted(gameData, playerId, card, restrictedSpellTypes, forbiddenCardNames)) continue;
            if (castingPermissionService.isNoncreatureSpellCastRestricted(gameData, card)) continue;
            if (castingPermissionService.isOpponentsManaValueSpellCastRestricted(gameData, playerId, card)) continue;
            if (castingPermissionService.isAdditionalNonartifactSpellRestricted(gameData, playerId, card)) continue;

            if (castingPermissionService.canCastWithTiming(gameData, playerId, card, isActivePlayer, isMainPhase, stackEmpty)) {
                if (castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, card)) {
                    playable.add(cardViewFactory.create(card));
                } else {
                    boolean playWithoutPaying = gameData.exilePlayWithoutPayingManaCost.contains(card.getId());
                    ManaCost cost = card.getParsedManaCost();
                    boolean canAfford;
                    if (anyManaTypeIds.contains(card.getId())) {
                        canAfford = cost.canPayAsGeneric(pool);
                    } else {
                        int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, card);
                        boolean isArtifact = card.hasType(CardType.ARTIFACT);
                        boolean isMyr = gameQueryService.cardHasSubtype(card, CardSubtype.MYR, gameData, playerId);
                        boolean hasRestrictedRedContext = isArtifact
                                || card.hasType(CardType.CREATURE);
                        canAfford = (isArtifact || isMyr || hasRestrictedRedContext)
                                ? cost.canPay(pool, additionalCost, isArtifact, isMyr, hasRestrictedRedContext)
                                : cost.canPay(pool, additionalCost);
                        // Check non-zero alternative cost from battlefield (e.g. Jodah)
                        if (!canAfford) {
                            canAfford = castingCostService.canAffordAlternativeCostFromBattlefield(gameData, playerId, card, pool, additionalCost);
                        }
                    }
                    if (playWithoutPaying || canAfford) {
                        playable.add(cardViewFactory.create(card));
                    }
                }
            }
        }

        return playable;
    }

    /**
     * Returns the top card of the player's library as a playable CardView if:
     * - the player has a permanent with AllowCastFromTopOfLibraryEffect
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

        // Collect castable types from all AllowCastFromTopOfLibraryEffect on the player's battlefield
        Set<CardType> castableTypes = castingPermissionService.getCastableTypesFromTopOfLibrary(gameData, playerId);
        if (castableTypes.isEmpty()) {
            return playable;
        }

        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            return playable;
        }

        Card topCard = deck.getFirst();

        // Check if the top card matches any castable type
        boolean matchesType = castableTypes.contains(topCard.getType())
                || topCard.getAdditionalTypes().stream().anyMatch(castableTypes::contains);
        if (!matchesType || topCard.getManaCost() == null) {
            return playable;
        }

        // The from-library-top cast path rejects cards with additional cast costs (no wire for
        // the payment selections) — never advertise them as playable.
        if (castingCostService.hasAdditionalSpellCosts(topCard)) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = castingPermissionService.getMaxSpellsPerTurn(gameData, playerId);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttack = castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId);
        Set<CardType> restrictedSpellTypes = castingPermissionService.getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = castingPermissionService.getForbiddenCardNames(gameData, playerId);

        if (spellLimitReached || cantCastDueToAttack) return playable;
        if (castingPermissionService.isSpellRestricted(gameData, playerId, topCard, restrictedSpellTypes, forbiddenCardNames)) return playable;
        if (castingPermissionService.isNoncreatureSpellCastRestricted(gameData, topCard)) return playable;
        if (castingPermissionService.isOpponentsManaValueSpellCastRestricted(gameData, playerId, topCard)) return playable;
        if (castingPermissionService.isAdditionalNonartifactSpellRestricted(gameData, playerId, topCard)) return playable;

        if (!castingPermissionService.canCastWithTiming(gameData, playerId, topCard, isActivePlayer, isMainPhase, stackEmpty)) return playable;

        // Check if spell requires a legal target (MTG rule 601.2c)
        if (EffectResolution.needsSpellCastTarget(topCard) && !validTargetService.hasValidTargetsForSpell(gameData, topCard, playerId)) {
            return playable;
        }

        if (castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, topCard, false)) {
            playable.add(cardViewFactory.create(topCard));
        } else {
            ManaCost cost = topCard.getParsedManaCost();
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, topCard);
            boolean canAfford = cost.canPay(pool, additionalCost);
            if (!canAfford && castingPermissionService.canSpendAnyManaTypeToCast(gameData, playerId, topCard)) {
                canAfford = cost.canPayAsGeneric(pool, 0, additionalCost);
            }
            if (!canAfford) {
                canAfford = castingCostService.canAffordAlternativeCostFromBattlefield(gameData, playerId, topCard, pool, additionalCost);
            }
            if (canAfford) {
                playable.add(cardViewFactory.create(topCard));
            }
        }

        return playable;
    }

    private CardView createHandCardView(GameData gameData, UUID playerId, Card card, List<CardSubtype> grantedSubtypes) {
        CardView view = cardViewFactory.create(card, grantedSubtypes);
        if (view.hasAlternateCastingCost()
                || !castingCostService.canPaySharedColorDiscardAlternativeCostFromBattlefield(gameData, playerId, card)) {
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
                getStackViews(data),
                getGraveyardViews(data)
        );
    }

    int getSearchTaxCost(GameData gameData, UUID playerId) {
        int unpaidCount = 0;
        Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds.get(playerId);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
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
