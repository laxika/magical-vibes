package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentCreatureEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToRandomOpponentCreatureEffect} by rebuilding the pool of creatures
 * controlled by the effect controller's opponents and choosing one uniformly at random.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToRandomOpponentCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToRandomOpponentCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToRandomOpponentCreatureEffect) effect;
        List<UUID> pool = new ArrayList<>();
        for (var battlefield : gameData.playerBattlefields.entrySet()) {
            if (battlefield.getKey().equals(entry.getControllerId())) continue;
            for (Permanent permanent : battlefield.getValue()) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    pool.add(permanent.getId());
                }
            }
        }
        if (pool.isEmpty()) return;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);

        UUID chosen = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        damageSupport.resolveAnyTargetDamage(gameData, entry, chosen, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
