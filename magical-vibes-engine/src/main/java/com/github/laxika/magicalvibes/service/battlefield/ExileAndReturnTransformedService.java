package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * "Exile this permanent, then return it to the battlefield transformed under its owner's control."
 * The returned permanent is a brand-new object built from the original card and flipped to its back
 * face — Kytheon, Hero of Akros (at end of combat) and Jace, Vryn's Prodigy (immediately, on
 * resolution) both land here, so the exile-and-return step lives in one place rather than being
 * re-derived per timing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExileAndReturnTransformedService {

    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    /**
     * Exiles the given permanent and immediately returns it transformed. No-op when the permanent
     * has already left the battlefield or its card has no back face.
     *
     * @return {@code true} when the permanent was actually exiled and returned transformed — the
     *         "if you do" condition callers such as Liliana, Heretical Healer hang their rider on
     */
    public boolean exileAndReturnTransformed(GameData gameData, UUID permanentId) {
        return exileAndReturn(gameData, permanentId, true);
    }

    public boolean exileAndReturnFront(GameData gameData, UUID permanentId) {
        return exileAndReturn(gameData, permanentId, false);
    }

    private boolean exileAndReturn(GameData gameData, UUID permanentId, boolean transformed) {
        Permanent perm = null;
        UUID controllerId = null;
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            for (Permanent p : entry.getValue()) {
                if (p.getId().equals(permanentId)) {
                    perm = p;
                    controllerId = entry.getKey();
                    break;
                }
            }
            if (perm != null) break;
        }
        if (perm == null) return false;

        Card originalCard = perm.getOriginalCard();
        Card returnedCard = transformed ? originalCard.getBackFaceCard() : originalCard;
        if (returnedCard == null) return false;
        UUID returnControllerId = originalCard.getOwnerId() != null
                ? originalCard.getOwnerId() : controllerId;

        permanentRemovalService.removePermanentToExile(gameData, perm);
        // Removed from exile immediately — it returns right away as the back face.
        gameData.removeFromExile(originalCard.getId());

        Permanent newPerm = new Permanent(originalCard);
        newPerm.setCard(returnedCard);
        newPerm.setTransformed(transformed);
        newPerm.setSummoningSick(false);
        newPerm.setEnteredFromExile(true);
        // A back face can be a planeswalker (Kytheon, Hero of Akros; Jace, Vryn's Prodigy): it
        // enters with its starting loyalty, otherwise the state-based check kills it immediately.
        if (returnedCard.hasType(CardType.PLANESWALKER) && returnedCard.getLoyalty() != null) {
            int loyalty = gameQueryService.replaceCounters(gameData, newPerm, returnControllerId,
                    CounterType.LOYALTY, returnedCard.getLoyalty(), returnControllerId);
            newPerm.setCounterCount(CounterType.LOYALTY, loyalty);
        }

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, returnControllerId, newPerm);

        if (transformed) {
            gameLogService.append(gameData, GameLog.cardTextCard(originalCard,
                    " is exiled and returns transformed as ", returnedCard, "."));
            log.info("Game {} - {} exiled and returned transformed as {}",
                    gameData.id, originalCard.getName(), returnedCard.getName());
        } else {
            gameLogService.append(gameData, GameLog.cardTextCard(originalCard,
                    " is exiled and returns as ", returnedCard, "."));
            log.info("Game {} - {} exiled and returned as {}",
                    gameData.id, originalCard.getName(), returnedCard.getName());
        }
        return true;
    }
}
