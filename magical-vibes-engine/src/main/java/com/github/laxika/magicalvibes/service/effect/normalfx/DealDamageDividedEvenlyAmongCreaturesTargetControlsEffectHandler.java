package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageDividedEvenlyAmongCreaturesTargetControlsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageDividedEvenlyAmongCreaturesTargetControlsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageDividedEvenlyAmongCreaturesTargetControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageDividedEvenlyAmongCreaturesTargetControlsEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) return;

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        List<Permanent> creatures = new ArrayList<>();
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatures.add(permanent);
            }
        }
        if (creatures.isEmpty()) return;

        int total = amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, null));
        int perCreature = total / creatures.size();
        if (perCreature <= 0) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, perCreature, entry);
        String cardName = entry.getCard().getName();

        for (Permanent creature : creatures) {
            if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, creature, entry.getCard())) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(cardName + "'s damage to ", creature.getCard(), " is prevented."));
                continue;
            }
            damageSupport.dealCreatureDamage(gameData, entry, creature, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
