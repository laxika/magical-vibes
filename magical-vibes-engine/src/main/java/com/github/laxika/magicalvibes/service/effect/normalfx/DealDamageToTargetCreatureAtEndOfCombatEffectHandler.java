package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DealDamageToPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToTargetCreatureAtEndOfCombatEffect}: schedule the source to deal the
 * evaluated damage to the targeted creature at end of combat via a
 * {@link DealDamageToPermanentAtEndOfCombat} delayed action. E.g. Dwarven Sea Clan.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DealDamageToTargetCreatureAtEndOfCombatEffect e = (DealDamageToTargetCreatureAtEndOfCombatEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        if (damage <= 0) {
            return;
        }

        gameData.queueDelayedAction(new DealDamageToPermanentAtEndOfCombat(target.getId(),
                entry.getSourcePermanentId(), entry.getCard(), entry.getControllerId(), damage));
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " will be dealt " + damage + " damage at end of combat."));
    }
}
