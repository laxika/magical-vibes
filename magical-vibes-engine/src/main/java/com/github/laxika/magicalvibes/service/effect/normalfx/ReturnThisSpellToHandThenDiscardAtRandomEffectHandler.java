package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnThisSpellToHandThenDiscardAtRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Hanabi Blast: return this spell to its owner's hand, then discard a card at random.
 *
 * <p>The spell is back in hand before the discard, so it is one of the candidates — with an empty
 * hand it discards itself, which is what keeps the card from being free repeatable damage. The
 * engine moves a resolved spell off the stack only after resolution finishes, so instead of putting
 * the card into the hand here the handler picks uniformly among the hand plus the spell card and
 * then sets the spell's disposition: return-to-hand when some other card was discarded, the default
 * graveyard when the spell picked itself.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnThisSpellToHandThenDiscardAtRandomEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnThisSpellToHandThenDiscardAtRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID ownerId = entry.getOwnerId();
        String playerName = gameData.playerIdToName.get(ownerId);
        Card spellCard = entry.getCard();
        if (spellCard == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(ownerId);
        int handSize = hand == null ? 0 : hand.size();

        // The extra slot is the spell card itself, which has just returned to the same hand. A
        // controller who is not the owner discards from their own hand, which the spell never joins.
        boolean spellJoinsHand = ownerId.equals(entry.getControllerId());
        int candidates = handSize + (spellJoinsHand ? 1 : 0);
        if (candidates == 0) {
            entry.setReturnToHandAfterResolving(true);
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards to discard."));
            return;
        }

        // Self-inflicted discard, so no opponent-discard punishers apply.
        gameData.discardCausedByOpponent = false;
        int randomIndex = ThreadLocalRandom.current().nextInt(candidates);

        Card discarded;
        if (randomIndex == handSize) {
            // The spell discarded itself: leave the default graveyard disposition in place.
            discarded = spellCard;
        } else {
            discarded = hand.remove(randomIndex);
            graveyardService.discardCard(gameData, ownerId, discarded);
            entry.setReturnToHandAfterResolving(true);
        }

        gameLogService.append(gameData, GameLog.textCardText(playerName + " discards ", discarded, " at random."));
        log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(),
                spellCard.getName());
        triggerCollectionService.checkDiscardTriggers(gameData, ownerId, discarded);

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics).
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }
}
