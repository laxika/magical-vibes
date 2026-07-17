package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
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
public class DealDamageToTargetAndTheirCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetAndTheirCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetAndTheirCreaturesEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        String cardName = entry.getCard().getName();

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        // Determine the affected player: if target is a player, use directly;
        // if target is a planeswalker, use its controller
        UUID affectedPlayerId;
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        if (targetIsPlayer) {
            affectedPlayerId = targetId;
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        } else {
            Permanent targetPw = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPw == null) return;
            affectedPlayerId = gameQueryService.findPermanentController(gameData, targetId);
            if (affectedPlayerId == null) return;

            if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, targetPw, entry.getCard())) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + "'s damage to " + targetPw.getCard().getName() + " is prevented."));
            } else {
                damageSupport.dealCreatureDamage(gameData, entry, targetPw, rawDamage);
            }
        }

        // Deal damage to each creature the affected player controls
        List<Permanent> battlefield = gameData.playerBattlefields.get(affectedPlayerId);
        if (battlefield != null) {
            for (Permanent creature : new ArrayList<>(battlefield)) {
                if (!gameQueryService.isCreature(gameData, creature)) continue;
                // Skip the planeswalker target (already dealt damage above)
                if (!targetIsPlayer && creature.getId().equals(targetId)) continue;
                if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, creature, entry.getCard())) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + "'s damage to " + creature.getCard().getName() + " is prevented."));
                    continue;
                }
                damageSupport.dealCreatureDamage(gameData, entry, creature, rawDamage);
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
