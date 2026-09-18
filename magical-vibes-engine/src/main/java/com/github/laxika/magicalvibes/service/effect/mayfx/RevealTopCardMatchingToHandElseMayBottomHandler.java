package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandElseMayBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RevealTopCardMatchingToHandElseMayBottomHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMatchingToHandElseMayBottomEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        List<Card> deck = gameData.playerDecks.get(player.getId());
        if (accepted && deck != null && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            deck.add(topCard);
            gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " puts ")
                    .card(topCard).text(" on the bottom of their library.").build());
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " leaves the revealed card on top of their library."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
