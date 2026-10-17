package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreaturesBlockingEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Mirror Match's copies of creatures attacking its controller. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfAttackingCreaturesBlockingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final CombatBlockService combatBlockService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfAttackingCreaturesBlockingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> attackers = gameData.orderedPlayerIds.stream()
                .flatMap(playerId -> gameData.playerBattlefields
                        .getOrDefault(playerId, List.of()).stream())
                .filter(permanent -> permanent.isAttacking()
                        && gameQueryService.isCreature(gameData, permanent)
                        && attacksControllerOrTheirPlaneswalker(gameData, permanent, controllerId))
                .toList();

        CreateTokenCopyOfTargetPermanentEffect copyEffect =
                CreateTokenCopyOfTargetPermanentEffect.exiledAtEndOfCombat();
        if (attackers.isEmpty()) {
            return;
        }
        // The copies are created as one event, so each sees the others enter (CR 603.6a).
        List<UUID> createdIds = tokenCopySupport.createTokenCopies(
                gameData, entry, attackers.stream().map(Permanent::getCard).toList(), attackers.getFirst(),
                controllerId, copyEffect, null);
        if (createdIds.size() != attackers.size()) {
            return;
        }
        for (int i = 0; i < attackers.size(); i++) {
            combatBlockService.markTokenAsBlocking(
                    gameData, gameQueryService.findPermanentById(gameData, createdIds.get(i)), attackers.get(i));
        }
    }

    private boolean attacksControllerOrTheirPlaneswalker(GameData gameData, Permanent attacker,
                                                          UUID controllerId) {
        UUID attackTargetId = attacker.getAttackTarget();
        if (attackTargetId == null || gameData.playerIds.contains(attackTargetId)) {
            return controllerId.equals(attackTargetId);
        }

        Permanent attackTarget = gameQueryService.findPermanentById(gameData, attackTargetId);
        return attackTarget != null
                && gameQueryService.isPlaneswalker(gameData, attackTarget)
                && controllerId.equals(gameQueryService.findPermanentController(gameData, attackTargetId));
    }
}
