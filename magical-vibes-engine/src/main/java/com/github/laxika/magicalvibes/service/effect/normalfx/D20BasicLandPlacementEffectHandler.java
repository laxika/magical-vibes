package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.D20BasicLandPlacementEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class D20BasicLandPlacementEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return D20BasicLandPlacementEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        D20BasicLandPlacementEffect placement = (D20BasicLandPlacementEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> cards = placement.cards();
        if (cards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        switch (placement.placement()) {
            case TO_HAND -> putIntoHand(gameData, controllerId, cards);
            case ALL_TO_BATTLEFIELD_TAPPED -> putAllOntoBattlefield(gameData, controllerId, cards);
            case ONE_TO_BATTLEFIELD_TAPPED -> putOneOntoBattlefieldAndPutRestIntoHand(
                    gameData, controllerId, cards);
        }
    }

    private void putIntoHand(GameData gameData, UUID controllerId, List<Card> cards) {
        for (Card card : cards) {
            gameData.addCardToHand(controllerId, card);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }

    private void putAllOntoBattlefield(GameData gameData, UUID controllerId, List<Card> cards) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> batch = new ArrayList<>();
        String playerName = gameData.playerIdToName.get(controllerId);
        for (Card card : cards) {
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)) {
                gameData.playerDecks.get(controllerId).add(card);
                continue;
            }
            Permanent permanent = new Permanent(card, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, batch);
            permanent.tap();
            batch.add(permanent);
            gameLogService.append(gameData,
                    GameLog.entersBattlefieldTappedUnder(card, playerName));
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }

    private void putOneOntoBattlefieldAndPutRestIntoHand(GameData gameData, UUID controllerId,
                                                          List<Card> cards) {
        String prompt = "Choose one of those cards to put onto the battlefield tapped; "
                + "the other goes into your hand.";
        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(cards))
                .reveals(true)
                .canFailToFind(false)
                .destination(LibrarySearchDestination.BATTLEFIELD_ONE_AND_PUT_REST_INTO_HAND)
                .sourceCards(new ArrayList<>(cards))
                .battlefieldIfChosenTapped(true)
                .shuffleAfterSelection(true)
                .prompt(prompt)
                .build();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, false));
    }
}
