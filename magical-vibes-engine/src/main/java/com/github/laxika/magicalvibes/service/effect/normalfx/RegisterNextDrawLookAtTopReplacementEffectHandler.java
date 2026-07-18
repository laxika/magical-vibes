package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawLookAtTopReplacementEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Aladdin's Lamp — registers a one-shot delayed replacement of the controller's next draw this turn:
 * "instead look at the top X cards of your library, put all but one on the bottom in a random order,
 * then draw a card." X is the value paid for the {@code {X}, {T}} activation (the stack entry's
 * {@code xValue}). The replacement is stored per-player in {@link GameData#pendingNextDrawLookAtTop}
 * and consumed by {@code DrawService.resolveDrawCard}; it expires at cleanup ("this turn").
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterNextDrawLookAtTopReplacementEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterNextDrawLookAtTopReplacementEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int x = entry.getXValue();
        // "X can't be 0." If a zero (or negative) X somehow reaches here, the activation does nothing.
        if (x < 1) {
            return;
        }

        gameData.pendingNextDrawLookAtTop.put(controllerId, x);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "The next time " + playerName + " would draw a card this turn, they'll look at the top "
                + x + " card" + (x != 1 ? "s" : "") + " of their library instead (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text("The next time " + playerName + " would draw a card this turn, they'll look at the top " + x + " card" + (x != 1 ? "s" : "") + " of their library instead (").card(entry.getCard()).text(").").build());
        log.info("Game {} - {} registers Aladdin's Lamp next-draw replacement (X={})", gameData.id, playerName, x);
    }
}
