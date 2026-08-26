package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.OpponentCantAttackSourceControllerThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpponentCantAttackSourceControllerThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentCantAttackSourceControllerThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackingPlayerId = entry.getTargetId();
        UUID protectedPlayerId = entry.getControllerId();
        if (attackingPlayerId == null || protectedPlayerId == null) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                protectedPlayerId,
                new CreaturesCantAttackControllerUnlessPredicateEffect(
                        new PermanentNotPredicate(new PermanentTruePredicate()), true, attackingPlayerId),
                null, protectedPlayerId, null, EffectDuration.UNTIL_END_OF_TURN, 0L));
    }
}
