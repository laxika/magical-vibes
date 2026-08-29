package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndBoostSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscardCardAndBoostSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardCardAndBoostSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardCardAndBoostSelfEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(controllerId) + " has no cards to discard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }
        gameData.discardCausedByOpponent = false;
        DiscardFollowUp followUp = e.drawCount() > 0
                ? DiscardFollowUp.rummageAndBoost(e.drawCount(), entry.getSourcePermanentId(), e.power(), e.toughness())
                : DiscardFollowUp.boost(entry.getSourcePermanentId(), e.power(), e.toughness());
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 1,
                followUp);
    }
}
