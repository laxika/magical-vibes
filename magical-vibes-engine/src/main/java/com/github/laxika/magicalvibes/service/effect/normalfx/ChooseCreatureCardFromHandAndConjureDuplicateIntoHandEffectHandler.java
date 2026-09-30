package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Holographic Double's creature-card choice and duplicate conjure. */
@Component
@RequiredArgsConstructor
public class ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect) effect;
        if (choiceEffect.chosenCard() != null) {
            Card duplicate = choiceEffect.chosenCard().createCardCopy();
            duplicate.setOwnerId(entry.getControllerId());
            duplicate.freeze();
            gameData.addCardToHand(entry.getControllerId(), duplicate);
            gameLogService.append(gameData, GameLog.cardThen(duplicate, " is conjured into "
                    + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
            return;
        }

        var originalTargetId = entry.getTargetId();
        entry.setTargetId(entry.getControllerId());
        try {
            playerInteractionSupport.resolveHandRevealAndChooseWithChosenCardThen(
                    gameData, entry, 1, List.of(), List.of(CardType.CREATURE), null,
                    false, false, null, false, false, 0, false,
                    false, false, false, 0, null, choiceEffect, true);
        } finally {
            entry.setTargetId(originalTargetId);
        }
    }
}
