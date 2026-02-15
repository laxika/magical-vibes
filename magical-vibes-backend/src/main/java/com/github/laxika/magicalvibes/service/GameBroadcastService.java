package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
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

    void broadcastGameState(GameData gameData) {
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
        UUID priorityPlayerId = gameData.awaitingInput != null ? null : gameQueryService.getPriorityPlayerId(gameData);

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

            sessionManager.sendToPlayer(playerId, new GameStateMessage(
                    gameData.status, gameData.activePlayerId, gameData.turnNumber,
                    gameData.currentStep, priorityPlayerId,
                    battlefields, stack, graveyards, deckSizes, handSizes, lifeTotals,
                    hand, opponentHand, mulliganCount, manaPool, autoStopSteps, playableCardIndices, newLogEntries
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
                    views.add(permanentViewFactory.create(p, bonus.power(), bonus.toughness(), bonus.keywords(), bonus.animatedCreature()));
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
            totals.add(gameData.playerLifeTotals.getOrDefault(pid, 20));
        }
        return totals;
    }

    List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.awaitingInput != null) {
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

        boolean stackEmpty = gameData.stack.isEmpty();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getType() == CardType.BASIC_LAND && isActivePlayer && isMainPhase && landsPlayed < 1 && stackEmpty) {
                playable.add(i);
            }
            if (card.getType() == CardType.CREATURE && card.getManaCost() != null && !spellLimitReached) {
                boolean hasFlash = card.getKeywords().contains(Keyword.FLASH);
                if (hasFlash || (isActivePlayer && isMainPhase && stackEmpty)) {
                    ManaCost cost = new ManaCost(card.getManaCost());
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.CREATURE);
                    if (cost.canPay(pool, additionalCost)) {
                        playable.add(i);
                    }
                }
            }
            if (card.getType() == CardType.ENCHANTMENT && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.ENCHANTMENT);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.ARTIFACT && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.ARTIFACT);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.SORCERY && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.SORCERY);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.INSTANT && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.INSTANT);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
        }

        return playable;
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

    int getAttackPaymentPerCreature(GameData gameData, UUID attackingPlayerId) {
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

    void logAndBroadcast(GameData gameData, String logEntry) {
        gameData.gameLog.add(logEntry);
    }
}
