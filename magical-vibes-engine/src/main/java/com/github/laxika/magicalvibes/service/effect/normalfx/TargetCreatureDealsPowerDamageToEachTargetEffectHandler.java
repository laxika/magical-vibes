package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureDealsPowerDamageToEachTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsPowerDamageToEachTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetCreatureDealsPowerDamageToEachTargetEffect) effect;
        List<UUID> sourceIds = entry.targetsForGroup(e.sourceTargetGroup());
        List<UUID> targetIds = entry.targetsForGroup(e.victimTargetGroup());
        if (sourceIds.isEmpty() || targetIds.isEmpty()) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceIds.getFirst());
        if (source == null || !gameQueryService.isCreature(gameData, source)) {
            return;
        }

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (sourceControllerId == null) {
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, source);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                sourceControllerId,
                source.getCard().getName() + "'s ability",
                List.of(),
                null,
                source.getId());
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);

        for (UUID targetId : targetIds) {
            damageSupport.resolveAnyTargetDamage(gameData, damageEntry, targetId, rawDamage, false);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
