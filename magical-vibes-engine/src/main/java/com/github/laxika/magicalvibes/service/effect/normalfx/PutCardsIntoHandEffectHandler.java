package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link PutCardsIntoHandEffect}: each card its subject names in a public zone or revealed in a library goes
 * to its owner's hand (CR 400.3).
 * It never prompts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCardsIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardsIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var put = (PutCardsIntoHandEffect) effect;
        List<SubjectResolutionService.LocatedCard> named =
                subjectResolutionService.locatedCards(gameData, entry, put, put.cards());
        returnFromGraveyards(gameData, named);
        for (SubjectResolutionService.LocatedCard located : named) {
            Card card = located.card();
            if (located.zone() == Zone.COMMAND) {
                List<Card> commandZone = gameData.playerCommandZones.get(located.ownerId());
                if (commandZone != null && commandZone.remove(card)) {
                    gameData.addCardToHand(located.ownerId(), card);
                    gameLogService.append(gameData, GameLog.textCardText(
                            gameData.playerIdToName.get(located.ownerId()) + " puts ", card,
                            " into their hand from the command zone."));
                }
                continue;
            }
            if (located.zone() == Zone.LIBRARY) {
                List<Card> library = gameData.playerDecks.get(located.ownerId());
                if (library != null && library.remove(card)) {
                    gameData.addCardToHand(located.ownerId(), card);
                    String ownerName = gameData.playerIdToName.get(located.ownerId());
                    // A card a search found without revealing it goes from one hidden zone to another unseen (CR 400.2).
                    gameLogService.append(gameData, foundUnrevealed(entry, card)
                            ? GameLog.text(ownerName + " puts a card into their hand.")
                            : GameLog.textCardText(ownerName + " puts ", card, " into their hand."));
                }
                continue;
            }
            if (located.zone() != Zone.EXILE) {
                continue;
            }
            ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
            boolean faceDown = exiled != null && exiled.faceDown();
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }
            UUID ownerId = located.ownerId() != null ? located.ownerId() : entry.getControllerId();
            gameData.addCardToHand(ownerId, card);
            String ownerName = gameData.playerIdToName.get(ownerId);
            // A card exiled face down goes to a hidden zone without being seen (CR 406.3).
            gameLogService.append(gameData, faceDown
                    ? GameLog.text(ownerName + " puts a face-down card from exile into their hand.")
                    : GameLog.textCardText(ownerName + " puts ", card, " into their hand."));
            log.info("Game {} - {} puts {} from exile into their hand", gameData.id, ownerName, card.getName());
        }
    }

    /** Whether the resolution's library search found {@code card} and no reveal step of it showed it. */
    private static boolean foundUnrevealed(StackEntry entry, Card card) {
        return entry.getResolutionResults().outcome(StepKind.SEARCH).objectIds().contains(card.getId())
                && !entry.getResolutionResults().outcome(StepKind.REVEAL).objectIds().contains(card.getId());
    }

    /** The named graveyard cards leave as one event, each for its owner's hand (CR 400.3). */
    private void returnFromGraveyards(GameData gameData, List<SubjectResolutionService.LocatedCard> named) {
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (SubjectResolutionService.LocatedCard located : named) {
                if (located.zone() != Zone.GRAVEYARD) {
                    continue;
                }
                Card card = located.card();
                UUID ownerId = located.ownerId();
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                permanentRemovalService.addCardToHandFromGraveyard(gameData, ownerId, ownerId, card);
                gameLogService.append(gameData, GameLog.textCardText(gameData.playerIdToName.get(ownerId) + " puts ",
                        card, " from their graveyard into their hand."));
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
    }
}
