package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnHalfCreaturesToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the target-player half-creature return choice. */
@Component
@RequiredArgsConstructor
public class ReturnHalfCreaturesToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnHalfCreaturesToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        UUID chooserId = entry.getControllerId();
        if (targetPlayerId == null || chooserId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<UUID> creatureIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(targetPlayerId, List.of())) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureIds.add(permanent.getId());
            }
        }

        if (creatureIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " has no creatures to return."));
            return;
        }

        int requiredCount = (creatureIds.size() + 1) / 2;
        playerInputService.beginMultiPermanentChoice(gameData, chooserId, creatureIds, requiredCount,
                new MultiPermanentChoiceContext.ReturnTargetPermanentsToHand(requiredCount),
                "Choose " + requiredCount + " creature" + (requiredCount == 1 ? "" : "s")
                        + " to return to their owners' hands.");
    }
}
