package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeparateCreaturesIntoPilesAndDestroyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeparateCreaturesIntoPilesAndDestroyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeparateCreaturesIntoPilesAndDestroyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        List<UUID> creatureIds = battlefield == null
                ? List.of()
                : battlefield.stream()
                        .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                        .map(Permanent::getId)
                        .toList();
        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(playerName + " controls no creatures to separate."));
            return;
        }

        gameData.queueInteraction(new PendingPileSeparation(controllerId, targetPlayerId,
                creatureIds, List.of(), Map.of(), List.of(), List.of(), CardPileDisposition.DESTROY, false));
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, creatureIds, creatureIds.size(),
                "Separate creatures into two piles. Select creatures for Pile 1 (unselected form Pile 2).");
    }
}
