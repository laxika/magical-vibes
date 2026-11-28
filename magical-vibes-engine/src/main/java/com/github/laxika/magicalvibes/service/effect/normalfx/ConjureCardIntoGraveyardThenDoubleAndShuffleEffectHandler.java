package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoGraveyardThenDoubleAndShuffleEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Mephidross Slime's graveyard conjure, perpetual doubling, and library shuffle. */
@Component
public class ConjureCardIntoGraveyardThenDoubleAndShuffleEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;
    private final SubjectResolutionService subjectResolutionService;
    private final CardCatalog cardCatalog;

    public ConjureCardIntoGraveyardThenDoubleAndShuffleEffectHandler(
            GameQueryService gameQueryService,
            GameLogService gameLogService,
            GraveyardService graveyardService,
            PermanentRemovalService permanentRemovalService,
            SubjectResolutionService subjectResolutionService,
            CardCatalog cardCatalog) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.graveyardService = graveyardService;
        this.permanentRemovalService = permanentRemovalService;
        this.subjectResolutionService = subjectResolutionService;
        this.cardCatalog = cardCatalog;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardIntoGraveyardThenDoubleAndShuffleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureCardIntoGraveyardThenDoubleAndShuffleEffect) effect;
        // "Mephidross Slime": the card the dying Slime became in the graveyard, while it is still that object
        // (CR 400.7, 400.7e); one exiled from the graveyard in response is neither doubled nor shuffled.
        var dying = subjectResolutionService.sourceObjectCard(gameData, entry, effect);
        Card dyingCard = dying != null && dying.zone() == Zone.GRAVEYARD ? dying.card() : null;

        UUID controllerId = entry.getControllerId();
        Card duplicate = cardCatalog.createCard(conjure.printing());
        duplicate.setOwnerId(controllerId);
        duplicate.freeze();
        boolean duplicateEnteredGraveyard = graveyardService.addCardToGraveyard(
                gameData, controllerId, duplicate);
        ActionEvents.conjured(gameData, controllerId, 1);

        // Each card's power and toughness as it is now, doubled (a Clone that copied the Slime is its own 0/0 card).
        if (dyingCard != null) {
            rememberDouble(gameData, dyingCard);
        }
        rememberDouble(gameData, duplicate);

        Map<UUID, Integer> movedByOwner = new HashMap<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            if (dyingCard != null) {
                moveToOwnersLibrary(gameData, dyingCard.getId(), movedByOwner);
            }
            if (duplicateEnteredGraveyard) {
                moveToOwnersLibrary(gameData, duplicate.getId(), movedByOwner);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        movedByOwner.forEach((ownerId, count) -> {
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        });
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a card named " + duplicate.getName()
                        + " into their graveyard, perpetually doubles both cards' power and toughness,"
                        + " then shuffles them into their owners' libraries."));
    }

    /** Doubles {@code card}'s power and toughness as they are now: it perpetually gets +X/+Y for them (CR 701.10b). */
    private void rememberDouble(GameData gameData, Card card) {
        Integer power = gameQueryService.getEffectiveCardPower(gameData, card);
        Integer toughness = gameQueryService.getEffectiveCardToughness(gameData, card);
        PerpetualCardPowerToughnessSupport.remember(gameData, card,
                power == null ? 0 : power, toughness == null ? 0 : toughness);
    }

    private void moveToOwnersLibrary(GameData gameData, UUID cardId, Map<UUID, Integer> movedByOwner) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        List<Card> library = ownerId == null ? null : gameData.playerDecks.get(ownerId);
        if (ownerId == null || library == null) {
            return;
        }

        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        library.add(card);
        movedByOwner.merge(ownerId, 1, Integer::sum);
    }
}
