package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealOrderedDamageToAnyTargetsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealOrderedDamageToAnyTargetsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealOrderedDamageToAnyTargetsEffect) effect;

        List<UUID> targets = entry.getTargetIds();
        List<Integer> damages = e.damageAmounts().stream()
                .map(d -> gameQueryService.applyDamageMultiplier(gameData, d, entry))
                .toList();
        String cardName = entry.getCard().getName();

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> destroyed = new ArrayList<>();

        for (int i = 0; i < Math.min(targets.size(), damages.size()); i++) {
            UUID targetId = targets.get(i);
            int damage = damages.get(i);

            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

            if (!targetIsPlayer && targetPermanent == null) continue;

            if (targetIsPlayer) {
                damageSupport.dealDamageToPlayer(gameData, entry, targetId, damage);
            } else {
                if (!(gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, targetPermanent, entry.getCard()))) {
                    if (damageSupport.dealCreatureDamage(gameData, entry, targetPermanent, damage)) {
                        destroyed.add(targetPermanent);
                    }
                } else {
                    gameBroadcastService.logAndBroadcast(gameData,
                            cardName + "'s damage to " + targetPermanent.getCard().getName() + " is prevented.");
                }
            }
        }

        damageSupport.destroyAllLethal(gameData, destroyed);

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
