package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePositiveNumberEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the positive-number choice used by Scrying Glass. */
@Component
@RequiredArgsConstructor
public class ChoosePositiveNumberEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChoosePositiveNumberEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        if (gameData.chosenXValue != null) {
            source.setChosenNumber(gameData.chosenXValue);
            gameData.chosenXValue = null;
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                entry.getControllerId(), 1, Integer.MAX_VALUE,
                "Choose a number greater than 0.", entry.getCard().getName()));
    }
}
