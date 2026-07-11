package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the whole discard family via {@link DiscardEffect}: the {@link DiscardRecipient} routes
 * who discards and {@code random} chooses between chosen and random discard. Single-player
 * recipients (controller / target player) evaluate the amount and discard directly; each-player
 * recipients iterate in APNAP order — chosen discards ride a {@link DiscardFollowUp} queue (each
 * player picks sequentially), random discards run inline. The {@code discardCausedByOpponent} flag
 * — read by discard-punisher triggers (e.g. Raider's Wake) — is set exactly as before: {@code true}
 * for target-player discards and per-player ({@code true} unless that player is the controller) for
 * each-player discards.
 */
@Component
@RequiredArgsConstructor
public class DiscardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardEffect) effect;

        // Source-relative amounts (e.g. CountersOnSource for Shrine of Limitless Power) use the
        // live source permanent when still on the battlefield, else the last-known snapshot
        // (sacrificed as an activation cost). X-based amounts (Mind Shatter) read xValue.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        switch (e.recipient()) {
            case CONTROLLER, TARGET_PLAYER -> resolveSinglePlayer(gameData, entry, e, amount);
            case EACH_PLAYER, EACH_OPPONENT -> resolveEachPlayer(gameData, entry, e, amount);
        }
    }

    private void resolveSinglePlayer(GameData gameData, StackEntry entry, DiscardEffect e, int amount) {
        boolean targeted = e.recipient() == DiscardRecipient.TARGET_PLAYER;
        UUID playerId = targeted ? entry.getTargetId() : entry.getControllerId();
        gameData.discardCausedByOpponent = targeted;

        if (amount <= 0) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + " discards 0 cards.");
            return;
        }

        if (e.random()) {
            playerInteractionSupport.resolveRandomDiscardCards(gameData, playerId,
                    entry.getCard().getName(), amount);
        } else {
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, amount);
        }
    }

    private void resolveEachPlayer(GameData gameData, StackEntry entry, DiscardEffect e, int amount) {
        UUID controllerId = entry.getControllerId();
        UUID activePlayerId = gameData.activePlayerId;
        boolean opponentsOnly = e.recipient() == DiscardRecipient.EACH_OPPONENT;

        if (e.random()) {
            // Random discards need no player choice, so run inline in APNAP order.
            String sourceName = entry.getCard().getName();
            discardRandomForPlayer(gameData, activePlayerId, controllerId, opponentsOnly, sourceName, amount);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(activePlayerId)) {
                    discardRandomForPlayer(gameData, playerId, controllerId, opponentsOnly, sourceName, amount);
                }
            }
            return;
        }

        // Chosen discards: build the APNAP-ordered chooser queue (active player first) and start
        // the first player's discard; the remainder rides the discard choice.
        List<UUID> choosers = new ArrayList<>();
        if (!opponentsOnly || !activePlayerId.equals(controllerId)) {
            choosers.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(activePlayerId)) {
                continue;
            }
            if (opponentsOnly && playerId.equals(controllerId)) {
                continue;
            }
            choosers.add(playerId);
        }
        playerInteractionSupport.startNextEachPlayerDiscard(gameData,
                DiscardFollowUp.eachPlayer(choosers, controllerId, amount));
    }

    private void discardRandomForPlayer(GameData gameData, UUID playerId, UUID controllerId,
            boolean opponentsOnly, String sourceName, int amount) {
        if (opponentsOnly && playerId.equals(controllerId)) {
            return;
        }
        gameData.discardCausedByOpponent = !playerId.equals(controllerId);
        playerInteractionSupport.resolveRandomDiscardCards(gameData, playerId, sourceName, amount);
    }
}
