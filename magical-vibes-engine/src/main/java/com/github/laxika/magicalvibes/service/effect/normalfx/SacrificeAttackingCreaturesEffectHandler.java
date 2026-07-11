package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeAttackingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeAttackingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeAttackingCreaturesEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
                if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                    return;
                }

                // Check metalcraft at resolution time (intervening-if)
                UUID controllerId = entry.getControllerId();
                int count = gameQueryService.isMetalcraftMet(gameData, controllerId)
                        ? e.metalcraftCount() : e.baseCount();

                // Collect attacking creatures on target player's battlefield
                List<UUID> attackingCreatureIds = destructionSupport.collectCreatureIds(gameData, targetPlayerId, Permanent::isAttacking);

                if (attackingCreatureIds.isEmpty()) {
                    String playerName = gameData.playerIdToName.get(targetPlayerId);
                    String logEntry = playerName + " has no attacking creatures to sacrifice.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} has no attacking creatures to sacrifice", gameData.id, playerName);
                    return;
                }

                if (attackingCreatureIds.size() <= count) {
                    // Auto-sacrifice all attacking creatures
                    for (UUID creatureId : attackingCreatureIds) {
                        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
                        if (creature != null) {
                            destructionSupport.sacrificeAndLog(gameData, creature, targetPlayerId);
                        }
                    }
                    return;
                }

                // More attacking creatures than required — prompt player to choose
                playerInputService.beginMultiPermanentChoice(gameData, targetPlayerId, attackingCreatureIds,
                        count, new MultiPermanentChoiceContext.SacrificeAttackingCreatures(),
                        "Choose " + count + " attacking creature" + (count > 1 ? "s" : "") + " to sacrifice.");
    }
}
