package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        int damage = entry.getEventValue();
        if (damagedPlayerId == null || damage <= 0
                || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        int adjustedDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
        List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (!gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.isPlaneswalker(gameData, permanent)) {
                    continue;
                }
                if (gameQueryService.isDamagePreventable(gameData)
                        && gameQueryService.hasProtectionFromSource(
                        gameData, permanent, entry.getCard(), entry.getControllerId())) {
                    gameLogService.append(gameData, GameLog.textCardText(
                            entry.getCard().getName() + "'s damage to ", permanent.getCard(), " is prevented."));
                    continue;
                }
                damageSupport.dealCreatureDamage(gameData, entry, permanent, adjustedDamage);
            }
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
