package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AmuletOfQuozAnteEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.outcome.LossOutcome;
import com.github.laxika.magicalvibes.service.outcome.LossReason;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AmuletOfQuozAnteEffect} (Amulet of Quoz): "Target opponent may ante the top card of
 * their library. If they don't, you flip a coin. If you win the flip, that player loses the game. If
 * you lose the flip, you lose the game."
 *
 * <p>The targeted opponent is the decision maker and is prompted via the may-ability system (the
 * accept/decline branch lives in {@code AmuletOfQuozAnteHandler}). An opponent with an empty library
 * has no top card to ante, so they can't ante and the coin flip happens immediately without a prompt.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AmuletOfQuozAnteEffectHandler implements NormalEffectHandlerBean {

    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AmuletOfQuozAnteEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        if (opponentId == null || !gameData.playerIds.contains(opponentId)) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(opponentId);
        if (library == null || library.isEmpty()) {
            // No top card to ante — the opponent can't ante, so they don't: flip right away.
            performFlip(gameData, entry.getCard(), controllerId, opponentId);
            return;
        }

        // Ask the targeted opponent. Carry the ability's controller — the coin flipper and the player
        // who loses on a lost flip — in the targetCardId slot for the accept/decline branch.
        String prompt = "Ante the top card of your library? If you don't, " + gameData.playerIdToName.get(controllerId)
                + " flips a coin and one of you loses the game. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), opponentId, List.of(effect), prompt, controllerId));
    }

    /**
     * Flips the coin for {@code controllerId} — the ability's controller: on a win the targeted
     * opponent loses the game, on a loss the controller does. Both branches go through
     * {@link GameOutcomeService#resolveLoss} so "can't lose" effects and loss replacers apply.
     */
    public void performFlip(GameData gameData, Card sourceCard, UUID controllerId, UUID opponentId) {
        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
        boolean wonFlip = result.heads();
        String controllerName = gameData.playerIdToName.get(controllerId);

        gameLogService.append(gameData, GameLog.textCardText(
                controllerName + (wonFlip ? " wins" : " loses") + " the coin flip for ", sourceCard,
                coinFlipService.replacementDetails(result) + "."));

        if (wonFlip) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
        }

        UUID losingPlayerId = wonFlip ? opponentId : controllerId;
        UUID winningPlayerId = wonFlip ? controllerId : opponentId;
        String loserName = gameData.playerIdToName.get(losingPlayerId);

        LossOutcome outcome = gameOutcomeService.resolveLoss(gameData, losingPlayerId, LossReason.EFFECT);
        if (outcome == LossOutcome.PREVENTED) {
            gameLogService.append(gameData, GameLog.text(loserName + " can't lose the game."));
            log.info("Game {} - {} can't lose the game (protected)", gameData.id, loserName);
            return;
        }
        if (outcome == LossOutcome.REPLACED) {
            // A replacer (Lich's Mirror) already logged and reset the game — nobody wins.
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(loserName + " loses the game from ", sourceCard, "."));
        log.info("Game {} - {} loses the game from {}", gameData.id, loserName, sourceCard.getName());
        gameOutcomeService.declareWinner(gameData, winningPlayerId);
    }
}
