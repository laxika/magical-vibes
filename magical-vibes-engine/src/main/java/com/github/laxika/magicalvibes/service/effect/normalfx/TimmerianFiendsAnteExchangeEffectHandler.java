package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TimmerianFiendsAnteExchangeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TimmerianFiendsAnteExchangeEffect} (Timmerian Fiends): "The owner of target
 * artifact may ante the top card of their library. If that player doesn't, exchange ownership of
 * that artifact and Timmerian Fiends."
 *
 * <p>The targeted artifact's owner is the decision maker and is prompted via the may-ability system
 * (the accept/decline branch lives in {@code TimmerianFiendsAnteExchangeHandler}). An owner with an
 * empty library has no top card to ante, so they can't ante and the exchange happens immediately
 * without a prompt.
 *
 * <p>The ante "exchange ownership … permanent" is resolved as the single-game observable zone
 * movements — see {@link TimmerianFiendsAnteExchangeEffect} — never a runtime {@code ownerId}
 * change.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimmerianFiendsAnteExchangeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TimmerianFiendsAnteExchangeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent artifact = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (artifact == null) {
            // Target left before resolution — the ability does nothing.
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID ownerId = artifactOwnerId(gameData, artifact);

        List<Card> library = gameData.playerDecks.get(ownerId);
        if (library == null || library.isEmpty()) {
            // No top card to ante — the owner can't ante, so they don't: exchange right away.
            performExchange(gameData, entry.getCard(), controllerId, ownerId, artifact.getId());
            return;
        }

        // Ask the artifact's owner. Carry the ability's controller — who ends up with the artifact
        // card — in the targetCardId slot, and the artifact itself in the sourcePermanentId slot,
        // for the accept/decline branch.
        String prompt = "Ante the top card of your library? If you don't, you lose "
                + artifact.getCard().getName() + " and gain " + entry.getCard().getName() + ". ("
                + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), ownerId, List.of(effect), prompt, controllerId, null, artifact.getId()));
    }

    /**
     * Performs the ante exchange: the targeted artifact is put into the ability controller's
     * graveyard and the Timmerian Fiends card (in the controller's graveyard after being sacrificed
     * as a cost) into the artifact owner's graveyard. Does nothing if the artifact has left the
     * battlefield in the meantime.
     */
    public void performExchange(GameData gameData, Card fiendsCard, UUID controllerId, UUID ownerId, UUID artifactId) {
        Permanent artifact = gameQueryService.findPermanentById(gameData, artifactId);
        if (artifact == null) {
            return;
        }
        Card artifactCard = artifact.getOriginalCard() != null ? artifact.getOriginalCard() : artifact.getCard();

        permanentRemovalService.removePermanentToGraveyard(gameData, artifact);
        permanentRemovalService.removeOrphanedAuras(gameData);

        // The artifact went to its owner's graveyard; the ownership swap makes "your graveyard" the
        // ability controller's, so relocate the physical card. Ownership itself is never mutated.
        moveGraveyardCard(gameData, artifactCard, controllerId);
        moveGraveyardCard(gameData, fiendsCard, ownerId);

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(ownerId) + " doesn't ante — ")
                .card(artifactCard)
                .text(" goes to " + gameData.playerIdToName.get(controllerId) + "'s graveyard and ")
                .card(fiendsCard)
                .text(" to " + gameData.playerIdToName.get(ownerId) + "'s graveyard.")
                .build());
        log.info("Game {} - {} exchange: {} to {}, {} to {}", gameData.id, fiendsCard.getName(),
                artifactCard.getName(), controllerId, fiendsCard.getName(), ownerId);
    }

    /**
     * Moves {@code card} out of whichever graveyard currently holds it into {@code targetPlayerId}'s
     * graveyard. The card stays in the graveyard zone throughout, so no zone-change triggers fire —
     * only the holder changes. No-op if the card is in no graveyard.
     */
    private void moveGraveyardCard(GameData gameData, Card card, UUID targetPlayerId) {
        for (List<Card> graveyard : gameData.playerGraveyards.values()) {
            if (graveyard.removeIf(c -> c.getId().equals(card.getId()))) {
                gameData.playerGraveyards.get(targetPlayerId).add(card);
                return;
            }
        }
    }

    /** The artifact's owner — the card's stamped owner, falling back to its controller. */
    private UUID artifactOwnerId(GameData gameData, Permanent artifact) {
        Card card = artifact.getOriginalCard() != null ? artifact.getOriginalCard() : artifact.getCard();
        if (card.getOwnerId() != null) {
            return card.getOwnerId();
        }
        return gameQueryService.findPermanentController(gameData, artifact.getId());
    }
}
