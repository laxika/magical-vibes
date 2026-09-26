package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDealtCombatDamageByNamedCreatureThisGameLosesLifeEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/** Resolves the game-wide named-creature combat-damage life-loss effect. */
@Component
@RequiredArgsConstructor
public class EachOpponentDealtCombatDamageByNamedCreatureThisGameLosesLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDealtCombatDamageByNamedCreatureThisGameLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var namedCreatureEffect = (EachOpponentDealtCombatDamageByNamedCreatureThisGameLosesLifeEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;

        int amount = amountEvaluationService.evaluate(
                gameData, namedCreatureEffect.amount(), AmountContext.forStackEntry(entry, null));
        if (amount <= 0) return;

        Set<UUID> damagedPlayers = gameData.combatDamageToPlayersByCreatureNameThisGame
                .getOrDefault(namedCreatureEffect.creatureName(), Set.of());
        String sourceName = entry.getCard().getName();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId) && damagedPlayers.contains(playerId)) {
                lifeSupport.applyLifeLoss(gameData, playerId, amount, sourceName);
            }
        }
    }
}
