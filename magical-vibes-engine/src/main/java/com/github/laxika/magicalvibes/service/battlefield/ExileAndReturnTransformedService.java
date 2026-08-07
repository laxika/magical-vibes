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
    private final GameLogService gameLogService;

    /**
     * Exiles the given permanent and immediately returns it transformed. No-op when the permanent
     * has already left the battlefield or its card has no back face.
     *
     * @return {@code true} when the permanent was actually exiled and returned transformed — the
     *         "if you do" condition callers such as Liliana, Heretical Healer hang their rider on
     */
    public boolean exileAndReturnTransformed(GameData gameData, UUID permanentId) {
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
        Card backFace = originalCard.getBackFaceCard();
        if (backFace == null) return false;

        permanentRemovalService.removePermanentToExile(gameData, perm);
        // Removed from exile immediately — it returns right away as the back face.
        gameData.removeFromExile(originalCard.getId());

        Permanent newPerm = new Permanent(originalCard);
        newPerm.setCard(backFace);
        newPerm.setTransformed(true);
        newPerm.setSummoningSick(false);
        // A back face can be a planeswalker (Kytheon, Hero of Akros; Jace, Vryn's Prodigy): it
        // enters with its starting loyalty, otherwise the state-based check kills it immediately.
        if (backFace.hasType(CardType.PLANESWALKER) && backFace.getLoyalty() != null) {
            newPerm.setCounterCount(CounterType.LOYALTY, backFace.getLoyalty());
        }

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, newPerm);

        gameLogService.append(gameData, GameLog.cardTextCard(originalCard,
                " is exiled and returns transformed as ", backFace, "."));
        log.info("Game {} - {} exiled and returned transformed as {}",
                gameData.id, originalCard.getName(), backFace.getName());
        return true;
    }
}
