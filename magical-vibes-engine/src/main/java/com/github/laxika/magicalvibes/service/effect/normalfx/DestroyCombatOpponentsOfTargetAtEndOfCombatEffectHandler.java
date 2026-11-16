package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link DestroyCombatOpponentsOfTargetAtEndOfCombatEffect}. As a spell's effect it registers the delayed
 * triggered ability about the target creature and the creatures that blocked or were blocked by it so far; as the
 * effect of a triggered ability (that delayed one, or Kjeldoran Frostbeast's own) it destroys the creature's combat
 * opponents, those remembered and those recorded since.
 */
@Component
@RequiredArgsConstructor
public class DestroyCombatOpponentsOfTargetAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCombatOpponentsOfTargetAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyCombatOpponentsOfTargetAtEndOfCombatEffect destroyEffect =
                (DestroyCombatOpponentsOfTargetAtEndOfCombatEffect) effect;
        if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            destroyCombatOpponents(gameData, entry, destroyEffect);
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData,
                entry.getTargetId() != null ? entry.getTargetId() : entry.getSourcePermanentId());
        if (target == null) {
            return;
        }
        Set<UUID> combatOpponentIds = combatOpponentIds(gameData, target.getId(), destroyEffect);
        List<UUID> remembered = new ArrayList<>(List.of(target.getId()));
        remembered.addAll(combatOpponentIds);
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStepThisTurn(
                Triggers.endOfCombat(), entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                target.getId(), DelayedReferents.permanents(remembered),
                new DestroyCombatOpponentsOfTargetAtEndOfCombatEffect(destroyEffect.onlyCreaturesBlockedByTarget())));
        String affectedCreatures = destroyEffect.onlyCreaturesBlockedByTarget()
                ? "'s blocked creatures will be destroyed at end of combat."
                : "'s combat opponents will be destroyed at end of combat.";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), affectedCreatures));
    }

    /**
     * The end-of-combat delayed trigger: destroys the creature's combat opponents, both those
     * remembered when the delayed trigger was created (carried as the entry's triggering
     * permanents) and those recorded since. When the effect is {@code onlyWhileInCombat}, a source
     * still on the battlefield that has been removed from combat (e.g. regenerated, CR 506.4) has no
     * combat opponents left, so the turn's recorded combat history is not consulted for it. They are
     * destroyed together, as one departure batch that asks every CR 616.1 question before any of them leaves.
     */
    private void destroyCombatOpponents(GameData gameData, StackEntry entry,
                                        DestroyCombatOpponentsOfTargetAtEndOfCombatEffect destroyEffect) {
        UUID creatureId = entry.getTargetId() != null ? entry.getTargetId() : entry.getSourcePermanentId();
        Set<UUID> opponentIds = new LinkedHashSet<>(entry.getDeclaredTargetIds());
        opponentIds.remove(creatureId);
        Permanent source = gameQueryService.findPermanentById(gameData, creatureId);
        boolean sourceOutOfCombat = destroyEffect.onlyWhileInCombat() && source != null
                && !source.isAttacking() && !source.isBlocking();
        if (!sourceOutOfCombat) {
            opponentIds.addAll(combatOpponentIds(gameData, creatureId, destroyEffect));
        }
        List<DepartureIntent> intents = new ArrayList<>();
        for (UUID opponentId : opponentIds) {
            if (gameQueryService.findPermanentById(gameData, opponentId) != null) {
                intents.add(DepartureIntent.destroy(opponentId, false));
            }
        }
        stepDepartureSupport.depart(gameData, entry, destroyEffect, DepartureBatch.of(intents));
    }

    private static Set<UUID> combatOpponentIds(GameData gameData, UUID creatureId,
                                               DestroyCombatOpponentsOfTargetAtEndOfCombatEffect destroyEffect) {
        if (creatureId == null) {
            return Set.of();
        }
        return destroyEffect.onlyCreaturesBlockedByTarget()
                ? gameData.combatOpponentIdsBlockedByThisTurn.getOrDefault(creatureId, Set.of())
                : gameData.combatBlockOpponentIdsThisTurn.getOrDefault(creatureId, Set.of());
    }
}
