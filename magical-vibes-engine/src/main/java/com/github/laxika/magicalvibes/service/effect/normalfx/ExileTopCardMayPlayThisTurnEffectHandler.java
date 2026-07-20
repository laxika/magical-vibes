package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Oracle's Vault: exile the top card of the controller's library; until end of turn the controller
 * may play that card (any type). The free variant additionally lets it be cast without paying its
 * mana cost.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardMayPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean withoutPaying = ((ExileTopCardMayPlayThisTurnEffect) effect).withoutPayingManaCost();
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(controllerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);

        // Play permission (any card type) expiring at end of turn.
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
        if (withoutPaying) {
            gameData.exilePlayWithoutPayingManaCost.add(topCard.getId());
        }

        String playNote = withoutPaying
                ? " (may play it without paying its mana cost this turn)"
                : " (may play it this turn)";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .text(controllerName + " exiles ").card(topCard)
                .text(" from the top of their library" + playNote + ".").build());
        log.info("Game {} - {} exiles {} from library top (withoutPaying={})",
                gameData.id, controllerName, topCard.getName(), withoutPaying);
    }
}
