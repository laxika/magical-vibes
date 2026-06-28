package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                                            SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !source.isAttached()) return;
        UUID enchantedPlayerId = source.getAttachedTo();

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        // Collect the names of Curses currently attached to the enchanted player so we can exclude them.
        Set<String> attachedCurseNames = new HashSet<>();
        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isAttached() && enchantedPlayerId.equals(perm.getAttachedTo())
                    && gameQueryService.cardHasSubtype(perm.getCard(), CardSubtype.CURSE, gameData, ownerId)) {
                attachedCurseNames.add(perm.getCard().getName());
            }
        });

        List<Card> matchingCards = deck.stream()
                .filter(card -> gameQueryService.cardHasSubtype(card, CardSubtype.CURSE, gameData, controllerId))
                .filter(card -> !attachedCurseNames.contains(card.getName()))
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no eligible Curse cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} searches library, no eligible Curse cards found", gameData.id, playerName);
            return;
        }

        String prompt = "Search your library for a Curse card and put it onto the battlefield attached to the enchanted player.";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PLAYER)
                .attachToPlayerId(enchantedPlayerId)
                .build(), prompt, true);

        log.info("Game {} - {} searches library for a Curse card ({} matches)", gameData.id, playerName, matchingCards.size());
    }
}
