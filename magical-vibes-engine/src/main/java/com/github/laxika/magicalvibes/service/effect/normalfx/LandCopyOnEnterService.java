package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CopyLandFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandCopyOnEnterService {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentCopierService permanentCopierService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final TurnProgressionService turnProgressionService;

    public boolean prepare(GameData gameData, UUID controllerId, Card physicalCard, Card enteringCard,
                           boolean landPlay, boolean initiallyTapped, String logSuffix) {
        return prepare(gameData, controllerId, physicalCard, enteringCard, landPlay, initiallyTapped,
                logSuffix, landPlay ? Zone.HAND : null);
    }

    public boolean prepare(GameData gameData, UUID controllerId, Card physicalCard, Card enteringCard,
                           boolean landPlay, boolean initiallyTapped, String logSuffix,
                           Zone landPlayZone) {
        if (findEffect(enteringCard) == null) {
            return false;
        }

        if (collectLandCards(gameData).isEmpty()) {
            return false;
        }

        gameData.landCopyOperation.physicalCard = physicalCard;
        gameData.landCopyOperation.enteringCard = enteringCard;
        gameData.landCopyOperation.controllerId = controllerId;
        gameData.landCopyOperation.landPlay = landPlay;
        gameData.landCopyOperation.landPlayZone = landPlayZone;
        gameData.landCopyOperation.initiallyTapped = initiallyTapped;
        gameData.landCopyOperation.logSuffix = logSuffix;

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                physicalCard,
                controllerId,
                List.of(findEffect(enteringCard)),
                physicalCard.getName() + " — You may have it enter tapped as a copy of any land card in a graveyard."
        ));
        playerInputService.processNextMayAbility(gameData);
        return true;
    }

    public void beginGraveyardChoice(GameData gameData, UUID controllerId) {
        List<Card> landCards = collectLandCards(gameData);
        if (landCards.isEmpty()) {
            return;
        }
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, IntStream.range(0, landCards.size()).boxed().toList(),
                        GraveyardChoiceDestination.COPY_ON_ENTER,
                        "Choose a land card in a graveyard to copy.")
                .cardPool(landCards)
                .mandatory(true)
                .build());
    }

    public void complete(GameData gameData, Card chosenLand) {
        Card physicalCard = gameData.landCopyOperation.physicalCard;
        Card enteringCard = gameData.landCopyOperation.enteringCard;
        UUID controllerId = gameData.landCopyOperation.controllerId;
        boolean landPlay = gameData.landCopyOperation.landPlay;
        Zone landPlayZone = gameData.landCopyOperation.landPlayZone;
        boolean initiallyTapped = gameData.landCopyOperation.initiallyTapped;
        String logSuffix = gameData.landCopyOperation.logSuffix;

        clearOperation(gameData);

        Permanent permanent = new Permanent(physicalCard);
        permanent.setCard(enteringCard);

        boolean copied = chosenLand != null
                && chosenLand.hasType(CardType.LAND)
                && gameQueryService.findGraveyardOwnerById(gameData, chosenLand.getId()) != null;
        if (copied) {
            permanentCopierService.applyCloneCopy(permanent, chosenLand, null, null, java.util.Set.of());
            List<CardSubtype> subtypes = new ArrayList<>(permanent.getCard().getSubtypes());
            if (!subtypes.contains(CardSubtype.CAVE)) {
                subtypes.add(CardSubtype.CAVE);
                permanent.getCard().setSubtypes(subtypes);
            }
            permanent.tap();
        } else if (initiallyTapped) {
            permanent.tap();
        }

        if (landPlay) {
            battlefieldEntryService.putLandOntoBattlefield(
                    gameData, controllerId, permanent, landPlayZone);
        } else {
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        }
        if (landPlay) {
            gameData.landsPlayedThisTurn.merge(controllerId, 1, Integer::sum);
        }

        String playerName = gameData.playerIdToName.getOrDefault(controllerId, "Player");
        String suffix = logSuffix == null ? "." : logSuffix;
        gameLogService.append(gameData, GameLog.playerPlays(playerName, permanent.getCard(), suffix));
        log.info("Game {} - {} plays {}{}", gameData.id, playerName,
                permanent.getCard().getName(), suffix);

        if (!gameData.interaction.isAwaitingInput()) {
            battlefieldEntryService.processLandETBEffects(gameData, controllerId, permanent.getCard());
            if (!gameData.interaction.isAwaitingInput() && landPlay) {
                triggerCollectionService.checkControllerPlaysLandTriggers(gameData, controllerId, permanent.getCard());
                if (!gameData.interaction.isAwaitingInput()) {
                    turnProgressionService.resolveAutoPass(gameData);
                }
            }
        }
    }

    private CopyLandFromGraveyardOnEnterEffect findEffect(Card card) {
        for (var effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof CopyLandFromGraveyardOnEnterEffect copyEffect) {
                return copyEffect;
            }
        }
        return null;
    }

    private List<Card> collectLandCards(GameData gameData) {
        List<Card> landCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Card card : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                if (card.hasType(CardType.LAND)) {
                    landCards.add(card);
                }
            }
        }
        return landCards;
    }

    private void clearOperation(GameData gameData) {
        gameData.landCopyOperation.physicalCard = null;
        gameData.landCopyOperation.enteringCard = null;
        gameData.landCopyOperation.controllerId = null;
        gameData.landCopyOperation.landPlay = false;
        gameData.landCopyOperation.landPlayZone = null;
        gameData.landCopyOperation.initiallyTapped = false;
        gameData.landCopyOperation.logSuffix = null;
    }
}
