package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect.Stage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Two-stage may for {@link LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect}:
 * matching card → may reveal to hand; if declined (or non-matching), may put into graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingToHandElseMayGraveyardHandler
        implements MayEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect)
                .map(e -> (LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect) e)
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                        player.getUsername() + " reveals ", topCard, " and puts it into their hand."));
                gameData.addCardToHand(controllerId, topCard);
                log.info("Game {} - {} reveals {} to hand (Archghoul-style)",
                        gameData.id, player.getUsername(), topCard.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            // Declined hand — offer the graveyard may.
            if (deck != null && !deck.isEmpty()) {
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
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (effect.stage() == Stage.MAY_GRAVEYARD) {
            if (accepted && deck != null && !deck.isEmpty()) {
                Card topCard = deck.removeFirst();
                gameData.playerGraveyards.get(controllerId).add(topCard);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                        .text(player.getUsername() + " puts ")
                        .card(topCard)
                        .text(" into their graveyard.")
                        .build());
                log.info("Game {} - {} puts {} into graveyard (Archghoul-style)",
                        gameData.id, player.getUsername(), topCard.getName());
            } else {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        player.getUsername() + " leaves the card on top of their library."));
                log.info("Game {} - {} leaves top card on library (Archghoul-style)",
                        gameData.id, player.getUsername());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
