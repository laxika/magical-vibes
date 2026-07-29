package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawFromExiledPileReplacementEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mangara's Tome — registers a one-shot delayed replacement of the controller's next draw this turn:
 * "instead put the top card of the exiled pile into its owner's hand." Each activation queues one
 * more replacement in {@link GameData#pendingNextDrawFromExiledPile} (two activations replace the
 * next two draws, since a single draw can only be replaced once); they are consumed in
 * {@code DrawService.resolveDrawCard} and expire at cleanup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterNextDrawFromExiledPileReplacementEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterNextDrawFromExiledPileReplacementEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        gameData.pendingNextDrawFromExiledPile
                .computeIfAbsent(controllerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(sourcePermanentId);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text("The next time " + playerName + " would draw a card this turn, they'll put the top card of the exiled pile into its owner's hand instead (")
                .card(entry.getCard())
                .text(").")
                .build());
        log.info("Game {} - {} registers a next-draw-from-exiled-pile replacement", gameData.id, playerName);
    }
}
