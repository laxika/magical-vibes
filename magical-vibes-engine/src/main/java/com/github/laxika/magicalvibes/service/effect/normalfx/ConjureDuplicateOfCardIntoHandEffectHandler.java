package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCardIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a duplicate conjure from a captured card snapshot. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfCardIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfCardIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card duplicate = ((ConjureDuplicateOfCardIntoHandEffect) effect).card().createCardCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        gameData.perpetualAnyColorManaForCastCardIds.add(duplicate.getId());
        gameData.addCardToHand(entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.cardThen(duplicate, " is conjured into "
                + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
    }
}
