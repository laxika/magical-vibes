package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureCause;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TimmerianFiendsAnteExchangeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
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
public class TimmerianFiendsAnteExchangeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;
    private final DepartureService departureService;

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
     *
     * <p>The artifact (and the Fiends, should it be on the battlefield) leaves as one departure batch, so
     * its CR 616.1 questions are asked before it leaves: the caller is stopped and run again with the answers
     * (this handler resolving as a marked stack step, or the declined ante prompt's replay). Nothing changes
     * before that departure. A replacement that sends a card elsewhere (an artifact creature exiled by
     * Kalitas) leaves it there.
     */
    public void performExchange(GameData gameData, Card fiendsCard, UUID controllerId, UUID ownerId, UUID artifactId) {
        Permanent artifact = gameQueryService.findPermanentById(gameData, artifactId);
        if (artifact == null) {
            return;
        }
        Card artifactCard = artifact.getOriginalCard() != null ? artifact.getOriginalCard() : artifact.getCard();

        List<DepartureIntent> intents = new ArrayList<>();
        intents.add(new DepartureIntent(artifact.getId(), DepartureCause.OTHER, controllerId));
        Permanent fiendsPermanent = findOnBattlefield(gameData, fiendsCard);
        if (fiendsPermanent != null && !fiendsPermanent.getId().equals(artifact.getId())) {
            intents.add(new DepartureIntent(fiendsPermanent.getId(), DepartureCause.OTHER, ownerId));
        }
        departureService.departNow(gameData, DepartureBatch.of(intents), DepartureService.Hooks.NONE);
        permanentRemovalService.removeOrphanedAuras(gameData);

        // The swap is permanent, but frozen cards keep their stamped owner, so the new owners are
        // recorded as overrides.
        if (fiendsPermanent == null) {
            moveCardToGraveyard(gameData, fiendsCard, ownerId);
        }
        gameData.cardOwnerOverrides.put(artifactCard.getId(), controllerId);
        gameData.cardOwnerOverrides.put(fiendsCard.getId(), ownerId);

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
     * Moves {@code card}, which is not on the battlefield, from wherever it currently is into
     * {@code targetPlayerId}'s graveyard. Moving between graveyards only changes the holder; moving
     * from another zone uses the shared graveyard path so replacement effects and zone-change
     * triggers still apply.
     */
    private void moveCardToGraveyard(GameData gameData, Card card, UUID targetPlayerId) {
        for (List<Card> graveyard : gameData.playerGraveyards.values()) {
            if (graveyard.removeIf(c -> c.getId().equals(card.getId()))) {
                gameData.playerGraveyards.get(targetPlayerId).add(card);
                return;
            }
        }

        var exiled = gameData.findExiledCard(card.getId());
        if (exiled != null && gameData.removeFromExile(card.getId())) {
            graveyardService.addCardToGraveyard(gameData, targetPlayerId, exiled.card(), Zone.EXILE);
            return;
        }

        for (List<Card> hand : gameData.playerHands.values()) {
            if (hand.removeIf(c -> c.getId().equals(card.getId()))) {
                graveyardService.addCardToGraveyard(gameData, targetPlayerId, card, Zone.HAND);
                return;
            }
        }

        for (List<Card> library : gameData.playerDecks.values()) {
            if (library.removeIf(c -> c.getId().equals(card.getId()))) {
                graveyardService.addCardToGraveyard(gameData, targetPlayerId, card, Zone.LIBRARY);
                return;
            }
        }
    }

    /** The permanent {@code card} is on the battlefield as, or {@code null}. */
    private static Permanent findOnBattlefield(GameData gameData, Card card) {
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(card.getId())
                        || (permanent.getOriginalCard() != null
                        && permanent.getOriginalCard().getId().equals(card.getId())))
                .findFirst()
                .orElse(null);
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
