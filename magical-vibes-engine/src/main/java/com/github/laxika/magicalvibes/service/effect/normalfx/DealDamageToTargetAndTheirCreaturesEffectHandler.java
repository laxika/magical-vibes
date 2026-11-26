package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
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
public class DealDamageToTargetAndTheirCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetAndTheirCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetAndTheirCreaturesEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        int damage = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, null));
        int rawDamage = damage;

        // Determine the affected player: if target is a player, use directly;
        // if target is a planeswalker, use its controller
        List<FloatingContinuousEffect> shieldsAtStart = gameData.shieldEffects(SourceNextDamageToAnyTargetShield.class);
        List<FloatingContinuousEffect> restoredShields = new ArrayList<>();
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

            damageSupport.dealCreatureDamage(gameData, entry, targetPw, rawDamage);
        }

        // Deal damage to each creature the affected player controls
        List<Permanent> battlefield = gameData.playerBattlefields.get(affectedPlayerId);
        if (battlefield != null) {
            for (Permanent creature : new ArrayList<>(battlefield)) {
                if (!gameQueryService.isCreature(gameData, creature)) continue;
                // Skip the planeswalker target (already dealt damage above)
                if (!targetIsPlayer && creature.getId().equals(targetId)) continue;
                restoreSimultaneousShields(gameData, shieldsAtStart, restoredShields);
                damageSupport.dealCreatureDamage(gameData, entry, creature, rawDamage);
            }
        }
        restoredShields.forEach(gameData::endFloatingEffect);

        gameOutcomeService.checkWinCondition(gameData);
    
    }

    /**
     * All damage this effect deals is one simultaneous event, so a "next time a source would deal
     * damage" shield consumed by an earlier recipient must still apply to the remaining recipients.
     */
    private void restoreSimultaneousShields(GameData gameData, List<FloatingContinuousEffect> shieldsAtStart,
                                            List<FloatingContinuousEffect> restoredShields) {
        List<FloatingContinuousEffect> inForce = gameData.shieldEffects(SourceNextDamageToAnyTargetShield.class);
        for (FloatingContinuousEffect fe : shieldsAtStart) {
            if (((SourceNextDamageToAnyTargetShield) fe.effect()).remainingDamage() == Integer.MAX_VALUE
                    && !inForce.contains(fe)) {
                gameData.reinstateShield(fe);
                restoredShields.add(fe);
            }
        }
    }
}
