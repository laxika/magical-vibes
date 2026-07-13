package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffect} (Oona, Queen of the
 * Fae): evaluates the number of cards to exile (the paid {@code X}), then pauses to let the
 * controller choose a color. The exile-count-and-create-tokens step runs in
 * {@code ChoiceHandlerService.handleExileTopCardsChosenColorTokensChoice} once the color is picked.
 */
@Component
@RequiredArgsConstructor
public class ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, source));

        playerInputService.beginExileTopCardsChosenColorTokensChoice(gameData, entry.getControllerId(),
                entry.getTargetId(), count, e.tokenTemplate(), entry.getCard().getSetCode());
    }
}
