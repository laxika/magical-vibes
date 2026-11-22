package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PutCardsIntoHandEffect}: each card its subject names in exile goes to its owner's hand (CR 400.3).
 * It never prompts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCardsIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardsIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var put = (PutCardsIntoHandEffect) effect;
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, put, put.cards())) {
            Card card = located.card();
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
}
