package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ChooseNumberEffect}: prompts the source permanent's controller to choose a number
 * in the effect's inclusive range, storing the answer on the permanent (read by a
 * characteristic-defining P/T). The "you may" wording is handled by the {@code MayEffect} wrapper, so
 * by the time this resolves the choice has been accepted. Used by Shapeshifter's upkeep re-choice.
 */
@Component
@RequiredArgsConstructor
public class ChooseNumberEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNumberEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var chooseEffect = (ChooseNumberEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        playerInputService.beginNumberChoice(gameData, entry.getControllerId(), source.getId(),
                chooseEffect.minNumber(), chooseEffect.maxNumber());
    }
}
