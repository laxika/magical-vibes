package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandElseMayGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealTopCardMatchingToHandElseMayGraveyardHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Autowired
    @Lazy
    private GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMatchingToHandElseMayGraveyardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        List<Card> deck = gameData.playerDecks.get(player.getId());
        if (accepted && deck != null && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            graveyardService.addCardToGraveyard(gameData, player.getId(), topCard, Zone.LIBRARY);
            gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " puts ").card(topCard)
                    .text(" into their graveyard.").build());
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " leaves the revealed card on top of their library."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
