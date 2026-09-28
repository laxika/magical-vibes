package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetChosenCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PerpetuallySetChosenCardCharacteristicsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallySetChosenCardCharacteristicsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PerpetuallySetChosenCardCharacteristicsEffect) effect;
        Card chosenCard = entry.getChosenObjectCard();
        if (chosenCard == null) {
            return;
        }

        Card modifiedCard = chosenCard.createRuntimeCopy();
        modifiedCard.setColor(e.color());
        modifiedCard.setColors(List.of(e.color()));
        modifiedCard.setManaCost(e.manaCost());
        modifiedCard.freeze();

        for (List<Card> hand : gameData.playerHands.values()) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).getId().equals(chosenCard.getId())) {
                    hand.set(i, modifiedCard);
                    entry.setChosenObjectCard(modifiedCard);
                    gameLogService.append(gameData, GameLog.cardThen(chosenCard,
                            " perpetually becomes " + e.color().name().toLowerCase()
                                    + " and its mana cost perpetually becomes " + e.manaCost() + "."));
                    return;
                }
            }
        }
    }
}
