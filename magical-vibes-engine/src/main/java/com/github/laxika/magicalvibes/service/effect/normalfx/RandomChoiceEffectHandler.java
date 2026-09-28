package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RandomChoiceEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RandomChoiceEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RandomChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RandomChoiceEffect randomChoice = (RandomChoiceEffect) effect;
        CardEffect selected = randomChoice.options().get(
                ThreadLocalRandom.current().nextInt(randomChoice.options().size()));
        int currentIndex = -1;
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex < 0) {
            throw new IllegalStateException("Random choice effect is not present on its stack entry");
        }
        entry.insertEffectsToResolve(currentIndex + 1, List.of(selected));
    }
}
