package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Resolves a Kardur-style goad effect as a dynamic floating combat requirement. */
@Component
public class GoadCreaturesUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadCreaturesUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GoadCreaturesUntilNextTurnEffect goad = (GoadCreaturesUntilNextTurnEffect) effect;
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard() == null ? "Goad" : entry.getCard().getName(),
                entry.getSourcePermanentId(),
                entry.getControllerId(),
                goad,
                null,
                null,
                goad.affectedPredicate(),
                EffectDuration.UNTIL_YOUR_NEXT_TURN,
                0));
    }
}
