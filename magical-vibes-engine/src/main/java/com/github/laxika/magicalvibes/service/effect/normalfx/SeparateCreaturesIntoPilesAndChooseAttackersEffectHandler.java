package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeparateCreaturesIntoPilesAndChooseAttackersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeparateCreaturesIntoPilesAndChooseAttackersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeparateCreaturesIntoPilesAndChooseAttackersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        List<UUID> creatureIds = battlefield == null
                ? List.of()
                : battlefield.stream()
                        .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                        .map(Permanent::getId)
                        .toList();

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(activePlayerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no creatures to separate."));
            log.info("Game {} - {} has no creatures to separate", gameData.id, playerName);
            return;
        }

        gameData.queueInteraction(new PendingPileSeparation(entry.getControllerId(), activePlayerId,
                creatureIds, List.of(), Map.of(), List.of(), List.of(), CardPileDisposition.ATTACKERS, false));
        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), creatureIds,
                creatureIds.size(), "Separate creatures into two piles. Select creatures for Pile 1 (unselected form Pile 2).");
    }
}
