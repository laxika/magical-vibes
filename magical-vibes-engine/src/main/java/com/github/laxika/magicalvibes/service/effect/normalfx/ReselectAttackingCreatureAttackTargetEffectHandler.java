package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectAttackingCreatureAttackTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.AttackLegalityService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Offers the new attack targets of {@link ReselectAttackingCreatureAttackTargetEffect}: the players and permanents
 * {@code AttackLegalityService.getValidReselectionTargetIds} allows (every attack requirement, restriction and cost
 * ignored), in turn order. The answer is checked again and applied by
 * {@code PermanentChoiceTriggerHandlerService.handleReselectAttackingCreatureTarget}.
 */
@Component
@RequiredArgsConstructor
public class ReselectAttackingCreatureAttackTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AttackLegalityService attackLegalityService;
    private final PlayerInputService playerInputService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReselectAttackingCreatureAttackTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReselectAttackingCreatureAttackTargetEffect reselect = (ReselectAttackingCreatureAttackTargetEffect) effect;
        Permanent attacker = subjectResolutionService.permanent(gameData, entry, effect, reselect.attacker());
        if (attacker == null || !attacker.isAttacking() || !gameQueryService.isCreature(gameData, attacker)) {
            return;
        }
        UUID attackerControllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        if (attackerControllerId == null) {
            return;
        }

        Set<UUID> validAttackTargetIds =
                attackLegalityService.getValidReselectionTargetIds(gameData, attackerControllerId);
        List<UUID> validPlayerIds = gameData.orderedPlayerIds.stream()
                .filter(validAttackTargetIds::contains)
                .filter(playerId -> !playerId.equals(attackerControllerId))
                .toList();
        List<UUID> validPermanentIds = reselect.playersOnly()
                ? List.of()
                : gameData.orderedPlayerIds.stream()
                        .flatMap(playerId -> gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream())
                        .map(Permanent::getId)
                        .filter(validAttackTargetIds::contains)
                        .toList();
        if (validPlayerIds.isEmpty() && validPermanentIds.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ReselectAttackingCreatureTarget(attacker.getId()));
        playerInputService.beginAnyTargetChoice(
                gameData,
                entry.getControllerId(),
                validPermanentIds,
                validPlayerIds,
                "Choose the player or permanent for the attacking creature to attack.");
    }
}
