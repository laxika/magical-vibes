package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PlayerNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromTargetToAnotherTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedirectNextDamageFromTargetToAnotherTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageFromTargetToAnotherTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var redirect = (RedirectNextDamageFromTargetToAnotherTargetEffect) effect;
        UUID protectedId = firstTarget(entry.targetsForGroup(redirect.protectedTargetGroup()));
        UUID destinationId = firstTarget(entry.targetsForGroup(redirect.redirectTargetGroup()));
        if (protectedId == null || destinationId == null || protectedId.equals(destinationId)) {
            return;
        }

        int amount = amountEvaluationService.evaluate(gameData, redirect.amount(),
                AmountContext.forStackEntry(entry, null));
        if (amount <= 0 || !existsAsTarget(gameData, protectedId) || !existsAsTarget(gameData, destinationId)) {
            return;
        }

        Permanent protectedPermanent = gameQueryService.findPermanentById(gameData, protectedId);
        Permanent destinationPermanent = gameQueryService.findPermanentById(gameData, destinationId);
        if (protectedPermanent == null) {
            gameData.playerNextDamageRedirectShields.add(
                    new PlayerNextDamageRedirectShield(protectedId, amount, destinationId));
        } else {
            gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                    protectedId, null, amount, destinationId));
        }

        GameLog.Builder logEntry = GameLog.builder()
                .text("The next " + amount + " damage that would be dealt to ");
        appendTarget(logEntry, gameData, protectedId, protectedPermanent);
        logEntry.text(" this turn is dealt to ");
        appendTarget(logEntry, gameData, destinationId, destinationPermanent);
        gameLogService.append(gameData, logEntry.text(" instead.").build());
    }

    private UUID firstTarget(List<UUID> targets) {
        return targets == null || targets.isEmpty() ? null : targets.getFirst();
    }

    private boolean existsAsTarget(GameData gameData, UUID id) {
        return gameData.playerIds.contains(id) || gameQueryService.findPermanentById(gameData, id) != null;
    }

    private void appendTarget(GameLog.Builder logEntry, GameData gameData, UUID id, Permanent permanent) {
        if (permanent != null) {
            logEntry.card(permanent.getCard());
        } else {
            logEntry.text(gameData.playerIdToName.get(id));
        }
    }
}
