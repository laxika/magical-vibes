package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect.Stage;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * May-ability follow-ups for {@link LookAtTopCardMayRevealMatchingToHandEffect}: matching card →
 * may reveal to hand; a declined card may be put into the graveyard or on the library bottom when
 * the effect declares that fallback, and otherwise stays on top of the library.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingToHandHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    @Autowired
    @Lazy
    private GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingToHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealMatchingToHandEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LookAtTopCardMayRevealMatchingToHandEffect)
                .map(e -> (LookAtTopCardMayRevealMatchingToHandEffect) e)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return;
        }

        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (effect.stage() == Stage.MAY_HAND) {
            if (accepted && deck != null && !deck.isEmpty()) {
                Card topCard = deck.removeFirst();
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " reveals ", topCard, " and puts it into their hand."));
                gameData.addCardToHand(controllerId, topCard);
                log.info("Game {} - {} reveals {} to hand", gameData.id, player.getUsername(), topCard.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            // Declined hand — offer the graveyard may when the effect has that fallback.
            if (effect.otherwiseDestination()
                    == LookAtTopCardMayRevealMatchingToHandEffect.OtherwiseDestination.BOTTOM
                    && deck != null && !deck.isEmpty()) {
                Card bottomCard = deck.removeFirst();
                deck.addLast(bottomCard);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " puts ", bottomCard, " on the bottom of their library."));
                log.info("Game {} - {} declines hand; puts {} on the bottom of their library",
                        gameData.id, player.getUsername(), bottomCard.getName());
            } else if (effect.otherwiseDestination()
                    == LookAtTopCardMayRevealMatchingToHandEffect.OtherwiseDestination.GRAVEYARD
                    && deck != null && !deck.isEmpty()) {
                Card topCard = deck.getFirst();
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        ability.sourceCard(),
                        controllerId,
                        List.of(effect.withStage(Stage.MAY_GRAVEYARD)),
                        ability.sourceCard().getName() + " — Put " + topCard.getName()
                                + " into your graveyard?"
                ));
                log.info("Game {} - {} declines hand; may put {} to graveyard",
                        gameData.id, player.getUsername(), topCard.getName());
            } else if (effect.otherwiseDestination()
                    == LookAtTopCardMayRevealMatchingToHandEffect.OtherwiseDestination.TOP) {
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " leaves the card on top of their library."));
                log.info("Game {} - {} declines hand; card stays on top", gameData.id, player.getUsername());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (effect.stage() == Stage.MAY_GRAVEYARD) {
            if (accepted && deck != null && !deck.isEmpty()) {
                Card topCard = deck.removeFirst();
                graveyardService.addCardToGraveyard(gameData, controllerId, topCard, Zone.LIBRARY);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " puts ")
                        .card(topCard)
                        .text(" into their graveyard.")
                        .build());
                log.info("Game {} - {} puts {} into graveyard", gameData.id, player.getUsername(), topCard.getName());
            } else {
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " leaves the card on top of their library."));
                log.info("Game {} - {} leaves top card on library", gameData.id, player.getUsername());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
