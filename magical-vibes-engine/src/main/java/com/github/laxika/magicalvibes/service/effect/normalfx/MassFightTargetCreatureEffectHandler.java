package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MassFightTargetCreatureEffect;
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
public class MassFightTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MassFightTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        // Collect other creatures on the same battlefield
        List<Permanent> otherCreatures = new ArrayList<>();
        for (Permanent p : new ArrayList<>(battlefield)) {
            if (p.getId().equals(target.getId())) continue;
            if (!gameQueryService.isCreature(gameData, p)) continue;
            otherCreatures.add(p);
        }

        // Step 1: Target creature deals damage equal to its power to each other creature
        int targetPower = gameQueryService.getPowerBasedDamage(gameData, target);
        boolean targetPrevented = gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, target);
        if (targetPrevented) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), "'s damage is prevented."));
        } else {
            for (Permanent other : otherCreatures) {
                if (gameQueryService.isDamagePreventable(gameData)
                        && gameQueryService.hasProtectionFromSource(gameData, other, target)) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(other.getCard(), " has protection — damage from ", target.getCard(), " prevented."));
                    continue;
                }
                int damage = gameQueryService.applyDamageMultiplier(gameData, targetPower, entry);
                damageSupport.dealCreatureDamage(gameData, entry, other, damage, target);
            }
        }

        // Step 2: Each other creature deals damage equal to its power to the targeted creature
        for (Permanent other : otherCreatures) {
            int otherPower = gameQueryService.getPowerBasedDamage(gameData, other);

            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, other)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(other.getCard(), "'s damage is prevented."));
                continue;
            }
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, target, other)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(target.getCard(), " has protection — damage from ", other.getCard(), " prevented."));
                continue;
            }
            int damage = gameQueryService.applyDamageMultiplier(gameData, otherPower, entry);
            damageSupport.dealCreatureDamage(gameData, entry, target, damage, other);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
