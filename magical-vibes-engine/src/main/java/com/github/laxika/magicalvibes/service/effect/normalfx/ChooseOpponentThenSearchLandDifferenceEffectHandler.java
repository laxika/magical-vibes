package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentThenSearchLandDifferenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Boreas Charger's resolution-time opponent choice and land search. */
@Component
@RequiredArgsConstructor
public class ChooseOpponentThenSearchLandDifferenceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOpponentThenSearchLandDifferenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var searchEffect = (ChooseOpponentThenSearchLandDifferenceEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> eligibleOpponents = eligibleOpponents(gameData, controllerId);
        if (eligibleOpponents.isEmpty()) {
            return;
        }

        if (eligibleOpponents.size() == 1) {
            beginSearch(gameData, controllerId, eligibleOpponents.getFirst(), searchEffect.subtype(),
                    entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChooseOpponentThenSearchLandDifference(
                        controllerId, searchEffect.subtype(), entry.getCard().getName()));
        playerInputService.beginPlayerChoice(gameData, controllerId, eligibleOpponents,
                entry.getCard().getName() + " — choose an opponent who controls more lands than you.");
    }

    public void completeChoice(GameData gameData, UUID chosenOpponentId,
                               PermanentChoiceContext.ChooseOpponentThenSearchLandDifference context) {
        if (gameData.pendingEffectResolutionEntry == null
                || !eligibleOpponents(gameData, context.controllerId()).contains(chosenOpponentId)) {
            throw new IllegalStateException("Invalid opponent choice");
        }

        beginSearch(gameData, context.controllerId(), chosenOpponentId, context.subtype(),
                context.sourceCardName());
    }

    private void beginSearch(GameData gameData, UUID controllerId, UUID opponentId,
                             CardSubtype subtype, String sourceCardName) {
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            return;
        }

        int difference = countLands(gameData, opponentId) - countLands(gameData, controllerId);
        if (difference <= 0) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> card.getSubtypes().contains(subtype))
                .filter(card -> !gameQueryService.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.LIBRARY))
                .toList();
        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but finds no " + subtype.getDisplayName()
                            + " cards. Library is shuffled."));
            return;
        }

        LibrarySearchFollowUp followUp = difference > 1
                ? LibrarySearchFollowUp.forLandSubtypeToHand(difference - 1, subtype)
                : LibrarySearchFollowUp.NONE;
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .reveals(true)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                        .shuffleAfterSelection(difference == 1)
                        .followUp(followUp)
                        .filterPredicate(new CardSubtypePredicate(subtype))
                        .build(),
                sourceCardName + " — search your library for a " + subtype.getDisplayName()
                        + " card to put onto the battlefield tapped.",
                true);
    }

    private List<UUID> eligibleOpponents(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .filter(playerId -> gameQueryService.controlsMoreLandsThan(gameData, playerId, controllerId))
                .toList();
    }

    private int countLands(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        return (int) battlefield.stream()
                .filter(permanent -> gameQueryService.isLand(gameData, permanent))
                .count();
    }
}
