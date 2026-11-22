package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlotExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Makes the exiled cards the subject names plotted. */
@Component
@RequiredArgsConstructor
public class PlotExiledCardsEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlotExiledCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var plot = (PlotExiledCardsEffect) effect;
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, plot, plot.cards())) {
            if (located.zone() != Zone.EXILE) {
                continue;
            }
            Card card = located.card();
            UUID ownerId = located.ownerId();
            gameData.plottedCardIds.add(card.getId());
            gameData.grantExilePlayPermission(card.getId(), ownerId, Duration.PERMANENT);
            gameData.setExilePlayWithoutPayingManaCost(card.getId(), true);
            ActionEvents.plotted(gameData, ownerId, card);
            gameLogService.append(gameData, gameData.isExiledFaceDown(card.getId())
                    ? GameLog.textCardText("A face-down card in exile becomes plotted by ", entry.getCard(), ".")
                    : GameLog.cardTextCard(card, " becomes plotted by ", entry.getCard(), "."));
        }
    }
}
