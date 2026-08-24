package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Returns a card from a graveyard to the battlefield transformed (back face up).
 *
 * <p>Shared by the immediate "return it to the battlefield transformed" effects and the
 * delayed end-step returns queued by {@code RegisterDelayedReturnSourceTransformedEffect},
 * so both honour the same graveyard lookup, back-face requirement and
 * can't-enter-from-graveyard replacement checks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraveyardTransformedReturnService {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    /**
     * Moves {@code cardId} from {@code ownerId}'s graveyard onto the battlefield transformed
     * under {@code controllerId}'s control.
     *
     * @return true when the card actually entered the battlefield
     */
    public boolean returnTransformed(GameData gameData, UUID cardId, UUID ownerId, UUID controllerId) {
        return returnTransformed(gameData, cardId, ownerId, controllerId, null);
    }

    /**
     * Moves {@code cardId} from the graveyard onto the battlefield transformed and, when supplied,
     * attaches the resulting permanent to the target identified by {@code attachmentTargetId}.
     */
    public boolean returnTransformed(GameData gameData, UUID cardId, UUID ownerId, UUID controllerId,
                                     UUID attachmentTargetId) {
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null) {
            return false;
        }
        Card cardToReturn = null;
        for (Card card : graveyard) {
            if (card.getId().equals(cardId)) {
                cardToReturn = card;
                break;
            }
        }
        if (cardToReturn == null) {
            log.info("Game {} - Transformed return for card {} skipped (no longer in graveyard)", gameData.id, cardId);
            return false;
        }
        Card backFace = cardToReturn.getBackFaceCard();
        if (backFace == null) {
            log.warn("Game {} - Transformed return skipped for {} (no back face)", gameData.id, cardToReturn.getName());
            return false;
        }
        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, Zone.GRAVEYARD)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(cardToReturn, " can't return from the graveyard; it stays in the graveyard."));
            return false;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
        Permanent permanent = new Permanent(cardToReturn);
        permanent.setCard(backFace);
        permanent.setTransformed(true);
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        if (attachmentTargetId != null) {
            permanent.setAttachedTo(attachmentTargetId);
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.cardTextCard(cardToReturn,
                " returns to the battlefield transformed as ", backFace,
                " under " + playerName + "'s control."));
        log.info("Game {} - {} returns transformed as {} for {}",
                gameData.id, cardToReturn.getName(), backFace.getName(), playerName);
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, backFace, null, false);
        return true;
    }
}
