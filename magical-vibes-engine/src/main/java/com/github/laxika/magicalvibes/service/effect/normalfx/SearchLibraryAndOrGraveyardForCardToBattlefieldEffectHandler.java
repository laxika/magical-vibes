package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryAndOrGraveyardForCardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndOrGraveyardForCardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryAndOrGraveyardForCardToBattlefieldEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                           SearchLibraryAndOrGraveyardForCardToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        Permanent sourcePermanent = effect.attachToSource() && entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            Optional<Card> graveyardMatch = graveyard.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, effect.filter(), sourceCardId, gameData, controllerId))
                    .findFirst();
            if (graveyardMatch.isPresent()) {
                Card found = graveyardMatch.get();
                graveyard.remove(found);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, found);
                Permanent entered = graveyardReturnSupport.putCardOntoBattlefield(
                        gameData, controllerId, found, null, null, false, false, null);
                attachToSource(entered, sourcePermanent);
                gameLogService.append(gameData, GameLog.textCardText(
                        playerName + " searches their graveyard, reveals ", found,
                        ", and puts it onto the battlefield."));
                log.info("Game {} - {} finds {} in graveyard", gameData.id, playerName, found.getName());
                return;
            }
        }

        if (effect.includeHand()) {
            List<Card> hand = gameData.playerHands.get(controllerId);
            if (hand != null) {
                Optional<Card> handMatch = hand.stream()
                        .filter(card -> predicateEvaluationService.matchesCardPredicate(
                                card, effect.filter(), sourceCardId, gameData, controllerId))
                        .findFirst();
                if (handMatch.isPresent()) {
                    Card found = handMatch.get();
                    hand.remove(found);
                    Permanent entered = new Permanent(found);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, entered);
                    attachToSource(entered, sourcePermanent);
                    gameLogService.append(gameData, GameLog.textCardText(
                            playerName + " reveals ", found, " from their hand and puts it onto the battlefield."));
                    log.info("Game {} - {} finds {} in hand", gameData.id, playerName, found.getName());
                    return;
                }
            }
        }

        String description = CardPredicateUtils.describeFilter(effect.filter());
        String pluralDescription = description.replaceFirst("\\bcard\\b", "cards");
        String prompt = "Search your library for a " + description + " and put it onto the battlefield.";
        librarySearchSupport.performLibrarySearch(
                gameData,
                controllerId,
                card -> predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), sourceCardId, gameData, controllerId),
                pluralDescription,
                prompt,
                false,
                true,
                sourcePermanent == null
                        ? LibrarySearchDestination.BATTLEFIELD
                        : LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT,
                LibrarySearchFollowUp.NONE,
                sourcePermanent == null ? null : sourcePermanent.getId());
    }

    private void attachToSource(Permanent entered, Permanent sourcePermanent) {
        if (entered != null && sourcePermanent != null) {
            entered.setAttachedTo(sourcePermanent.getId());
        }
    }
}
