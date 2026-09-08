package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillActivePlayerAndCreateTokensByManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillActivePlayerAndCreateTokensByManaValueEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillActivePlayerAndCreateTokensByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillActivePlayerAndCreateTokensByManaValueEffect) effect;
        UUID playerId = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getActivePlayerId() != null ? entry.getActivePlayerId() : entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card milled = deck.getFirst();
        int manaValue = Math.max(0, milled.getManaValue());
        List<Card> milledCards = graveyardService.resolveMillPlayer(gameData, playerId, 1);
        if (!milledCards.contains(milled)) {
            return;
        }

        entry.setEventValue(manaValue);
        if (manaValue > 0) {
            permanentControlSupport.applyCreateToken(
                    gameData, playerId, e.tokenTemplate().withAmount(manaValue),
                    entry.getCard().getSetCode());
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " mills ").card(milled)
                .text(" and creates " + manaValue + " " + e.tokenTemplate().tokenName()
                        + " token" + (manaValue != 1 ? "s" : "."))
                .build());
        log.info("Game {} - {} milled {} (mana value {}), creating {} {} tokens",
                gameData.id, playerName, milled.getName(), manaValue, manaValue,
                e.tokenTemplate().tokenName());
    }
}
