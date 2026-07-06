package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsMayPlayUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsMayPlayUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (mirrors the other amount-driven handlers).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = amountEvaluationService.evaluate(gameData,
                ((ExileTopCardsMayPlayUntilNextTurnEffect) effect).count(),
                AmountContext.forStackEntry(entry, source));
        if (count <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            String logEntry = controllerName + "'s library is empty — nothing to exile.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<String> exiledNames = new ArrayList<>();
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exileSupport.grantPlayUntilOwnersNextTurn(gameData, topCard.getId(), controllerId);
            exiledNames.add(topCard.getName());
        }

        String logEntry = controllerName + " exiles "
                + String.join(", ", exiledNames)
                + " from the top of their library (may play until end of next turn).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} cards from library top (may play until end of next turn)",
                gameData.id, controllerName, exiledNames.size());
    }
}
