package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOneShotCreatureSpellPerpetualPowerBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import org.springframework.stereotype.Component;

import java.util.List;

/** Registers Dragonborn Immolator's source-independent Gift of Tiamat boon. */
@Component
public class RegisterOneShotCreatureSpellPerpetualPowerBoostEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterOneShotCreatureSpellPerpetualPowerBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int power = Math.max(0, entry.getEventValue());
        if (power == 0) {
            return;
        }

        gameData.queueDelayedAction(new DelayedControllerSpellCastTrigger(
                entry.getControllerId(),
                null,
                entry.getCard(),
                new CardTypePredicate(CardType.CREATURE),
                null,
                List.of(new PerpetuallyBoostTriggeringCardEffect(power, 0)),
                true,
                false,
                null,
                null,
                null,
                false,
                true,
                gameData.turnNumber
        ));
    }
}
