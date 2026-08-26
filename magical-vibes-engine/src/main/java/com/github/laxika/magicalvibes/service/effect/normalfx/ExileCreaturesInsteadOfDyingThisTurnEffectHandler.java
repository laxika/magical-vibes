package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesInsteadOfDyingThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileCreaturesInsteadOfDyingThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreaturesInsteadOfDyingThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileCreaturesInsteadOfDyingThisTurnEffect exileEffect =
                (ExileCreaturesInsteadOfDyingThisTurnEffect) effect;
        if (exileEffect.opponentsOnly()) {
            gameData.playersExilingOpponentCreaturesInsteadOfDyingThisTurn.add(entry.getControllerId());
        } else {
            gameData.playersExilingCreaturesInsteadOfDyingThisTurn.add(entry.getControllerId());
        }
        gameLogService.append(gameData, GameLog.text(
                exileEffect.opponentsOnly()
                        ? "Creatures opponents control that would die this turn are exiled instead."
                        : "Creatures that would die this turn are exiled instead."));
    }
}
