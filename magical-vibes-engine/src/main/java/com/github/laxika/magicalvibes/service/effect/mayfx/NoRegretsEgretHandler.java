package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NoRegretsEgretEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.MulliganService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Handles No-Regrets Egret's optional mulligan-time library look. */
@Component
@RequiredArgsConstructor
public class NoRegretsEgretHandler implements MayEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final MulliganService mulliganService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NoRegretsEgretEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            mulliganService.declineMulliganAction(gameData, player);
            return;
        }

        Card egret = ability.sourceCard();
        cardRevealService.revealToAllPlayers(
                gameData, player.getId(), GameEventFact.RevealZone.HAND, List.of(egret));

        List<Card> library = gameData.playerDecks.getOrDefault(player.getId(), List.of());
        int lookedAtCount = Math.min(2, library.size());
        List<Card> topCards = library.subList(0, lookedAtCount);
        if (lookedAtCount > 0) {
            cardRevealService.revealToPlayer(
                    gameData, player.getId(), GameEventFact.RevealZone.LIBRARY,
                    topCards, player.getId());
        }

        String cardWord = lookedAtCount == 1 ? "card" : "cards";
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " reveals ").card(egret)
                .text(" and looks at the top " + lookedAtCount + " " + cardWord
                        + " of their library.")
                .build());
        mulliganService.continueMulliganDecision(gameData, player);
    }
}
