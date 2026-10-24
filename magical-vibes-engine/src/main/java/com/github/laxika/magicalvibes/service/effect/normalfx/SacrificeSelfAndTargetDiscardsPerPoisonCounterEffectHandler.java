package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificeSelfAndTargetDiscardsPerPoisonCounterEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        

        UUID targetPlayerId = entry.getTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        if (targetPlayerId == null || sourcePermanentId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles — source no longer on the battlefield."));
            return;
        }

        if (!entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, sourcePermanentId))) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " can't be sacrificed \u2014 its controller no longer controls it."));
            return;
        }

        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, sourcePermanentId, this);
    }

    /** "If you do, that player discards a card for each poison counter they have." */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (!entry.getResolutionResults().anySacrificed()) {
            return;
        }
        int poisonCounters = gameData.playerPoisonCounters.getOrDefault(targetPlayerId, 0);
        if (poisonCounters <= 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no poison counters — no cards to discard."));
            return;
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " must discard " + poisonCounters + " card" + (poisonCounters > 1 ? "s" : "") + " (", entry.getCard(), ")."));

        playerInteractionSupport.resolveDiscardCards(gameData, targetPlayerId, poisonCounters);
    
    }
}
