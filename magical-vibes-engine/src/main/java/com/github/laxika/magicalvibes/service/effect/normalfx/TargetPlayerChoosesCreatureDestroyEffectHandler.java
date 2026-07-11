package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCreatureDestroyEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPlayerChoosesCreatureDestroyEffect}: the targeted player chooses a creature
 * they control and it is destroyed (Imperial Edict). Unlike the forced-sacrifice edicts, the chosen
 * creature is destroyed, so regeneration and indestructible apply. With 0 creatures nothing happens;
 * with exactly 1 it is destroyed automatically; with 2+ the target player picks which to lose.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerChoosesCreatureDestroyEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesCreatureDestroyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        String cardName = entry.getCard().getName();
        List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, targetPlayerId, p -> true);

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no creatures to destroy.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures to destroy", gameData.id, playerName);
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                destructionSupport.tryDestroyAndLog(gameData, creature, cardName);
            }
            return;
        }

        // Multiple creatures — prompt the target player to choose which one to destroy.
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DestroyChosenCreature(targetPlayerId, cardName));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                "Choose a creature to destroy.");
    }
}
