package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RandomChoiceEffect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Splices the randomly selected option into the current stack entry for ordinary resolution. */
@Component
public class RandomChoiceEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RandomChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RandomChoiceEffect randomChoice = (RandomChoiceEffect) effect;
        int selectedOption = ThreadLocalRandom.current().nextInt(randomChoice.options().size());
        var selectedEffects = randomChoice.options().get(selectedOption);
        int currentIndex = entry.getResolvingEffectIndex();

        entry.replaceEffectToResolve(currentIndex, selectedEffects.getFirst());
        if (selectedEffects.size() > 1) {
            entry.insertEffectsToResolve(currentIndex + 1, selectedEffects.subList(1, selectedEffects.size()));
        }
    }
}
