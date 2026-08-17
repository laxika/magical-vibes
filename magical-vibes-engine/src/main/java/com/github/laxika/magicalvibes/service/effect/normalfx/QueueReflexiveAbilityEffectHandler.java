package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Resolves a continuation by placing its effect on the stack as a reflexive ability. */
@Component
public class QueueReflexiveAbilityEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return QueueReflexiveAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        QueueReflexiveAbilityEffect queueEffect = (QueueReflexiveAbilityEffect) effect;
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s reflexive ability",
                new ArrayList<>(List.of(queueEffect.effect())),
                0,
                entry.getSourcePermanentId()));
    }
}
