package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyChangeCastCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Records the cost change of each card the subject names, logged as {@link PerpetualChangeLog} says. It never
 * prompts.
 */
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
        List<SubjectResolutionService.LocatedCard> located =
                subjectResolutionService.locatedCards(gameData, entry, change, change.cards());
        for (SubjectResolutionService.LocatedCard card : located) {
            if (change.change() > 0) {
                PerpetualCardCastCostSupport.rememberIncrease(gameData, card.card(), amount);
            } else {
                PerpetualCardCastCostSupport.remember(gameData, card.card(), amount);
            }
        }
        PerpetualChangeLog.log(gameLogService, subjectResolutionService, gameData, entry, change, change.cards(),
                located, " perpetually costs " + amount + " generic mana "
                        + (change.change() > 0 ? "more" : "less") + " to cast.");
    }
}
