package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
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
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, change, change.cards())) {
            Card card = located.card();
            if (change.change() > 0) {
                PerpetualCardCastCostSupport.rememberIncrease(gameData, card, amount);
            } else {
                PerpetualCardCastCostSupport.remember(gameData, card, amount);
            }
            String changed = " perpetually costs " + amount + " generic mana "
                    + (change.change() > 0 ? "more" : "less") + " to cast.";
            // A card in a hand or library is hidden (CR 400.2): the log doesn't say which it is.
            gameLogService.append(gameData, located.zone() == Zone.HAND || located.zone() == Zone.LIBRARY
                    ? GameLog.text("A card in " + gameData.playerIdToName.get(located.ownerId()) + "'s "
                            + (located.zone() == Zone.HAND ? "hand" : "library") + changed)
                    : GameLog.cardThenUnlessHidden(gameData, card, changed));
        }
    }
}
