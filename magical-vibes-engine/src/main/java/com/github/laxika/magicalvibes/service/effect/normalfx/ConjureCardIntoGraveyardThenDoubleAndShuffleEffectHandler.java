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
        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, conjure.dyingCardId());
        if (dyingCard == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Card duplicate = conjure.cardFactory().get();
        duplicate.setOwnerId(controllerId);
        duplicate.freeze();
        boolean duplicateEnteredGraveyard = graveyardService.addCardToGraveyard(
                gameData, controllerId, duplicate);

        rememberDouble(gameData, dyingCard);
        rememberDouble(gameData, duplicate);

        Map<UUID, Integer> movedByOwner = new HashMap<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            moveToOwnersLibrary(gameData, dyingCard, movedByOwner);
            if (duplicateEnteredGraveyard) {
                moveToOwnersLibrary(gameData, duplicate, movedByOwner);
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

    private void rememberDouble(GameData gameData, Card card) {
        int power = effectiveValue(card.getPower(), gameData.perpetualCardPowerToughnessModifiers
                .get(card.getId()), true);
        int toughness = effectiveValue(card.getToughness(), gameData.perpetualCardPowerToughnessModifiers
                .get(card.getId()), false);
        PerpetualCardPowerToughnessSupport.remember(gameData, card, power, toughness);
    }

    private int effectiveValue(Integer printedValue, CardPowerToughnessModifier modifier, boolean power) {
        int value = printedValue == null ? 0 : printedValue;
        if (modifier != null) {
            value += power ? modifier.power() : modifier.toughness();
        }
        return value;
    }

    private void moveToOwnersLibrary(GameData gameData, Card card, Map<UUID, Integer> movedByOwner) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        List<Card> library = ownerId == null ? null : gameData.playerDecks.get(ownerId);
        if (ownerId == null || library == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        library.add(card);
        movedByOwner.merge(ownerId, 1, Integer::sum);
    }
}
