package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchZonesForCardNamedToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LibrarySearchSupport librarySearchSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchZonesForCardNamedToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchZonesForCardNamedToBattlefieldEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                           SearchZonesForCardNamedToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = effect.cardName();

        // For the attach variant the host is a true target: an illegal one on resolution fizzles the
        // whole ability, so no zone is searched at all.
        Permanent host = null;
        if (effect.attachToTarget()) {
            host = entry.getTargetId() == null ? null : gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (host == null || !gameQueryService.isCreature(gameData, host)) {
                log.info("Game {} - {}'s search fizzles, no legal creature to attach to", gameData.id, cardName);
                return;
            }
        }

        // Graveyard first (a public zone). A match is taken automatically — no interactive pick.
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            Optional<Card> graveyardMatch = graveyard.stream()
                    .filter(card -> cardName.equals(card.getName()))
                    .findFirst();
            if (graveyardMatch.isPresent()) {
                Card found = graveyardMatch.get();
                graveyard.remove(found);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, found);
                if (host == null) {
                    graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, found);
                } else {
                    Permanent attached = new Permanent(found);
                    attached.setAttachedTo(host.getId());
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, attached);
                    gameLogService.append(gameData, GameLog.builder()
                            .card(found)
                            .text(" enters the battlefield attached to ")
                            .card(host.getCard())
                            .text(".")
                            .build());
                }
                log.info("Game {} - {} finds {} in graveyard", gameData.id, playerName, cardName);
                return;
            }
        }

        // Hand (a hidden zone the controller already sees). A match is also taken automatically.
        List<Card> hand = effect.includeHand() ? gameData.playerHands.get(controllerId) : null;
        if (hand != null) {
            Optional<Card> handMatch = hand.stream()
                    .filter(card -> cardName.equals(card.getName()))
                    .findFirst();
            if (handMatch.isPresent()) {
                Card found = handMatch.get();
                hand.remove(found);
                Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
                Permanent permanent = new Permanent(found);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);
                gameLogService.append(gameData, GameLog.textCardText(
                        playerName + " reveals ", found, " from their hand and puts it onto the battlefield."));
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, found);
                log.info("Game {} - {} finds {} in hand", gameData.id, playerName, cardName);
                return;
            }
        }

        // Library last — interactive pick with a shuffle afterwards ("If you search your library
        // this way, shuffle"). Handles Leonin Arbiter, an empty library, and no matches internally.
        String prompt = "Search your library for a card named " + cardName + " and put it onto the battlefield"
                + (host == null ? "." : " attached to " + host.getCard().getName() + ".");
        librarySearchSupport.performLibrarySearch(
                gameData,
                controllerId,
                card -> cardName.equals(card.getName()),
                "cards named " + cardName,
                prompt,
                false,
                true,
                host == null ? LibrarySearchDestination.BATTLEFIELD
                        : LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT,
                LibrarySearchFollowUp.NONE,
                host == null ? null : host.getId());
    }
}
