package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectAttackingCreatureEffect;
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
public class ReselectAttackingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AttackLegalityService attackLegalityService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReselectAttackingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackingCreatureId = entry.getTargetId();
        Permanent attackingCreature = attackingCreatureId == null
                ? null : gameQueryService.findPermanentById(gameData, attackingCreatureId);
        if (attackingCreature == null || !attackingCreature.isAttacking()
                || !gameQueryService.isCreature(gameData, attackingCreature)) {
            return;
        }

        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, attackingCreatureId);
        if (creatureControllerId == null) {
            return;
        }

        Set<UUID> validAttackTargetIds = attackLegalityService
                .getValidAttackTargetIds(gameData, creatureControllerId);
        List<UUID> validPlayerIds = validAttackTargetIds.stream()
                .filter(gameData.playerIds::contains)
                .filter(playerId -> !playerId.equals(creatureControllerId))
                .toList();
        List<UUID> validPermanentIds = validAttackTargetIds.stream()
                .filter(targetId -> !gameData.playerIds.contains(targetId))
                .map(targetId -> gameQueryService.findPermanentById(gameData, targetId))
                .filter(target -> target != null
                        && !creatureControllerId.equals(gameQueryService.findPermanentController(
                        gameData, target.getId())))
                .map(Permanent::getId)
                .toList();
        if (validPlayerIds.isEmpty() && validPermanentIds.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ReselectAttackingCreatureTarget(attackingCreatureId));
        playerInputService.beginAnyTargetChoice(
                gameData,
                entry.getControllerId(),
                validPermanentIds,
                validPlayerIds,
                "Choose the player or permanent for the attacking creature to attack.");
    }
}
