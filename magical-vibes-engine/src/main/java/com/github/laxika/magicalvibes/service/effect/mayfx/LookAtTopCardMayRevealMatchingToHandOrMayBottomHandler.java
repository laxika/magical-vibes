package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect.Stage;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingToHandOrMayBottomHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect effect = ability.effects().stream()
                .filter(LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect.class::isInstance)
                .map(LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(player.getId());
        if (deck == null || deck.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (effect.stage() == Stage.MAY_HAND) {
            if (accepted) {
                Card topCard = deck.removeFirst();
                gameData.addCardToHand(player.getId(), topCard);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " reveals ", topCard, " and puts it into their hand."));
            } else {
                Card topCard = deck.getFirst();
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        ability.sourceCard(), player.getId(),
                        List.of(effect.withStage(Stage.MAY_BOTTOM)),
                        ability.sourceCard().getName() + " - Put " + topCard.getName()
                                + " on the bottom of your library?"));
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            Card topCard = deck.removeFirst();
            deck.add(topCard);
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " puts the top card on the bottom of their library."));
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " leaves the card on top of their library."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
