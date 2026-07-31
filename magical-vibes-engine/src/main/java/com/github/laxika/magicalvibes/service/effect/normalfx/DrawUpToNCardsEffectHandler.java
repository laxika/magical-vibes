package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToRecipient;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link DrawUpToNCardsEffect}: the resolving controller picks how many cards to draw
 * (0 to {@code max}) through an {@code XValueChoice} interaction, then draws that many. The handler
 * re-enters after the choice, reading {@code gameData.chosenXValue}.
 */
@Component
@RequiredArgsConstructor
public class DrawUpToNCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawUpToNCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawUpToNCardsEffect) effect;
        UUID playerId = e.recipient() == DrawUpToRecipient.OPPONENT
                ? gameQueryService.getOpponentId(gameData, entry.getControllerId())
                : entry.getControllerId();
        if (playerId == null) {
            return;
        }
        String cardName = entry.getCard().getName();

        if (gameData.chosenXValue != null) {
            int chosen = gameData.chosenXValue;
            gameData.chosenXValue = null;
            if (chosen > 0) {
                playerInteractionSupport.applyDrawCards(gameData, playerId, chosen);
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                        + " draws " + chosen + " card" + (chosen != 1 ? "s" : "") + "."));
            }
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, e.max(), "Draw up to " + e.max() + " cards for " + cardName + ".", cardName));
    }
}
