package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseOrStopContinuationEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Shared coin-flip and continuation helpers for {@link FlipUntilLoseOrStopEffectHandler}. */
@Component
@RequiredArgsConstructor
public class FlipUntilLoseOrStopSupport {

    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;

    /** Performs and logs one coin flip from the stack entry's controller's perspective. */
    public boolean flip(GameData gameData, UUID controllerId, String sourceName) {
        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
        boolean won = result.heads();
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName
                + (won ? " wins" : " loses") + " the coin flip for " + sourceName
                + coinFlipService.replacementDetails(result) + "."));
        return won;
    }

    /** Offers the controller the choice to stop or continue after a win. */
    public void queueContinue(GameData gameData, Card sourceCard, UUID controllerId, UUID targetId,
                              int wins, List<CardEffect> rewards) {
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                sourceCard,
                controllerId,
                List.of(new FlipUntilLoseOrStopContinuationEffect(wins, rewards)),
                sourceCard.getName() + " — Flip again?",
                targetId));
    }

    /** Resumes the parked spell with the achieved rewards in their original order. */
    public void queueRewards(GameData gameData, int wins, List<CardEffect> rewards) {
        List<CardEffect> achieved = new ArrayList<>();
        for (int i = 0; i < Math.min(wins, rewards.size()); i++) {
            achieved.add(rewards.get(i));
        }
        if (achieved.isEmpty()) {
            return;
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("Fiery Gambit continuation has no parked spell resolution");
        }
        entry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, achieved);
    }
}
