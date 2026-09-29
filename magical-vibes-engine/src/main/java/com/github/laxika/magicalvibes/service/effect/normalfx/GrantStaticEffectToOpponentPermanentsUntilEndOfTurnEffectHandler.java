package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect) effect;
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), new GrantEffectEffect(grant.staticEffect(), GrantScope.TARGET),
                null, null,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                EffectDuration.UNTIL_END_OF_TURN, 0));
    }
}
