package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreaturesThenCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreaturesThenCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreaturesThenCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetCreaturesThenCreateTokensEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty() || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int rawDamage = evaluatedDamage;

        Map<UUID, Integer> markedDamageBefore = new HashMap<>();
        gameData.forEachPermanent((playerId, permanent) ->
                markedDamageBefore.put(permanent.getId(), permanent.getMarkedDamage()));
        Set<UUID> damagedCreatureIds = new HashSet<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)
                    || damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
                continue;
            }
            if (damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage) > 0) {
                damagedCreatureIds.add(targetId);
            }
        }
        // Damage redirected to another creature still counts as that creature being dealt damage this way.
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && permanent.getMarkedDamage() > markedDamageBefore.getOrDefault(permanent.getId(), 0)) {
                damagedCreatureIds.add(permanent.getId());
            }
        });

        if (!damagedCreatureIds.isEmpty()) {
            createTokenEffectHandler.resolve(gameData, entry, e.token().withAmount(damagedCreatureIds.size()));
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
