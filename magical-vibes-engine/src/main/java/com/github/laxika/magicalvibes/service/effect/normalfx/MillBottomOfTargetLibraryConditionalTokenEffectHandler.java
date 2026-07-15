package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomOfTargetLibraryConditionalTokenEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillBottomOfTargetLibraryConditionalTokenEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillBottomOfTargetLibraryConditionalTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillBottomOfTargetLibraryConditionalTokenEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            String logEntry = targetPlayerName + "'s library is empty — " + sourceName + "'s ability does nothing.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} ability: {}'s library is empty", gameData.id, sourceName, targetPlayerName);
            return;
        }

        Card bottomCard = deck.removeLast();
        graveyardService.addCardToGraveyard(gameData, targetPlayerId, bottomCard);

        String logEntry = targetPlayerName + " puts " + bottomCard.getName() + " from the bottom of their library into their graveyard.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} ability: {} puts {} from bottom of library into graveyard",
                gameData.id, sourceName, targetPlayerName, bottomCard.getName());

        if (bottomCard.hasType(e.conditionType())) {
            CreateTokenEffect tokenEffect = new CreateTokenEffect(
                    e.tokenName(), e.tokenPower(), e.tokenToughness(),
                    e.tokenColor(), e.tokenSubtypes(),
                    Set.of(), Set.of()
            );
            permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode()
            );
        }
    }
}
