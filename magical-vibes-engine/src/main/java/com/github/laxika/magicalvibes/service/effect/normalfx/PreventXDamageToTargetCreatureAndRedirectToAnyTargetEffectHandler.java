package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageToTargetCreatureAndRedirectToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventXDamageToTargetCreatureAndRedirectToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventXDamageToTargetCreatureAndRedirectToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventXDamageToTargetCreatureAndRedirectToAnyTargetEffect) effect;
        List<UUID> protectedTargets = entry.targetsForGroup(e.protectedTargetGroup());
        List<UUID> redirectTargets = entry.targetsForGroup(e.redirectTargetGroup());
        if (protectedTargets.isEmpty() || redirectTargets.isEmpty()) return;

        UUID protectedTargetId = protectedTargets.getFirst();
        Permanent protectedCreature = gameQueryService.findPermanentById(gameData, protectedTargetId);
        if (protectedCreature == null || !gameQueryService.isCreature(gameData, protectedCreature)) return;

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) return;

        UUID redirectTargetId = redirectTargets.getFirst();
        gameData.damageRedirectShields.add(new DamageRedirectShield(
                entry.getControllerId(), amount, entry.getSourcePermanentId(), entry.getCard(),
                redirectTargetId, false, protectedTargetId));

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" - the next " + amount + " damage that would be dealt to the target creature this turn is prevented. If prevented, ")
                .card(entry.getCard())
                .text(" deals that much damage to the other target.")
                .build());
    }
}
