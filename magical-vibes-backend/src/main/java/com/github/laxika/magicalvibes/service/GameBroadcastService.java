package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;

import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSharedCardTypeWithImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfMetalcraftEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePhyrexianPaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        List<List<CardView>> revealedLibraryTopCards = getRevealedLibraryTopCards(gameData);
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
                }
            }

            sessionManager.sendToPlayer(playerId, new GameStateMessage(
                    gameData.status, gameData.activePlayerId, gameData.turnNumber,
                    gameData.currentStep, priorityPlayerId,
                    battlefields, stack, graveyards, deckSizes, handSizes, lifeTotals, poisonCounters,
                    hand, opponentHand, mulliganCount, manaPool, autoStopSteps, playableCardIndices,
                    playableGraveyardLandIndices, playableExileCards, newLogEntries, searchTaxCost,
                    gameData.mindControlledPlayerId, revealedLibraryTopCards, playableFlashbackIndices
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
                    views.add(permanentViewFactory.create(p, bonus.power(), bonus.toughness(), bonus.keywords(), bonus.animatedCreature(), bonus.grantedActivatedAbilities(), bonus.grantedColors(), bonus.grantedSubtypes(), bonus.grantedCardTypes(), bonus.colorOverriding(), bonus.subtypeOverriding(), bonus.landSubtypeOverriding(), bonus.removedKeywords()));
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

    List<List<CardView>> getRevealedLibraryTopCards(GameData data) {
        // Determine which players have their top card revealed (e.g. Vampire Nocturnus)
        Set<UUID> revealedPlayerIds = new HashSet<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof PlayWithTopCardRevealedEffect) {
                        revealedPlayerIds.add(pid);
                        break;
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
        int spellsCast = gameData.spellsCastThisTurn.getOrDefault(playerId, 0);
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
                    ManaCost cost = new ManaCost(card.getManaCost());
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    int additionalCost = getCastCostModifier(gameData, playerId, card);
                    boolean isArtifact = card.hasType(CardType.ARTIFACT);
                    boolean isMyr = card.getSubtypes().contains(CardSubtype.MYR);
                    boolean hasRestrictedRedContext = isArtifact
                            || card.hasType(CardType.CREATURE);
                    boolean canAfford = (isArtifact || isMyr || hasRestrictedRedContext)
                            ? cost.canPay(pool, additionalCost, isArtifact, isMyr, hasRestrictedRedContext)
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
                    if (!playable.contains(i) && canAlternateCast(gameData, playerId, card, battlefield)) {
                        playable.add(i);
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
        int spellsCast = gameData.spellsCastThisTurn.getOrDefault(playerId, 0);
        int maxSpells = getMaxSpellsPerTurn(gameData);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttack = isPlayerPreventedFromCasting(gameData, playerId);

        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            var flashback = card.getCastingOption(FlashbackCast.class);
            if (flashback.isEmpty() || spellLimitReached || cantCastDueToAttack) {
                continue;
            }

            boolean isInstantSpeed = card.hasType(CardType.INSTANT);
            boolean canCastTiming = isInstantSpeed || (isActivePlayer && isMainPhase && stackEmpty);
            if (!canCastTiming) {
                continue;
            }

            var manaCostOpt = flashback.get().getCost(ManaCastingCost.class);
            if (manaCostOpt.isEmpty()) {
                continue;
            }
            ManaCost cost = new ManaCost(manaCostOpt.get().manaCost());
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int additionalCost = getCastCostModifier(gameData, playerId, card);
            if (cost.canPay(pool, additionalCost)) {
                playable.add(i);
            }
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
        int spellsCast = gameData.spellsCastThisTurn.getOrDefault(playerId, 0);
        int maxSpells = getMaxSpellsPerTurn(gameData);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttackExile = isPlayerPreventedFromCasting(gameData, playerId);
        Set<CardType> restrictedSpellTypes = getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = getForbiddenCardNames(gameData);

        List<Card> exiledCards = gameData.playerExiledCards.get(playerId);
        if (exiledCards == null) {
            return playable;
        }

        ManaPool pool = gameData.playerManaPools.get(playerId);

        for (Card card : exiledCards) {
            UUID permittedPlayer = gameData.exilePlayPermissions.get(card.getId());
            if (permittedPlayer == null || !permittedPlayer.equals(playerId)) {
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
                ManaCost cost = new ManaCost(card.getManaCost());
                int additionalCost = getCastCostModifier(gameData, playerId, card);
                boolean isArtifact = card.hasType(CardType.ARTIFACT);
                boolean isMyr = card.getSubtypes().contains(CardSubtype.MYR);
                boolean hasRestrictedRedContext = isArtifact
                        || card.hasType(CardType.CREATURE);
                boolean canAfford = (isArtifact || isMyr || hasRestrictedRedContext)
                        ? cost.canPay(pool, additionalCost, isArtifact, isMyr, hasRestrictedRedContext)
                        : cost.canPay(pool, additionalCost);
                if (canAfford) {
                    playable.add(cardViewFactory.create(card));
                }
            }
        }

        return playable;
    }

    private boolean hasFlashGrantForCard(GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantFlashToCardTypeEffect grant) {
                    if (grant.cardType() == null
                            || card.hasType(grant.cardType())) {
                        return true;
                    }
                }
            }
        }
        return false;
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
                }
            }
        }
        return forbidden;
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

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card) {
        int increase = getOpponentCostIncrease(gameData, playerId, card.getType());
        int reduction = getOwnCostReduction(gameData, playerId, card);
        return increase - reduction;
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
        }

        // Cost reduction from battlefield permanents with imprinted cards (e.g. Semblance Anvil)
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
                }
            }
        }

        return reduction;
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


