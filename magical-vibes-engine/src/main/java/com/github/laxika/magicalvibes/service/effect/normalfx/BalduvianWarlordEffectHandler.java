package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
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
import java.util.UUID;

/**
 * Resolves {@link BalduvianWarlordEffect}: the target leaves combat through {@link CombatRemovalSupport} (the attackers
 * it blocked that had no other blocker this combat become unblocked), then, when it can block an attacker, it blocks
 * the attacker the effect's controller chooses, a lone legal attacker chosen unasked
 * (CR 608.2d). With {@link BalduvianWarlordEffect#mayReblock()} the controller is first asked whether it blocks.
 */
@Component
@RequiredArgsConstructor
public class BalduvianWarlordEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final BlockLegalityService blockLegalityService;
    private final CombatBlockService combatBlockService;
    private final PlayerInputService playerInputService;
    private final CombatRemovalSupport combatRemovalSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BalduvianWarlordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BalduvianWarlordEffect warlordEffect = (BalduvianWarlordEffect) effect;
        Permanent creature = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (creature == null || (!warlordEffect.mayReblock() && !creature.isBlocking())) {
            return;
        }

        combatRemovalSupport.removeFromCombatAndUnblockSoleBlockers(gameData, entry, creature);
        if (legalAttackerIds(gameData, creature).isEmpty()) {
            return;
        }
        if (warlordEffect.mayReblock()) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    entry.getControllerId(),
                    List.of(warlordEffect),
                    "You may have it block an attacking creature of your choice?",
                    creature.getId()));
            return;
        }
        chooseAttackerToBlock(gameData, entry.getControllerId(), entry.getCard().getName(), creature);
    }

    /** "It blocks an attacking creature of your choice": the choice, unasked when only one attacker can be chosen. */
    public void chooseAttackerToBlock(GameData gameData, UUID choosingPlayerId, String sourceCardName,
                                      Permanent blocker) {
        List<UUID> legalAttackerIds = legalAttackerIds(gameData, blocker);
        if (legalAttackerIds.isEmpty()) {
            return;
        }

        PermanentChoiceContext.BalduvianWarlordChoosesAttacker context =
                new PermanentChoiceContext.BalduvianWarlordChoosesAttacker(blocker.getId(), sourceCardName);
        if (legalAttackerIds.size() == 1) {
            completeChoice(gameData, legalAttackerIds.getFirst(), context);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, choosingPlayerId, legalAttackerIds,
                sourceCardName + " - choose an attacking creature for it to block.");
    }

    public void completeChoice(GameData gameData, UUID chosenAttackerId,
                               PermanentChoiceContext.BalduvianWarlordChoosesAttacker context) {
        Permanent blocker = gameQueryService.findPermanentById(gameData, context.blockerId());
        Permanent attacker = gameQueryService.findPermanentById(gameData, chosenAttackerId);
        if (blocker == null || attacker == null
                || !attacker.isAttacking() || !gameQueryService.isCreature(gameData, attacker)) {
            return;
        }
        if (!legalAttackerIds(gameData, blocker).contains(chosenAttackerId)) {
            return;
        }

        UUID defenderId = gameQueryService.findPermanentController(gameData, blocker.getId());
        if (defenderId == null || !attacks(gameData, defenderId, attacker)) {
            return;
        }

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.getOrDefault(defenderId, List.of());
        BlockLegalityContext blockContext =
                blockLegalityService.createBlockLegalityContext(gameData, defenderBattlefield);
        if (!blockLegalityService.canBeMadeToBlockAttacker(blockContext, blocker, attacker)) {
            return;
        }

        boolean wasBlocked = attacker.isBlockedWithoutBlockers()
                || gameQueryService.isBlockedByAnyCreature(gameData, attacker);
        combatBlockService.applyBlockFromEffect(gameData, blocker, attacker, wasBlocked, context.sourceCardName());
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
                    && blockLegalityService.canBeMadeToBlockAttacker(blockContext, blocker, attacker)
                    && !gameQueryService.cantBeChosen(gameData, attacker)) {
                result.add(attacker.getId());
            }
        });
        List<UUID> requiredAttackers = result.stream().filter(id -> {
            Permanent attacker = gameQueryService.findPermanentById(gameData, id);
            return attacker != null && gameQueryService.isRequiredToBlockByLure(gameData, attacker, blocker);
        }).toList();
        if (!requiredAttackers.isEmpty()) {
            return requiredAttackers;
        }
        return result;
    }

    private boolean attacks(GameData gameData, UUID defenderId, Permanent attacker) {
        UUID attackTarget = attacker.getAttackTarget();
        return defenderId.equals(attackTarget)
                || (attackTarget != null
                && !gameData.playerIds.contains(attackTarget)
                && defenderId.equals(gameQueryService.findPermanentController(gameData, attackTarget)));
    }
}
