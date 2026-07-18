package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToAttackedTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAttackedTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToAttackedTargetEffect) effect;

        UUID targetId = entry.getAttackedTargetId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        if (gameData.playerIds.contains(targetId)) {
            if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
                damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
            }
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !target.getCard().hasType(CardType.PLANESWALKER)) return;
        Card source = entry.getEffectiveDamageSourceCard();
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)
                || damageSupport.isSourcePermanentPreventedFromDealingDamage(gameData, entry)
                || gameQueryService.hasProtectionFromSource(gameData, target, source)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source, "'s damage is prevented."));
            return;
        }

        int newLoyalty = Math.max(0, target.getCounterCount(CounterType.LOYALTY) - rawDamage);
        target.setCounterCount(CounterType.LOYALTY, newLoyalty);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(source).text(" deals " + rawDamage + " damage to ").card(target.getCard()).text(".").build());
        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
