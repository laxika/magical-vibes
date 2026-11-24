package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardCouldEnchantPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIdSetPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasAllCardNamesPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Unified handler for {@link SearchLibraryEffect}: searches the library of the player its {@code searchPlayer} names
 * (the controller by default) for up to
 * {@code count} cards matching the effect's filter (plus an optional X-relative mana-value bound)
 * and routes them to the effect's {@link LibrarySearchDestination} through the shared
 * {@link LibrarySearchSupport}/{@code LibraryChoiceHandlerService} interaction pipeline.
 *
 * <p>Collapses the former {@code SearchLibraryFor*} family (to-hand tutors, by-name searches,
 * to-top, creature-to-battlefield with MV/colour/subtype constraints, card-types-to-battlefield).
 * Reveal / fail-to-find behaviour is derived uniformly: a restricted search (non-null filter or a
 * mana-value bound) can fail to find, and reveals its pick for {@code HAND},
 * {@code HAND_OR_GRAVEYARD}, or {@code TOP_OF_LIBRARY} destinations; an unrestricted search does
 * neither.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final SubjectResolutionService subjectResolutionService;
    private final AuraAttachmentService auraAttachmentService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryEffect) effect, LibrarySearchFollowUp.NONE, null, List.of());
    }

    /**
     * Searches and puts the found card onto the battlefield together with {@code enteringWithFound}
     * (cards already in exile that enter at the same time, e.g. Sword of Hearth and Home). They are
     * carried by the library search and enter when it completes, whether or not a card is found.
     */
    void resolveWithCardsEnteringTogether(GameData gameData, StackEntry entry, SearchLibraryEffect effect,
                                          List<Card> enteringWithFound) {
        doResolve(gameData, entry, effect, LibrarySearchFollowUp.NONE, null, enteringWithFound);
    }

    void resolveWithFollowUp(GameData gameData, StackEntry entry, SearchLibraryEffect effect,
                             LibrarySearchFollowUp followUp) {
        doResolve(gameData, entry, effect, followUp, null, List.of());
    }

    void resolveWithTotalManaValueCap(GameData gameData, StackEntry entry, SearchLibraryEffect effect,
                                      int maxTotalManaValue) {
        doResolve(gameData, entry, effect, LibrarySearchFollowUp.NONE, maxTotalManaValue, List.of());
    }

    private void doResolve(GameData gameData, StackEntry entry, SearchLibraryEffect effect,
                           LibrarySearchFollowUp followUp, Integer totalManaValueBound,
                           List<Card> enteringWithFound) {
        entry.recordStep(StepKind.SEARCH, StepOutcome.NONE);
        UUID controllerId = subjectResolutionService.player(gameData, entry, effect, effect.searchPlayer());
        if (controllerId == null) return;
        if (effect.destination() == LibrarySearchDestination.EXILE_IMPRINT
                && followUp == LibrarySearchFollowUp.NONE) {
            // Imprint: the found card is exiled and imprinted on the source permanent (Strata Scythe, Hoarding Dragon).
            followUp = LibrarySearchFollowUp.imprint(entry.getSourcePermanentId());
        }
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId, effect.shuffleAfterSelection())) {
            insertNoCardFollowUp(gameData, entry, followUp);
            return;
        }

        AmountContext amountContext = AmountContext.forStackEntry(entry, resolveSource(gameData, entry));

        int count = (entry.isCastWithFlashback() || entry.getSourceZone() == Zone.GRAVEYARD)
                && effect.castFromGraveyardCount() != 1
                ? effect.castFromGraveyardCount()
                : Math.max(0, amountEvaluationService.evaluate(gameData, effect.count(), amountContext));

        // "A card with the same name" as the triggering permanent, read as the ability resolves (Verdant Succession).
        CardPredicate describedFilter = AmountEvaluationService.bindTriggeringPermanentName(gameData, effect.filter(),
                entry);
        // "A different name than each Aura you control", "an Aura card that could enchant that creature": bound to what
        // the subjects name now (CR 608.2h).
        CardPredicate filter = bindCouldEnchant(gameData, entry, effect, controllerId,
                subjectResolutionService.bindCardSubjects(gameData, entry, effect, describedFilter));
        ManaValueBound bound = effect.manaValueBound();
        boolean restricted = filter != null || bound != null || totalManaValueBound != null;
        CardPredicate searchFilter = restricted
                ? new CardAnyOfPredicate(List.of(
                        filter == null ? new CardTruePredicate() : filter,
                        new CardHasAllCardNamesPredicate()))
                : null;
        Integer boundValue = bound == null ? null
                : amountEvaluationService.evaluate(gameData, bound.amount(), amountContext) + bound.offset();

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, controllerId);
            if (effect.shuffleAfterSelection()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            insertNoCardFollowUp(gameData, entry, followUp);
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but it is empty."
                    + (effect.shuffleAfterSelection() ? " Library is shuffled." : "")));
            return;
        }

        // "Up to X" with X=0 still searches and shuffles (Uncage the Menagerie ruling).
        if (count <= 0) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, controllerId);
            if (effect.shuffleAfterSelection()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            insertNoCardFollowUp(gameData, entry, followUp);
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library."
                    + (effect.shuffleAfterSelection() ? " Library is shuffled." : "")));
            return;
        }

        Predicate<Card> deckFilter = card -> card.hasAllCardNames()
                || ((filter == null || predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, controllerId))
                        && matchesBound(card, boundValue, bound)
                        && (totalManaValueBound == null || card.getManaValue() <= totalManaValueBound)
                        && (!putsOntoBattlefield(effect.destination())
                        || !gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)));
        int topLibraryCardLimit = topLibraryCardLimit(deck.size(), effect.topLibraryFractionDenominator());
        List<Card> searchableCards = topLibraryCardLimit > 0
                ? deck.subList(0, topLibraryCardLimit)
                : deck;
        List<Card> matchingCards = searchableCards.stream().filter(deckFilter).toList();

        String baseDesc = describe(describedFilter, boundValue, bound);
        if (totalManaValueBound != null) {
            baseDesc += " with total mana value " + totalManaValueBound + " or less";
        }

        if (totalManaValueBound != null) {
            count = Math.min(count, matchingCards.size());
        }

        if (matchingCards.isEmpty()) {
            if (!librarySearchSupport.librarySearchCastableCards(gameData, controllerId).isEmpty()) {
                LibrarySearchDestination destination = effect.destination();
                String prompt = buildPrompt(baseDesc, destination, restricted, count,
                        effect.requireDifferentNames(), effect.topLibraryPosition());
                librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                        LibrarySearchParams.builder(controllerId, new ArrayList<>())
                                .remainingCount(count)
                                .canFailToFind(true)
                                .destination(destination)
                                .topLibraryPosition(effect.topLibraryPosition())
                                .topLibraryCardLimit(topLibraryCardLimit)
                                .filterPredicate(restricted ? searchFilter : null)
                                .requireDifferentNames(effect.requireDifferentNames())
                                .manaValueBound(boundValue, bound != null && bound.exact())
                                .totalManaValueBound(totalManaValueBound)
                                .grantHaste(effect.grantHaste())
                                .exileAtEndStep(effect.exileAtEndStep())
                                .returnToHandAtEndStep(effect.returnToHandAtEndStep())
                                .animateFound(effect.animateFound())
                                .accumulatedCards(new ArrayList<>(enteringWithFound))
                                .placeBattlefieldCardsSimultaneously(
                                        effect.animateFound() != null || !enteringWithFound.isEmpty())
                                .battlefieldCounter(effect.battlefieldCounter())
                                .followUp(followUp)
                                .shuffleAfterSelection(effect.shuffleAfterSelection())
                                .battlefieldIfChosenBeholdType(effect.battlefieldIfChosenBeholdType()
                                        ? entry.getBeholdChosenSubtype() : null)
                                .battlefieldIfOpponentControlsMoreLands(effect.battlefieldIfOpponentControlsMoreLands())
                                .build(),
                        prompt, true);
                return;
            }
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, controllerId);
            if (effect.shuffleAfterSelection()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            insertNoCardFollowUp(gameData, entry, followUp);
            // Pluralize the target description ("artifact card" -> "artifact cards", "card named X"
            // -> "cards named X") by promoting the first whole-word "card"; a mana-value-bound
            // description stays singular ("creature card with mana value N").
            String noMatchDesc = bound != null ? baseDesc : baseDesc.replaceFirst("\\bcard\\b", "cards");
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no " + noMatchDesc + "."
                    + (effect.shuffleAfterSelection() ? " Library is shuffled." : "")));
            log.info("Game {} - {} searches library, no {} found", gameData.id, playerName, noMatchDesc);
            return;
        }

        LibrarySearchDestination destination = effect.destination();
        String prompt = buildPrompt(baseDesc, destination, restricted, count, effect.requireDifferentNames(),
                effect.topLibraryPosition());

        LibrarySearchParams.Builder params = LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .remainingCount(count)
                        .reveals(reveals(restricted, destination))
                        .canFailToFind(restricted || effect.optionalCount())
                        .destination(destination)
                        .topLibraryPosition(effect.topLibraryPosition())
                        .topLibraryCardLimit(topLibraryCardLimit)
                        .filterPredicate(restricted ? searchFilter : null)
                        .requireDifferentNames(effect.requireDifferentNames())
                        .manaValueBound(boundValue, bound != null && bound.exact())
                        .totalManaValueBound(totalManaValueBound)
                        .grantHaste(effect.grantHaste())
                        .exileAtEndStep(effect.exileAtEndStep())
                        .returnToHandAtEndStep(effect.returnToHandAtEndStep())
                        .animateFound(effect.animateFound())
                        .accumulatedCards(new ArrayList<>(enteringWithFound))
                                .placeBattlefieldCardsSimultaneously(
                                        effect.animateFound() != null || !enteringWithFound.isEmpty())
                        .battlefieldCounter(effect.battlefieldCounter())
                        .followUp(followUp)
                        .enterWithCounters(effect.enterWithCounters())
                        .shuffleAfterSelection(effect.shuffleAfterSelection())
                        .battlefieldIfChosenBeholdType(effect.battlefieldIfChosenBeholdType()
                                ? entry.getBeholdChosenSubtype() : null)
                        .battlefieldIfOpponentControlsMoreLands(effect.battlefieldIfOpponentControlsMoreLands());
        if (destination == LibrarySearchDestination.BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER) {
            params.battlefieldControllerId(entry.getTargetId());
        }
        gameData.suppressNextLibrarySearchTriggers = effect.continuesPreviousSearch();
        try {
            librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, params.build(),
                    prompt, restricted);
        } finally {
            gameData.suppressNextLibrarySearchTriggers = false;
        }

        log.info("Game {} - {} searches library for {} card(s) to {} ({} matches)",
                gameData.id, playerName, count, destination, matchingCards.size());
    }

    /**
     * {@code filter} with each {@link CardCouldEnchantPredicate} bound to the cards of {@code searcherId}'s library that
     * are Auras that could enchant one of the permanents its subject names: each as it is, or as it last existed on the
     * battlefield (CR 608.2h), judged by the Aura's enchant ability and what would make the attachment illegal
     * (protection, "can't be enchanted"; CR 303.4a, 702.16c). A filter without the predicate is returned as it is.
     */
    private CardPredicate bindCouldEnchant(GameData gameData, StackEntry entry, SearchLibraryEffect effect,
                                           UUID searcherId, CardPredicate filter) {
        return switch (filter) {
            case null -> null;
            case CardCouldEnchantPredicate couldEnchant -> {
                List<Permanent> hosts = subjectResolutionService.permanentIds(gameData, entry, effect,
                                couldEnchant.permanents()).stream()
                        .map(id -> asItIsOrLastWas(gameData, entry, id))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                java.util.Set<UUID> ids = new java.util.HashSet<>();
                for (Card card : gameData.playerDecks.getOrDefault(searcherId, List.of())) {
                    if (card.isAura() && !card.isEnchantPlayer() && hosts.stream().anyMatch(host ->
                            !gameQueryService.cantBeEnchantedByOtherAuras(gameData, host)
                                    && auraAttachmentService.canEnchant(gameData, card, searcherId, host))) {
                        ids.add(card.getId());
                    }
                }
                yield new CardIdSetPredicate(ids);
            }
            case CardAllOfPredicate all -> new CardAllOfPredicate(all.predicates().stream()
                    .map(part -> bindCouldEnchant(gameData, entry, effect, searcherId, part)).toList());
            case CardAnyOfPredicate any -> new CardAnyOfPredicate(any.predicates().stream()
                    .map(part -> bindCouldEnchant(gameData, entry, effect, searcherId, part)).toList());
            case CardNotPredicate not -> new CardNotPredicate(
                    bindCouldEnchant(gameData, entry, effect, searcherId, not.predicate()));
            default -> filter;
        };
    }

    /** The permanent {@code permanentId} as it is on the battlefield, else as it last existed there, or {@code null}. */
    private Permanent asItIsOrLastWas(GameData gameData, StackEntry entry, UUID permanentId) {
        Permanent live = gameQueryService.findPermanentById(gameData, permanentId);
        if (live != null) {
            return live;
        }
        for (Permanent snapshot : java.util.Arrays.asList(entry.getSourcePermanentSnapshot(),
                entry.getTriggeringPermanentSnapshot())) {
            if (snapshot != null && permanentId.equals(snapshot.getId())) {
                return snapshot;
            }
        }
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(departed -> permanentId.equals(departed.getId()))
                .findFirst().orElse(null);
    }

    private void insertNoCardFollowUp(GameData gameData, StackEntry entry, LibrarySearchFollowUp followUp) {
        if (followUp.selectedCardFollowUp() == null
                || followUp.selectedCardFollowUp().effectIfNoCardChosen() == null) {
            return;
        }
        CardEffect effect = followUp.selectedCardFollowUp().effectIfNoCardChosen();
        if (gameData.interaction.isAwaitingInput()
                && gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex, List.of(effect));
        } else if (effect instanceof CreateTokenEffect createTokenEffect) {
            createTokenEffectHandler.resolve(gameData, entry, createTokenEffect);
        }
    }

    private Permanent resolveSource(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        return source != null ? source : entry.getSourcePermanentSnapshot();
    }

    private boolean matchesBound(Card card, Integer boundValue, ManaValueBound bound) {
        if (boundValue == null) return true;
        return bound.exact()
                ? card.getManaValue() == boundValue
                : card.getManaValue() <= boundValue;
    }

    private boolean putsOntoBattlefield(LibrarySearchDestination destination) {
        return switch (destination) {
            case BATTLEFIELD, BATTLEFIELD_TAPPED, BATTLEFIELD_ATTACHED_TO_PLAYER,
                    BATTLEFIELD_ATTACHED_TO_CREATURE, BATTLEFIELD_ATTACHED_TO_PERMANENT,
                    BATTLEFIELD_UNDER_SEARCHER -> true;
            default -> false;
        };
    }

    private int topLibraryCardLimit(int librarySize, int denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return Math.min(librarySize, (librarySize + denominator - 1) / denominator);
    }

    /** Human description of the search target, e.g. "creature card with mana value 3 or less". */
    private String describe(CardPredicate filter, Integer boundValue, ManaValueBound bound) {
        String desc = CardPredicateUtils.describeFilter(filter);
        if (boundValue != null) {
            desc += bound.exact()
                    ? " with mana value " + boundValue
                    : " with mana value " + boundValue + " or less";
        }
        return desc;
    }

    private boolean reveals(boolean restricted, LibrarySearchDestination destination) {
        return restricted && (destination == LibrarySearchDestination.HAND
                || destination == LibrarySearchDestination.HAND_OR_GRAVEYARD
                || destination == LibrarySearchDestination.TOP_OF_LIBRARY
                || destination == LibrarySearchDestination.REVEAL_ONLY);
    }

    private String buildPrompt(String desc, LibrarySearchDestination destination,
                               boolean restricted, int count, boolean requireDifferentNames,
                               int topLibraryPosition) {
        String remaining = count > 1 ? " (" + count + " remaining)" : "";
        String distinct = requireDifferentNames ? " with a different name" : "";
        return switch (destination) {
            case HAND -> "Search your library for a " + desc + distinct
                    + (restricted ? " to reveal and put into your hand" : " to put into your hand")
                    + remaining + ".";
            case HAND_OR_GRAVEYARD -> "Search your library for a " + desc
                    + distinct + " to reveal and put into your hand or graveyard" + remaining + ".";
            case TOP_OF_LIBRARY -> "Search your library for a " + desc
                    + (restricted
                            ? ", reveal it, then shuffle and put that card "
                                    + topLibraryPositionText(topLibraryPosition) + "."
                            : ", then shuffle and put that card "
                                    + topLibraryPositionText(topLibraryPosition) + ".");
            case EXILE -> "Search your library for a " + desc + " to exile" + remaining + ".";
            case EXILE_FOR_MAY_CAST -> "Search your library for a " + desc + " to exile" + remaining + ".";
            case EXILE_FOR_MAY_CAST_WITH_NORMAL_COST ->
                    "Search your library for a " + desc + " to exile" + remaining + ".";
            case EXILE_PLAYABLE_ANY_NUMBER -> "Search your library for matching cards to exile (any number).";
            case GRAVEYARD -> count > 1
                    ? "Search your library for a " + desc + " to put into your graveyard" + remaining + "."
                    : "Search your library for a " + desc + " and put it into your graveyard.";
            case BATTLEFIELD_TAPPED -> count > 1
                    ? "Search your library for a " + desc + " to put onto the battlefield tapped" + remaining + "."
                    : "Search your library for a " + desc + " and put it onto the battlefield tapped.";
            case BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER -> "Search your library for a " + desc
                    + " and put it onto the battlefield tapped under target player's control.";
            default -> count > 1
                    ? "Search your library for a " + desc + " to put onto the battlefield" + remaining + "."
                    : "Search your library for a " + desc + " and put it onto the battlefield.";
        };
    }

    private String topLibraryPositionText(int position) {
        return switch (position) {
            case 0 -> "on top";
            case 1 -> "second from the top";
            case 2 -> "third from the top";
            default -> {
                int ordinal = position + 1;
                int lastTwoDigits = ordinal % 100;
                String suffix = lastTwoDigits >= 11 && lastTwoDigits <= 13
                        ? "th"
                        : switch (ordinal % 10) {
                            case 1 -> "st";
                            case 2 -> "nd";
                            case 3 -> "rd";
                            default -> "th";
                        };
                yield ordinal + suffix + " from the top";
            }
        };
    }
}
