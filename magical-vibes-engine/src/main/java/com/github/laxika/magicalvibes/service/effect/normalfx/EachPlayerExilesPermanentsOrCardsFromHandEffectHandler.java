package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesPermanentsOrCardsFromHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerExilesPermanentsOrCardsFromHandEffect}: every player picks X objects
 * from their own battlefield and/or hand, and all of them are exiled together once the last
 * player has chosen.
 *
 * <p>The prompts run in APNAP order (the engine can't take simultaneous answers), but nothing is
 * exiled until the queue drains, so each player still chooses against the same board. A player
 * with fewer than X objects exiles all of them without being prompted; X = 0 does nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerExilesPermanentsOrCardsFromHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerExilesPermanentsOrCardsFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerExilesPermanentsOrCardsFromHandEffect e =
                (EachPlayerExilesPermanentsOrCardsFromHandEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int count = amountEvaluationService.evaluate(
                gameData, e.amount(), AmountContext.forStackEntry(entry, source));
        if (count <= 0) {
            return;
        }

        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "the source";
        UUID excludedPlayerId = e.opponentsOnly() ? entry.getControllerId() : null;
        beginNextPlayer(gameData, apnapOrder(gameData, excludedPlayerId), List.of(), count, sourceName);
    }

    /**
     * Prompt the next player who still has a real choice, or apply every accumulated pick when the
     * queue is empty. Players whose objects number at most {@code count} have nothing to decide —
     * everything they own goes, so their ids are folded straight into the accumulator.
     */
    public void beginNextPlayer(GameData gameData, List<UUID> remainingPlayerIds,
            List<UUID> accumulatedCardIds, int count, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        List<UUID> accumulated = new ArrayList<>(accumulatedCardIds);

        while (!remaining.isEmpty()) {
            UUID playerId = remaining.removeFirst();
            List<UUID> choosableIds = choosableCardIds(gameData, playerId);
            if (choosableIds.isEmpty()) {
                continue;
            }
            if (choosableIds.size() <= count) {
                accumulated.addAll(choosableIds);
                continue;
            }

            playerInputService.beginExilePermanentsOrHandCardsChoice(gameData,
                    new PendingInteraction.ExilePermanentsOrHandCardsChoice(playerId, choosableIds,
                            count, List.copyOf(remaining), List.copyOf(accumulated), sourceName));
            return;
        }

        applyExiles(gameData, accumulated, sourceName);
    }

    /**
     * Choice completion: record this player's picks (topping up from their remaining objects if
     * the answer came in short — the exile is mandatory), then prompt the next player or apply.
     */
    public void completeChoice(GameData gameData, List<UUID> chosenCardIds,
            PendingInteraction.ExilePermanentsOrHandCardsChoice interaction) {
        Set<UUID> picks = new LinkedHashSet<>(chosenCardIds);
        int required = Math.min(interaction.count(), interaction.validCardIds().size());
        for (UUID candidate : interaction.validCardIds()) {
            if (picks.size() >= required) {
                break;
            }
            picks.add(candidate);
        }

        List<UUID> accumulated = new ArrayList<>(interaction.accumulatedCardIds());
        accumulated.addAll(picks);
        beginNextPlayer(gameData, interaction.remainingPlayerIds(), accumulated,
                interaction.count(), interaction.sourceName());
    }

    /** Exile every chosen object at once, permanents and hand cards alike. */
    private void applyExiles(GameData gameData, List<UUID> chosenCardIds, String sourceName) {
        Set<UUID> chosen = new HashSet<>(chosenCardIds);
        if (chosen.isEmpty()) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            int exiled = 0;

            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent perm : List.copyOf(battlefield)) {
                    if (chosen.contains(perm.getCard().getId())
                            && permanentRemovalService.removePermanentToExile(gameData, perm)) {
                        exiled++;
                    }
                }
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand != null) {
                List<Card> toExile = hand.stream().filter(c -> chosen.contains(c.getId())).toList();
                hand.removeAll(toExile);
                for (Card card : toExile) {
                    gameData.addToExile(playerId, card);
                }
                exiled += toExile.size();
            }

            if (exiled > 0) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData, GameLog.text(playerName + " exiles " + exiled
                        + (exiled == 1 ? " object" : " objects") + " (" + sourceName + ")."));
                log.info("Game {} - {} exiles {} object(s) for {}", gameData.id, playerName, exiled,
                        sourceName);
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /** Card ids of everything this player could exile: their permanents first, then their hand. */
    private List<UUID> choosableCardIds(GameData gameData, UUID playerId) {
        List<UUID> ids = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            battlefield.forEach(perm -> ids.add(perm.getCard().getId()));
        }
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand != null) {
            hand.forEach(card -> ids.add(card.getId()));
        }
        return ids;
    }

    /** Every player, active player first (CR 101.4). */
    private List<UUID> apnapOrder(GameData gameData, UUID excludedPlayerId) {
        List<UUID> ordered = gameData.orderedPlayerIds.stream()
                .filter(playerId -> excludedPlayerId == null || !playerId.equals(excludedPlayerId))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
