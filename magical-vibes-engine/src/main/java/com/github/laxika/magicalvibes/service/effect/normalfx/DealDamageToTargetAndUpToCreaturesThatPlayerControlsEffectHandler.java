package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect) effect;

        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.isEmpty()) {
            return;
        }

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        UUID firstTargetId = targets.getFirst();
        UUID affectedPlayerId = resolveAffectedPlayer(gameData, entry, e, firstTargetId);
        if (affectedPlayerId != null) {
            int playerDamage = gameQueryService.applyDamageMultiplier(gameData, e.playerDamage(), entry);
            damageSupport.resolveAnyTargetDamage(gameData, entry, firstTargetId, playerDamage, false);
        }

        for (int i = 1; i < targets.size(); i++) {
            UUID creatureId = targets.get(i);
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature == null || !gameQueryService.isCreature(gameData, creature)) {
                continue;
            }
            UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creatureId);
            if (affectedPlayerId == null || !Objects.equals(affectedPlayerId, creatureControllerId)) {
                continue;
            }
            if (!damageSupport.isDamagePreventedForCreature(gameData, entry, creature)) {
                int creatureDamage = gameQueryService.applyDamageMultiplier(gameData, e.creatureDamage(), entry);
                damageSupport.dealCreatureDamage(gameData, entry, creature, creatureDamage);
            }
            // The can't-block rider is a separate part of the effect, so it applies even when the
            // damage itself was prevented.
            if (e.creaturesCantBlock()) {
                creature.setCantBlockThisTurn(true);
                gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), " can't block this turn."));
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves the player whose creatures may be hit: the first target itself when it is a player,
     * or the controller of the targeted planeswalker. Returns null when the first target is not a
     * legal player/planeswalker for this effect's configuration.
     */
    private UUID resolveAffectedPlayer(GameData gameData,
                                       StackEntry entry,
                                       DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect e,
                                       UUID firstTargetId) {
        if (gameData.playerIds.contains(firstTargetId)) {
            if (e.opponentOnly() && firstTargetId.equals(entry.getControllerId())) {
                return null;
            }
            return firstTargetId;
        }
        if (e.opponentOnly()) {
            return null;
        }
        Permanent planeswalker = gameQueryService.findPermanentById(gameData, firstTargetId);
        if (planeswalker == null) {
            return null;
        }
        return gameQueryService.findPermanentController(gameData, firstTargetId);
    }
}
