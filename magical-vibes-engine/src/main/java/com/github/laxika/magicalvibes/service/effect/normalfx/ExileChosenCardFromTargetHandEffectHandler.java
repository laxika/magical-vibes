package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileChosenCardFromTargetHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Exiles the hand card chosen earlier in the same resolution from the targeted player's hand. */
@Component
@RequiredArgsConstructor
public class ExileChosenCardFromTargetHandEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileChosenCardFromTargetHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card chosen = entry.getChosenObjectCard();
        UUID targetPlayerId = entry.getTargetId();
        if (chosen == null || targetPlayerId == null) {
            return;
        }
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || !hand.removeIf(card -> card.getId().equals(chosen.getId()))) {
            return;
        }
        exileService.exileCard(gameData, targetPlayerId, chosen);
        gameLogService.append(gameData, GameLog.cardThen(chosen,
                " is exiled from " + gameData.playerIdToName.get(targetPlayerId) + "'s hand."));
    }
}
