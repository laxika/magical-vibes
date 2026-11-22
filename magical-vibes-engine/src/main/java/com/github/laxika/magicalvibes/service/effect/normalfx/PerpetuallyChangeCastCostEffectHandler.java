package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyChangeCastCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Records the cost change of each card the subject names. It never prompts. */
@Component
@RequiredArgsConstructor
public class PerpetuallyChangeCastCostEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyChangeCastCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var change = (PerpetuallyChangeCastCostEffect) effect;
        int amount = Math.abs(change.change());
        for (Card card : subjectResolutionService.cards(gameData, entry, change, change.cards())) {
            if (change.change() > 0) {
                PerpetualCardCastCostSupport.rememberIncrease(gameData, card, amount);
            } else {
                PerpetualCardCastCostSupport.remember(gameData, card, amount);
            }
            gameLogService.append(gameData, GameLog.cardThenUnlessHidden(gameData, card, " perpetually costs " + amount
                    + " generic mana " + (change.change() > 0 ? "more" : "less") + " to cast."));
        }
    }
}
