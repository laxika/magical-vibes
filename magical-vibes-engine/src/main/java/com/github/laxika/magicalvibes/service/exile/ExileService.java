package com.github.laxika.magicalvibes.service.exile;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExileService {

    private final GameQueryService gameQueryService;

    /**
     * Exiles a card, adding it to the specified player's exile zone.
     */
    public void exileCard(GameData gameData, UUID ownerId, Card card) {
        gameData.addToExile(ownerId, card);
    }

    /**
     * Exiles a card and tracks it with a source permanent (imprint).
     */
    public void exileCard(GameData gameData, UUID ownerId, Card card, UUID sourcePermanentId) {
        gameData.addToExile(ownerId, card, sourcePermanentId);
    }

    /**
     * Exiles a card face down (CR 406.3), optionally tracked with a source permanent.
     */
    public void exileCardFaceDown(GameData gameData, UUID ownerId, Card card, UUID sourcePermanentId) {
        gameData.addToExile(ownerId, card, sourcePermanentId, true);
    }

    /** Exiles a card face down while remembering which player exiled it. */
    public void exileCardFaceDown(GameData gameData, UUID ownerId, Card card, UUID sourcePermanentId,
                                  UUID exilerId) {
        gameData.addToExile(ownerId, card, sourcePermanentId, true, exilerId);
    }

    /**
     * Records an exiled card as the imprint of the permanent that exiled it. No-op if the
     * source permanent has already left the battlefield.
     */
    public void setImprintedCardOnPermanent(GameData gameData, UUID sourcePermanentId, Card card) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            gameData.setImprintedCard(source.getCard(), card);
        }
    }
}
