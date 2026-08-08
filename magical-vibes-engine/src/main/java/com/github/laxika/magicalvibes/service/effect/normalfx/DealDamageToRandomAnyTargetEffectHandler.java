package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomAnyTargetEffect;
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
 * Resolves {@link DealDamageToRandomAnyTargetEffect}: builds the pool of every legal "any target"
 * in the game (each creature, each planeswalker, each player), picks one uniformly at random and
 * routes the damage through the normal any-target damage pipeline. Nothing was targeted, so the
 * pool is recomputed at resolution.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToRandomAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToRandomAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToRandomAnyTargetEffect) effect;

        List<UUID> pool = new ArrayList<>(gameData.playerIds);
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        || gameQueryService.isPlaneswalker(gameData, permanent)) {
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
