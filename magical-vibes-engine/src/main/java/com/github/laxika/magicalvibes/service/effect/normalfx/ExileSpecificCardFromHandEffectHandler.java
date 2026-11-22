package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpecificCardFromHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/** Exiles the named cards that are still in a hand. */
@Component
@RequiredArgsConstructor
public class ExileSpecificCardFromHandEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSpecificCardFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileSpecificCardFromHandEffect) effect;
        for (SubjectResolutionService.LocatedCard named
                : subjectResolutionService.locatedCards(gameData, entry, exile, exile.cards())) {
            if (named.zone() != Zone.HAND
                    || !gameData.playerHands.get(named.ownerId()).removeIf(
                            held -> held.getId().equals(named.card().getId()))) {
                continue;
            }
            exileService.exileCard(gameData, named.ownerId(), named.card());
            gameLogService.append(gameData, GameLog.cardThen(named.card(),
                    " is exiled from " + gameData.playerIdToName.get(named.ownerId()) + "'s hand."));
        }
    }
}
