package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The targeted player chooses one card from their own hand to exile. The hand is not revealed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerExilesCardFromHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerExilesCardFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }
        List<Card> hand = gameData.playerHands.get(targetId);
        if (hand == null || hand.isEmpty()) {
            String name = gameData.playerIdToName.get(targetId);
            gameLogService.append(gameData, GameLog.text(name + " has no cards to exile from hand."));
            return;
        }
        playerInputService.beginExileFromHandChoice(gameData, targetId, null, 1);
        log.info("Game {} - {} chooses a card from hand to exile", gameData.id, targetId);
    }
}
