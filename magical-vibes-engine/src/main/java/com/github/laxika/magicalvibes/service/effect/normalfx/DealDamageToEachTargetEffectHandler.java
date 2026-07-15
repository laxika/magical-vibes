package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEachTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEachTargetEffect) effect;

        List<UUID> targets = entry.getTargetIds();
        if (targets.isEmpty()) {
            if (entry.getTargetId() != null) {
                targets = List.of(entry.getTargetId());
            } else {
                return;
            }
        }

        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
        String cardName = entry.getCard().getName();

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> destroyed = new ArrayList<>();

        for (UUID targetId : targets) {
            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

            if (!targetIsPlayer && targetPermanent == null) continue;

            if (targetIsPlayer) {
                damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
            } else {
                if (!(gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, targetPermanent, entry.getCard()))) {
                    if (damageSupport.dealCreatureDamage(gameData, entry, targetPermanent, rawDamage)) {
                        destroyed.add(targetPermanent);
                    }
                } else {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + "'s damage to " + targetPermanent.getCard().getName() + " is prevented."));
                }
            }
        }

        damageSupport.destroyAllLethal(gameData, destroyed);
        gameOutcomeService.checkWinCondition(gameData);

    }
}
