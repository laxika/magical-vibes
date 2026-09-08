package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToOtherCreaturesControlledByTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToOtherCreaturesControlledByTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToOtherCreaturesControlledByTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToOtherCreaturesControlledByTargetEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) return;

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        int damage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield != null) {
            for (Permanent creature : new ArrayList<>(battlefield)) {
                if (creature.getId().equals(target.getId()) || !gameQueryService.isCreature(gameData, creature)) {
                    continue;
                }
                if (gameQueryService.isDamagePreventable(gameData)
                        && gameQueryService.hasProtectionFromSource(gameData, creature, entry.getCard(), entry.getControllerId())) {
                    gameLogService.append(gameData, GameLog.textCardText(entry.getCard().getName() + "'s damage to ", creature.getCard(), " is prevented."));
                    continue;
                }
                damageSupport.dealCreatureDamage(gameData, entry, creature, damage);
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
