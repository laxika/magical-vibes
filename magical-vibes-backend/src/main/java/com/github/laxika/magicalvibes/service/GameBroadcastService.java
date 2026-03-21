package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemGrantsFlashbackEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseEachPlayerCastCostPerSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;

import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CastPermanentSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSharedCardTypeWithImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfControlsSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfMetalcraftEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostPerCreatureOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesForCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePhyrexianPaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GameBroadcastService {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final PermanentViewFactory permanentViewFactory;
    private final StackEntryViewFactory stackEntryViewFactory;
    private final GameQueryService gameQueryService;
    private final ValidTargetService validTargetService;

    public void broadcastGameState(GameData gameData) {
        List<String> newLogEntries;
        int logSize = gameData.gameLog.size();
        if (logSize > gameData.lastBroadcastedLogSize) {
            newLogEntries = new ArrayList<>(gameData.gameLog.subList(gameData.lastBroadcastedLogSize, logSize));
        } else {
            newLogEntries = List.of();
        }
        gameData.lastBroadcastedLogSize = logSize;

        List<List<PermanentView>> battlefields = getBattlefields(gameData);
        List<StackEntryView> stack = getStackViews(gameData);
        List<List<CardView>> graveyards = getGraveyardViews(gameData);
        List<Integer> deckSizes = getDeckSizes(gameData);
        List<Integer> handSizes = getHandSizes(gameData);
        List<Integer> lifeTotals = getLifeTotals(gameData);
        List<Integer> poisonCounters = getPoisonCounters(gameData);
        UUID priorityPlayerId = gameData.interaction.isAwaitingInput() ? null : gameQueryService.getPriorityPlayerId(gameData);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<CardView> hand = gameData.playerHands.getOrDefault(playerId, List.of())
                    .stream().map(cardViewFactory::create).toList();
            List<CardView> opponentHand = getRevealedOpponentHand(gameData, playerId);
            int mulliganCount = gameData.mulliganCounts.getOrDefault(playerId, 0);
            Map<String, Integer> manaPool = getManaPool(gameData, playerId);
            List<TurnStep> autoStopSteps = gameData.playerAutoStopSteps.containsKey(playerId)
                    ? new ArrayList<>(gameData.playerAutoStopSteps.get(playerId))
                    : List.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
            List<Integer> playableCardIndices = getPlayableCardIndices(gameData, playerId);
            List<Integer> playableGraveyardLandIndices = getPlayableGraveyardLandIndices(gameData, playerId);
            List<CardView> playableExileCards = getPlayableExileCards(gameData, playerId);
            List<Integer> playableFlashbackIndices = getPlayableFlashbackIndices(gameData, playerId);
            List<List<CardView>> revealedLibraryTopCards = getRevealedLibraryTopCards(gameData, playerId);
            List<CardView> playableLibraryTopCards = getPlayableLibraryTopCards(gameData, playerId);
            int searchTaxCost = getSearchTaxCost(gameData, playerId);

            // Mindslaver: controller sees the controlled player's hand and playable indices
            if (gameData.mindControllerPlayerId != null && playerId.equals(gameData.mindControllerPlayerId)) {
                UUID controlledId = gameData.mindControlledPlayerId;
                if (controlledId != null) {
                    opponentHand = gameData.playerHands.getOrDefault(controlledId, List.of())
                            .stream().map(cardViewFactory::create).toList();
                    playableCardIndices = getPlayableCardIndices(gameData, controlledId);
                    playableGraveyardLandIndices = getPlayableGraveyardLandIndices(gameData, controlledId);
                    playableExileCards = getPlayableExileCards(gameData, controlledId);
                    playableFlashbackIndices = getPlayableFlashbackIndices(gameData, controlledId);
                    playableLibraryTopCards = getPlayableLibraryTopCards(gameData, controlledId);
                }
            }

            sessionManager.sendToPlayer(playerId, new GameStateMessage(
                    gameData.status, gameData.activePlayerId, gameData.turnNumber,
                    gameData.currentStep, priorityPlayerId,
                    battlefields, stack, graveyards, deckSizes, handSizes, lifeTotals, poisonCounters,
                    hand, opponentHand, mulliganCount, manaPool, autoStopSteps, playableCardIndices,
                    playableGraveyardLandIndices, playableExileCards, newLogEntries, searchTaxCost,
                    gameData.mindControlledPlayerId, revealedLibraryTopCards, playableFlashbackIndices,
                    playableLibraryTopCards
            ));
        }
    }

    List<StackEntryView> getStackViews(GameData gameData) {
        return gameData.stack.stream().map(stackEntryViewFactory::create).toList();
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
                    GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(data, p);
                    // Compute adjusted bonus P/T to account for static base P/T overrides (e.g. Deep Freeze)
                    int adjustedBonusPower = gameQueryService.getEffectivePower(p, bonus) - p.getEffectivePower();
                    int adjustedBonusToughness = gameQueryService.getEffectiveToughness(p, bonus) - p.getEffectiveToughness();
                    List<ActivatedAbility> allGrantedAbilities = new ArrayList<>(bonus.grantedActivatedAbilities());
                    allGrantedAbilities.addAll(p.getTemporaryActivatedAbilities());
                    allGrantedAbilities.addAll(p.getUntilNextTurnActivatedAbilities());
                    views.add(permanentViewFactory.create(p, adjustedBonusPower, adjustedBonusToughness, bonus.keywords(), bonus.animatedCreature(), allGrantedAbilities, bonus.grantedColors(), bonus.grantedSubtypes(), bonus.grantedCardTypes(), bonus.colorOverriding(), bonus.subtypeOverriding(), bonus.landSubtypeOverriding(), bonus.removedKeywords(), bonus.losesAllAbilities() || p.isLosesAllAbilitiesUntilEndOfTurn(), bonus.grantedSupertypes()));
                }
                battlefields.add(views);
            }
        }
        return battlefields;
    }

    List<List<CardView>> getGraveyardViews(GameData data) {
        List<List<CardView>> graveyards = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> gy = data.playerGraveyards.get(pid);
            graveyards.add(gy != null ? gy.stream().map(cardViewFactory::create).toList() : new ArrayList<>());
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
        // (handled separately in broadcastGameState — overrides opponentHand for controller)

        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return List.of();
        boolean reveals = false;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RevealOpponentHandsEffect) {
                    reveals = true;
                    break;
                }
            }
            if (reveals) break;
        }
        if (!reveals) return List.of();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (!opponentId.equals(playerId)) {
                return gameData.playerHands.getOrDefault(opponentId, List.of())
                        .stream().map(cardViewFactory::create).toList();
            }
        }
        return List.of();
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

    public List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId) {
        return getPlayableCardIndices(gameData, playerId, 0);
    }

    public List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId, int extraConvokeMana) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = getMaxSpellsPerTurn(gameData);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttack = isPlayerPreventedFromCasting(gameData, playerId);

        boolean stackEmpty = gameData.stack.isEmpty();
        Set<CardType> restrictedSpellTypes = getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = getForbiddenCardNames(gameData);

        // Count untapped creatures for convoke playability
        int untappedCreatureCount = 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm) && !perm.isTapped()) {
                    untappedCreatureCount++;
                }
            }
        }

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND) && isActivePlayer && isMainPhase && landsPlayed < 1 && stackEmpty) {
                playable.add(i);
            }
            if (card.getManaCost() != null && !spellLimitReached && !cantCastDueToAttack) {
                if (restrictedSpellTypes.contains(card.getType())
                        || card.getAdditionalTypes().stream().anyMatch(restrictedSpellTypes::contains)) continue;
                if (forbiddenCardNames.contains(card.getName())) continue;

                boolean isInstantSpeed = card.hasType(CardType.INSTANT)
                        || card.getKeywords().contains(Keyword.FLASH)
                        || hasFlashGrantForCard(gameData, playerId, card);
                boolean canCastTiming = isInstantSpeed || (isActivePlayer && isMainPhase && stackEmpty);

                if (canCastTiming) {
                    // Alternative zero cost (e.g. Rooftop Storm for Zombie creature spells)
                    if (hasAlternativeZeroCostFromBattlefield(gameData, playerId, card)) {
                        playable.add(i);
                    } else {
                        ManaCost cost = new ManaCost(card.getManaCost());
                        ManaPool pool = gameData.playerManaPools.get(playerId);
                        int additionalCost = getCastCostModifier(gameData, playerId, card);
                        boolean isArtifact = card.hasType(CardType.ARTIFACT);
                        boolean isMyr = card.getSubtypes().contains(CardSubtype.MYR);
                        boolean hasRestrictedRedContext = isArtifact
                                || card.hasType(CardType.CREATURE);
                        boolean hasKicker = card.getEffects(EffectSlot.STATIC).stream()
                                .anyMatch(e -> e instanceof KickerEffect);
                        boolean kickedOnlyGreen = hasKicker && pool.getKickedOnlyGreen() > 0;
                        boolean instantSorceryOnlyColorless = (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                                && pool.getInstantSorceryOnlyColorless() > 0;
                        boolean hasRestricted = isArtifact || isMyr || hasRestrictedRedContext || kickedOnlyGreen || instantSorceryOnlyColorless;
                        boolean canAfford = hasRestricted
                                ? cost.canPay(pool, additionalCost, isArtifact, isMyr, hasRestrictedRedContext, kickedOnlyGreen, instantSorceryOnlyColorless)
                                : cost.canPay(pool, additionalCost);
                        if (canAfford && card.isRequiresCreatureMana()) {
                            canAfford = cost.canPayCreatureOnly(pool, additionalCost);
                        }
                        if (canAfford) {
                            playable.add(i);
                        } else if (card.getKeywords().contains(Keyword.CONVOKE)) {
                            // Check if castable with convoke: mana pool + untapped creatures >= total cost
                            int convokeCreatures = extraConvokeMana > 0 ? extraConvokeMana : untappedCreatureCount;
                            int totalAvailable = pool.getTotal() + convokeCreatures;
                            if (totalAvailable >= cost.getManaValue() + additionalCost) {
                                playable.add(i);
                            }
                        }
                        if (!playable.contains(i)) {
                            // Check if castable with sacrifice-for-cost-reduction (e.g. Torgaar)
                            SacrificeCreaturesForCostReductionEffect sacReduce =
                                    (SacrificeCreaturesForCostReductionEffect) card.getEffects(EffectSlot.STATIC).stream()
                                            .filter(SacrificeCreaturesForCostReductionEffect.class::isInstance)
                                            .findFirst().orElse(null);
                            if (sacReduce != null) {
                                int creatureCount = 0;
                                if (battlefield != null) {
                                    for (Permanent perm : battlefield) {
                                        if (gameQueryService.isCreature(gameData, perm)) {
                                            creatureCount++;
                                        }
                                    }
                                }
                                int maxReduction = creatureCount * sacReduce.reductionPerCreature();
                                if (cost.canPay(pool, additionalCost - maxReduction)) {
                                    playable.add(i);
                                }
                            }
                        }
                        // Check non-zero alternative cost from battlefield (e.g. Jodah)
                        if (!playable.contains(i) && canAffordAlternativeCostFromBattlefield(gameData, playerId, card, pool, additionalCost)) {
                            playable.add(i);
                        }
                        if (!playable.contains(i) && canAlternateCast(gameData, playerId, card, battlefield)) {
                            playable.add(i);
                        }
                    }
                }
            } else if (card.getManaCost() == null && canAlternateCast(gameData, playerId, card, battlefield)) {
                // Card with no mana cost but has alternate cost (e.g. some future cards)
                boolean isInstantSpeed = card.hasType(CardType.INSTANT)
                        || card.getKeywords().contains(Keyword.FLASH)
                        || hasFlashGrantForCard(gameData, playerId, card);
                boolean canCastTiming = isInstantSpeed || (isActivePlayer && isMainPhase && stackEmpty);
                if (canCastTiming && !spellLimitReached && !cantCastDueToAttack) {
                    playable.add(i);
                }
            }
        }

        // MTG rule 601.2c: a spell can't be cast unless a legal set of targets can be chosen for it
        playable.removeIf(i -> {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) return false;
            return card.isNeedsSpellCastTarget() && !validTargetService.hasValidTargetsForSpell(gameData, card, playerId);
        });

        // MTG rule 601.2b: can't cast if additional cost requiring N graveyard cards can't be paid
        playable.removeIf(i -> {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) return false;
            ExileNCardsFromGraveyardCost exileCost = (ExileNCardsFromGraveyardCost) card.getEffects(EffectSlot.SPELL).stream()
                    .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                    .findFirst().orElse(null);
            if (exileCost == null) return false;
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
            long matchingCount = graveyard.stream()
                    .filter(c -> exileCost.requiredType() == null || c.hasType(exileCost.requiredType()))
                    .count();
            return matchingCount < exileCost.count();
        });

        // MTG rule 714.1: can't cast a legendary sorcery unless you control a legendary creature or planeswalker
        playable.removeIf(i -> {
            Card card = hand.get(i);
            if (!card.getSupertypes().contains(CardSupertype.LEGENDARY)) return false;
            if (!card.hasType(CardType.SORCERY)) return false;
            return !controlsLegendaryCreatureOrPlaneswalker(gameData, playerId);
        });

        return playable;
    }

    public List<Integer> getPlayableGraveyardLandIndices(GameData gameData, UUID playerId) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        if (!canPlayLandsFromGraveyard(gameData, playerId)) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        boolean stackEmpty = gameData.stack.isEmpty();

        if (!isActivePlayer || !isMainPhase || landsPlayed >= 1 || !stackEmpty) {
            return playable;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            return playable;
        }

        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).hasType(CardType.LAND)) {
                playable.add(i);
            }
        }

        return playable;
    }

    public List<Integer> getPlayableFlashbackIndices(GameData gameData, UUID playerId) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = getMaxSpellsPerTurn(gameData);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttack = isPlayerPreventedFromCasting(gameData, playerId);
        Optional<UUID> graveyardCastSourceId = findGraveyardCastSourcePermanentId(gameData, playerId);
        Set<CardType> typesCastFromGraveyard = graveyardCastSourceId
                .map(id -> gameData.permanentTypesCastFromGraveyardThisTurn.getOrDefault(id, Set.of()))
                .orElse(Set.of());

        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            if (spellLimitReached || cantCastDueToAttack) {
                continue;
            }

            var flashback = card.getCastingOption(FlashbackCast.class);
            var graveyardCast = card.getCastingOption(GraveyardCast.class);
            boolean grantedFlashback = flashback.isEmpty()
                    && gameData.cardsGrantedFlashbackUntilEndOfTurn.contains(card.getId());
            boolean emblemFlashback = flashback.isEmpty() && !grantedFlashback
                    && hasEmblemGrantedFlashback(gameData, playerId, card);

            // Check if this card is castable via a Muldrotha-style graveyard permanent cast effect
            boolean isGrantedGraveyardCast = false;
            if (flashback.isEmpty() && !grantedFlashback && !emblemFlashback && graveyardCast.isEmpty()
                    && graveyardCastSourceId.isPresent()) {
                // Card must be a non-land permanent type with at least one unused type slot
                isGrantedGraveyardCast = hasUnusedPermanentTypeSlot(card, typesCastFromGraveyard);
            }

            if (flashback.isEmpty() && !grantedFlashback && !emblemFlashback && graveyardCast.isEmpty()
                    && !isGrantedGraveyardCast) {
                continue;
            }

            boolean isGraveyardCast = graveyardCast.isPresent() && flashback.isEmpty() && !grantedFlashback && !emblemFlashback;
            boolean isInstantSpeed = card.hasType(CardType.INSTANT);
            boolean canCastTiming = isInstantSpeed || (isActivePlayer && isMainPhase && stackEmpty);
            if (!canCastTiming) {
                continue;
            }

            // GraveyardCast, granted flashback, emblem flashback, and granted graveyard cast use the card's mana cost
            String manaCostStr = (isGraveyardCast || grantedFlashback || emblemFlashback || isGrantedGraveyardCast)
                    ? card.getManaCost()
                    : flashback.get().getCost(ManaCastingCost.class).map(ManaCastingCost::manaCost).orElse(null);
            if (manaCostStr == null) {
                continue;
            }
            ManaCost cost = new ManaCost(manaCostStr);
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int additionalCost = getCastCostModifier(gameData, playerId, card);
            if (!cost.canPay(pool, additionalCost)) {
                continue;
            }

            // For GraveyardCast with ExileNCardsFromGraveyardCost, check that enough
            // qualifying cards exist in the graveyard (excluding the card being cast)
            if (isGraveyardCast) {
                ExileNCardsFromGraveyardCost exileNCost = card.getEffects(EffectSlot.SPELL).stream()
                        .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                        .map(ExileNCardsFromGraveyardCost.class::cast)
                        .findFirst().orElse(null);
                if (exileNCost != null) {
                    long availableCards = graveyard.stream()
                            .filter(c -> c != card)
                            .filter(c -> exileNCost.requiredType() == null || c.hasType(exileNCost.requiredType()))
                            .count();
                    if (availableCards < exileNCost.count()) {
                        continue;
                    }
                }
            }

            playable.add(i);
        }

        return playable;
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
        int maxSpells = getMaxSpellsPerTurn(gameData);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttackExile = isPlayerPreventedFromCasting(gameData, playerId);
        Set<CardType> restrictedSpellTypes = getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = getForbiddenCardNames(gameData);

        // Collect card IDs castable via AllowCastFromCardsExiledWithSourceEffect
        Set<UUID> castableFromExileWithSource = getCastableExiledCardIds(gameData, playerId);

        List<Card> exiledCards = gameData.getPlayerExiledCards(playerId);
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
                if (isActivePlayer && isMainPhase && landsPlayed < 1 && stackEmpty) {
                    playable.add(cardViewFactory.create(card));
                }
                continue;
            }

            if (card.getManaCost() == null || spellLimitReached || cantCastDueToAttackExile) continue;
            if (restrictedSpellTypes.contains(card.getType())
                    || card.getAdditionalTypes().stream().anyMatch(restrictedSpellTypes::contains)) continue;
            if (forbiddenCardNames.contains(card.getName())) continue;

            boolean isInstantSpeed = card.hasType(CardType.INSTANT)
                    || card.getKeywords().contains(Keyword.FLASH)
                    || hasFlashGrantForCard(gameData, playerId, card);
            boolean canCastTiming = isInstantSpeed || (isActivePlayer && isMainPhase && stackEmpty);

            if (canCastTiming) {
                if (hasAlternativeZeroCostFromBattlefield(gameData, playerId, card)) {
                    playable.add(cardViewFactory.create(card));
                } else {
                    ManaCost cost = new ManaCost(card.getManaCost());
                    int additionalCost = getCastCostModifier(gameData, playerId, card);
                    boolean isArtifact = card.hasType(CardType.ARTIFACT);
                    boolean isMyr = card.getSubtypes().contains(CardSubtype.MYR);
                    boolean hasRestrictedRedContext = isArtifact
                            || card.hasType(CardType.CREATURE);
                    boolean canAfford = (isArtifact || isMyr || hasRestrictedRedContext)
                            ? cost.canPay(pool, additionalCost, isArtifact, isMyr, hasRestrictedRedContext)
                            : cost.canPay(pool, additionalCost);
                    // Check non-zero alternative cost from battlefield (e.g. Jodah)
                    if (!canAfford) {
                        canAfford = canAffordAlternativeCostFromBattlefield(gameData, playerId, card, pool, additionalCost);
                    }
                    if (canAfford) {
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
        Set<CardType> castableTypes = getCastableTypesFromTopOfLibrary(gameData, playerId);
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

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = getMaxSpellsPerTurn(gameData);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttack = isPlayerPreventedFromCasting(gameData, playerId);
        Set<CardType> restrictedSpellTypes = getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = getForbiddenCardNames(gameData);

        if (spellLimitReached || cantCastDueToAttack) return playable;
        if (restrictedSpellTypes.contains(topCard.getType())
                || topCard.getAdditionalTypes().stream().anyMatch(restrictedSpellTypes::contains)) return playable;
        if (forbiddenCardNames.contains(topCard.getName())) return playable;

        boolean isInstantSpeed = topCard.hasType(CardType.INSTANT)
                || topCard.getKeywords().contains(Keyword.FLASH)
                || hasFlashGrantForCard(gameData, playerId, topCard);
        boolean canCastTiming = isInstantSpeed || (isActivePlayer && isMainPhase && stackEmpty);

        if (!canCastTiming) return playable;

        // Check if spell requires a legal target (MTG rule 601.2c)
        if (topCard.isNeedsSpellCastTarget() && !validTargetService.hasValidTargetsForSpell(gameData, topCard, playerId)) {
            return playable;
        }

        if (hasAlternativeZeroCostFromBattlefield(gameData, playerId, topCard)) {
            playable.add(cardViewFactory.create(topCard));
        } else {
            ManaCost cost = new ManaCost(topCard.getManaCost());
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int additionalCost = getCastCostModifier(gameData, playerId, topCard);
            boolean canAfford = cost.canPay(pool, additionalCost);
            if (!canAfford) {
                canAfford = canAffordAlternativeCostFromBattlefield(gameData, playerId, topCard, pool, additionalCost);
            }
            if (canAfford) {
                playable.add(cardViewFactory.create(topCard));
            }
        }

        return playable;
    }

    /**
     * Returns the set of card types that the player can cast from the top of their library,
     * based on AllowCastFromTopOfLibraryEffect on their permanents.
     */
    public Set<CardType> getCastableTypesFromTopOfLibrary(GameData gameData, UUID playerId) {
        Set<CardType> castableTypes = new HashSet<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return castableTypes;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AllowCastFromTopOfLibraryEffect allow) {
                    castableTypes.addAll(allow.castableTypes());
                }
            }
        }
        return castableTypes;
    }

    /**
     * Returns the set of exiled card IDs that the player can cast via
     * {@link AllowCastFromCardsExiledWithSourceEffect} on their permanents.
     */
    private Set<UUID> getCastableExiledCardIds(GameData gameData, UUID playerId) {
        Set<UUID> castableIds = new HashSet<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return castableIds;
        for (Permanent perm : battlefield) {
            boolean hasEffect = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof AllowCastFromCardsExiledWithSourceEffect);
            if (hasEffect) {
                List<Card> exiledWithPerm = gameData.getCardsExiledByPermanent(perm.getId());
                for (Card c : exiledWithPerm) {
                    castableIds.add(c.getId());
                }
            }
        }
        return castableIds;
    }

    private boolean hasFlashGrantForCard(GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantFlashToCardTypeEffect grant) {
                    if (gameQueryService.matchesCardPredicate(card, grant.filter(), null)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasAlternativeZeroCostFromBattlefield(GameData gameData, UUID playerId, Card card) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AlternativeCostForSpellsEffect altCost
                        && new ManaCost(altCost.manaCost()).getManaValue() == 0
                        && gameQueryService.matchesCardPredicate(card, altCost.filter(), null)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any permanent the player controls provides a non-zero alternative mana cost
     * for the given card AND the player's mana pool can pay that alternative cost (plus any modifiers).
     */
    public boolean canAffordAlternativeCostFromBattlefield(GameData gameData, UUID playerId, Card card, ManaPool pool, int additionalCost) {
        return findAffordableAlternativeCostFromBattlefield(gameData, playerId, card, pool, additionalCost) != null;
    }

    /**
     * Returns the mana cost string of an affordable non-zero alternative cost from the battlefield,
     * or null if none exists or none is affordable.
     */
    public String findAffordableAlternativeCostFromBattlefield(GameData gameData, UUID playerId, Card card, ManaPool pool, int additionalCost) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AlternativeCostForSpellsEffect altCost
                        && gameQueryService.matchesCardPredicate(card, altCost.filter(), null)) {
                    ManaCost alternativeManaCost = new ManaCost(altCost.manaCost());
                    if (alternativeManaCost.getManaValue() > 0 && alternativeManaCost.canPay(pool, additionalCost)) {
                        return altCost.manaCost();
                    }
                }
            }
        }
        return null;
    }

    private boolean canAlternateCast(GameData gameData, UUID playerId, Card card, List<Permanent> battlefield) {
        var altCastOpt = card.getCastingOption(AlternateHandCast.class);
        if (altCastOpt.isEmpty()) return false;
        AlternateHandCast altCast = altCastOpt.get();

        var lifeCost = altCast.getCost(LifeCastingCost.class);
        if (lifeCost.isPresent() && gameData.getLife(playerId) < lifeCost.get().amount()) return false;

        var sacCost = altCast.getCost(SacrificePermanentsCost.class);
        if (sacCost.isPresent()) {
            if (battlefield == null) return false;
            long matchingCount = battlefield.stream()
                    .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, sacCost.get().filter()))
                    .count();
            if (matchingCount < sacCost.get().count()) return false;
        }

        return true;
    }

    private boolean canPlayLandsFromGraveyard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PlayLandsFromGraveyardEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the permanent ID of the first permanent the player controls that has
     * CastPermanentSpellsFromGraveyardEffect, or empty if none.
     * The returned UUID is used to key per-instance graveyard cast tracking.
     */
    public Optional<UUID> findGraveyardCastSourcePermanentId(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return Optional.empty();
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CastPermanentSpellsFromGraveyardEffect) {
                    return Optional.of(perm.getId());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns true if the card has at least one non-land permanent type whose slot
     * has not been used this turn (for Muldrotha-style graveyard casting).
     */
    public static boolean hasUnusedPermanentTypeSlot(Card card, Set<CardType> typesCastFromGraveyard) {
        // Check primary type
        CardType primary = card.getType();
        if (primary.isPermanentType() && primary != CardType.LAND && !typesCastFromGraveyard.contains(primary)) {
            return true;
        }
        // Check additional types
        for (CardType t : card.getAdditionalTypes()) {
            if (t.isPermanentType() && t != CardType.LAND && !typesCastFromGraveyard.contains(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the player is prevented from casting spells (e.g. Angelic Arbiter:
     * "Each opponent who attacked with a creature this turn can't cast spells").
     */
    boolean isPlayerPreventedFromCasting(GameData gameData, UUID playerId) {
        if (gameData.playersSilencedThisTurn.contains(playerId)) return true;

        if (!gameData.playersDeclaredAttackersThisTurn.contains(playerId)) return false;

        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsCantCastSpellsIfAttackedThisTurnEffect) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasEmblemGrantedFlashback(GameData gameData, UUID playerId, Card card) {
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(playerId)) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof EmblemGrantsFlashbackEffect egf) {
                    for (CardType type : egf.cardTypes()) {
                        if (card.hasType(type)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    int getMaxSpellsPerTurn(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof LimitSpellsPerTurnEffect limit) {
                        return limit.maxSpells();
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    Set<CardType> getRestrictedSpellTypes(GameData gameData, UUID playerId) {
        Set<CardType> restricted = EnumSet.noneOf(CardType.class);
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return restricted;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CantCastSpellTypeEffect cantCast) {
                    restricted.addAll(cantCast.restrictedTypes());
                }
            }
        }
        return restricted;
    }

    Set<String> getForbiddenCardNames(GameData gameData) {
        Set<String> forbidden = new HashSet<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CantCastSpellsWithSameNameAsExiledCardEffect) {
                        Card imprinted = perm.getCard().getImprintedCard();
                        if (imprinted != null) {
                            forbidden.add(imprinted.getName());
                        }
                    }
                    if (effect instanceof SpellsWithChosenNameCantBeCastEffect) {
                        String chosenName = perm.getChosenName();
                        if (chosenName != null) {
                            forbidden.add(chosenName);
                        }
                    }
                }
            }
        }
        return forbidden;
    }

    boolean controlsLegendaryCreatureOrPlaneswalker(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, perm);
            boolean isLegendary = perm.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)
                    || bonus.grantedSupertypes().contains(CardSupertype.LEGENDARY);
            if (isLegendary) {
                if (gameQueryService.isCreature(gameData, perm)
                        || perm.getCard().hasType(CardType.PLANESWALKER)) {
                    return true;
                }
            }
        }
        return false;
    }

    int getOpponentCostIncrease(GameData gameData, UUID playerId, CardType cardType) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, playerId);
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.get(opponentId);
        if (opponentBattlefield == null) return 0;

        int totalIncrease = 0;
        for (Permanent perm : opponentBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof IncreaseOpponentCastCostEffect increase) {
                    if (increase.affectedTypes().contains(cardType)) {
                        totalIncrease += increase.amount();
                    }
                }
            }
        }
        return totalIncrease;
    }

    /**
     * Returns true if the player is allowed to cast this spell considering non-mana
     * restrictions: spell limit, type restrictions, forbidden names, silence, etc.
     */
    public boolean isSpellCastingAllowed(GameData gameData, UUID playerId, Card card) {
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = getMaxSpellsPerTurn(gameData);
        if (spellsCast >= maxSpells) return false;
        if (isPlayerPreventedFromCasting(gameData, playerId)) return false;
        Set<CardType> restricted = getRestrictedSpellTypes(gameData, playerId);
        if (restricted.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(restricted::contains)) return false;
        Set<String> forbidden = getForbiddenCardNames(gameData);
        if (forbidden.contains(card.getName())) return false;
        // MTG rule 714.1: legendary sorceries require controlling a legendary creature or planeswalker
        if (card.getSupertypes().contains(CardSupertype.LEGENDARY)
                && card.hasType(CardType.SORCERY)
                && !controlsLegendaryCreatureOrPlaneswalker(gameData, playerId)) return false;
        return true;
    }

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card) {
        int increase = getOpponentCostIncrease(gameData, playerId, card.getType());
        increase += getSpellCastTaxIncrease(gameData, playerId);
        int reduction = getOwnCostReduction(gameData, playerId, card);
        return increase - reduction;
    }

    private int getSpellCastTaxIncrease(GameData gameData, UUID playerId) {
        int taxAmount = 0;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null) {
                for (Permanent perm : bf) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof IncreaseEachPlayerCastCostPerSpellThisTurnEffect tax) {
                            taxAmount += tax.amountPerSpell();
                        }
                    }
                }
            }
        }
        if (taxAmount == 0) return 0;
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        return taxAmount * spellsCast;
    }

    private int getOwnCostReduction(GameData gameData, UUID playerId, Card card) {
        int reduction = 0;
        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect reduceEffect
                    && anyOpponentControlsAtLeastNMoreCreatures(gameData, playerId, reduceEffect.minimumCreatureDifference())) {
                reduction += reduceEffect.amount();
            }
            if (effect instanceof ReduceOwnCastCostIfMetalcraftEffect metalcraftReduce) {
                if (gameQueryService.isMetalcraftMet(gameData, playerId)) {
                    reduction += metalcraftReduce.amount();
                }
            }
            if (effect instanceof ReduceOwnCastCostPerCreatureOnBattlefieldEffect perCreatureReduce) {
                int totalCreatures = countCreaturesOnAllBattlefields(gameData);
                reduction += perCreatureReduce.amountPerCreature() * totalCreatures;
            }
            if (effect instanceof ReduceOwnCastCostIfControlsSubtypeEffect subtypeReduce) {
                if (controlsSubtype(gameData, playerId, subtypeReduce.subtype())) {
                    reduction += subtypeReduce.amount();
                }
            }
        }

        // Cost reduction from battlefield permanents (e.g. Semblance Anvil, Heartless Summoning)
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ReduceOwnCastCostForSharedCardTypeWithImprintEffect reduceEffect) {
                        Card imprinted = perm.getCard().getImprintedCard();
                        if (imprinted != null && sharesCardType(card, imprinted)) {
                            reduction += reduceEffect.amount();
                        }
                    }
                    if (effect instanceof ReduceOwnCastCostForCardTypeEffect cardTypeReduce) {
                        if (cardTypeReduce.affectedTypes().contains(card.getType())) {
                            reduction += cardTypeReduce.amount();
                        }
                    }
                    if (effect instanceof ReduceOwnCastCostForSubtypeEffect subtypeReduce) {
                        for (CardSubtype subtype : subtypeReduce.affectedSubtypes()) {
                            if (card.getSubtypes().contains(subtype)) {
                                reduction += subtypeReduce.amount();
                                break;
                            }
                        }
                    }
                    if (effect instanceof ReduceCastCostForMatchingSpellsEffect matchReduce
                            && matchReduce.scope() == CostModificationScope.SELF
                            && gameQueryService.matchesCardPredicate(card, matchReduce.predicate(), null)) {
                        reduction += matchReduce.amount();
                    }
                }
            }
        }

        // Cost reduction from opponent's battlefield permanents (OPPONENT-scoped)
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(playerId)) continue;
            List<Permanent> opponentBf = gameData.playerBattlefields.get(opponentId);
            if (opponentBf != null) {
                for (Permanent perm : opponentBf) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof ReduceCastCostForMatchingSpellsEffect matchReduce
                                && matchReduce.scope() == CostModificationScope.OPPONENT
                                && gameQueryService.matchesCardPredicate(card, matchReduce.predicate(), null)) {
                            reduction += matchReduce.amount();
                        }
                    }
                }
            }
        }

        return reduction;
    }

    private boolean controlsSubtype(GameData gameData, UUID playerId, CardSubtype subtype) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent p : battlefield) {
            if (p.getCard().getSubtypes().contains(subtype)
                    || p.getTransientSubtypes().contains(subtype)
                    || p.getGrantedSubtypes().contains(subtype)
                    || p.hasKeyword(Keyword.CHANGELING)) {
                return true;
            }
        }
        return false;
    }

    private boolean sharesCardType(Card spell, Card imprinted) {
        EnumSet<CardType> spellTypes = EnumSet.of(spell.getType());
        spellTypes.addAll(spell.getAdditionalTypes());

        EnumSet<CardType> imprintedTypes = EnumSet.of(imprinted.getType());
        imprintedTypes.addAll(imprinted.getAdditionalTypes());

        for (CardType type : spellTypes) {
            if (imprintedTypes.contains(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean anyOpponentControlsAtLeastNMoreCreatures(GameData gameData, UUID playerId, int minimumDifference) {
        int yourCreatures = countCreaturesControlled(gameData, playerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(playerId)) {
                continue;
            }
            int opponentCreatures = countCreaturesControlled(gameData, candidateOpponentId);
            if (opponentCreatures >= yourCreatures + minimumDifference) {
                return true;
            }
        }
        return false;
    }

    private int countCreaturesControlled(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                count++;
            }
        }
        return count;
    }

    private int countCreaturesOnAllBattlefields(GameData gameData) {
        int total = 0;
        for (UUID pid : gameData.orderedPlayerIds) {
            total += countCreaturesControlled(gameData, pid);
        }
        return total;
    }

    public int getAttackPaymentPerCreature(GameData gameData, UUID attackingPlayerId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, attackingPlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        if (defenderBattlefield == null) return 0;

        int totalTax = 0;
        for (Permanent perm : defenderBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RequirePaymentToAttackEffect tax) {
                    totalTax += tax.amountPerAttacker();
                }
            }
        }
        return totalTax;
    }

    public List<ManaColor> getPhyrexianAttackPaymentsPerCreature(GameData gameData, UUID attackingPlayerId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, attackingPlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        if (defenderBattlefield == null) return List.of();

        List<ManaColor> payments = new java.util.ArrayList<>();
        for (Permanent perm : defenderBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RequirePhyrexianPaymentToAttackEffect tax) {
                    payments.add(tax.color());
                }
            }
        }
        return payments;
    }

    public JoinGame getJoinGame(GameData data, UUID playerId) {
        List<CardView> hand = playerId != null
                ? data.playerHands.getOrDefault(playerId, List.of()).stream().map(cardViewFactory::create).toList()
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
                new ArrayList<>(data.gameLog),
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
                    if (effect instanceof CantSearchLibrariesEffect) {
                        if (paidSet == null || !paidSet.contains(perm.getId())) {
                            unpaidCount++;
                        }
                    }
                }
            }
        }
        return unpaidCount * 2;
    }

    public void logAndBroadcast(GameData gameData, String logEntry) {
        gameData.gameLog.add(logEntry);
    }
}


