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
        for (Permanent attacker : attackers) {
            List<UUID> createdIds = tokenCopySupport.createTokenCopies(
                    gameData, entry, List.of(attacker.getCard()), attacker, controllerId, copyEffect);
            for (UUID createdId : createdIds) {
                combatBlockService.markTokenAsBlocking(
                        gameData, gameQueryService.findPermanentById(gameData, createdId), attacker);
            }
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
