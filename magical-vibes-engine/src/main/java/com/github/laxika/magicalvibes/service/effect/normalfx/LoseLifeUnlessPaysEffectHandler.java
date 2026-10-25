package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "Target player loses N life unless they pay {M}" (Rhystic Syphon, Isolation Cell, Elesh Norn). A player who can't
 * pay loses the life at once; one who can is asked through the may ability system.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the life loss of a player who can't pay is its first change (asking
 * the player changes nothing yet), so a Lich's Duel Mastery that loss would sacrifice is asked about (CR 616.1)
 * before any life is lost.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoseLifeUnlessPaysEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final com.github.laxika.magicalvibes.service.cast.PotentialManaService potentialManaService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LoseLifeUnlessPaysEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        com.github.laxika.magicalvibes.model.ManaCost cost = new com.github.laxika.magicalvibes.model.ManaCost("{" + e.payAmount() + "}");
        com.github.laxika.magicalvibes.model.ManaPool pool = gameData.playerManaPools.get(targetPlayerId);
        // Mana abilities may be activated while paying, so mana the player could still produce counts.
        boolean canPay = cost.canPayResolutionCost(pool)
                || cost.canPayResolutionCost(potentialManaService.buildVirtualManaPool(gameData, targetPlayerId));

        if (!canPay) {
            // Can't pay — auto-apply life loss
            if (!gameQueryService.canPlayerLoseLife(gameData, targetPlayerId)) {
                gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            } else {
                int lifeLoss = e.lifeLoss();
                lifeSupport.applyLifeLoss(gameData, targetPlayerId, lifeLoss, entry.getCard().getName());
                String logEntry = playerName + " can't pay {" + e.payAmount() + "}. " + playerName + " loses " + lifeLoss + " life.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} loses {} life (can't pay {}, {})",
                        gameData.id, playerName, lifeLoss, e.payAmount(), entry.getCard().getName());
            }
            applyLifeGainIfConfigured(gameData, entry, e);
            return;
        }

        // Can pay — ask the target player via the may ability system
        String prompt = "Pay {" + e.payAmount() + "}? If you don't, you lose " + e.lifeLoss() + " life. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(e), prompt,
                null, null, null, null, 0, 0, null, null, null, null,
                entry.getControllerId(), null, 0
        ));
    }

    private void applyLifeGainIfConfigured(GameData gameData, StackEntry entry,
                                           LoseLifeUnlessPaysEffect effect) {
        if (effect.controllerGainsLifeLost()) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), effect.lifeLoss());
        }
    }
}
