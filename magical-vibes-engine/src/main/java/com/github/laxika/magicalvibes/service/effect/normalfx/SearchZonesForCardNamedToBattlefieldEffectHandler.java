package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
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
    private final EquipSupport equipSupport;
    private final ChooseOneEffectHandler chooseOneEffectHandler;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchZonesForCardNamedToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var search = (SearchZonesForCardNamedToBattlefieldEffect) effect;
        if (!search.additionalCardNames().isEmpty()) {
            resolveMultipleNames(gameData, entry, search);
        } else {
            doResolve(gameData, entry, search);
        }
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
        if (effect.attachToTarget() && effect.selectedZones().isEmpty()) {
            var options = List.of(
                    new ChooseOneEffect.ChooseOneOption("Search your graveyard",
                            new SearchZonesForCardNamedToBattlefieldEffect(cardName, false, true,
                                    List.of(), Set.of(Zone.GRAVEYARD))),
                    new ChooseOneEffect.ChooseOneOption("Search your library",
                            new SearchZonesForCardNamedToBattlefieldEffect(cardName, false, true,
                                    List.of(), Set.of(Zone.LIBRARY))),
                    new ChooseOneEffect.ChooseOneOption("Search your graveyard and library",
                            new SearchZonesForCardNamedToBattlefieldEffect(cardName, false, true,
                                    List.of(), Set.of(Zone.GRAVEYARD, Zone.LIBRARY))));
            chooseOneEffectHandler.resolve(gameData, entry, new ChooseOneEffect(options));
            return;
        }

        boolean searchGraveyard = effect.selectedZones().isEmpty() || effect.selectedZones().contains(Zone.GRAVEYARD);
        boolean searchLibrary = effect.selectedZones().isEmpty() || effect.selectedZones().contains(Zone.LIBRARY);
        if (effect.attachToTarget() && searchGraveyard && searchLibrary) {
            List<Card> matches = new java.util.ArrayList<>(gameData.playerGraveyards.get(controllerId).stream()
                    .filter(card -> cardName.equals(card.getName())).toList());
            librarySearchSupport.performLibrarySearch(gameData, controllerId,
                    card -> cardName.equals(card.getName()), "cards named " + cardName,
                    "Find a card named " + cardName + " in your graveyard or library.", false, true,
                    LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT,
                    LibrarySearchFollowUp.NONE, host.getId());
            var pending = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            if (pending != null) {
                matches.addAll(pending.params().cards());
                gameData.interaction.beginInteraction(new PendingInteraction.LibrarySearch(
                        pending.params().withCards(matches), pending.messagePrompt(), true));
                return;
            }
            searchLibrary = false;
        }

        List<Card> graveyard = searchGraveyard ? gameData.playerGraveyards.get(controllerId) : null;
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
                    Permanent attached = new Permanent(found, com.github.laxika.magicalvibes.model.Zone.GRAVEYARD);
                    attached.setAttachedTo(host.getId());
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, attached);
                    equipSupport.notifyEquipmentAttached(gameData, attached, null);
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
        if (!searchLibrary) return;
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
    private void resolveMultipleNames(GameData gameData, StackEntry entry,
                                      SearchZonesForCardNamedToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        java.util.List<String> names = new java.util.ArrayList<>();
        names.add(effect.cardName());
        names.addAll(effect.additionalCardNames());
        List<Card> found = new java.util.ArrayList<>();
        List<String> libraryNames = new java.util.ArrayList<>();
        for (String name : names) {
            List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
            Card card = graveyard.stream().filter(candidate -> name.equals(candidate.getName())).findFirst().orElse(null);
            if (card != null) {
                graveyard.remove(card);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
            } else if (effect.includeHand()) {
                List<Card> hand = gameData.playerHands.get(controllerId);
                card = hand.stream().filter(candidate -> name.equals(candidate.getName())).findFirst().orElse(null);
                if (card != null) hand.remove(card);
            }
            if (card != null) found.add(card);
            else libraryNames.add(name);
        }
        if (libraryNames.isEmpty() || librarySearchSupport.isSearchPrevented(
                gameData, controllerId, controllerId, false, entry.getControllerId())) {
            placeFoundCardsTogether(gameData, entry, found);
            return;
        }
        List<Card> deck = gameData.playerDecks.get(controllerId);
        int searchLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId);
        List<Card> searched = new java.util.ArrayList<>(deck.subList(0, Math.min(deck.size(), searchLimit)));
        deck.removeAll(searched);
        LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, controllerId);
        if (searched.stream().noneMatch(card -> libraryNames.contains(card.getName()))) {
            deck.addAll(searched);
            com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            placeFoundCardsTogether(gameData, entry, found);
            return;
        }
        var laterPicks = libraryNames.subList(1, libraryNames.size()).stream()
                .map(name -> new LibrarySearchFollowUp.SecondBoundedPick.PredicatePick(
                        new com.github.laxika.magicalvibes.model.filter.CardNamedPredicate(name),
                        "You may find a card named " + name + ".")).toList();
        LibrarySearchFollowUp followUp = laterPicks.isEmpty()
                ? LibrarySearchFollowUp.forBoundedPick(LibrarySearchFollowUp.SecondBoundedPick.terminal(
                        true, LibrarySearchDestination.BATTLEFIELD))
                : LibrarySearchFollowUp.forBoundedPick(LibrarySearchFollowUp.SecondBoundedPick.predicate(
                        laterPicks.getFirst().predicate(), laterPicks.getFirst().prompt(), true,
                        LibrarySearchDestination.BATTLEFIELD, laterPicks.subList(1, laterPicks.size())));
        String prompt = "You may find a card named " + libraryNames.getFirst() + ".";
        var params = com.github.laxika.magicalvibes.model.LibrarySearchParams.builder(controllerId,
                        searched.stream().filter(candidate -> libraryNames.getFirst().equals(candidate.getName())).toList())
                .canFailToFind(true).reveals(true).sourceCards(searched).accumulatedCards(found)
                .reorderRemainingToBottom(true).shuffleAfterSelection(false)
                .placeBattlefieldCardsSimultaneously(true).destination(LibrarySearchDestination.BATTLEFIELD)
                .followUp(followUp).prompt(prompt).build();
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                librarySearchSupport.applyOppositionAgentControl(gameData, params), prompt, true));
    }

    private void placeFoundCardsTogether(GameData gameData, StackEntry entry, List<Card> found) {
        List<Permanent> entered = new java.util.ArrayList<>();
        var enterTapped = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        for (Card card : found) {
            Permanent permanent = new Permanent(card);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent,
                    enterTapped, List.copyOf(entered));
            entered.add(permanent);
        }
        for (Permanent permanent : entered) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(),
                    permanent.getCard(), null, false);
        }
    }

}
