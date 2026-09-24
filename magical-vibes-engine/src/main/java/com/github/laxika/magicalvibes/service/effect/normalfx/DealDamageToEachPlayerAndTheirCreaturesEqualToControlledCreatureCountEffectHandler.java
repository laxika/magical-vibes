package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Incite Rebellion's per-player creature-count damage. */
@Component
@RequiredArgsConstructor
public class DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
            int creatureCount = (int) battlefield.stream()
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                    .count();
            if (creatureCount == 0) {
                continue;
            }

            int damage = gameQueryService.applyDamageMultiplier(gameData, creatureCount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
            damageSupport.damageFilteredCreatures(gameData, entry, damage, battlefield,
                    permanent -> gameQueryService.isCreature(gameData, permanent));
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
