package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect.Stage;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Completes Amareth's optional reveal-and-draw choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandHandler
        implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect)
                .map(e -> (LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect) e)
                .findFirst()
                .orElse(null);
        if (effect == null || effect.stage() != Stage.MAY_HAND) {
            return;
        }

        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (accepted && deck != null && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " reveals ", topCard, " and puts it into their hand."));
            gameData.addCardToHand(controllerId, topCard);
            log.info("Game {} - {} reveals {} to hand", gameData.id, player.getUsername(), topCard.getName());
        } else {
            gameLogService.append(gameData,
                    GameLog.text(player.getUsername() + " leaves the card on top of their library."));
            log.info("Game {} - {} declines Amareth's reveal", gameData.id, player.getUsername());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
