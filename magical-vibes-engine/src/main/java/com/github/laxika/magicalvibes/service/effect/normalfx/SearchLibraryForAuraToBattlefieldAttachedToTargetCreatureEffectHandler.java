package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        // The host is the lone attacker recorded on the trigger. When wrapped in MayEffect (Sovereigns)
        // the combat trigger records it as the source permanent (CR 603.5 resolution-time may path);
        // a mandatory (non-may) use would instead record it as the non-targeting targetId.
        UUID hostId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent host = hostId == null ? null : gameQueryService.findPermanentById(gameData, hostId);
        if (host == null || !gameQueryService.isCreature(gameData, host)) return;

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> couldEnchant(gameData, card, host, controllerId))
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " searches their library but finds no Aura that could enchant " + host.getCard().getName() + ". Library is shuffled."));
            log.info("Game {} - {} searches library, no eligible Aura cards found", gameData.id, playerName);
            return;
        }

        String prompt = "Search your library for an Aura card and put it onto the battlefield attached to " + host.getCard().getName() + ".";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT)
                        .attachToPermanentId(host.getId())
                        .build(), prompt, true);

        log.info("Game {} - {} searches library for an Aura card ({} matches)", gameData.id, playerName, matchingCards.size());
    }

    /**
     * An Aura "could enchant" the host if it is an Aura that enchants a permanent (not a player) and
     * the host satisfies the card's enchant restriction — its declared target filter (CR 303.4a).
     * Hexproof/shroud are ignored: the Aura is not targeted, so it may attach to such a creature.
     */
    private boolean couldEnchant(GameData gameData, Card card, Permanent host, UUID controllerId) {
        if (!card.isAura() || card.isEnchantPlayer()) return false;
        TargetFilter filter = card.getTargetFilter();
        if (!(filter instanceof PermanentPredicateTargetFilter)
                && !(filter instanceof ControlledPermanentPredicateTargetFilter)
                && !(filter instanceof OwnedPermanentPredicateTargetFilter)) {
            return false;
        }
        FilterContext context = new FilterContext(gameData, card.getId(), controllerId, null);
        return predicateEvaluationService.checkTargetFilter(filter, host, context).isEmpty();
    }
}
