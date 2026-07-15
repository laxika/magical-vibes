package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cragganwick Cremator: discard a card at random. If a creature card is discarded this way, the
 * source deals damage equal to that card's power to target player or planeswalker.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " has no cards to discard."));
            return;
        }

        // Discard a card at random (self-inflicted, so no opponent-discard replacements apply).
        gameData.discardCausedByOpponent = false;
        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card discarded = hand.remove(randomIndex);
        graveyardService.addCardToGraveyard(gameData, controllerId, discarded);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " discards " + discarded.getName() + " at random."));
        log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(), sourceName);
        triggerCollectionService.checkDiscardTriggers(gameData, controllerId, discarded);

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics).
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }

        // Only a discarded creature card deals damage.
        if (!discarded.hasType(CardType.CREATURE)) {
            return;
        }

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        int power = discarded.getPower() != null ? Math.max(0, discarded.getPower()) : 0;
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
