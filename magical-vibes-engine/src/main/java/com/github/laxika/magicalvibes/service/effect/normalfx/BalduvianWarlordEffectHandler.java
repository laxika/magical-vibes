package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BalduvianWarlordEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityContext;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BalduvianWarlordEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final BlockLegalityService blockLegalityService;
    private final CombatBlockService combatBlockService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BalduvianWarlordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent blocker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (blocker == null || !blocker.isBlocking()) {
            return;
        }

        removeFromCombatAndUpdateAttackers(gameData, blocker);

        List<UUID> legalAttackerIds = legalAttackerIds(gameData, blocker);
        if (legalAttackerIds.isEmpty()) {
            return;
        }

        PermanentChoiceContext.BalduvianWarlordChoosesAttacker context =
                new PermanentChoiceContext.BalduvianWarlordChoosesAttacker(
                        blocker.getId(), entry.getCard().getName());
        if (legalAttackerIds.size() == 1) {
            completeChoice(gameData, legalAttackerIds.getFirst(), context);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), legalAttackerIds,
                entry.getCard().getName() + " - choose an attacking creature for it to block.");
    }

    public void completeChoice(GameData gameData, UUID chosenAttackerId,
                               PermanentChoiceContext.BalduvianWarlordChoosesAttacker context) {
        Permanent blocker = gameQueryService.findPermanentById(gameData, context.blockerId());
        Permanent attacker = gameQueryService.findPermanentById(gameData, chosenAttackerId);
        if (blocker == null || attacker == null
                || !attacker.isAttacking() || !gameQueryService.isCreature(gameData, attacker)) {
            return;
        }

        UUID defenderId = gameQueryService.findPermanentController(gameData, blocker.getId());
        if (defenderId == null || !attacks(gameData, defenderId, attacker)) {
            return;
        }

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.getOrDefault(defenderId, List.of());
        BlockLegalityContext blockContext =
                blockLegalityService.createBlockLegalityContext(gameData, defenderBattlefield);
        if (!blockLegalityService.canBlockAttacker(blockContext, blocker, attacker)) {
            return;
        }

        boolean wasBlocked = attacker.isBlockedWithoutBlockers()
                || gameQueryService.isBlockedByAnyCreature(gameData, attacker);
        combatBlockService.applyBlockFromEffect(gameData, blocker, attacker, wasBlocked, context.sourceCardName());
    }

    private void removeFromCombatAndUpdateAttackers(GameData gameData, Permanent blocker) {
        List<UUID> attackerIds = new ArrayList<>(blocker.getBlockingTargetIds());
        for (UUID attackerId : attackerIds) {
            Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
            if (attacker == null || !attacker.isAttacking()) {
                continue;
            }

            Set<UUID> blockersThisCombat = gameData.combatBlockOpponentIdsThisCombat.get(attackerId);
            boolean hadAnotherBlocker = blockersThisCombat != null
                    ? blockersThisCombat.stream().anyMatch(id -> !id.equals(blocker.getId()))
                    : hasCurrentOtherBlocker(gameData, attackerId, blocker.getId());
            boolean hasCurrentOtherBlocker = hasCurrentOtherBlocker(gameData, attackerId, blocker.getId());
            attacker.setBlockedWithoutBlockers(!hasCurrentOtherBlocker && hadAnotherBlocker);
        }

        blocker.setBlocking(false);
        blocker.getBlockingTargets().clear();
        blocker.getBlockingTargetIds().clear();
    }

    private List<UUID> legalAttackerIds(GameData gameData, Permanent blocker) {
        UUID defenderId = gameQueryService.findPermanentController(gameData, blocker.getId());
        if (defenderId == null) {
            return List.of();
        }

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.getOrDefault(defenderId, List.of());
        BlockLegalityContext blockContext =
                blockLegalityService.createBlockLegalityContext(gameData, defenderBattlefield);
        List<UUID> result = new ArrayList<>();
        gameData.forEachPermanent((ignored, attacker) -> {
            if (attacker.isAttacking()
                    && gameQueryService.isCreature(gameData, attacker)
                    && attacks(gameData, defenderId, attacker)
                    && blockLegalityService.canBlockAttacker(blockContext, blocker, attacker)) {
                result.add(attacker.getId());
            }
        });
        return result;
    }

    private boolean attacks(GameData gameData, UUID defenderId, Permanent attacker) {
        UUID attackTarget = attacker.getAttackTarget();
        return defenderId.equals(attackTarget)
                || (attackTarget != null
                && !gameData.playerIds.contains(attackTarget)
                && defenderId.equals(gameQueryService.findPermanentController(gameData, attackTarget)));
    }

    private boolean hasCurrentOtherBlocker(GameData gameData, UUID attackerId, UUID excludedBlockerId) {
        final boolean[] found = {false};
        gameData.forEachPermanent((ignored, permanent) -> {
            if (!found[0] && permanent.isBlocking()
                    && !permanent.getId().equals(excludedBlockerId)
                    && permanent.getBlockingTargetIds().contains(attackerId)) {
                found[0] = true;
            }
        });
        return found[0];
    }
}
