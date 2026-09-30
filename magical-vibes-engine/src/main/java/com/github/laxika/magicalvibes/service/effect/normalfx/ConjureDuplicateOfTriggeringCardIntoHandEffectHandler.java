package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringCardIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a duplicate of the card that caused a trigger into its controller's hand. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTriggeringCardIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTriggeringCardIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card triggeringCard = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringCard == null) {
            return;
        }

        Card duplicate = triggeringCard.createRuntimeCopyWithNewId();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.setToken(true);
        duplicate.setTokenCard(true);
        duplicate.freeze();
        gameData.addCardToHand(entry.getControllerId(), duplicate);

        gameLogService.append(gameData,
                GameLog.cardThen(entry.getCard(), " conjures a duplicate of "
                        + triggeringCard.getName() + " into their hand."));
    }
}
