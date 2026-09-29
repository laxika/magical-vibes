package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RandomPlayerThenFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureControlledByPlayerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Resolves Strax's random-player reflexive fight ability. */
@Component
@RequiredArgsConstructor
public class RandomPlayerThenFightsTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RandomPlayerThenFightsTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.orderedPlayerIds.isEmpty()) {
            return;
        }

        var selectedPlayerId = gameData.orderedPlayerIds.get(
                ThreadLocalRandom.current().nextInt(gameData.orderedPlayerIds.size()));
        queueReflexiveAbilityEffectHandler.resolve(gameData, entry,
                new QueueReflexiveAbilityEffect(new SourceFightsTargetCreatureControlledByPlayerEffect(
                        selectedPlayerId, entry.getSourcePermanentId())));
    }
}
