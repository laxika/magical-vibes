package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MayPayLifeEffectResolutionHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPayLifeEffect mayPayLife = (MayPayLifeEffect) effect;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(mayPayLife),
                entry.getCard().getName() + " - " + mayPayLife.prompt(),
                entry.getTargetId(),
                null,
                entry.getSourcePermanentId()));
    }
}
