package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferSearchLibraryForLandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Tempt with Discovery's controller and opponent land searches. */
@Component
@RequiredArgsConstructor
public class TemptingOfferSearchLibraryForLandToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private static final CardTypePredicate LAND = new CardTypePredicate(CardType.LAND);

    private final LibrarySearchSupport librarySearchSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferSearchLibraryForLandToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TemptingOfferSearchLibraryForLandToBattlefieldEffect offer =
                (TemptingOfferSearchLibraryForLandToBattlefieldEffect) effect;
        UUID controllerId = offer.abilityControllerId() != null
                ? offer.abilityControllerId() : entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        if (offer.remainingOpponentIds() == null) {
            List<UUID> opponents = new ArrayList<>(
                    AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(
                            gameData, controllerId));
            boolean searchAllowed = !librarySearchSupport.isSearchPrevented(gameData, controllerId);
            TemptingOfferSearchLibraryForLandToBattlefieldEffect afterInitialSearch =
                    new TemptingOfferSearchLibraryForLandToBattlefieldEffect(
                    opponents, List.of(), controllerId, 0,
                    searchAllowed ? addSearchedPlayer(controllerId, List.of()) : List.of());
            if (!searchAllowed || !startSearch(gameData, controllerId, afterInitialSearch, afterInitialSearch)) {
                resolve(gameData, entry, afterInitialSearch);
            }
            return;
        }

        if (!offer.remainingOpponentIds().isEmpty()) {
            promptNextOpponent(gameData, entry.getCard(), offer);
            return;
        }

        if (offer.acceptedOpponentIds() != null && !offer.acceptedOpponentIds().isEmpty()) {
            startNextAcceptedOpponentSearch(gameData, entry, offer);
            return;
        }

        if (offer.controllerSearchesRemaining() > 0) {
            boolean searchAllowed = !librarySearchSupport.isSearchPrevented(gameData, controllerId);
            TemptingOfferSearchLibraryForLandToBattlefieldEffect afterSearch =
                    new TemptingOfferSearchLibraryForLandToBattlefieldEffect(
                    List.of(), List.of(), controllerId, offer.controllerSearchesRemaining() - 1,
                    searchAllowed
                            ? addSearchedPlayer(controllerId, offer.searchedPlayerIds())
                            : offer.searchedPlayerIds());
            if (!searchAllowed || !startSearch(gameData, controllerId, afterSearch, afterSearch)) {
                resolve(gameData, entry, afterSearch);
            }
            return;
        }

        finishSearches(gameData, offer.searchedPlayerIds());
    }

    public void completeOpponentChoice(GameData gameData, PendingMayAbility ability,
                                       TemptingOfferSearchLibraryForLandToBattlefieldEffect effect,
                                       boolean accepted) {
        List<UUID> acceptedOpponents = effect.acceptedOpponentIds() == null
                ? new ArrayList<>() : new ArrayList<>(effect.acceptedOpponentIds());
        if (accepted) {
            acceptedOpponents.add(ability.controllerId());
        }

        List<UUID> remainingOpponents = new ArrayList<>(effect.remainingOpponentIds());
        if (!remainingOpponents.isEmpty()) {
            promptNextOpponent(gameData, ability.sourceCard(),
                    new TemptingOfferSearchLibraryForLandToBattlefieldEffect(
                        remainingOpponents, acceptedOpponents, effect.abilityControllerId(), 0,
                        effect.searchedPlayerIds()));
            return;
        }

        insertContinuation(gameData, new TemptingOfferSearchLibraryForLandToBattlefieldEffect(
                List.of(), acceptedOpponents, effect.abilityControllerId(), 0,
                effect.searchedPlayerIds()));
    }

    private void promptNextOpponent(GameData gameData, Card sourceCard,
                                     TemptingOfferSearchLibraryForLandToBattlefieldEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.removeFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(new TemptingOfferSearchLibraryForLandToBattlefieldEffect(
                        remaining, effect.acceptedOpponentIds(), effect.abilityControllerId(), 0,
                        effect.searchedPlayerIds())),
                "You may search your library for a land card and put it onto the battlefield. "
                        + "If you do, " + sourceCard.getName()
                        + "'s controller searches their library for a land card and puts it onto the battlefield."));
    }

    private void startNextAcceptedOpponentSearch(GameData gameData, StackEntry entry,
                                                  TemptingOfferSearchLibraryForLandToBattlefieldEffect effect) {
        UUID opponentId = effect.acceptedOpponentIds().getFirst();
        List<UUID> remaining = new ArrayList<>(effect.acceptedOpponentIds());
        remaining.removeFirst();

        if (librarySearchSupport.isSearchPrevented(gameData, opponentId)) {
            resolve(gameData, entry, new TemptingOfferSearchLibraryForLandToBattlefieldEffect(
                    List.of(), remaining, effect.abilityControllerId(), effect.controllerSearchesRemaining(),
                    effect.searchedPlayerIds()));
            return;
        }

        TemptingOfferSearchLibraryForLandToBattlefieldEffect afterSearch =
                new TemptingOfferSearchLibraryForLandToBattlefieldEffect(
                List.of(), remaining, effect.abilityControllerId(), effect.controllerSearchesRemaining() + 1,
                addSearchedPlayer(opponentId, effect.searchedPlayerIds()));

        boolean started = librarySearchSupport.performLibrarySearch(
                gameData,
                opponentId,
                card -> card.hasType(CardType.LAND),
                "land cards",
                "Search your library for a land card and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD,
                searchFollowUp(afterSearch, afterSearch),
                null,
                null,
                false);
        if (!started || !gameData.interaction.isAwaitingInput()) {
            resolve(gameData, entry, afterSearch);
        }
    }

    private boolean startSearch(GameData gameData, UUID playerId,
                                TemptingOfferSearchLibraryForLandToBattlefieldEffect selected,
                                TemptingOfferSearchLibraryForLandToBattlefieldEffect declined) {
        boolean started = librarySearchSupport.performLibrarySearch(
                gameData,
                playerId,
                card -> card.hasType(CardType.LAND),
                "land cards",
                "Search your library for a land card and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD,
                searchFollowUp(selected, declined),
                null,
                null,
                false);
        return started && gameData.interaction.isAwaitingInput();
    }

    private List<UUID> addSearchedPlayer(UUID playerId, List<UUID> searchedPlayerIds) {
        List<UUID> updated = searchedPlayerIds == null
                ? new ArrayList<>() : new ArrayList<>(searchedPlayerIds);
        if (!updated.contains(playerId)) {
            updated.add(playerId);
        }
        return List.copyOf(updated);
    }

    private void finishSearches(GameData gameData, List<UUID> searchedPlayerIds) {
        if (searchedPlayerIds == null) {
            return;
        }
        for (UUID playerId : searchedPlayerIds) {
            if (gameData.playerDecks.get(playerId) == null) {
                continue;
            }
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + "'s library is shuffled."));
        }
    }

    private LibrarySearchFollowUp searchFollowUp(
            TemptingOfferSearchLibraryForLandToBattlefieldEffect selected,
            TemptingOfferSearchLibraryForLandToBattlefieldEffect declined) {
        return LibrarySearchFollowUp.forSelectedCard(LAND, selected, declined);
    }

    private void insertContinuation(GameData gameData, CardEffect continuation) {
        gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                gameData.pendingEffectResolutionIndex, List.of(continuation));
    }
}
