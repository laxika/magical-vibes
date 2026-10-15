package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCardPutIntoHandFromLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Kithkin Brinefarer's duplicate trigger. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfCardPutIntoHandFromLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfCardPutIntoHandFromLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card triggeringCard = entry.getTriggeringCardSnapshot();
        if (triggeringCard == null) {
            return;
        }

        Card duplicate = triggeringCard.createConjuredCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        gameData.addCardToHand(entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.cardThen(duplicate, " is conjured into "
                + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
        triggerCollectionService.checkConjureTriggers(gameData, entry.getControllerId(), 1);
    }
}
