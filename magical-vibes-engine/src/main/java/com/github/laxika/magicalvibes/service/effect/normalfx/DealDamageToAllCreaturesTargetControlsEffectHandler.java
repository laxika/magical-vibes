package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesTargetControlsEffect;
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
public class DealDamageToAllCreaturesTargetControlsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAllCreaturesTargetControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToAllCreaturesTargetControlsEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        String cardName = entry.getCard().getName();

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            List<Permanent> destroyed = new ArrayList<>();
            for (Permanent creature : new ArrayList<>(battlefield)) {
                if (!gameQueryService.isCreature(gameData, creature)) continue;
                if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, creature, entry.getCard())) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + "'s damage to " + creature.getCard().getName() + " is prevented."));
                    continue;
                }
                if (damageSupport.dealCreatureDamage(gameData, entry, creature, rawDamage)) {
                    destroyed.add(creature);
                }
            }
            damageSupport.destroyAllLethal(gameData, destroyed);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
