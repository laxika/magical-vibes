package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeGrantingEquipmentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a granted attack ability that sacrifices its granting Equipment before its payload. */
@Component
@RequiredArgsConstructor
public class SacrificeGrantingEquipmentThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeGrantingEquipmentThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrificeThen = (SacrificeGrantingEquipmentThenEffect) effect;
        UUID equipmentId = sacrificeThen.grantingEquipmentId();
        if (equipmentId == null) {
            return;
        }

        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null
                || !entry.getControllerId().equals(
                        gameQueryService.findPermanentController(gameData, equipmentId))) {
            return;
        }

        if (!permanentRemovalService.sacrificePermanentToGraveyard(gameData, equipment)) {
            return;
        }

        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                gameData, entry.getControllerId(), equipment.getCard());
        gameLogService.append(gameData, GameLog.cardThen(equipment.getCard(), " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        beginReflexiveTrigger(gameData, entry, sacrificeThen.thenEffect());
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        TargetSpec targetSpec = thenEffect.targetSpec();
        if (!targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                && !targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            StackEntry reflexiveTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    entry.getCard(),
                    entry.getControllerId(),
                    entry.getCard().getName() + "'s ability",
                    List.of(thenEffect),
                    (UUID) null,
                    entry.getSourcePermanentId());
            reflexiveTrigger.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
            gameData.stack.add(reflexiveTrigger);
            return;
        }

        TargetPredicate targetPredicate = targetSpec.targetPredicate();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<UUID> validPermanentTargets = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (targetPredicateEvaluationService.matchesPermanent(
                            targetPredicate, permanent, filterContext)) {
                        validPermanentTargets.add(permanent.getId());
                    }
                }
            }
        }

        List<UUID> validPlayerTargets = targetSpec.admits(TargetPredicate.Kind.PLAYER)
                ? gameData.orderedPlayerIds.stream()
                .filter(playerId -> targetPredicateEvaluationService.matchesPlayer(
                        targetPredicate, playerId, entry.getControllerId(), gameData))
                .toList()
                : List.of();
        if (validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s reflexive ability has no legal targets."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(thenEffect),
                entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot()));
        String prompt = entry.getCard().getName() + "'s reflexive ability - Choose target.";
        if (validPlayerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(
                    gameData, entry.getControllerId(), validPermanentTargets, prompt);
        } else {
            playerInputService.beginAnyTargetChoice(
                    gameData, entry.getControllerId(), validPermanentTargets, validPlayerTargets, prompt);
        }
    }
}
