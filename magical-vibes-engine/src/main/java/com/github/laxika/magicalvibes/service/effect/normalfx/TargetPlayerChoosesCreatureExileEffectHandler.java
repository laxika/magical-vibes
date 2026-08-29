package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCreatureExileEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPlayerChoosesCreatureExileEffect}: the targeted player chooses a creature
 * they control and it is exiled (Doomfall). Unlike the destroy edict, exile ignores regeneration
 * and indestructible and fires no "dies" triggers. With 0 creatures nothing happens; with exactly
 * 1 it is exiled automatically; with 2+ the target player picks which to lose.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerChoosesCreatureExileEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesCreatureExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayerChoosesCreatureExileEffect exileEffect = (TargetPlayerChoosesCreatureExileEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        String cardName = entry.getCard().getName();
        List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, targetPlayerId,
                p -> !exileEffect.nontokenOnly() || !p.getCard().isToken());
        if (exileEffect.greatestPowerOnly()) {
            creatureIds = creaturesWithGreatestPower(gameData, creatureIds);
        }

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no creatures to exile.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no creatures to exile", gameData.id, playerName);
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                if (exileEffect.trackWithSource() && entry.getSourcePermanentId() != null) {
                    exileSupport.exilePermanentAndTrackWithSource(
                            gameData, creature, entry.getSourcePermanentId(), entry.getCard());
                } else {
                    exileSupport.exilePermanentAndLog(gameData, creature, cardName);
                }
            }
            return;
        }

        // Multiple creatures — prompt the target player to choose which one to exile.
        UUID choosingPlayerId = exileEffect.greatestPowerOnly() ? entry.getControllerId() : targetPlayerId;
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DestroyChosenCreature(
                        choosingPlayerId, cardName, true,
                        exileEffect.trackWithSource() ? entry.getSourcePermanentId() : null,
                        exileEffect.trackWithSource() ? entry.getCard() : null));
        playerInputService.beginPermanentChoice(gameData, choosingPlayerId, creatureIds,
                exileEffect.greatestPowerOnly()
                        ? "Choose a creature with greatest power to exile."
                        : "Choose a creature to exile.");
    }

    private List<UUID> creaturesWithGreatestPower(GameData gameData, List<UUID> creatureIds) {
        int greatestPower = creatureIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .mapToInt(permanent -> gameQueryService.getEffectivePower(gameData, permanent))
                .max()
                .orElse(Integer.MIN_VALUE);

        List<UUID> greatestCreatures = new ArrayList<>();
        for (UUID creatureId : creatureIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature != null && gameQueryService.getEffectivePower(gameData, creature) == greatestPower) {
                greatestCreatures.add(creatureId);
            }
        }
        return greatestCreatures;
    }
}
