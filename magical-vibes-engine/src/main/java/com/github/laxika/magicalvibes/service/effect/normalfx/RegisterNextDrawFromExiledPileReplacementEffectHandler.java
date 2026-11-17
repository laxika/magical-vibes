package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawFromExiledPileReplacementEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mangara's Tome — registers a one-shot delayed replacement of the controller's next draw this turn:
 * "instead put the top card of the exiled pile into its owner's hand." Each activation makes one
 * more floating effect on the controller (two activations replace the next two draws, since a single draw can only
 * be replaced once); they are consumed in {@code DrawService.resolveDrawCard} and end at cleanup.
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

        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), new RegisterNextDrawFromExiledPileReplacementEffect(sourcePermanentId), controllerId,
                Duration.UNTIL_END_OF_TURN));

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text("The next time " + playerName + " would draw a card this turn, they'll put the top card of the exiled pile into its owner's hand instead (")
                .card(entry.getCard())
                .text(").")
                .build());
        log.info("Game {} - {} registers a next-draw-from-exiled-pile replacement", gameData.id, playerName);
    }
}
