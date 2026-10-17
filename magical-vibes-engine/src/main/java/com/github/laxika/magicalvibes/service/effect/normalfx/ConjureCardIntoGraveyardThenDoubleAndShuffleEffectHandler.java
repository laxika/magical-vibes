package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoGraveyardThenDoubleAndShuffleEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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
    private final TriggerCollectionService triggerCollectionService;

    public ConjureCardIntoGraveyardThenDoubleAndShuffleEffectHandler(
            GameQueryService gameQueryService,
            GameLogService gameLogService,
            GraveyardService graveyardService,
            PermanentRemovalService permanentRemovalService,
            TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.graveyardService = graveyardService;
        this.permanentRemovalService = permanentRemovalService;
        this.triggerCollectionService = triggerCollectionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardIntoGraveyardThenDoubleAndShuffleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureCardIntoGraveyardThenDoubleAndShuffleEffect) effect;
        if (conjure.dyingCardId() == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Card duplicate = conjure.cardFactory().get();
        duplicate.setOwnerId(controllerId);
        duplicate.freeze();
        boolean duplicateEnteredGraveyard = graveyardService.addCardToGraveyard(
                gameData, controllerId, duplicate);
        triggerCollectionService.checkConjureTriggers(gameData, controllerId, 1);

        rememberDouble(gameData, conjure.dyingCardId(), duplicate);
        rememberDouble(gameData, duplicate.getId(), duplicate);

        Map<UUID, Integer> movedByOwner = new HashMap<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            moveToOwnersLibrary(gameData, conjure.dyingCardId(), movedByOwner);
            if (duplicateEnteredGraveyard) {
                moveToOwnersLibrary(gameData, duplicate.getId(), movedByOwner);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        movedByOwner.forEach((ownerId, count) -> {
            triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, ownerId, count);
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        });
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a card named " + duplicate.getName()
                        + " into their graveyard, perpetually doubles both cards' power and toughness,"
                        + " then shuffles them into their owners' libraries."));
    }

    private void rememberDouble(GameData gameData, UUID cardId, Card printedSource) {
        CardPowerToughnessModifier current = gameData.perpetualCardPowerToughnessModifiers.get(cardId);
        int power = effectiveValue(printedSource.getPower(), current, true);
        int toughness = effectiveValue(printedSource.getToughness(), current, false);
        gameData.perpetualCardPowerToughnessModifiers.merge(
                cardId,
                new CardPowerToughnessModifier(power, toughness),
                (existing, added) -> existing.add(added.power(), added.toughness()));
    }

    private int effectiveValue(Integer printedValue, CardPowerToughnessModifier modifier, boolean power) {
        int value = printedValue == null ? 0 : printedValue;
        if (modifier != null) {
            value += power ? modifier.power() : modifier.toughness();
        }
        return value;
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
