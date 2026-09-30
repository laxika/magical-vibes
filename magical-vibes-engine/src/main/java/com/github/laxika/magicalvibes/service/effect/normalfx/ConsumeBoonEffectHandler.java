package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Boon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConsumeBoonEffect;
import org.springframework.stereotype.Component;

/** Consumes the source card's boon after its end-step trigger resolves. */
@Component
public class ConsumeBoonEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConsumeBoonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConsumeBoonEffect consume = (ConsumeBoonEffect) effect;
        gameData.boons.stream()
                .filter(boon -> matches(boon, entry, consume))
                .findFirst()
                .ifPresent(gameData.boons::remove);
    }

    private boolean matches(Boon boon, StackEntry entry, ConsumeBoonEffect consume) {
        return boon.controllerId().equals(entry.getControllerId())
                && boon.sourceCard().getId().equals(entry.getCard().getId())
                && boon.trigger() == consume.trigger();
    }
}
