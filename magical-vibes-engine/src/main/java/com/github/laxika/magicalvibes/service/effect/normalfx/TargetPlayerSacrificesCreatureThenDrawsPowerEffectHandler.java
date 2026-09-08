package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureThenDrawsPowerEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Twisted Justice's target-player sacrifice and power-based draw. */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerSacrificesCreatureThenDrawsPowerEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesCreatureThenDrawsPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        if (!gameQueryService.canEffectCauseSacrifice(gameData, targetPlayerId, entry.getControllerId())) {
            return;
        }

        List<UUID> creatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.cantBeSacrificed(gameData, permanent)) {
                    creatureIds.add(permanent.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no creatures to sacrifice."));
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                sacrificeAndDraw(gameData, creature, targetPlayerId, entry.getControllerId(), entry.getCard());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TargetPlayerSacrificesCreatureThenDrawsPower(
                        targetPlayerId, entry.getControllerId(), entry.getCard()));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                entry.getCard().getName() + " — Choose a creature to sacrifice.");
    }

    public void sacrificeAndDraw(GameData gameData, Permanent creature, UUID sacrificingPlayerId,
                                 UUID drawingPlayerId, Card sourceCard) {
        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, creature));
        destructionSupport.sacrificeAndLog(gameData, creature, sacrificingPlayerId);

        for (int i = 0; i < power; i++) {
            drawService.resolveDrawCard(gameData, drawingPlayerId);
        }

        log.info("Game {} - {} draws {} cards after {} sacrifices {}",
                gameData.id, gameData.playerIdToName.get(drawingPlayerId), power,
                sourceCard.getName(), creature.getCard().getName());
    }
}
