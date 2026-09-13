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
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect auraSearch =
                (SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect) effect;

        // The host is the recorded source permanent when wrapped in MayEffect (Sovereigns and
        // Light-Paws); a mandatory (non-may) use would instead record it as the non-targeting targetId.
        UUID hostId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent host = hostId == null ? null : gameQueryService.findPermanentById(gameData, hostId);
        if (host == null
                || (!auraSearch.restrictToTriggeringAura() && !gameQueryService.isCreature(gameData, host))) return;

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        int maxManaValue = auraSearch.restrictToTriggeringAura()
                ? triggeringAuraManaValue(gameData, entry)
                : Integer.MAX_VALUE;
        Set<String> controlledAuraNames = auraSearch.restrictToTriggeringAura()
                ? controlledAuraNames(gameData, controllerId)
                : Set.of();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> couldEnchant(gameData, card, host, controllerId))
                .filter(card -> card.getManaValue() <= maxManaValue)
                .filter(card -> !controlledAuraNames.contains(card.getName()))
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no Aura that could enchant " + host.getCard().getName() + ". Library is shuffled."));
            log.info("Game {} - {} searches library, no eligible Aura cards found", gameData.id, playerName);
            return;
        }

        String prompt = maxManaValue == Integer.MAX_VALUE
                ? "Search your library for an Aura card and put it onto the battlefield attached to " + host.getCard().getName() + "."
                : "Search your library for an Aura card with mana value " + maxManaValue
                + " or less and a different name than each Aura you control, then put it onto the battlefield attached to "
                + host.getCard().getName() + ".";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT)
                        .attachToPermanentId(host.getId())
                        .build(), prompt, true);

        log.info("Game {} - {} searches library for an Aura card ({} matches)", gameData.id, playerName, matchingCards.size());
    }

    private int triggeringAuraManaValue(GameData gameData, StackEntry entry) {
        if (entry.getEventValue() > 0) return entry.getEventValue();
        UUID triggeringPermanentId = entry.getTriggeringPermanentId();
        Permanent triggeringPermanent = triggeringPermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, triggeringPermanentId);
        return triggeringPermanent == null ? entry.getEventValue() : triggeringPermanent.getCard().getManaValue();
    }

    private Set<String> controlledAuraNames(GameData gameData, UUID controllerId) {
        Set<String> names = new HashSet<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            battlefield.stream()
                    .map(Permanent::getCard)
                    .filter(Card::isAura)
                    .map(Card::getName)
                    .forEach(names::add);
        }
        return names;
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
        FilterContext context = new FilterContext(gameData, card.getId(), controllerId, null, null);
        return predicateEvaluationService.checkTargetFilter(filter, host, context).isEmpty();
    }
}
