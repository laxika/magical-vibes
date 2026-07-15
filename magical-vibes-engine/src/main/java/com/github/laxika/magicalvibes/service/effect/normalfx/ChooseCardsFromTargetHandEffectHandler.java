package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Reveal the target player's hand, let the caster choose card(s), and route each chosen card to the
 * effect's {@link com.github.laxika.magicalvibes.model.effect.HandChoiceDestination}:
 * <ul>
 *   <li>DISCARD / EXILE reuse {@link PlayerInteractionSupport#resolveHandRevealAndChoose} (type
 *       filtering + "reveals their hand" flow); DISCARD sets {@code discardCausedByOpponent} and
 *       EXILE forwards the source permanent id when {@code returnOnSourceLeave}.</li>
 *   <li>TOP_OF_LIBRARY reveals every card ("looks at ... hand") with no type filter and begins a
 *       put-on-top choice; the final ordering is applied by the RevealedHandChoice answer handler.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCardsFromTargetHandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardsFromTargetHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseCardsFromTargetHandEffect) effect;

        int count = amountEvaluationService.evaluate(gameData, e.count(), AmountContext.forStackEntry(entry, null));

        // An X of 0 (e.g. Mind Warp for X=0) chooses no cards: nothing to reveal-and-choose.
        if (count <= 0) {
            return;
        }

        switch (e.destination()) {
            case DISCARD -> {
                gameData.discardCausedByOpponent = true;
                playerInteractionSupport.resolveHandRevealAndChoose(gameData, entry, count,
                        e.excludedTypes(), e.includedTypes(), true, false, null);
            }
            case EXILE -> {
                UUID sourcePermanentId = e.returnOnSourceLeave() ? entry.getSourcePermanentId() : null;
                playerInteractionSupport.resolveHandRevealAndChoose(gameData, entry, count,
                        e.excludedTypes(), e.includedTypes(), false, true, sourcePermanentId);
            }
            case TOP_OF_LIBRARY -> resolveToTopOfLibrary(gameData, entry, count);
        }
    }

    private void resolveToTopOfLibrary(GameData gameData, StackEntry entry, int count) {
        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            return;
        }

        // Log and reveal hand to caster
        String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
        String logEntry = casterName + " looks at " + targetName + "'s hand: " + cardNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        int cardsToChoose = Math.min(count, hand.size());

        // Build valid indices (all cards in hand)
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                casterId, targetPlayerId, validIndices, cardsToChoose, false, false, List.of(), null,
                "Choose a card to put on top of " + targetName + "'s library.", false, false));

        log.info("Game {} - {} choosing {} card(s) from {}'s hand to put on top of library",
                gameData.id, casterName, cardsToChoose, targetName);
    }
}
