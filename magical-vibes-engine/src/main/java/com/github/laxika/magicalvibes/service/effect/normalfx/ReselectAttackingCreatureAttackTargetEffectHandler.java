package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectAttackingCreatureAttackTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.AttackLegalityService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReselectAttackingCreatureAttackTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AttackLegalityService attackLegalityService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReselectAttackingCreatureAttackTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackerId = entry.getTargetIds().isEmpty()
                ? entry.getTargetId()
                : entry.getTargetIds().getFirst();
        Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
        if (attacker == null || !attacker.isAttacking() || !gameQueryService.isCreature(gameData, attacker)) {
            return;
        }

        UUID attackerControllerId = gameData.findControllerOf(attacker);
        if (attackerControllerId == null) {
            return;
        }

        Set<UUID> validAttackTargetIds =
                attackLegalityService.getValidAttackTargetIds(gameData, attackerControllerId);
        List<UUID> validPlayerIds = gameData.orderedPlayerIds.stream()
                .filter(validAttackTargetIds::contains)
                .filter(playerId -> !playerId.equals(attackerControllerId))
                .filter(playerId -> attackLegalityService.canAttackDefender(gameData, attacker, playerId))
                .toList();
        List<UUID> validPermanentIds = gameData.orderedPlayerIds.stream()
                .flatMap(playerId -> gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream())
                .filter(permanent -> validAttackTargetIds.contains(permanent.getId()))
                .filter(permanent -> !attackerControllerId.equals(gameData.findControllerOf(permanent)))
                .map(Permanent::getId)
                .filter(targetId -> attackLegalityService.canAttackDefender(gameData, attacker, targetId))
                .toList();

        if (validPlayerIds.isEmpty() && validPermanentIds.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ReselectAttackTarget(attacker.getId()));
        playerInputService.beginAnyTargetChoice(
                gameData,
                entry.getControllerId(),
                validPermanentIds,
                validPlayerIds,
                "Choose the player or permanent for the attacking creature to attack.");
    }
}
