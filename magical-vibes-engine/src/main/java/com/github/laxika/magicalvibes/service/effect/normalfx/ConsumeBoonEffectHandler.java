package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConsumeBoonEffect;
import org.springframework.stereotype.Component;

/** Uses up the boon whose ability is resolving, which its ability uses up as it resolves. */
@Component
public class ConsumeBoonEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConsumeBoonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        synchronized (gameData.temporaryGlobalTriggeredAbilities) {
            gameData.temporaryGlobalTriggeredAbilities.stream()
                    .filter(delayed -> isUsedUpBy(delayed, entry))
                    .findFirst()
                    .ifPresent(gameData.temporaryGlobalTriggeredAbilities::remove);
        }
    }

    private static boolean isUsedUpBy(TemporaryGlobalTriggeredAbility delayed, StackEntry entry) {
        return delayed.boon() && delayed.repeats()
                && delayed.controllerId().equals(entry.getControllerId())
                && delayed.sourceCard().getId().equals(entry.getCard().getId());
    }
}
