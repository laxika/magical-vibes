package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfDefendingPlayerCreatureAndAttackEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Midnight Crusader Shuttle's control-and-attack alternative. */
@Component
@RequiredArgsConstructor
@Slf4j
public class GainControlOfDefendingPlayerCreatureAndAttackEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfDefendingPlayerCreatureAndAttackEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID defendingPlayerId = defendingPlayerId(gameData, entry.getAttackedTargetId());
        if (defendingPlayerId == null) {
            return;
        }

        List<UUID> creatureIds = creatureIdsControlledBy(gameData, defendingPlayerId);
        if (creatureIds.isEmpty()) {
            return;
        }
        if (creatureIds.size() == 1) {
            apply(gameData, entry.getControllerId(), defendingPlayerId, creatureIds.getFirst(),
                    entry.getSourcePermanentId(), entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.GainControlOfDefendingPlayerCreatureAndAttack(
                        entry.getControllerId(), defendingPlayerId, entry.getSourcePermanentId(),
                        entry.getCard().getName()));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), creatureIds,
                "Choose a creature to gain control of and attack with.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.GainControlOfDefendingPlayerCreatureAndAttack context) {
        apply(gameData, context.controllerId(), context.defendingPlayerId(), permanentId,
                context.sourcePermanentId(), context.sourceCardName());
    }

    private void apply(GameData gameData, UUID newControllerId, UUID defendingPlayerId,
                       UUID permanentId, UUID sourcePermanentId, String sourceCardName) {
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        if (creature == null || !defendingPlayerId.equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        boolean applied = creatureControlService.applyControlEffect(
                gameData, newControllerId, creature,
                new GainControlOfTargetEffect(com.github.laxika.magicalvibes.model.effect.ControlDuration.END_OF_TURN),
                com.github.laxika.magicalvibes.model.effect.ControlDuration.END_OF_TURN.toEffectDuration(),
                sourcePermanentId, sourceCardName);
        if (!applied) {
            return;
        }

        tapUntapSupport.tapPermanent(gameData, creature, newControllerId);
        creature.setAttacking(true);
        creature.setAttackedOrBlockedSinceLastUpkeep(true);
        creature.setAttackTarget(defendingPlayerId);
        log.info("Game {} - {} gains control of {} until end of turn and attacks {} via {}",
                gameData.id, gameData.playerIdToName.get(newControllerId), creature.getCard().getName(),
                gameData.playerIdToName.get(defendingPlayerId), sourceCardName);
    }

    private List<UUID> creatureIdsControlledBy(GameData gameData, UUID controllerId) {
        List<UUID> ids = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                ids.add(permanent.getId());
            }
        }
        return ids;
    }

    private UUID defendingPlayerId(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null) {
            return null;
        }
        if (gameData.playerIds.contains(attackedTargetId)) {
            return attackedTargetId;
        }
        return gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}
