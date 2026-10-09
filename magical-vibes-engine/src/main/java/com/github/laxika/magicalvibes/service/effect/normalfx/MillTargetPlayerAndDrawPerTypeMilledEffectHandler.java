package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndDrawPerTypeMilledEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillTargetPlayerAndDrawPerTypeMilledEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final DrawService drawService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTargetPlayerAndDrawPerTypeMilledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillTargetPlayerAndDrawPerTypeMilledEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> milled = graveyardService.resolveMillPlayer(gameData, targetPlayerId, e.count());
        int cardsToMill = milled.size();
        int matchCount = (int) milled.stream().filter(card -> card.hasType(e.cardType())).count();

        UUID controllerId = entry.getControllerId();
        for (int i = 0; i < matchCount; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }

        if (matchCount > 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " draws " + matchCount
                    + " card" + (matchCount != 1 ? "s" : "") + "."));
        }
        log.info("Game {} - {} milled {} card(s), {} {} card(s), drawing {}",
                gameData.id, entry.getCard().getName(), cardsToMill, matchCount, e.cardType(), matchCount);
    }
}
