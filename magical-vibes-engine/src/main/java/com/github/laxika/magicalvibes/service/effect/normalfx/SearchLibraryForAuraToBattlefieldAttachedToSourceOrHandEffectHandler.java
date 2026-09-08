package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForAuraToBattlefieldAttachedToSourceOrHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForAuraToBattlefieldAttachedToSourceOrHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForAuraToBattlefieldAttachedToSourceOrHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        boolean sourceOnBattlefield = source != null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName
                    + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        Permanent host = source;
        List<Card> matchingCards = deck.stream()
                .filter(Card::isAura)
                .filter(card -> !card.isEnchantPlayer())
                .filter(card -> auraAttachmentService.canEnchant(gameData, card, controllerId, host))
                .toList();
        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(playerName
                    + " searches their library but finds no Aura that could enchant "
                    + host.getCard().getName() + ". Library is shuffled."));
            log.info("Game {} - {} searches library, no eligible Aura cards found", gameData.id, playerName);
            return;
        }

        LibrarySearchDestination destination = sourceOnBattlefield
                ? LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT
                : LibrarySearchDestination.HAND;
        String prompt = sourceOnBattlefield
                ? "Search your library for an Aura card that could enchant " + host.getCard().getName()
                        + " and put it onto the battlefield attached to it."
                : "Search your library for an Aura card that could enchant " + host.getCard().getName()
                        + ", reveal it, and put it into your hand.";

        LibrarySearchParams.Builder params = LibrarySearchParams.builder(controllerId,
                        new ArrayList<>(matchingCards))
                .reveals(!sourceOnBattlefield)
                .canFailToFind(true)
                .destination(destination);
        if (sourceOnBattlefield) {
            params.attachToPermanentId(source.getId());
        }
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, params.build(), prompt, true);
        log.info("Game {} - {} searches library for an Aura card ({} matches)",
                gameData.id, playerName, matchingCards.size());
    }
}
