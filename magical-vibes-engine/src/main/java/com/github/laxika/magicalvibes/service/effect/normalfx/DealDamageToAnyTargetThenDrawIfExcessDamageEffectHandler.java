package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenDrawIfExcessDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToAnyTargetThenDrawIfExcessDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final DrawCardEffectHandler drawCardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAnyTargetThenDrawIfExcessDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToAnyTargetThenDrawIfExcessDamageEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Map<UUID, Integer> damageBefore = new HashMap<>(gameData.damageDealtToPermanentsThisTurn);
        Map<UUID, Integer> toughnessBefore = new HashMap<>();
        Map<UUID, Integer> markedDamageBefore = new HashMap<>();
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    toughnessBefore.put(permanent.getId(),
                            gameQueryService.getEffectiveToughness(gameData, permanent));
                    markedDamageBefore.put(permanent.getId(), permanent.getMarkedDamage());
                }
            }
        }
        boolean sourceHasDeathtouch = gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.DEATHTOUCH);

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, damageEffect.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluatedDamage, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, damage, false);

        for (Map.Entry<UUID, Integer> dealtDamage : gameData.damageDealtToPermanentsThisTurn.entrySet()) {
            int previous = damageBefore.getOrDefault(dealtDamage.getKey(), 0);
            int damageDealt = dealtDamage.getValue() - previous;
            if (damageDealt <= 0) {
                continue;
            }
            Permanent damagedPermanent = gameQueryService.findPermanentById(gameData, dealtDamage.getKey());
            if (damagedPermanent == null || !gameQueryService.isCreature(gameData, damagedPermanent)) {
                continue;
            }
            int excessDamage = damageSupport.computeExcessDamageToAnyTarget(
                    damageDealt, true,
                    toughnessBefore.getOrDefault(damagedPermanent.getId(),
                            gameQueryService.getEffectiveToughness(gameData, damagedPermanent)),
                    markedDamageBefore.getOrDefault(damagedPermanent.getId(),
                            Math.max(0, damagedPermanent.getMarkedDamage() - damageDealt)),
                    sourceHasDeathtouch, false, 0, false, 0);
            if (excessDamage > 0) {
                drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect());
                break;
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
