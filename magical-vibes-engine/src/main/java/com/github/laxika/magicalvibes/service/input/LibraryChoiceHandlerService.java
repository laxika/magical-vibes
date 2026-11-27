package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.AnyManaSpending;
import com.github.laxika.magicalvibes.model.ChosenOpponentFollowUp;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.DelayedExiledCardReturns;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.service.trigger.pipeline.AttachmentTriggers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingAllureOfTheUnknownChoice;
import com.github.laxika.magicalvibes.model.PendingDubiousChallengeChoice;
import com.github.laxika.magicalvibes.model.PendingEachPlayerLibraryExile;
import com.github.laxika.magicalvibes.model.PendingGuildFeud;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingLibraryRevealRemainder;
import com.github.laxika.magicalvibes.model.PendingManifoldInsightsChoice;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingMemoriesReturningChoice;
import com.github.laxika.magicalvibes.model.PendingOpponentExileChoice;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.PendingPortalPileSearch;
import com.github.laxika.magicalvibes.model.PendingPsychoticEpisodeChoice;
import com.github.laxika.magicalvibes.model.PendingReturnExiledWithSourceCard;
import com.github.laxika.magicalvibes.model.PendingSphinxAmbassadorChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.SpellFace;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.TriggeringCardInExile;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryNinjutsuEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOpponentsWhoCastNamedSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithNormalCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithoutPayingManaCostOrPutIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PayFreeCastCostIncreaseEffect;
import com.github.laxika.magicalvibes.model.effect.PayFreeCastCostIncreaseEffect.IfNotCast;
import com.github.laxika.magicalvibes.service.effect.normalfx.FreeCastCostIncreaseSupport;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryRevealSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.SplitCardCastSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.SpellEvents;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryChoiceHandlerService
        implements com.github.laxika.magicalvibes.service.effect.normalfx.ChosenOpponentContinuation<
                ChosenOpponentFollowUp.ThreatsUndetectedReveal> {

    public static final String HELD_TO_BATTLEFIELD = "Put the chosen cards onto the battlefield";

    public static final String HELD_TO_HAND = "Put the chosen cards into your hand";

    private final com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

    private final GameQueryService gameQueryService;

    private final com.github.laxika.magicalvibes.service.cast.CastingCostService castingCostService;

    private final PredicateEvaluationService predicateEvaluationService;

    private final GraveyardService graveyardService;

    private final BattlefieldEntryService battlefieldEntryService;

    private final LegendRuleService legendRuleService;

    private final StateBasedActionService stateBasedActionService;

    private final GameLogService gameLogService;

    private final InputCompletionService inputCompletionService;

    private final PlayerInputService playerInputService;

    private final EffectResolutionService effectResolutionService;

    private final ExileService exileService;

    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    private final SpellEvents spellEvents;
    private final AttachmentTriggers attachmentTriggers;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport librarySearchSupport;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport lifeSupport;

    private final DrawService drawService;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport animationSupport;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.MemoriesReturningEffectHandler memoriesReturningEffectHandler;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.ManifoldInsightsEffectHandler manifoldInsightsEffectHandler;

    private final com.github.laxika.magicalvibes.service.effect.AmountEvaluationService amountEvaluationService;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.BasicLandSearchQueueSupport basicLandSearchQueueSupport;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.GuildFeudSupport guildFeudSupport;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.ReturnCardExiledWithSourceToBattlefieldEffectHandler returnCardExiledWithSourceToBattlefieldEffectHandler;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.ManifestService manifestService;

    private final com.github.laxika.magicalvibes.service.cast.CastingPermissionService castingPermissionService;

    private final com.github.laxika.magicalvibes.service.effect.normalfx.ChosenOpponentSupport chosenOpponentSupport;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService triggeredAbilityStackService;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.battlefield.BattlefieldPlacementService battlefieldPlacementService;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.battlefield.AsEntersInteractionService asEntersInteractionService;

    @Autowired @Lazy
    private SpellCastingService spellCastingService;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.effect.normalfx.FreeCastCostIncreaseSupport freeCastCostIncreaseSupport;

    @Autowired @Lazy
    private TargetLegalityService targetLegalityService;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.effect.LandEquilibriumSupport landEquilibriumSupport;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.effect.normalfx.AllureOfTheUnknownEffectHandler allureOfTheUnknownEffectHandler;

    @Autowired @Lazy
    private AbilityActivationService abilityActivationService;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport exileFreeCastQueueSupport;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport exileFreeCastSupport;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService conditionEvaluationService;

    @Autowired @Lazy
    private PermanentChoiceHandlerService permanentChoiceHandlerService;

    public void handleLibraryCardChosen(GameData gameData, Player player, int cardIndex) {
        if (gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class) == null) {
            throw new IllegalStateException("Not awaiting library search");
        }
        PendingInteraction.LibrarySearch activeSearch =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        LibrarySearchParams librarySearch = activeSearch != null ? activeSearch.params() : null;
        if (librarySearch == null || !player.getId().equals(activeSearch.decidingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = librarySearch.playerId();
        List<Card> searchCards = librarySearch.cards();

        boolean reveals = librarySearch.reveals();
        boolean canFailToFind = librarySearch.canFailToFind();
        UUID targetPlayerId = librarySearch.targetPlayerId();
        int remainingCount = librarySearch.remainingCount();
        List<Card> sourceCards = librarySearch.sourceCards();
        boolean reorderRemainingToBottom = librarySearch.reorderRemainingToBottom();
        boolean reorderRemainingToTop = librarySearch.reorderRemainingToTop();
        boolean restToGraveyard = librarySearch.restToGraveyard();
        boolean restToExile = librarySearch.restToExile();
        boolean restToHand = librarySearch.restToHand();
        boolean shuffleAfterSelection = librarySearch.shuffleAfterSelection();
        LibrarySearchFollowUp followUp = librarySearch.followUp();
        LibrarySearchDestination destination = librarySearch.destination() != null
                ? librarySearch.destination()
                : LibrarySearchDestination.HAND;
        int topLibraryPosition = Math.max(0, librarySearch.topLibraryPosition());
        boolean toBattlefield = destination == LibrarySearchDestination.BATTLEFIELD
                || destination == LibrarySearchDestination.BATTLEFIELD_TAPPED
                || destination == LibrarySearchDestination.BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER
                || destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PLAYER
                || destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT;
        boolean toBattlefieldTapped = destination == LibrarySearchDestination.BATTLEFIELD_TAPPED
                || destination == LibrarySearchDestination.BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER;
        boolean finalCardToHand = librarySearch.finalCardToHand() && remainingCount == 1;
        boolean toGraveyard = destination == LibrarySearchDestination.GRAVEYARD;
        Set<CardType> filterCardTypes = librarySearch.filterCardTypes();
        String filterCardName = librarySearch.filterCardName();
        com.github.laxika.magicalvibes.model.filter.CardPredicate filterPredicate = librarySearch.filterPredicate();
        List<Card> accumulatedCards = librarySearch.accumulatedCards() != null
                ? new ArrayList<>(librarySearch.accumulatedCards()) : new ArrayList<>();
        boolean requireDifferentNames = librarySearch.requireDifferentNames();
        boolean requireDifferentPowers = librarySearch.requireDifferentPowers();
        Integer manaValueBoundValue = librarySearch.manaValueBoundValue();
        boolean manaValueExact = librarySearch.manaValueExact();
        Integer totalManaValueBound = librarySearch.totalManaValueBound();
        List<String> excludedCardNames = new ArrayList<>(librarySearch.excludedCardNames());
        boolean grantHaste = librarySearch.grantHaste();
        boolean sacrificeAtEndStep = librarySearch.sacrificeAtEndStep();
        boolean exileAtEndStep = librarySearch.exileAtEndStep();
        boolean returnToHandAtEndStep = librarySearch.returnToHandAtEndStep();
        AnimatePermanentsEffect animateFound = librarySearch.animateFound();
        CounterType battlefieldCounter = librarySearch.battlefieldCounter();
        com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect enterWithCounters =
                librarySearch.enterWithCounters();
        CardSubtype battlefieldIfChosenBeholdType = librarySearch.battlefieldIfChosenBeholdType();
        boolean battlefieldIfChosenTapped = librarySearch.battlefieldIfChosenTapped();
        boolean battlefieldIfOpponentControlsMoreLands = librarySearch.battlefieldIfOpponentControlsMoreLands();
        boolean placeBattlefieldCardsSimultaneously = librarySearch.placeBattlefieldCardsSimultaneously();

        UUID deckOwnerId = targetPlayerId != null ? targetPlayerId : playerId;
        UUID handOwnerId = targetPlayerId != null ? targetPlayerId : playerId;
        UUID battlefieldControllerId = librarySearch.battlefieldControllerId() != null
                ? librarySearch.battlefieldControllerId() : handOwnerId;

        // Validate before touching interaction state: a rejected answer must leave the prompt
        // standing so the player can answer again. Clearing first and then throwing (every branch
        // below repeats these same two checks) destroys the only thing that would resume the entry
        // parked in pendingEffectResolutionEntry, wedging the game on a stale client answer.
        if (cardIndex == -1) {
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
        } else if (cardIndex < 0 || cardIndex >= searchCards.size()) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        if (cardIndex >= 0
                && librarySearch.allowCastFromLibraryWhileSearching()
                && (targetPlayerId == null || targetPlayerId.equals(playerId))
                && sourceCards == null
                && librarySearch.decisionPlayerId() == null
                && (librarySearch.castOnlyCardIds().contains(searchCards.get(cardIndex).getId())
                // Library ninjutsu is an activated ability, not a cast: it is on offer for a card the search
                // would find anyway, which is the point of activating it while searching.
                || librarySearchSupport.libraryNinjutsuEffect(searchCards.get(cardIndex)) != null)
                && librarySearchSupport.isLibrarySearchCastableCard(searchCards.get(cardIndex))) {
            Card selectedCard = searchCards.get(cardIndex);
            LibraryNinjutsuEffect libraryNinjutsu = librarySearchSupport.libraryNinjutsuEffect(selectedCard);
            if (libraryNinjutsu == null) {
                handleLibrarySearchCast(gameData, player, activeSearch, selectedCard);
                return;
            }
            // Library ninjutsu can only be activated with an unblocked attacker to shuffle; without one
            // the found card is simply taken as a plain search result.
            if (!unblockedAttackerIds(gameData, playerId).isEmpty()) {
                handleLibraryNinjutsu(gameData, player, activeSearch, selectedCard, libraryNinjutsu);
                return;
            }
        }

        if (cardIndex >= 0) {
            activeSearch = markSearchChosen(gameData, activeSearch);
            followUp = activeSearch.params().followUp();
        }

        gameData.interaction.clearAwaitingInput();

        boolean oppositionAgentDecision = librarySearch.decisionPlayerId() != null
                && !librarySearch.decisionPlayerId().equals(playerId);

        if (followUp.remainingSameNamePicks() != null && followUp.remainingSameNamePicks().simultaneous()) {
            var queue = followUp.remainingSameNamePicks();
            if (cardIndex >= 0 && oppositionAgentDecision) {
                exileCardFoundUnderOppositionAgent(gameData, player, activeSearch, searchCards.get(cardIndex),
                        gameData.playerDecks.get(deckOwnerId), deckOwnerId);
            } else if (cardIndex >= 0) {
                queue = queue.withSelectedCard(searchCards.get(cardIndex));
                recordFoundCard(gameData, searchCards.get(cardIndex), playerId, deckOwnerId, null);
            }
            var next = followUp.withRemainingSameNamePicks(queue);
            if (librarySearchSupport.startNextSameNamePick(gameData, playerId, next)) return;
            UUID queuedBattlefieldControllerId = queue.battlefieldControllerId() != null
                    ? queue.battlefieldControllerId() : playerId;
            boolean enterTapped = queue.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED;
            battlefieldEntryBatchSupport.begin(gameData, queue.selectedCards().stream()
                            .map(selected -> new com.github.laxika.magicalvibes.model.BattlefieldEntryCard(
                                    queuedBattlefieldControllerId, deckOwnerId, selected, Zone.LIBRARY, null).withTapped(enterTapped))
                            .toList(),
                    List.of(new com.github.laxika.magicalvibes.model.BattlefieldEntryLibraryRemainder(
                            deckOwnerId, List.of(), true)));
            finishSearchAndResume(gameData);
            return;
        }

        if (followUp.subtypeToBattlefieldPicks() != null) {
            var queue = followUp.subtypeToBattlefieldPicks();
            if (cardIndex >= 0 && oppositionAgentDecision) {
                exileCardFoundUnderOppositionAgent(gameData, player, activeSearch, searchCards.get(cardIndex),
                        gameData.playerDecks.get(deckOwnerId), deckOwnerId);
            } else if (cardIndex >= 0) {
                queue = queue.withSelectedCard(searchCards.get(cardIndex));
                recordFoundCard(gameData, searchCards.get(cardIndex), playerId, deckOwnerId, queue.destination());
            }
            if (librarySearchSupport.startNextSubtypeToBattlefieldPick(gameData, playerId,
                    followUp.withSubtypeToBattlefieldPicks(queue))) {
                return;
            }
            if (queue.destination() == LibrarySearchDestination.GRAVEYARD) {
                // The found cards enter the graveyard together, then the library is shuffled once.
                Set<UUID> foundIds = queue.selectedCards().stream().map(Card::getId)
                        .collect(java.util.stream.Collectors.toSet());
                gameData.playerDecks.get(deckOwnerId).removeIf(card -> foundIds.contains(card.getId()));
                graveyardService.addCardsFromLibraryToGraveyard(gameData, deckOwnerId, queue.selectedCards());
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                gameLogService.append(gameData, GameLog.text(player.getUsername() + " puts "
                        + queue.selectedCards().size() + " card(s) into their graveyard. Library is shuffled."));
                finishSearchAndResume(gameData);
                return;
            }
            boolean enterTapped = queue.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED;
            battlefieldEntryBatchSupport.begin(gameData, queue.selectedCards().stream()
                            .map(selected -> new com.github.laxika.magicalvibes.model.BattlefieldEntryCard(
                                    playerId, deckOwnerId, selected, Zone.LIBRARY, null).withTapped(enterTapped))
                            .toList(),
                    List.of(new com.github.laxika.magicalvibes.model.BattlefieldEntryLibraryRemainder(
                            deckOwnerId, List.of(), true)));
            finishSearchAndResume(gameData);
            return;
        }

        if (followUp.basicLandSearchQueue() != null
                && followUp.basicLandSearchQueue().enterAfterAllSearches()
                && (librarySearch.decisionPlayerId() == null
                || librarySearch.decisionPlayerId().equals(playerId))) {
            handleDeferredBasicLandSearchChoice(gameData, activeSearch, followUp, cardIndex);
            return;
        }

        List<Card> deck = gameData.playerDecks.get(deckOwnerId);
        List<Card> sourceZone;
        if (sourceCards != null && (followUp.selectedCardFollowUp() != null
                || (destination == LibrarySearchDestination.EXILE_FACE_DOWN_AND_MAY_CAST_OR_PUT_INTO_HAND
                && reorderRemainingToBottom))) {
            sourceZone = sourceCards;
        } else {
            sourceZone = librarySearch.sourceSideboard()
                    ? com.github.laxika.magicalvibes.service.OutsideGameCards.view(gameData, deckOwnerId)
                    : deck;
        }

        if (sourceCards != null && followUp.selectedCardFollowUp() != null
                && followUp.selectedCardFollowUp().effect() instanceof
                com.github.laxika.magicalvibes.model.effect.MayPutSelectedCardOntoBattlefieldEffect) {
            Card chosenCard = cardIndex < 0 ? null : searchCards.get(cardIndex);
            if (chosenCard != null) {
                sourceCards.removeIf(card -> card.getId().equals(chosenCard.getId()));
                if (predicateEvaluationService.matchesCardPredicate(chosenCard,
                        followUp.selectedCardFollowUp().predicate(), null, gameData, playerId)) {
                    insertSelectedCardFollowUp(gameData, followUp, chosenCard, playerId);
                } else {
                    gameData.addCardToHand(handOwnerId, chosenCard);
                }
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " reveals ", chosenCard, "."));
            }
            List<Card> rest = new ArrayList<>(sourceCards);
            Collections.shuffle(rest);
            deck.addAll(rest);
            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.GIFTS_UNGIVEN_POOL
                && followUp.selectedCardFollowUp() != null
                && followUp.selectedCardFollowUp().d20BasicLandSearch() != null) {
            handleD20BasicLandSearchChoice(gameData, player, cardIndex, searchCards, deck,
                    remainingCount, followUp, filterPredicate, accumulatedCards);
            return;
        }

        if (reorderRemainingToBottom || reorderRemainingToTop || restToGraveyard || restToExile || restToHand
                || destination == LibrarySearchDestination.HOLD_OUT
                || destination == LibrarySearchDestination.HOLD_OUT_THEN_HAND
                || destination == LibrarySearchDestination.HOLD_OUT_THEN_BATTLEFIELD_OR_HAND
                || destination == LibrarySearchDestination.BATTLEFIELD_ONE_AND_PUT_REST_INTO_HAND) {
            if (sourceCards == null) {
                throw new IllegalStateException("Missing source cards for revealed-card choice");
            }

            if (destination == LibrarySearchDestination.HOLD_OUT_THEN_HAND
                    || destination == LibrarySearchDestination.HOLD_OUT_THEN_BATTLEFIELD_OR_HAND) {
                handleHeldCardsChoice(gameData, player, cardIndex, canFailToFind, searchCards, sourceCards,
                        accumulatedCards, followUp, destination, deckOwnerId);
                return;
            }

            if (destination == LibrarySearchDestination.HOLD_OUT) {
                handleHeldOutCardChoice(gameData, player, cardIndex, searchCards, sourceCards,
                        accumulatedCards, followUp);
                return;
            }

            if (destination == LibrarySearchDestination.BATTLEFIELD_ONE_AND_PUT_REST_INTO_HAND) {
                handleBattlefieldOneAndPutRestIntoHandChoice(
                        gameData, player, cardIndex, searchCards, sourceCards,
                        battlefieldIfChosenTapped, shuffleAfterSelection);
                return;
            }

            // Sunbird's Invocation: cast chosen card without paying, rest to bottom in random order
            if (destination == LibrarySearchDestination.CAST_WITHOUT_PAYING) {
                handleCastWithoutPayingChoice(gameData, player, cardIndex, canFailToFind,
                        searchCards, sourceCards, deck, deckOwnerId, grantHaste, sacrificeAtEndStep,
                        librarySearch.mayCastManaValueAtMost() == null
                                ? Integer.MAX_VALUE : librarySearch.mayCastManaValueAtMost(),
                        restToExile, reorderRemainingToTop && !reorderRemainingToBottom,
                        librarySearch.offeredFaces());
                return;
            }

            if (destination == LibrarySearchDestination.CAST_WITHOUT_PAYING_AND_SHUFFLE_LIBRARY) {
                handleCastWithoutPayingAndShuffleLibraryChoice(gameData, player, cardIndex,
                        canFailToFind, searchCards, sourceCards, deck, deckOwnerId);
                return;
            }

            if (destination == LibrarySearchDestination.DISCOVER) {
                handleDiscoverChoice(gameData, player, cardIndex, searchCards, sourceCards, deck,
                        librarySearch.discoverValue());
                return;
            }

            if (destination == LibrarySearchDestination.CAST_ONE_AND_PUT_OTHER_INTO_HAND) {
                handleCastOneAndPutOtherIntoHandChoice(
                        gameData, player, cardIndex, searchCards, sourceCards, deck);
                return;
            }

            if (destination == LibrarySearchDestination.CAST_ONE_AND_PUT_REST_INTO_HAND) {
                handleCastOneAndPutRestIntoHandChoice(
                        gameData, player, cardIndex, searchCards, sourceCards, deckOwnerId);
                return;
            }

            if (destination == LibrarySearchDestination.PUT_ONE_INTO_HAND_REST_TO_BOTTOM_RANDOM) {
                handlePutOneIntoHandRestToBottomRandom(
                        gameData, cardIndex, searchCards, sourceCards, deck, deckOwnerId);
                return;
            }

            if (destination == LibrarySearchDestination.EXILE_AND_MAY_CAST_WITHOUT_PAYING) {
                handleExileAndMayCastWithoutPayingChoice(gameData, player, cardIndex, canFailToFind,
                        searchCards, sourceCards, deck, deckOwnerId);
                return;
            }

            if (destination == LibrarySearchDestination.EXILE_FACE_DOWN_AND_MAY_CAST_OR_PUT_INTO_HAND
                    && reorderRemainingToBottom) {
                handleExileFaceDownAndMayCastOrPutIntoHandChoice(
                        gameData, player, cardIndex, canFailToFind, searchCards, sourceCards, deck,
                        deckOwnerId, filterCardTypes, librarySearch.mayCastManaValueAtMost());
                return;
            }

            // Aladdin's Lamp: keep the chosen card on top, rest to bottom in a random order, then draw it.
            if (destination == LibrarySearchDestination.DRAW_CHOSEN_REST_TO_BOTTOM_RANDOM) {
                handleDrawChosenRestToBottomRandom(gameData, cardIndex, searchCards, sourceCards, deck, deckOwnerId);
                return;
            }

            Card chosenCard = null;
            if (cardIndex == -1) {
                if (!canFailToFind) {
                    throw new IllegalStateException("Cannot fail to find with an unrestricted search");
                }
            } else {
                if (cardIndex < 0 || cardIndex >= searchCards.size()) {
                    throw new IllegalStateException("Invalid card index: " + cardIndex);
                }
                chosenCard = searchCards.get(cardIndex);
                if (destination == LibrarySearchDestination.BATTLEFIELD && chosenCard.isAura()
                        && !placeBattlefieldCardsSimultaneously && remainingCount == 1
                        && !reorderRemainingToBottom && !restToGraveyard && !restToExile) {
                    battlefieldEntryBatchSupport.begin(gameData, List.of(
                            new com.github.laxika.magicalvibes.model.BattlefieldEntryCard(
                                    battlefieldControllerId, deckOwnerId, chosenCard, Zone.LIBRARY, null)));
                    if (shuffleAfterSelection) {
                        LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                    }
                    if (!gameData.interaction.isAwaitingInput()) {
                        finishSearchAndResume(gameData);
                    }
                    return;
                }
                boolean auraStayedInSourceCards = false;
                if (destination == LibrarySearchDestination.EXILE_PLAYABLE_REST_TO_BOTTOM_RANDOM) {
                    exileService.exileCard(gameData, deckOwnerId, chosenCard);
                    gameData.grantExilePlayPermission(chosenCard.getId(), playerId, Duration.UNTIL_END_OF_TURN);
                    if (librarySearch.withoutPayingManaCost()) {
                        gameData.setExilePlayWithoutPayingManaCost(chosenCard.getId(), true);
                    }
                } else if (destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM
                        || destination == LibrarySearchDestination.EXILE_TWO_FACE_DOWN_REST_TO_BOTTOM_RANDOM
                        || destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM
                        || destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_WITH_HATCHING_COUNTER_REST_TO_BOTTOM
                        || destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD) {
                    exileService.exileCardFaceDown(gameData, deckOwnerId, chosenCard,
                            librarySearch.sourcePermanentId(), playerId);
                    if (destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_WITH_HATCHING_COUNTER_REST_TO_BOTTOM) {
                        gameData.addExiledCardCounters(chosenCard.getId(), CounterType.HATCHING, 1);
                        gameData.grantExileLookPermission(chosenCard.getId(), playerId);
                    }
                    if (followUp != null && followUp.imprintSourcePermanentId() != null) {
                        exileService.setImprintedCardOnPermanent(gameData,
                                followUp.imprintSourcePermanentId(), chosenCard);
                    }
                    if (librarySearch.grantExilePlayPermission()) {
                        gameData.grantExilePlayPermission(chosenCard.getId(), playerId, Duration.PERMANENT);
                        if (destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD) {
                            // The permission is to cast the card, not to play it, so an exiled land stays unplayable.
                            gameData.updateExilePlayPermission(chosenCard.getId(), granted -> granted.withSpellFilter(
                                    new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
                        }
                        if (librarySearch.exileManaSpending().isAny()) {
                            gameData.updateExilePlayPermission(chosenCard.getId(),
                                    granted -> granted.withManaSpending(librarySearch.exileManaSpending()));
                        }
                        if (librarySearch.exilePlayPermissionFilter() != null) {
                            gameData.updateExilePlayPermission(chosenCard.getId(), granted -> granted.withSpellFilter(librarySearch.exilePlayPermissionFilter()));
                        }
                    }
                } else if (destination == LibrarySearchDestination.EXILE_IMPRINT) {
                    exileService.exileCardFaceDown(gameData, playerId, chosenCard, null);
                    UUID sourcePermanentId = followUp.imprintSourcePermanentId();
                    if (sourcePermanentId != null) {
                        exileService.setImprintedCardOnPermanent(gameData, sourcePermanentId, chosenCard);
                    }
                } else if (destination == LibrarySearchDestination.EXILE) {
                    exileService.exileCard(gameData, deckOwnerId, chosenCard);
                    if (gameData.pendingEffectResolutionEntry != null) {
                        gameData.pendingEffectResolutionEntry.addToStep(StepKind.EXILE,
                                StepOutcome.ofObjects(List.of(chosenCard.getId()), List.of(deckOwnerId)));
                    }
                } else if (toGraveyard) {
                    if (remainingCount > 1 || !accumulatedCards.isEmpty()) {
                        accumulatedCards.add(chosenCard);
                    } else {
                        graveyardService.addCardToGraveyard(gameData, deckOwnerId, chosenCard, Zone.LIBRARY);
                    }
                } else if (toBattlefield && !placeBattlefieldCardsSimultaneously
                        && destination == LibrarySearchDestination.BATTLEFIELD && chosenCard.isAura()
                        && !toBattlefieldTapped && !grantHaste && !returnToHandAtEndStep && !exileAtEndStep
                        && battlefieldCounter == null && enterWithCounters == null
                        && gameData.findExiledCard(chosenCard.getId()) == null) {
                    // CR 303.4f: the Aura's host is chosen as it enters. The revealed card is put back so
                    // the entry removes it from the library; one that can't enter stays among the rest.
                    UUID auraId = chosenCard.getId();
                    deck.add(chosenCard);
                    battlefieldEntryBatchSupport.begin(gameData, List.of(
                            new com.github.laxika.magicalvibes.model.BattlefieldEntryCard(
                                    battlefieldControllerId, deckOwnerId, chosenCard, Zone.LIBRARY, null)));
                    if (!gameData.interaction.isAwaitingInput()) {
                        auraStayedInSourceCards = deck.removeIf(deckCard -> deckCard.getId().equals(auraId));
                    }
                } else if (toBattlefield && !placeBattlefieldCardsSimultaneously) {
                    boolean selectedFromExile = gameData.findExiledCard(chosenCard.getId()) != null;
                    if (selectedFromExile) {
                        gameData.removeFromExile(chosenCard.getId());
                    }
                    Permanent perm = new Permanent(chosenCard, selectedFromExile ? Zone.EXILE : Zone.LIBRARY);
                    var landEquilibriumPlan = playerId.equals(battlefieldControllerId) ? null
                            : landEquilibriumSupport.findPlan(gameData, playerId, perm);
                    if (grantHaste) {
                        gameData.addEffectOnPermanent(null, null, battlefieldControllerId, new GrantKeywordEffect(Keyword.HASTE, Subjects.target()), perm.getId(), Duration.UNTIL_END_OF_TURN);
                    }
                    if (toBattlefieldTapped) perm.tap();
                    Runnable placeCard = () -> battlefieldEntryService.putPermanentOntoBattlefield(
                            gameData, battlefieldControllerId, perm,
                            battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of(), enterWithCounters);
                    if (playerId.equals(battlefieldControllerId)) {
                        placeCard.run();
                    } else {
                        landEquilibriumSupport.runWithPutterOtherThanController(placeCard);
                    }
                    landEquilibriumSupport.applyPlan(gameData, playerId, perm, landEquilibriumPlan, null);
                    placeBattlefieldCounter(gameData, perm, battlefieldCounter);
                    if (gameData.pendingEffectResolutionEntry != null) {
                        gameData.pendingEffectResolutionEntry.setChosenPermanentId(perm.getId());
                    }
                    if (returnToHandAtEndStep) {
                        if (librarySearch.returnToHandAtControllerEndStepId() != null) {
                            gameData.grantAbilityToPermanent(FaceDownVisibility.publicName(gameData, perm), librarySearch.sourcePermanentId(),
                                    playerId, new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU),
                                            com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect.self()),
                                    perm.getId(), com.github.laxika.magicalvibes.model.effect.Duration.PERMANENT);
                        } else {
                            // The delayed trigger belongs to the ability that searched (e.g. Nahiri, the
                            // Harbinger's -8), not to the fetched permanent, even after control changes.
                            Card searchSourceCard = delayedTriggerSourceCard(gameData, librarySearch);
                            DelayedPermanentActions.schedule(gameData, playerId,
                                    searchSourceCard != null ? searchSourceCard : perm.getCard(),
                                    searchSourceCard != null ? librarySearch.sourcePermanentId() : perm.getId(),
                                    new DelayedPermanentAction(perm.getId(),
                                            DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP),
                                    ActivePlayer.ANY);
                        }
                    } else if (exileAtEndStep) {
                        // CR 603.7e: the delayed exile is controlled by the searching ability's controller as it
                        // resolved and keeps that ability's source, even if the fetched permanent changes control.
                        Card searchSourceCard = delayedTriggerSourceCard(gameData, librarySearch);
                        DelayedPermanentActions.schedule(gameData, playerId,
                                searchSourceCard != null ? searchSourceCard : perm.getCard(),
                                searchSourceCard != null ? librarySearch.sourcePermanentId() : perm.getId(),
                                new DelayedPermanentAction(perm.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP),
                                ActivePlayer.ANY);
                    }

                    if (destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PLAYER && librarySearch.attachToPlayerId() != null) {
                        perm.setAttachedTo(librarySearch.attachToPlayerId());
                    }
                    if (destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT && librarySearch.attachToPermanentId() != null) {
                        perm.setAttachedTo(librarySearch.attachToPermanentId());
                        attachmentTriggers.equipmentBecameAttached(
                                gameData, perm, librarySearch.attachToPermanentId());
                    }
                    if (!perm.getCard().isAura()) {
                        attachmentTriggers.equipmentBecameAttached(gameData, perm, null);
                    }
                    if (chosenCard.hasType(CardType.CREATURE)) {
                        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, battlefieldControllerId, chosenCard, null, false);
                    }
                    if (chosenCard.hasType(CardType.PLANESWALKER) && chosenCard.getLoyalty() != null) {
                        int loyalty = gameQueryService.replaceCounters(gameData, perm,
                                CounterType.LOYALTY, chosenCard.getLoyalty());
                        perm.setCounterCount(CounterType.LOYALTY, loyalty);
                        perm.setSummoningSick(false);
                    }
                    if (!gameData.interaction.isAwaitingInput()) {
                        legendRuleService.checkLegendRule(gameData, battlefieldControllerId);
                    }
                } else if (destination == LibrarySearchDestination.TOP_OF_LIBRARY) {
                    // Chosen card stays on top; the rest are reordered to the bottom below
                    // (Cream of the Crop). No shuffle — unlike the non-reorder TOP_OF_LIBRARY path.
                    addCardAtLibraryPosition(deck, chosenCard, topLibraryPosition);
                } else if (!toBattlefield) {
                    addCardFromLibraryToHand(gameData, handOwnerId, chosenCard, deckOwnerId, sourceZone == deck);
                }
                for (int i = 0; i < sourceCards.size() && !auraStayedInSourceCards; i++) {
                    if (sourceCards.get(i).getId().equals(chosenCard.getId())) {
                        sourceCards.remove(i);
                        break;
                    }
                }
                if (librarySearch.sourceSideboard()) {
                    gameData.spendPlayFromOutsideGame(chosenCard.getId());
                }
            }

            // Log the result
            if (toBattlefield) {
                if (!placeBattlefieldCardsSimultaneously) {
                    if (chosenCard == null) {
                        gameLogService.append(gameData, GameLog.text(player.getUsername() + " puts no card onto the battlefield."));
                    } else {
                        gameLogService.append(gameData,
                                GameLog.entersBattlefieldUnder(chosenCard, player.getUsername()));
                    }
                }
            } else {
                GameLogEntry logEntry;
                if (destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM) {
                    logEntry = GameLog.text(player.getUsername()
                            + " exiles a card face down and puts the rest on the bottom of the library in a random order.");
                } else if (destination == LibrarySearchDestination.EXILE_TWO_FACE_DOWN_REST_TO_BOTTOM_RANDOM) {
                    logEntry = GameLog.text(player.getUsername()
                            + " exiles two cards face down and puts the rest on the bottom of the library in a random order.");
                } else if (destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM) {
                    logEntry = GameLog.text(player.getUsername()
                            + " exiles a card face down and puts the rest on the bottom of the library in any order.");
                } else if (destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_WITH_HATCHING_COUNTER_REST_TO_BOTTOM) {
                    logEntry = GameLog.text(player.getUsername()
                            + " exiles a card face down with a hatching counter and puts the rest on the bottom of the library in any order.");
                } else if (destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD) {
                    logEntry = GameLog.text(player.getUsername()
                            + " exiles a card face down and puts the rest into the graveyard.");
                } else if (destination == LibrarySearchDestination.EXILE_PLAYABLE_REST_TO_BOTTOM_RANDOM) {
                    logEntry = chosenCard == null
                            ? GameLog.text(player.getUsername() + " does not exile a card.")
                            : GameLog.textCardText(player.getUsername() + " exiles ", chosenCard,
                                    " and may play it this turn; the rest are put on the bottom of the library in a random order.");
                } else if (destination == LibrarySearchDestination.EXILE_IMPRINT) {
                    logEntry = chosenCard == null
                            ? GameLog.text(player.getUsername() + "'s imprint ability does nothing.")
                            : GameLog.text(player.getUsername() + " exiles a card face down.");
                } else if (destination == LibrarySearchDestination.EXILE) {
                    logEntry = chosenCard == null
                            ? GameLog.text(player.getUsername() + " does not exile a card.")
                            : GameLog.textCardText(player.getUsername() + " exiles ", chosenCard, ".");
                } else if (toGraveyard) {
                    logEntry = chosenCard == null
                            ? GameLog.text(player.getUsername() + " does not put a card into the graveyard.")
                            : GameLog.textCardText(player.getUsername() + " puts ", chosenCard, " into the graveyard.");
                } else if (destination == LibrarySearchDestination.TOP_OF_LIBRARY) {
                    logEntry = chosenCard == null
                            ? GameLog.text(player.getUsername() + " puts no card on top of their library.")
                            : GameLog.text(player.getUsername() + " puts a card on top of their library.");
                } else {
                    logEntry = chosenCard == null
                            ? GameLog.text(player.getUsername() + " does not reveal a card.")
                            : librarySearch.reveals()
                            ? GameLog.textCardText(player.getUsername() + " reveals ", chosenCard, " and puts it into their hand.")
                            : GameLog.text(player.getUsername() + " puts a card into their hand.");
                }
                gameLogService.append(gameData, logEntry);
            }

            if (toBattlefield && placeBattlefieldCardsSimultaneously && chosenCard != null) {
                accumulatedCards.add(chosenCard);
            }

            // If ETB or legend rule caused awaiting input, defer remaining card reorder
            if (gameData.interaction.isAwaitingInput()) {
                if (restToHand) {
                    for (Card card : new ArrayList<>(sourceCards)) {
                        gameData.addCardToHand(deckOwnerId, card);
                    }
                } else if (!sourceCards.isEmpty()) {
                    if (toBattlefield && reorderRemainingToBottom && sourceCards.size() > 1
                            && gameData.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
                        // The rest go to the bottom while the spell resolves, before the entering
                        // permanent's triggered ability chooses its targets: order them first and
                        // return to the target prompt afterwards.
                        gameData.interaction.openNestedPromptWindow();
                        try {
                            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                                    playerId, new ArrayList<>(sourceCards), true, deckOwnerId,
                                    "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
                        } finally {
                            gameData.interaction.closeNestedPromptWindow();
                        }
                    } else {
                        gameData.pendingLibraryBottomReorders.addLast(
                                new LibraryBottomReorderRequest(deckOwnerId, new ArrayList<>(sourceCards)));
                    }
                }
                return;
            }

            if (toGraveyard && !accumulatedCards.isEmpty()) {
                if (remainingCount > 1 && !sourceCards.isEmpty()) {
                    beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                            LibrarySearchParams.builder(playerId, new ArrayList<>(sourceCards))
                                    .reveals(reveals)
                                    .canFailToFind(false)
                                    .targetPlayerId(deckOwnerId)
                                    .remainingCount(remainingCount - 1)
                                    .sourceCards(new ArrayList<>(sourceCards))
                                    .accumulatedCards(accumulatedCards)
                                    .reorderRemainingToTop(reorderRemainingToTop)
                                    .shuffleAfterSelection(false)
                                    .prompt(librarySearch.prompt())
                                    .destination(destination)
                                    .build(), librarySearch.prompt(), false));
                    return;
                }
                graveyardService.addCardsFromLibraryToGraveyard(gameData, deckOwnerId, accumulatedCards);
                accumulatedCards.clear();
            }

            if (destination == LibrarySearchDestination.EXILE_TWO_FACE_DOWN_REST_TO_BOTTOM_RANDOM
                    && chosenCard != null && remainingCount > 1 && !sourceCards.isEmpty()) {
                String prompt = "Exile another card face down. Put the rest on the bottom of that library in a random order.";
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(playerId, new ArrayList<>(sourceCards))
                                .canFailToFind(false)
                                .targetPlayerId(deckOwnerId)
                                .remainingCount(remainingCount - 1)
                                .sourceCards(new ArrayList<>(sourceCards))
                                .reorderRemainingToBottom(true)
                                .shuffleAfterSelection(false)
                                .prompt(prompt)
                                .destination(destination)
                                .sourcePermanentId(librarySearch.sourcePermanentId())
                                .build(),
                        prompt,
                        false));
                return;
            }

            if (destination == LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM
                    || destination == LibrarySearchDestination.EXILE_TWO_FACE_DOWN_REST_TO_BOTTOM_RANDOM
                    || destination == LibrarySearchDestination.EXILE_PLAYABLE_REST_TO_BOTTOM_RANDOM) {
                Collections.shuffle(sourceCards);
                deck.addAll(sourceCards);
                finishSearchAndResume(gameData);
                return;
            }

            // "Exile any number of them" (Ancestral Knowledge): re-open the same pick over what is
            // left until the controller declines, which then falls through to the reorder-to-top
            // disposal below.
            if (librarySearch.repeatUntilDecline() && chosenCard != null && !sourceCards.isEmpty()) {
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        librarySearch.withCards(new ArrayList<>(sourceCards)),
                        librarySearch.prompt(),
                        true));
                return;
            }

            if (destination == LibrarySearchDestination.TOP_OF_LIBRARY
                    && chosenCard != null && remainingCount > 1 && !sourceCards.isEmpty()) {
                String prompt = "You may put another card on top of your library. The rest go into your graveyard.";
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(playerId, new ArrayList<>(sourceCards))
                                .canFailToFind(true)
                                .remainingCount(remainingCount - 1)
                                .sourceCards(new ArrayList<>(sourceCards))
                                .restToGraveyard(true)
                                .shuffleAfterSelection(false)
                                .prompt(prompt)
                                .destination(destination)
                                .build(),
                        prompt,
                        true));
                return;
            }

            // Two-bounded-pick (Gift of the Gargantuan / Benefaction of Rhonas): after the first
            // pick, run the second-type pick over the same looked-at cards before disposing the rest.
            if (followUp.secondBoundedPick() != null
                    && startSecondBoundedPick(gameData, deckOwnerId, sourceCards, accumulatedCards,
                            followUp.secondBoundedPick())) {
                return;
            }

            if (toBattlefield && placeBattlefieldCardsSimultaneously && !accumulatedCards.isEmpty()) {
                placeCardsOnBattlefieldSimultaneously(gameData, accumulatedCards, handOwnerId,
                        toBattlefieldTapped, grantHaste, exileAtEndStep,
                        returnToHandAtEndStep, animateFound, battlefieldCounter, enterWithCounters);
                accumulatedCards.clear();
            }

            if (followUp.secondBoundedPick() != null
                    && followUp.secondBoundedPick().randomRest()) {
                insertSelectedCardFollowUp(gameData, followUp, chosenCard, playerId);
                Collections.shuffle(sourceCards);
                deck.addAll(sourceCards);
                gameLogService.append(gameData, GameLog.text(player.getUsername()
                        + " puts the unchosen cards on the bottom of their library in a random order."));
                finishSearchAndResume(gameData);
                return;
            }

            if (restToExile) {
                if (!sourceCards.isEmpty()) {
                    GameLog.Builder exileLog = GameLog.builder().text(player.getUsername() + " exiles ");
                    for (int i = 0; i < sourceCards.size(); i++) {
                        if (i > 0) {
                            exileLog.text(", ");
                        }
                        exileLog.card(sourceCards.get(i));
                    }
                    exileLog.text(".");
                    for (Card card : new ArrayList<>(sourceCards)) {
                        if (gameData.findExiledCard(card.getId()) == null) {
                            exileService.exileCard(gameData, deckOwnerId, card);
                        }
                    }
                    gameLogService.append(gameData, exileLog.build());
                }
                finishSearchAndResume(gameData);
                return;
            }

            if (restToHand) {
                for (Card card : new ArrayList<>(sourceCards)) {
                    gameData.addCardToHand(deckOwnerId, card);
                }
                if (!sourceCards.isEmpty()) {
                    gameLogService.append(gameData, GameLog.text(player.getUsername()
                            + " puts the rest into their hand."));
                }
                finishSearchAndResume(gameData);
                return;
            }

            if (restToGraveyard) {
                if (!sourceCards.isEmpty()) {
                    GameLog.Builder graveyardLog = GameLog.builder().text(player.getUsername() + " puts ");
                    for (int i = 0; i < sourceCards.size(); i++) {
                        if (i > 0) {
                            graveyardLog.text(", ");
                        }
                        graveyardLog.card(sourceCards.get(i));
                    }
                    graveyardLog.text(" into their graveyard.");
                    graveyardService.addCardsFromLibraryToGraveyard(gameData, deckOwnerId, new ArrayList<>(sourceCards));
                    gameLogService.append(gameData, graveyardLog.build());
                }
                finishSearchAndResume(gameData);
                return;
            }

            if (sourceCards.size() > 1) {
                boolean toBottom = !reorderRemainingToTop;
                UUID reorderPlayerId = reorderRemainingToTop ? playerId : deckOwnerId;
                String reorderPrompt = toBottom
                        ? "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."
                        : "Put these cards back on top of your library in any order (top to bottom).";
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                        reorderPlayerId, sourceCards, toBottom, deckOwnerId, reorderPrompt));
                return;
            } else if (sourceCards.size() == 1) {
                if (reorderRemainingToTop) {
                    deck.addFirst(sourceCards.getFirst());
                } else {
                    deck.add(sourceCards.getFirst());
                }
            }

            if (toBattlefield) {
                performStateBasedActionsIfResolutionComplete(gameData);
            }

            if (chosenCard != null) {
                insertSelectedCardFollowUp(gameData, followUp, chosenCard, playerId);
            }
            finishSearchAndResume(gameData);
            return;
        }

        if (cardIndex == -1) {
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
            if (destination == LibrarySearchDestination.EXILE_FOR_FREE_CAST) {
                resolveEachPlayerNonlandExilePick(gameData, player, deckOwnerId, null);
                return;
            }
            // Gifts Ungiven stopped short of four cards: the pool found so far still goes to the
            // opponent for the two-card disposal choice.
            if (destination == LibrarySearchDestination.GIFTS_UNGIVEN_POOL
                    || destination == LibrarySearchDestination.THREATS_UNDETECTED_POOL) {
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " stops searching."
                                + (destination == LibrarySearchDestination.GIFTS_UNGIVEN_POOL
                                ? " Library is shuffled." : "")));
                finishGiftsUngivenSearch(gameData, playerId, accumulatedCards);
                return;
            }
            // Signal the Clans stopped short of three creature cards: fewer than three revealed
            // cards can never satisfy "three cards with different names", so nothing goes to hand.
            if (destination == LibrarySearchDestination.SIGNAL_THE_CLANS_POOL) {
                gameLogService.append(gameData, GameLog.text(player.getUsername() + " stops searching."));
                finishSignalTheClansSearch(gameData, playerId, accumulatedCards);
                return;
            }
            if (destination == LibrarySearchDestination.EXILE_PLAYABLE_ANY_NUMBER) {
                if (shuffleAfterSelection) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                }
                gameLogService.append(gameData, GameLog.text(player.getUsername()
                        + " stops searching. Library is shuffled."));
                finishSearchAndResume(gameData);
                return;
            }
            if (destination == LibrarySearchDestination.EXILE_FOR_MAY_CAST) {
                if (shuffleAfterSelection) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                }
                gameLogService.append(gameData, GameLog.text(player.getUsername()
                        + " stops searching. Library is shuffled."));
                finishSearchAndResume(gameData);
                return;
            }
            if (destination == LibrarySearchDestination.EXILE_WITH_SOURCE) {
                if (shuffleAfterSelection) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                    gameLogService.append(gameData, GameLog.text(player.getUsername()
                            + " stops searching. " + player.getUsername() + "'s library is shuffled."));
                }
                finishSearchAndResume(gameData);
                return;
            }
            // A pile search stopped early still shuffles whatever was already exiled into it.
            if (destination == LibrarySearchDestination.EXILE_FACE_DOWN_PILE) {
                gameData.shuffleExilePile(librarySearch.sourcePermanentId());
            }
            if (followUp.secondBoundedPick() != null
                    && startSecondBoundedPick(gameData, deckOwnerId,
                            new ArrayList<>(librarySearch.sourceCards()), accumulatedCards,
                            followUp.secondBoundedPick())) {
                return;
            }
            if (followUp.secondBoundedPick() != null
                    && followUp.secondBoundedPick().randomRest()
                    && librarySearch.sourceCards() != null) {
                List<Card> remainingCards = new ArrayList<>(librarySearch.sourceCards());
                Collections.shuffle(remainingCards);
                deck.addAll(remainingCards);
                gameLogService.append(gameData, GameLog.text(player.getUsername()
                        + " puts the unchosen cards on the bottom of their library in a random order."));
                finishSearchAndResume(gameData);
                return;
            }
            // Cards found under Opposition Agent were exiled as they were found, not accumulated for the destination.
            if (librarySearch.decisionPlayerId() != null && !librarySearch.decisionPlayerId().equals(playerId)) {
                accumulatedCards.clear();
            }
            // CR 608.2f: Place any accumulated battlefield cards before finishing
            if (!accumulatedCards.isEmpty() && toBattlefield) {
                placeCardsOnBattlefieldSimultaneously(gameData, accumulatedCards, handOwnerId, toBattlefieldTapped,
                        grantHaste, exileAtEndStep, returnToHandAtEndStep, animateFound,
                        battlefieldCounter, enterWithCounters);
                accumulatedCards.clear();
            }
            if (!accumulatedCards.isEmpty() && toGraveyard) {
                graveyardService.addCardsFromLibraryToGraveyard(gameData, deckOwnerId, accumulatedCards);
                accumulatedCards.clear();
            }
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            String logEntry = shuffleAfterSelection
                    ? player.getUsername() + " chooses not to take a card. Library is shuffled."
                    : player.getUsername() + " chooses not to take a card.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            if (shuffleAfterSelection && !deckOwnerId.equals(playerId)) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(deckOwnerId) + "'s library is shuffled."));
            }
            log.info("Game {} - {} declines to take a card from library", gameData.id, player.getUsername());
            insertNoCardFollowUp(gameData, followUp);
            // Per ruling: if you find only one basic land with Cultivate, it must go to
            // the battlefield tapped — skipping the battlefield pick means finding zero,
            // so drop the pending hand search and shuffle.
            // Same for hand-then-graveyard (Jarad's Orders): declining the hand pick finds zero,
            // so drop the pending graveyard pick and shuffle.
            if (followUp.basicLandToHand() != null || followUp.cardToGraveyard() != null) {
                if (!shuffleAfterSelection) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                    gameLogService.append(gameData, GameLog.text("Library is shuffled."));
                }
            }
            if (followUp.basicLandToHand() != null && !followUp.basicLandToHand().basicOnly()
                    && followUp.basicLandToHand().subtype() != null) {
                finishSearchAndResume(gameData);
                return;
            }
            if (librarySearchSupport.startNextTargetPlayerTopSearch(gameData, followUp)) return;
            if (librarySearchSupport.startNextSameNamePick(gameData, playerId, followUp)) return;
            if (startNextToHandPick(gameData, playerId, followUp, librarySearch.shuffleAfterSelection())) return;
            if (basicLandSearchQueueSupport.advance(gameData, followUp)) return;
            finishSearchAndResume(gameData);
            return;
        }

        if (cardIndex < 0 || cardIndex >= searchCards.size()) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        Card chosenCard = searchCards.get(cardIndex);

        boolean selectedFromGraveyard = (destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT
                || destination == LibrarySearchDestination.BATTLEFIELD
                || destination == LibrarySearchDestination.HAND)
                && gameData.playerGraveyards.getOrDefault(deckOwnerId, List.of()).contains(chosenCard);
        boolean selectedFromHand = destination == LibrarySearchDestination.BATTLEFIELD
                && !selectedFromGraveyard
                && gameData.playerHands.getOrDefault(deckOwnerId, List.of()).contains(chosenCard);
        if (selectedFromGraveyard) {
            sourceZone = gameData.playerGraveyards.get(deckOwnerId);
        } else if (selectedFromHand) {
            sourceZone = gameData.playerHands.get(deckOwnerId);
        }

        if (librarySearch.decisionPlayerId() != null
                && !librarySearch.decisionPlayerId().equals(playerId)) {
            handleOppositionAgentChoice(gameData, player, activeSearch, chosenCard, sourceZone, deck, deckOwnerId);
            return;
        }

        boolean putChosenCardOnBattlefield = destination == LibrarySearchDestination.HAND
                && battlefieldIfChosenBeholdType != null
                && chosenCard.hasType(CardType.CREATURE)
                && predicateEvaluationService.matchesCardPredicate(chosenCard,
                new CardSubtypePredicate(battlefieldIfChosenBeholdType), chosenCard.getId(),
                gameData, playerId);
        if (putChosenCardOnBattlefield) {
            toBattlefield = true;
            toBattlefieldTapped = false;
        }

        if (toBattlefield && !chosenCard.getType().isPermanentType()) {
            // CR 304.4/307.4: an instant or sorcery instructed to enter stays in its current zone.
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            finishSearchAndResume(gameData);
            return;
        }
        recordFoundCard(gameData, chosenCard, playerId,
                destination == LibrarySearchDestination.HAND ? handOwnerId : deckOwnerId,
                toBattlefield || putChosenCardOnBattlefield ? null : destination);
        if (destination == LibrarySearchDestination.BATTLEFIELD && chosenCard.isAura()
                && !placeBattlefieldCardsSimultaneously && remainingCount == 1) {
            battlefieldEntryBatchSupport.begin(gameData, List.of(
                    new com.github.laxika.magicalvibes.model.BattlefieldEntryCard(
                            battlefieldControllerId, deckOwnerId, chosenCard, Zone.LIBRARY, null)));
            if (shuffleAfterSelection) LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            if (!gameData.interaction.isAwaitingInput()) finishSearchAndResume(gameData);
            return;
        }
        boolean removed = false;
        for (int i = 0; i < sourceZone.size(); i++) {
            if (sourceZone.get(i).getId().equals(chosenCard.getId())) {
                sourceZone.remove(i);
                removed = true;
                break;
            }
        }

        if (requireDifferentNames) {
            excludedCardNames.add(chosenCard.getName());
        }

        if (!removed) {
            throw new IllegalStateException("Chosen card not found in library");
        }
        if (selectedFromGraveyard) {
            graveyardService.notifyCardsLeftGraveyard(gameData, deckOwnerId, chosenCard);
        }
        if (librarySearch.sourceSideboard()) {
            gameData.spendPlayFromOutsideGame(chosenCard.getId());
        }

        if (destination == LibrarySearchDestination.EXILE_FACE_DOWN_AND_MAY_CAST_OR_PUT_INTO_HAND) {
            exileService.exileCardFaceDown(gameData, deckOwnerId, chosenCard, null);
            if (sourceCards != null && reorderRemainingToBottom) {
                Collections.shuffle(sourceCards);
                deck.addAll(sourceCards);
            } else if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }

            Integer maxManaValue = librarySearch.mayCastManaValueAtMost();
            boolean mayCast = canCastSearchedFace(chosenCard, filterCardTypes, maxManaValue);
            if (mayCast) {
                Card sourceCard = gameData.pendingEffectResolutionEntry != null
                        ? gameData.pendingEffectResolutionEntry.getCard() : chosenCard;
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        sourceCard,
                        playerId,
                        List.of(new MayCastExiledCardWithoutPayingManaCostOrPutIntoHandEffect(maxManaValue)),
                        "Cast " + chosenCard.getName()
                                + " without paying its mana cost? If you don't, put it into your hand.",
                        chosenCard.getId()));
            } else {
                gameData.removeFromExile(chosenCard.getId());
                gameData.addCardToHand(deckOwnerId, chosenCard);
            }

            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " exiles a card face down", chosenCard,
                    mayCast ? "." : " and puts it into their hand."));
            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.HAND_OR_GRAVEYARD) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " reveals ", chosenCard, "."));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearchDestinationChoice(
                    playerId, chosenCard));
            return;
        }

        if (destination == LibrarySearchDestination.HAND
                && battlefieldIfOpponentControlsMoreLands
                && gameQueryService.anyOpponentControlsMoreLands(gameData, playerId)) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " reveals ", chosenCard, "."));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearchDestinationChoice(
                    playerId, chosenCard, true));
            return;
        }

        if (destination == LibrarySearchDestination.EXILE_PLAYABLE_ANY_NUMBER) {
            exileService.exileCard(gameData, deckOwnerId, chosenCard);
            com.github.laxika.magicalvibes.service.effect.normalfx
                    .ExileMatchingCardsFromGraveyardAndLibrarySupport.grantCastPermission(
                            gameData, playerId, chosenCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " exiles ", chosenCard, "."));

            List<Card> remainingMatches = filterPredicate == null
                    ? new ArrayList<>(deck)
                    : deck.stream().filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, filterPredicate, null, gameData, deckOwnerId)).toList();
            if (!remainingMatches.isEmpty()) {
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(playerId, new ArrayList<>(remainingMatches))
                                .remainingCount(remainingMatches.size())
                                .canFailToFind(true)
                                .destination(LibrarySearchDestination.EXILE_PLAYABLE_ANY_NUMBER)
                                .filterPredicate(filterPredicate)
                                .build(),
                        "Search your library for matching cards to exile (any number).", true));
                return;
            }

            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.GIFTS_UNGIVEN_POOL
                || destination == LibrarySearchDestination.THREATS_UNDETECTED_POOL) {
            // Gifts-style searches hold revealed cards outside every zone until the opponent has
            // chosen the cards for the effect's configured destination.
            accumulatedCards.add(chosenCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " reveals ", chosenCard, "."));

            java.util.Set<String> excluded = java.util.Set.copyOf(excludedCardNames);
            List<Card> remainingMatches = deck.stream()
                    .filter(c -> !excluded.contains(c.getName()))
                    .filter(c -> !requireDifferentPowers || accumulatedCards.stream()
                            .noneMatch(selected -> java.util.Objects.equals(
                                    searchPower(gameData, selected, deckOwnerId),
                                    searchPower(gameData, c, deckOwnerId))))
                    .filter(c -> filterPredicate == null || predicateEvaluationService.matchesCardPredicate(
                            c, filterPredicate, null, gameData, deckOwnerId))
                    .toList();
            if (remainingCount > 1 && !remainingMatches.isEmpty()) {
                int newRemaining = remainingCount - 1;
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(playerId, new ArrayList<>(remainingMatches))
                                .remainingCount(newRemaining)
                                .reveals(true)
                                .canFailToFind(true)
                                .destination(destination)
                                .filterPredicate(filterPredicate)
                                .requireDifferentNames(requireDifferentNames)
                                .requireDifferentPowers(requireDifferentPowers)
                                .accumulatedCards(accumulatedCards)
                                .excludedCardNames(excludedCardNames)
                                .build(),
                        "Search your library for a card with a different "
                                + (requireDifferentPowers ? "power" : "name") + " to reveal ("
                                + newRemaining + " remaining).", true));
                return;
            }

            gameLogService.append(gameData, GameLog.text(player.getUsername() + "'s library is shuffled."));
            finishGiftsUngivenSearch(gameData, playerId, accumulatedCards);
            return;
        }

        if (destination == LibrarySearchDestination.SIGNAL_THE_CLANS_POOL) {
            // Signal the Clans: the revealed card leaves the library but enters no zone — the pool
            // is only resolved once all three picks are made (or the searcher stops short).
            accumulatedCards.add(chosenCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " reveals ", chosenCard, "."));

            List<Card> remainingCreatures = deck.stream()
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .toList();
            if (remainingCount > 1 && !remainingCreatures.isEmpty()) {
                int newRemaining = remainingCount - 1;
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(playerId, new ArrayList<>(remainingCreatures))
                                .remainingCount(newRemaining)
                                .reveals(true)
                                .canFailToFind(true)
                                .destination(LibrarySearchDestination.SIGNAL_THE_CLANS_POOL)
                                .accumulatedCards(accumulatedCards)
                                .build(),
                        "Search your library for a creature card to reveal ("
                                + newRemaining + " remaining).", true));
                return;
            }

            finishSignalTheClansSearch(gameData, playerId, accumulatedCards);
            return;
        }

        if (destination == LibrarySearchDestination.BATTLEFIELD_UNDER_SEARCHER) {
            // Bribery: the chosen card is put onto the battlefield under the SEARCHER's control
            // (not the searched library's owner), then that player shuffles.
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, chosenCard, Zone.LIBRARY)) {
                deck.add(chosenCard);
                gameLogService.append(gameData, GameLog.cardThen(chosenCard,
                        " can't enter the battlefield from a library; it stays in the library."));
            } else {
                Permanent perm = new Permanent(chosenCard, Zone.LIBRARY);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, perm,
                            battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of(), enterWithCounters);
                if (chosenCard.hasType(CardType.CREATURE)) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, chosenCard, null, false);
                }
                if (!gameData.interaction.isAwaitingInput()) {
                    legendRuleService.checkLegendRule(gameData, playerId);
                }
                gameLogService.append(gameData,
                        GameLog.entersBattlefieldUnder(chosenCard, player.getUsername()));
            }

            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                if (targetPlayerId != null) {
                    String targetName = gameData.playerIdToName.get(targetPlayerId);
                    gameLogService.append(gameData, GameLog.text(targetName + "'s library is shuffled."));
                }
            }
            log.info("Game {} - {} puts {} onto the battlefield under their control from a library search",
                    gameData.id, player.getUsername(), chosenCard.getName());
            performStateBasedActionsIfResolutionComplete(gameData);
            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.CAST_WITHOUT_PAYING) {
            // The chosen card leaves the searched library; the owner then shuffles (the chosen card
            // is already removed so it is not shuffled back). The card is cast under the searcher's
            // control without paying its mana cost (e.g. Knowledge Exploitation).
            //
            // Knowledge Exploitation never exiles: "Search target opponent's library for an instant
            // or sorcery card. You may cast that card without paying its mana cost. Then that player
            // shuffles." A card that can't be cast (CR 601.2c: no legal target) is therefore simply
            // left in the library it was found in, and is shuffled back in with the rest.
            String refusal = freeCastRefusal(gameData, playerId, chosenCard);
            boolean castable = refusal == null;
            if (!castable) {
                deck.add(chosenCard);
            }
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                if (targetPlayerId != null) {
                    String targetName = gameData.playerIdToName.get(targetPlayerId);
                    gameLogService.append(gameData, GameLog.text(targetName + "'s library is shuffled."));
                }
            }
            if (!castable) {
                gameLogService.append(gameData, GameLog.cardThen(chosenCard, refusal + " and stays in the library."));
                log.info("Game {} - {} cast-without-paying can't be cast; card stays in library",
                        gameData.id, chosenCard.getName());
                finishSearchAndResume(gameData);
                return;
            }
            // A cascade hit is still in exile when it is cast, so it is recorded as cast from exile;
            // library searches cast from the library.
            boolean inExile = gameData.exiledCards.stream()
                    .anyMatch(exiled -> exiled.card().getId().equals(chosenCard.getId()));
            castCardWithoutPaying(gameData, player, chosenCard, null, grantHaste, sacrificeAtEndStep,
                    inExile ? Zone.EXILE : Zone.LIBRARY, Integer.MAX_VALUE,
                    inExile ? IfNotCast.STAYS_EXILED
                            : shuffleAfterSelection ? IfNotCast.LIBRARY_SHUFFLED_IN : IfNotCast.LIBRARY_BOTTOM,
                    deckOwnerId);
            return;
        }

        if (destination == LibrarySearchDestination.EXILE_FOR_MAY_CAST) {
            boolean castable = canCastWithoutPaying(gameData, playerId, chosenCard);
            exileService.exileCard(gameData, deckOwnerId, chosenCard);
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " exiles ", chosenCard,
                    shuffleAfterSelection ? ". Library is shuffled." : "."));
            if (castable) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        chosenCard,
                        playerId,
                        List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                        "Cast " + chosenCard.getName() + " without paying its mana cost?",
                        chosenCard.getId()));
            }
            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.EXILE_FOR_MAY_CAST_WITH_NORMAL_COST) {
            exileService.exileCard(gameData, deckOwnerId, chosenCard);
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " exiles ", chosenCard,
                    shuffleAfterSelection ? ". Library is shuffled." : "."));
            // CR 305.9: a land that is also another card type can't be cast, so a may-cast offer is never made for it.
            if (!chosenCard.hasType(CardType.LAND)) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        chosenCard,
                        playerId,
                        List.of(new MayCastExiledCardWithNormalCostEffect(UUID.randomUUID(), false)),
                        "Cast " + chosenCard.getName() + "?",
                        chosenCard.getId()));
            }
            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.EXILE_FOR_FREE_CAST) {
            resolveEachPlayerNonlandExilePick(gameData, player, deckOwnerId, chosenCard);
            return;
        }

        if (destination == LibrarySearchDestination.EXILE) {
            exileService.exileCard(gameData, deckOwnerId, chosenCard);

            // Multi-card exile (Jester's Cap): re-prompt for the next card until the count is
            // spent or the library runs out. Only the final pick shuffles the library.
            String exileLog = player.getUsername() + " exiles a card.";

            if (remainingCount > 1) {
                int newRemaining = remainingCount - 1;
                List<Card> newSearchCards = filterPredicate == null
                        ? new ArrayList<>(deck)
                        : new ArrayList<>(deck.stream()
                                .filter(c -> predicateEvaluationService.matchesCardPredicate(
                                        c, filterPredicate, null, gameData, playerId))
                                .toList());
                newSearchCards = newSearchCards.stream().filter(searchCards::contains)
                        .filter(c -> !requireDifferentNames || !excludedCardNames.contains(c.getName()))
                        .toList();
                if (newSearchCards.isEmpty()) {
                    if (shuffleAfterSelection) {
                        LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                    }
                    gameLogService.append(gameData, GameLog.text(shuffleAfterSelection
                            ? exileLog + " Library is shuffled." : exileLog));
                    finishSearchAndResume(gameData);
                    return;
                }
                String exilePrompt = targetPlayerId != null
                        ? "Search " + gameData.playerIdToName.get(targetPlayerId) + "'s library for a card to exile (" + newRemaining + " remaining)."
                        : "Search your library for a card to exile (" + newRemaining + " remaining).";
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(playerId, newSearchCards)
                                .targetPlayerId(targetPlayerId)
                                .remainingCount(newRemaining)
                                .canFailToFind(canFailToFind)
                                .destination(LibrarySearchDestination.EXILE)
                                .filterPredicate(filterPredicate)
                                .shuffleAfterSelection(shuffleAfterSelection)
                                .requireDifferentNames(requireDifferentNames)
                                .excludedCardNames(excludedCardNames)
                                .build(),
                        exilePrompt, canFailToFind));
                gameLogService.append(gameData, GameLog.text(exileLog));
                return;
            }

            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }

            String logMsg = shuffleAfterSelection ? exileLog + " Library is shuffled." : exileLog;
            gameLogService.append(gameData, GameLog.text(logMsg));
            log.info("Game {} - {} exiles {} from library search", gameData.id, player.getUsername(), chosenCard.getName());

            if (followUp.opponentExileChoice() != null) {
                PendingOpponentExileChoice pending = followUp.opponentExileChoice();
                // "Any opponent may": the opponents decide in turn order (Distant Memories ruling 2011-06-01).
                List<UUID> opponents = com.github.laxika.magicalvibes.service.effect.SubjectResolutionService
                        .apnapOpponents(gameData, pending.controllerId());
                if (!opponents.isEmpty()) {
                    MayPenaltyChoiceHandlerService.offerOpponentExileChoice(gameData, chosenCard,
                            opponents.getFirst(), new OpponentMayReturnExiledCardOrDrawEffect(
                                    pending.drawCountOnDecline(), pending.controllerId()));
                    playerInputService.processNextMayAbility(gameData);
                }
            } else {
                insertSelectedCardFollowUp(gameData, followUp, chosenCard, playerId);
                finishSearchAndResume(gameData);
            }
            return;
        }

        if (destination == LibrarySearchDestination.EXILE_FACE_DOWN_PILE) {
            // Mangara's Tome: exile each found card face down into a pile tracked with the source.
            UUID pileSourceId = librarySearch.sourcePermanentId();
            exileService.exileCardFaceDown(gameData, deckOwnerId, chosenCard, pileSourceId, playerId);
            Permanent pileSource = gameQueryService.findPermanentById(gameData, pileSourceId);
            if (pileSource != null && pileSource.getCard().getStaticEffects().stream()
                    .anyMatch(cardEffect -> cardEffect instanceof AllowCastFromCardsExiledWithSourceEffect pilePermission
                            && pilePermission.persistsAfterSourceLeaves())) {
                gameData.grantExilePlayPermission(chosenCard.getId(), playerId, Duration.PERMANENT);
            }

            // Repeat until the requested count is spent or the library runs out; only the final pick
            // shuffles the pile ("shuffle that pile") and then the library ("then shuffle your library").
            List<Card> remainingPileCards = searchCards.stream()
                    .filter(card -> deck.contains(card))
                    .toList();
            if (remainingCount > 1 && !remainingPileCards.isEmpty()) {
                int newRemaining = remainingCount - 1;
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(playerId, new ArrayList<>(remainingPileCards))
                                .remainingCount(newRemaining)
                                .canFailToFind(canFailToFind)
                                .destination(LibrarySearchDestination.EXILE_FACE_DOWN_PILE)
                                .sourcePermanentId(pileSourceId)
                                .shuffleAfterSelection(shuffleAfterSelection)
                                .build(),
                        "Search your library for a card to exile in the face-down pile ("
                                + newRemaining + " remaining).", canFailToFind));
                return;
            }

            gameData.shuffleExilePile(pileSourceId);
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            gameLogService.append(gameData, GameLog.text(player.getUsername()
                    + " exiles a face-down pile and shuffles it. Library is shuffled."));
            log.info("Game {} - {} finishes exiling a face-down pile", gameData.id, player.getUsername());
            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.EXILE_WITH_SOURCE) {
            UUID sourcePermanentId = librarySearch.sourcePermanentId();
            exileService.exileCard(gameData, deckOwnerId, chosenCard, sourcePermanentId);
            if (gameData.pendingEffectResolutionEntry != null) {
                StackEntry resolving = gameData.pendingEffectResolutionEntry;
                resolving.setEventValue(resolving.getEventValue() + 1);
                List<UUID> exiledThisResolution = new ArrayList<>(resolving.getResolutionExiledCardIds());
                exiledThisResolution.add(chosenCard.getId());
                resolving.setResolutionExiledCardIds(exiledThisResolution);
                resolving.addToStep(StepKind.EXILE,
                        StepOutcome.ofObjects(List.of(chosenCard.getId()), List.of(deckOwnerId)));
            }
            if (requireDifferentNames) {
                excludedCardNames.add(chosenCard.getName());
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " exiles ", chosenCard, "."));
            log.info("Game {} - {} exiles {} with source (any-number search)",
                    gameData.id, player.getUsername(), chosenCard.getName());

            // "Any number": re-prompt for the next matching card until none remain or the player
            // declines (handled by the cardIndex == -1 branch above, which shuffles and finishes).
            List<Card> remainingMatches = filterPredicate != null
                    ? deck.stream().filter(c -> predicateEvaluationService.matchesCardPredicate(
                            c, filterPredicate, null, gameData, deckOwnerId)
                            && (!requireDifferentNames || !excludedCardNames.contains(c.getName()))).toList()
                    : new ArrayList<>(deck);
            if (requireDifferentNames && filterPredicate == null) {
                remainingMatches = remainingMatches.stream()
                        .filter(c -> !excludedCardNames.contains(c.getName()))
                        .toList();
            }
            if (remainingCount > 1 && !remainingMatches.isEmpty()) {
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(playerId, new ArrayList<>(remainingMatches))
                                .remainingCount(remainingCount - 1)
                                .canFailToFind(true)
                                .destination(LibrarySearchDestination.EXILE_WITH_SOURCE)
                                .filterPredicate(filterPredicate)
                                .sourcePermanentId(sourcePermanentId)
                                .shuffleAfterSelection(shuffleAfterSelection)
                                .requireDifferentNames(requireDifferentNames)
                                .excludedCardNames(excludedCardNames)
                                .build(),
                        requireDifferentNames
                                ? "Search your library for a card with a different name to exile ("
                                        + (remainingCount - 1) + " remaining)."
                                : "Search your library for a card to exile (any number).",
                        true));
                return;
            }

            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                gameLogService.append(gameData, GameLog.text(player.getUsername() + "'s library is shuffled."));
            }
            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.HEIST) {
            exileService.exileCardFaceDown(gameData, deckOwnerId, chosenCard, null, playerId);
            gameData.grantExilePlayPermission(chosenCard.getId(), playerId, Duration.PERMANENT);
            gameData.updateExilePlayPermission(chosenCard.getId(), granted -> granted.withAnyManaType(true));
            String targetName = gameData.playerIdToName.get(deckOwnerId);
            gameLogService.append(gameData,
                    GameLog.text(player.getUsername() + " exiles a card face down from "
                            + targetName + "'s library with Heist."));
            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.EXILE_PLAYABLE
                || destination == LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP
                || destination == LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_END_OF_TURN) {
            boolean faceUp = filterPredicate != null
                    || destination == LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP
                    || destination == LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_END_OF_TURN;
            if (faceUp) {
                exileService.exileCard(gameData, deckOwnerId, chosenCard);
            } else {
                exileService.exileCardFaceDown(gameData, deckOwnerId, chosenCard, null, playerId);
            }
            gameData.grantExilePlayPermission(chosenCard.getId(), playerId,
                    destination == LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_END_OF_TURN
                            ? Duration.UNTIL_END_OF_TURN
                            : destination == LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP
                            ? Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP : Duration.PERMANENT);
            if (destination == LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP) {
                // Grinning Totem: permission lasts only until the searcher's next upkeep; an unplayed
                // card is put into its owner's graveyard then.
                Card sourceCard = (sourceCards != null && !sourceCards.isEmpty()) ? sourceCards.getFirst()
                        : chosenCard;
                gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(playerId,
                        sourceCard, new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                                TriggerOptions.DEFAULT.withEventCondition(new TriggeringCardInExile()),
                                new PutExiledCardsIntoOwnersGraveyardEffect(
                                        Subjects.cardsThisWay(StepKind.EXILE))),
                        null, null, null,
                        DelayedExiledCardReturns.exiled(gameData, List.of(chosenCard.getId()))));
            }
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }

            String logMsg = player.getUsername()
                    + (faceUp ? " exiles a card." : " exiles a card face down.")
                    + (shuffleAfterSelection ? " Library is shuffled." : "");
            gameLogService.append(gameData, GameLog.text(logMsg));
            log.info("Game {} - {} exiles {} from library search (with play permission)", gameData.id, player.getUsername(), chosenCard.getName());

            finishSearchAndResume(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.SPHINX_AMBASSADOR) {
            // Card is "set aside" (already removed from deck above).
            // Update the pending choice with the selected card, then prompt the opponent to name a card.
            PendingSphinxAmbassadorChoice pending = gameData.pollPendingInteraction(PendingSphinxAmbassadorChoice.class);
            gameData.queueInteraction(new PendingSphinxAmbassadorChoice(
                    chosenCard, pending.controllerId(), pending.targetPlayerId(), pending.sourceCard()));

            UUID opponentId = pending.targetPlayerId();
            String controllerName = gameData.playerIdToName.get(pending.controllerId());
            String opponentName = gameData.playerIdToName.get(opponentId);

            String logMsg = controllerName + " selects a card from " + opponentName + "'s library.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            log.info("Game {} - {} selects {} from {}'s library for Sphinx Ambassador",
                    gameData.id, controllerName, chosenCard.getName(), opponentName);

            // Prompt the opponent to name a card
            playerInputService.beginSphinxAmbassadorCardNameChoice(gameData, opponentId, pending.controllerId());
            return;
        }

        if (destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_CREATURE) {
            // Stonehewer Giant and Quest for the Holy Relic: put the Equipment onto the battlefield, let
            // the controller attach it to a creature they control, then shuffle.
            Permanent equipment = new Permanent(chosenCard, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, equipment,
                    battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of(), enterWithCounters);
            performStateBasedActionsIfResolutionComplete(gameData);

            List<UUID> creatureIds = new ArrayList<>();
            for (Permanent p : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                // A creature the Equipment can't be attached to (protection from artifacts) isn't offered.
                if (gameQueryService.isCreature(gameData, p)
                        && !gameQueryService.cantBeEquipped(gameData, p)
                        && !gameQueryService.hasProtectionFromSource(gameData, p, equipment)) {
                    creatureIds.add(p.getId());
                }
            }
            if (creatureIds.isEmpty()) {
                if (shuffleAfterSelection) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                    gameLogService.append(gameData, GameLog.builder()
                            .card(chosenCard)
                            .text(" enters the battlefield under " + player.getUsername() + "'s control. Library is shuffled.")
                            .build());
                } else {
                    gameLogService.append(gameData,
                            GameLog.entersBattlefieldUnder(chosenCard, player.getUsername()));
                }
                finishSearchAndResume(gameData);
                return;
            }
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(chosenCard, player.getUsername()));
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachEquipmentToCreature(equipment.getId(), playerId,
                            shuffleAfterSelection));
            playerInputService.beginPermanentChoice(gameData, playerId, creatureIds,
                    "Choose a creature you control to attach " + chosenCard.getName() + " to.");
            return;
        }

        if (destination == LibrarySearchDestination.TOP_OF_LIBRARY) {
            // Shuffle library first, then put the card on top (MTG rule: "shuffle and put on top")
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            addCardAtLibraryPosition(deck, chosenCard, topLibraryPosition);
            String positionText = topLibraryPositionText(topLibraryPosition);
            gameLogService.append(gameData, reveals
                    ? GameLog.textCardText(player.getUsername() + " reveals ", chosenCard,
                    " and puts it " + positionText + " of their library. Library is shuffled.")
                    : GameLog.text(player.getUsername()
                    + " puts a card " + positionText + " of their library. Library is shuffled."));
            log.info("Game {} - {} searches library and puts {} {}",
                    gameData.id, player.getUsername(), chosenCard.getName(), positionText);
            if (librarySearchSupport.startNextTargetPlayerTopSearch(gameData, followUp)) return;
            finishSearchAndResume(gameData);
            return;
        } else if (toGraveyard && (remainingCount > 1 || !accumulatedCards.isEmpty())) {
            // Cards found by one search enter the graveyard together, so "one or more cards" triggers fire once.
            accumulatedCards.add(chosenCard);
            if (remainingCount <= 1) {
                graveyardService.addCardsFromLibraryToGraveyard(gameData, deckOwnerId, accumulatedCards);
                accumulatedCards.clear();
            }
        } else if (toGraveyard) {
            graveyardService.addCardToGraveyard(gameData, deckOwnerId, chosenCard, Zone.LIBRARY);
        } else if (destination == LibrarySearchDestination.REVEAL_ONLY
                || destination == LibrarySearchDestination.KEEP_IN_LIBRARY) {
            deck.add(chosenCard);
        } else if (destination == LibrarySearchDestination.HAND) {
            if (putChosenCardOnBattlefield) {
                placeCardsOnBattlefieldSimultaneously(gameData, List.of(chosenCard), handOwnerId,
                        toBattlefieldTapped, false, false, false, null, battlefieldCounter, enterWithCounters);
            } else if (isHeldOutForBattlefieldDecision(gameData, followUp, chosenCard, playerId)) {
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " holds ", chosenCard, " for the battlefield decision."));
            } else {
                addCardFromLibraryToHand(gameData, handOwnerId, chosenCard, deckOwnerId, sourceZone == deck);
            }
        } else if (destination == LibrarySearchDestination.EXILE) {
            exileService.exileCard(gameData, deckOwnerId, chosenCard);
        } else if (destination == LibrarySearchDestination.EXILE_IMPRINT) {
            exileService.exileCard(gameData, playerId, chosenCard);
            UUID sourcePermanentId = followUp.imprintSourcePermanentId();
            if (sourcePermanentId != null) {
                exileService.setImprintedCardOnPermanent(gameData, sourcePermanentId, chosenCard);
            }
        } else if (destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PLAYER) {
            Permanent perm = new Permanent(chosenCard, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, perm,
                    battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of(), enterWithCounters);
            placeBattlefieldCounter(gameData, perm, battlefieldCounter);
            if (librarySearch.attachToPlayerId() != null) {
                perm.setAttachedTo(librarySearch.attachToPlayerId());
            }
            if (!perm.getCard().isAura()) {
                attachmentTriggers.equipmentBecameAttached(gameData, perm, null);
            }
        } else if (destination == LibrarySearchDestination.BATTLEFIELD && (selectedFromGraveyard || selectedFromHand)) {
            Permanent perm = new Permanent(chosenCard, selectedFromGraveyard ? Zone.GRAVEYARD : Zone.HAND);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, perm,
                    battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of(), enterWithCounters);
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, playerId, perm.getCard(), null, false);
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, playerId);
            }
        } else if (destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT) {
            Permanent perm = new Permanent(chosenCard, selectedFromGraveyard ? Zone.GRAVEYARD : Zone.LIBRARY);
            perm.setAttachedTo(librarySearch.attachToPermanentId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, perm,
                    battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of(), enterWithCounters);
            placeBattlefieldCounter(gameData, perm, battlefieldCounter);
            if (librarySearch.attachToPermanentId() != null) {
                perm.setAttachedTo(librarySearch.attachToPermanentId());
                attachmentTriggers.equipmentBecameAttached(
                        gameData, perm, librarySearch.attachToPermanentId());
            }
            if (!perm.getCard().isAura()) {
                attachmentTriggers.equipmentBecameAttached(gameData, perm, null);
            }
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, playerId, perm.getCard(), null, false);
        } else {
            if (finalCardToHand) {
                if (!accumulatedCards.isEmpty()) {
                    placeCardsOnBattlefieldSimultaneously(gameData, accumulatedCards, battlefieldControllerId,
                        toBattlefieldTapped, grantHaste, exileAtEndStep, returnToHandAtEndStep,
                        animateFound, battlefieldCounter, enterWithCounters, false, playerId);
                }
                addCardFromLibraryToHand(gameData, handOwnerId, chosenCard, deckOwnerId, sourceZone == deck);
            } else if (remainingCount > 1) {
                // CR 608.2f: Accumulate for simultaneous battlefield entry
                accumulatedCards.add(chosenCard);
            } else {
                // Final pick (or single pick) — place all accumulated + current simultaneously
                List<Card> allCards = new ArrayList<>(accumulatedCards);
                allCards.add(chosenCard);
                placeCardsOnBattlefieldSimultaneously(gameData, allCards, battlefieldControllerId, toBattlefieldTapped,
                        grantHaste, exileAtEndStep, returnToHandAtEndStep, animateFound,
                        battlefieldCounter, enterWithCounters, false, playerId);
            }
        }

        if (remainingCount > 1) {
            int newRemaining = remainingCount - 1;
            List<Card> newSearchCards;
            if (filterCardName != null) {
                newSearchCards = deck.stream().filter(c -> filterCardName.equals(c.getName())).toList();
            } else if (filterPredicate != null) {
                final com.github.laxika.magicalvibes.model.filter.CardPredicate fp = filterPredicate;
                newSearchCards = deck.stream().filter(c -> predicateEvaluationService.matchesCardPredicate(c, fp, null)).toList();
            } else if (filterCardTypes != null) {
                newSearchCards = deck.stream().filter(c -> filterCardTypes.contains(c.getType()) || c.getAdditionalTypes().stream().anyMatch(filterCardTypes::contains)).toList();
            } else {
                newSearchCards = new ArrayList<>(deck);
            }

            // Later picks belong to the same search and retain its original searchable cards.
            newSearchCards = newSearchCards.stream().filter(searchCards::contains).toList();

            if (manaValueBoundValue != null) {
                final int bound = manaValueBoundValue;
                final boolean exact = manaValueExact;
                newSearchCards = newSearchCards.stream()
                        .filter(c -> c.hasAllCardNames()
                                || (exact ? c.getManaValue() == bound : c.getManaValue() <= bound))
                        .toList();
            }
            if (totalManaValueBound != null) {
                int selectedManaValue = accumulatedCards.stream()
                        .mapToInt(Card::getManaValue)
                        .sum();
                int remainingManaValue = totalManaValueBound - selectedManaValue;
                newSearchCards = newSearchCards.stream()
                        .filter(c -> c.hasAllCardNames() || c.getManaValue() <= remainingManaValue)
                        .toList();
            }
            if (requireDifferentNames && !excludedCardNames.isEmpty()) {
                java.util.Set<String> excluded = java.util.Set.copyOf(excludedCardNames);
                newSearchCards = newSearchCards.stream()
                        .filter(c -> !excluded.contains(c.getName()))
                        .toList();
            }
            if (librarySearch.requireSharedLandType() && !accumulatedCards.isEmpty()) {
                newSearchCards = newSearchCards.stream()
                        .filter(c -> accumulatedCards.stream().allMatch(selected ->
                                gameQueryService.landTypesOf(c).stream()
                                        .anyMatch(gameQueryService.landTypesOf(selected)::contains)))
                        .toList();
            }
            if (requireDifferentPowers && !accumulatedCards.isEmpty()) {
                newSearchCards = newSearchCards.stream()
                        .filter(c -> accumulatedCards.stream()
                                .noneMatch(selected -> java.util.Objects.equals(selected.getPower(), c.getPower())))
                        .toList();
            }

            if (newSearchCards.isEmpty()) {
                if (reveals) {
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " reveals ", chosenCard, "."));
                }
                // CR 608.2f: Place any accumulated battlefield cards before finishing
                if (!accumulatedCards.isEmpty() && toBattlefield) {
                    placeCardsOnBattlefieldSimultaneously(gameData, accumulatedCards, handOwnerId, toBattlefieldTapped,
                        grantHaste, exileAtEndStep, returnToHandAtEndStep, animateFound,
                        battlefieldCounter, enterWithCounters);
                }
                if (!accumulatedCards.isEmpty() && toGraveyard) {
                    graveyardService.addCardsFromLibraryToGraveyard(gameData, deckOwnerId, accumulatedCards);
                    accumulatedCards.clear();
                }
                // No more matching cards — shuffle and finish. A basic-land queue that shuffles after all
                // its searches shuffles once for the whole queue instead.
                boolean queueShufflesLater = !shuffleAfterSelection && followUp.basicLandSearchQueue() != null
                        && followUp.basicLandSearchQueue().shuffleAfterQueue();
                String shuffledText = queueShufflesLater ? "" : " Library is shuffled.";
                if (!queueShufflesLater) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                }
                String logMsg;
                if (targetPlayerId != null) {
                    String targetName = gameData.playerIdToName.get(targetPlayerId);
                    logMsg = player.getUsername() + " finds no more matching cards in " + targetName + "'s library." + shuffledText;
                } else {
                    logMsg = player.getUsername() + " finds no more matching cards." + shuffledText;
                }
                gameLogService.append(gameData, GameLog.text(logMsg));
                if (toBattlefield) {
                    performStateBasedActionsIfResolutionComplete(gameData);
                }
                // The multi-pick ran dry, but any queued follow-up work still has to happen.
                if (basicLandSearchQueueSupport.advance(gameData, followUp)) return;
                finishSearchAndResume(gameData);
                return;
            }

            if (reveals) {
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " reveals ", chosenCard, "."));
            }
            String prompt;
            if (targetPlayerId != null) {
                String targetName = gameData.playerIdToName.get(targetPlayerId);
                String destinationDesc = toGraveyard ? "their graveyard" : "their hand";
                prompt = "Search " + targetName + "'s library for a card to put into " + destinationDesc + " (" + newRemaining + " remaining).";
            } else {
                String destinationDesc = librarySearch.finalCardToHand() && newRemaining == 1
                        ? "into their hand"
                        : toBattlefieldTapped ? "onto the battlefield tapped"
                        : toBattlefield ? "onto the battlefield" : "into your hand";
                String distinct = requireDifferentNames ? " with a different name" : "";
                prompt = "Search your library for a matching card" + distinct + " to put " + destinationDesc + " (" + newRemaining + " remaining).";
            }

            beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                    LibrarySearchParams.builder(playerId, new ArrayList<>(newSearchCards))
                    .targetPlayerId(targetPlayerId)
                    .remainingCount(newRemaining)
                    .reveals(reveals)
                    .canFailToFind(toGraveyard || canFailToFind)
                    .destination(destination)
                    .filterCardTypes(filterCardTypes)
                    .filterCardName(filterCardName)
                    .filterPredicate(filterPredicate)
                    .shuffleAfterSelection(shuffleAfterSelection)
                     .accumulatedCards(accumulatedCards)
                    .battlefieldControllerId(battlefieldControllerId)
                    .followUp(followUp)
                    .requireDifferentNames(requireDifferentNames)
                    .requireDifferentPowers(requireDifferentPowers)
                    .requireSharedLandType(librarySearch.requireSharedLandType())
                    .manaValueBound(manaValueBoundValue, manaValueExact)
                    .totalManaValueBound(totalManaValueBound)
                    .excludedCardNames(excludedCardNames)
                    .grantHaste(grantHaste)
                    .sacrificeAtEndStep(sacrificeAtEndStep)
                    .exileAtEndStep(exileAtEndStep)
                    .returnToHandAtEndStep(returnToHandAtEndStep)
                    .animateFound(animateFound)
                    .battlefieldCounter(battlefieldCounter)
                    .enterWithCounters(enterWithCounters)
                    .battlefieldIfChosenBeholdType(battlefieldIfChosenBeholdType)
                    .battlefieldIfChosenTapped(battlefieldIfChosenTapped)
                    .finalCardToHand(librarySearch.finalCardToHand())
                    .build(),
                    prompt, toGraveyard || canFailToFind));

            log.info("Game {} - {} picks from library, {} remaining", gameData.id, player.getUsername(), newRemaining);
            return;
        }

        if (shuffleAfterSelection) {
            LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
        }

        if (finalCardToHand) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " reveals ", chosenCard,
                    " and puts it into their hand."));
        }

        // When simultaneous placement was done, individual entry logs were already emitted
        if (!accumulatedCards.isEmpty() && toBattlefield) {
            if (shuffleAfterSelection) {
                String shuffleLog = player.getUsername() + "'s library is shuffled.";
                gameLogService.append(gameData, GameLog.text(shuffleLog));
            }
        } else {
            String destinationText = switch (putChosenCardOnBattlefield
                    ? LibrarySearchDestination.BATTLEFIELD : destination) {
                case BATTLEFIELD -> "onto the battlefield";
                case BATTLEFIELD_TAPPED -> "onto the battlefield tapped";
                case BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER -> "onto the battlefield tapped under the target player's control";
                case BATTLEFIELD_ATTACHED_TO_PLAYER -> "onto the battlefield";
                case BATTLEFIELD_ATTACHED_TO_PERMANENT -> "onto the battlefield";
                case BATTLEFIELD_ATTACHED_TO_CREATURE -> throw new IllegalStateException("BATTLEFIELD_ATTACHED_TO_CREATURE should be handled earlier");
                case HAND -> "into their hand";
                case HAND_OR_GRAVEYARD -> throw new IllegalStateException("HAND_OR_GRAVEYARD should be handled earlier");
                case REVEAL_ONLY, KEEP_IN_LIBRARY -> "back into their library";
                case EXILE_IMPRINT -> "into exile (imprint)";
            case EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM, EXILE_TWO_FACE_DOWN_REST_TO_BOTTOM_RANDOM,
                        EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD -> "into exile face down";
                case EXILE, EXILE_PLAYABLE, EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP, EXILE_PLAYABLE_UNTIL_END_OF_TURN, HEIST,
                        EXILE_PLAYABLE_REST_TO_BOTTOM_RANDOM, EXILE_FOR_MAY_CAST,
                        EXILE_FOR_MAY_CAST_WITH_NORMAL_COST -> "into exile";
                case EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM,
                        EXILE_ONE_FACE_DOWN_WITH_HATCHING_COUNTER_REST_TO_BOTTOM -> "into exile face down";
                case EXILE_WITH_SOURCE -> throw new IllegalStateException("EXILE_WITH_SOURCE should be handled earlier");
                case EXILE_PLAYABLE_ANY_NUMBER -> throw new IllegalStateException(
                        "EXILE_PLAYABLE_ANY_NUMBER should be handled earlier");
                case EXILE_FACE_DOWN_PILE -> throw new IllegalStateException("EXILE_FACE_DOWN_PILE should be handled earlier");
                case TOP_OF_LIBRARY -> "on top of their library";
                case GRAVEYARD -> "into their graveyard";
                case SPHINX_AMBASSADOR -> throw new IllegalStateException("SPHINX_AMBASSADOR should be handled earlier");
                case CAST_WITHOUT_PAYING -> throw new IllegalStateException("CAST_WITHOUT_PAYING should be handled earlier");
                case CAST_WITHOUT_PAYING_AND_SHUFFLE_LIBRARY -> throw new IllegalStateException(
                        "CAST_WITHOUT_PAYING_AND_SHUFFLE_LIBRARY should be handled earlier");
                case DISCOVER -> throw new IllegalStateException("DISCOVER should be handled earlier");
                case CAST_ONE_AND_PUT_OTHER_INTO_HAND -> throw new IllegalStateException(
                        "CAST_ONE_AND_PUT_OTHER_INTO_HAND should be handled earlier");
                case CAST_ONE_AND_PUT_REST_INTO_HAND -> throw new IllegalStateException(
                        "CAST_ONE_AND_PUT_REST_INTO_HAND should be handled earlier");
                case HOLD_OUT -> throw new IllegalStateException("HOLD_OUT should be handled earlier");
                case HOLD_OUT_THEN_HAND, HOLD_OUT_THEN_BATTLEFIELD_OR_HAND -> throw new IllegalStateException(
                        "HOLD_OUT_THEN_* should be handled earlier");
                case BATTLEFIELD_ONE_AND_PUT_REST_INTO_HAND -> throw new IllegalStateException(
                        "BATTLEFIELD_ONE_AND_PUT_REST_INTO_HAND should be handled earlier");
                case PUT_ONE_INTO_HAND_REST_TO_BOTTOM_RANDOM -> throw new IllegalStateException(
                        "PUT_ONE_INTO_HAND_REST_TO_BOTTOM_RANDOM should be handled earlier");
                case EXILE_AND_MAY_CAST_WITHOUT_PAYING -> throw new IllegalStateException(
                        "EXILE_AND_MAY_CAST_WITHOUT_PAYING should be handled earlier");
                case EXILE_FACE_DOWN_AND_MAY_CAST_OR_PUT_INTO_HAND -> throw new IllegalStateException(
                        "EXILE_FACE_DOWN_AND_MAY_CAST_OR_PUT_INTO_HAND should be handled earlier");
                case EXILE_FOR_FREE_CAST -> throw new IllegalStateException("EXILE_FOR_FREE_CAST should be handled earlier");
                case BATTLEFIELD_UNDER_SEARCHER -> throw new IllegalStateException("BATTLEFIELD_UNDER_SEARCHER should be handled earlier");
                case DRAW_CHOSEN_REST_TO_BOTTOM_RANDOM -> throw new IllegalStateException("DRAW_CHOSEN_REST_TO_BOTTOM_RANDOM should be handled earlier");
                case GIFTS_UNGIVEN_POOL -> throw new IllegalStateException("GIFTS_UNGIVEN_POOL should be handled earlier");
                case THREATS_UNDETECTED_POOL -> throw new IllegalStateException("THREATS_UNDETECTED_POOL should be handled earlier");
                case SIGNAL_THE_CLANS_POOL -> throw new IllegalStateException("SIGNAL_THE_CLANS_POOL should be handled earlier");
            };
            GameLogEntry logEntry;
            if (targetPlayerId != null) {
                String targetName = gameData.playerIdToName.get(targetPlayerId);
                String suffix = " and puts it " + destinationText + " for " + targetName + "."
                        + (shuffleAfterSelection ? " Library is shuffled." : "");
                logEntry = reveals
                        ? GameLog.textCardText(player.getUsername() + " reveals ", chosenCard, suffix)
                        : GameLog.text(shuffleAfterSelection
                                ? player.getUsername() + " puts cards " + destinationText + " for " + targetName + ". " + targetName + "'s library is shuffled."
                                : player.getUsername() + " puts cards " + destinationText + " for " + targetName + ".");
            } else if (reveals) {
                logEntry = GameLog.textCardText(player.getUsername() + " reveals ", chosenCard,
                        shuffleAfterSelection
                                ? " and puts it " + destinationText + ". Library is shuffled."
                                : " and puts it " + destinationText + ".");
            } else {
                logEntry = GameLog.text(shuffleAfterSelection
                        ? player.getUsername() + " puts a card " + destinationText + ". Library is shuffled."
                        : player.getUsername() + " puts a card " + destinationText + ".");
            }
            gameLogService.append(gameData, logEntry);
            log.info("Game {} - {} searches library and puts {} {}",
                    gameData.id, player.getUsername(), chosenCard.getName(), destinationText);
        }

        if (toBattlefield) {
            performStateBasedActionsIfResolutionComplete(gameData);
        }

        if (sourceCards != null
                && followUp.selectedCardFollowUp() != null
                && followUp.secondBoundedPick() != null
                && followUp.secondBoundedPick().randomRest()) {
            Collections.shuffle(sourceCards);
            deck.addAll(sourceCards);
            gameLogService.append(gameData, GameLog.text(player.getUsername()
                    + " puts the unchosen cards on the bottom of their library in a random order."));
        }

        insertSelectedCardFollowUp(gameData, followUp, chosenCard, playerId);

        if (startPendingBasicLandToHandSearch(gameData, playerId, followUp, searchCards)) return;
        if (startPendingCardToGraveyardSearch(gameData, playerId, followUp)) return;
        if (librarySearchSupport.startNextTargetPlayerTopSearch(gameData, followUp)) return;
        if (librarySearchSupport.startNextSameNamePick(gameData, playerId, followUp)) return;
        if (startNextToHandPick(gameData, playerId, followUp, librarySearch.shuffleAfterSelection())) return;
        if (basicLandSearchQueueSupport.advance(gameData, followUp)) return;
        if (followUp.grimReminderSearch() != null && gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex,
                    List.of(new LoseLifeToOpponentsWhoCastNamedSpellThisTurnEffect(
                            chosenCard.getName(), followUp.grimReminderSearch().lifeLoss())));
        }
        finishSearchAndResume(gameData);
    }

    /**
     * Adds {@code card}, which the search of the parked entry's library-search step found, to its
     * {@link StepKind#SEARCH} outcome, with the zone {@code destination} puts it in (a hand, a graveyard, exile, or the
     * library for a search that keeps it there); a card going onto the battlefield or to a destination that hides or
     * holds it is recorded with no zone. Called as each pick is answered, before the card moves.
     */
    private static void recordFoundCard(GameData gameData, Card card, UUID searcherId, UUID zoneOwnerId,
                                        LibrarySearchDestination destination) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }
        Zone zone = destination == null ? null : switch (destination) {
            case HAND -> Zone.HAND;
            case GRAVEYARD -> Zone.GRAVEYARD;
            case EXILE, EXILE_IMPRINT -> Zone.EXILE;
            case REVEAL_ONLY, KEEP_IN_LIBRARY, TOP_OF_LIBRARY -> Zone.LIBRARY;
            default -> null;
        };
        entry.addToStep(StepKind.SEARCH, StepOutcome.ofObjects(List.of(card.getId()), List.of(searcherId),
                zone == null ? List.of() : List.of(new MovedCard(card.getId(), card.getId(), zone, zoneOwnerId))));
    }

    private boolean startNextToHandPick(GameData gameData, UUID playerId, LibrarySearchFollowUp followUp, boolean alreadyShuffled) {
        // Basic-land queues have their own end-of-queue shuffle; the generic descriptor helper
        // treats an absent empty descriptor queue as an exhausted queue and would shuffle here.
        return !alreadyShuffled && followUp.basicLandSearchQueue() == null
                && !followUp.remainingToHandPicks().isEmpty()
                && librarySearchSupport.startNextToHandPick(gameData, playerId, followUp);
    }

    /**
     * The searcher of a "may search" in a search queue has chosen to search, by taking a card or by acting while
     * searching: the search is recorded once and the queue knows them as a searcher, who shuffles with the others
     * (also when Opposition Agent exiles what is found).
     */
    private PendingInteraction.LibrarySearch markSearchChosen(GameData gameData,
                                                              PendingInteraction.LibrarySearch search) {
        LibrarySearchParams params = search.params();
        LibrarySearchFollowUp.BasicLandSearchQueue queue = params.followUp().basicLandSearchQueue();
        if (queue == null
                || !com.github.laxika.magicalvibes.service.effect.normalfx.BasicLandSearchQueueSupport
                        .markSearchedOnPick(queue)
                || queue.searchedPlayerIds().contains(params.playerId())) {
            return search;
        }
        LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, params.playerId());
        return new PendingInteraction.LibrarySearch(
                params.withFollowUp(params.followUp().withBasicLandSearchQueue(
                        queue.withSearchedPlayer(params.playerId()))),
                search.messagePrompt(), search.messageCanFailToFind());
    }

    private void handleOppositionAgentChoice(GameData gameData, Player player,
                                             PendingInteraction.LibrarySearch activeSearch,
                                             Card chosenCard, List<Card> sourceZone, List<Card> deck,
                                             UUID deckOwnerId) {
        LibrarySearchParams librarySearch = activeSearch.params();
        exileCardFoundUnderOppositionAgent(gameData, player, activeSearch, chosenCard, sourceZone, deckOwnerId);

        List<String> excludedCardNames = new ArrayList<>(librarySearch.excludedCardNames());
        if (librarySearch.requireDifferentNames()) {
            excludedCardNames.add(chosenCard.getName());
        }

        if (librarySearch.remainingCount() > 1) {
            List<Card> accumulated = new ArrayList<>(librarySearch.accumulatedCards() == null
                    ? List.of() : librarySearch.accumulatedCards());
            accumulated.add(chosenCard);
            LibrarySearchParams advanced = librarySearch.withAccumulatedCards(accumulated);
            List<Card> stillSearchable = librarySearch.cards().stream()
                    .filter(card -> !card.getId().equals(chosenCard.getId()))
                    .filter(deck::contains)
                    .toList();
            List<Card> remainingMatches = findOppositionAgentSearchMatches(
                    gameData, stillSearchable, advanced, deckOwnerId, excludedCardNames);
            if (!remainingMatches.isEmpty()) {
                int newRemaining = librarySearch.remainingCount() - 1;
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        advanced.withCards(new ArrayList<>(remainingMatches))
                                .withRemainingCount(newRemaining)
                                .withExcludedCardNames(excludedCardNames),
                        activeSearch.messagePrompt(), activeSearch.messageCanFailToFind()));
                return;
            }
        }

        if (librarySearch.shuffleAfterSelection()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
        }
        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + "'s library is shuffled after Opposition Agent's replacement."));
        insertSelectedCardFollowUp(gameData, librarySearch.followUp(), chosenCard, librarySearch.playerId());

        LibrarySearchFollowUp followUp = librarySearch.followUp();
        UUID playerId = librarySearch.playerId();
        if (startPendingBasicLandToHandSearch(gameData, playerId, followUp, librarySearch.cards())) return;
        if (startPendingCardToGraveyardSearch(gameData, playerId, followUp)) return;
        if (librarySearchSupport.startNextTargetPlayerTopSearch(gameData, followUp)) return;
        if (librarySearchSupport.startNextSameNamePick(gameData, playerId, followUp)) return;
        if (startNextToHandPick(gameData, playerId, followUp, librarySearch.shuffleAfterSelection())) return;
        if (basicLandSearchQueueSupport.advance(gameData, followUp)) return;
        finishSearchAndResume(gameData);
    }

    /**
     * Opposition Agent: the searching opponent exiles the card found, and the Agent's controller
     * ({@code player}, who made the choice) may play it for as long as it remains exiled.
     */
    private void exileCardFoundUnderOppositionAgent(GameData gameData, Player player,
                                                    PendingInteraction.LibrarySearch activeSearch,
                                                    Card chosenCard, List<Card> sourceZone, UUID deckOwnerId) {
        boolean removed = sourceZone.removeIf(card -> card.getId().equals(chosenCard.getId()));
        if (!removed) {
            throw new IllegalStateException("Chosen card not found in library");
        }

        UUID agentControllerId = activeSearch.decidingPlayerId();
        if (sourceZone == gameData.playerGraveyards.get(deckOwnerId)) {
            graveyardService.notifyCardsLeftGraveyard(gameData, deckOwnerId, chosenCard);
        }
        exileService.exileCard(gameData, deckOwnerId, chosenCard);
        gameData.grantExilePlayPermission(chosenCard.getId(), agentControllerId, Duration.PERMANENT);
        // "You may spend mana as though it were mana of any color to cast them": not {C} (CR 107.4c).
        gameData.updateExilePlayPermission(chosenCard.getId(),
                granted -> granted.withManaSpending(AnyManaSpending.AS_THOUGH_ANY_COLOR));
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " exiles ", chosenCard, " and may play it for as long as it remains exiled."));
    }

    private List<Card> findOppositionAgentSearchMatches(GameData gameData, List<Card> deck,
                                                         LibrarySearchParams librarySearch,
                                                         UUID deckOwnerId, List<String> excludedCardNames) {
        List<Card> matches;
        if (librarySearch.filterCardName() != null) {
            matches = deck.stream().filter(c -> librarySearch.filterCardName().equals(c.getName())).toList();
        } else if (librarySearch.filterPredicate() != null) {
            matches = deck.stream().filter(c -> predicateEvaluationService.matchesCardPredicate(
                    c, librarySearch.filterPredicate(), null, gameData, deckOwnerId)).toList();
        } else if (librarySearch.filterCardTypes() != null) {
            matches = deck.stream().filter(c -> com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport.matchesCardTypes(
                    c, librarySearch.filterCardTypes())).toList();
        } else {
            matches = new ArrayList<>(deck);
        }

        if (librarySearch.manaValueBoundValue() != null) {
            int bound = librarySearch.manaValueBoundValue();
            boolean exact = librarySearch.manaValueExact();
            matches = matches.stream()
                    .filter(c -> c.hasAllCardNames()
                            || (exact ? c.getManaValue() == bound : c.getManaValue() <= bound))
                    .toList();
        }
        if (librarySearch.totalManaValueBound() != null) {
            List<Card> accumulatedCards = librarySearch.accumulatedCards() == null
                    ? List.of() : librarySearch.accumulatedCards();
            int selectedManaValue = accumulatedCards.stream()
                    .mapToInt(Card::getManaValue).sum();
            int remainingManaValue = librarySearch.totalManaValueBound() - selectedManaValue;
            matches = matches.stream()
                    .filter(c -> c.hasAllCardNames() || c.getManaValue() <= remainingManaValue)
                    .toList();
        }
        if (librarySearch.requireDifferentNames() && !excludedCardNames.isEmpty()) {
            Set<String> excluded = Set.copyOf(excludedCardNames);
            matches = matches.stream().filter(c -> !excluded.contains(c.getName())).toList();
        }
        return matches;
    }

    private void handleDeferredBasicLandSearchChoice(GameData gameData,
            PendingInteraction.LibrarySearch activeSearch, LibrarySearchFollowUp currentFollowUp, int cardIndex) {
        LibrarySearchParams params = activeSearch.params();
        UUID ownerId = params.targetPlayerId() != null ? params.targetPlayerId() : params.playerId();
        UUID controllerId = params.battlefieldControllerId() != null
                ? params.battlefieldControllerId() : ownerId;
        LibrarySearchFollowUp.BasicLandSearchQueue queue = currentFollowUp.basicLandSearchQueue();
        if (cardIndex >= 0) {
            Card chosen = params.cards().get(cardIndex);
            if (!gameData.playerDecks.getOrDefault(ownerId, List.of()).contains(chosen)) {
                throw new IllegalStateException("Chosen card not found in library");
            }
            boolean tapped = params.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED
                    || params.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER;
            queue = queue.withSelectedLand(new LibrarySearchFollowUp.DeferredBasicLand(
                    chosen, ownerId, controllerId, tapped));
            recordFoundCard(gameData, chosen, params.playerId(), ownerId, params.destination());
        }
        LibrarySearchFollowUp followUp = currentFollowUp.withBasicLandSearchQueue(queue);
        if (cardIndex >= 0 && params.remainingCount() > 1) {
            Set<UUID> selectedIds = queue.selectedLands().stream()
                    .map(selected -> selected.card().getId()).collect(java.util.stream.Collectors.toSet());
            List<Card> remaining = params.cards().stream()
                    .filter(card -> !selectedIds.contains(card.getId()))
                    .filter(gameData.playerDecks.getOrDefault(ownerId, List.of())::contains)
                    .toList();
            if (!remaining.isEmpty()) {
                int count = params.remainingCount() - 1;
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        params.withCards(new ArrayList<>(remaining)).withRemainingCount(count).withFollowUp(followUp),
                        "Choose up to " + count + " more "
                                + com.github.laxika.magicalvibes.model.filter.CardPredicateUtils.describeFilter(queue.filter())
                                + (count == 1 ? "." : "s."), true));
                return;
            }
        }
        if (params.shuffleAfterSelection()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        }
        if (basicLandSearchQueueSupport.advance(gameData, followUp)) return;
        finishSearchAndResume(gameData);
    }

    /**
     * Whether the found card is offered for the battlefield "instead" of going to hand, so it must not
     * enter the hand first. The follow-up carries the card and puts it into hand if the offer is declined.
     */
    private boolean isHeldOutForBattlefieldDecision(GameData gameData, LibrarySearchFollowUp followUp,
            Card chosenCard, UUID playerId) {
        LibrarySearchFollowUp.SelectedCardFollowUp selected = followUp.selectedCardFollowUp();
        if (selected == null || selected.predicate() == null || gameData.pendingEffectResolutionEntry == null
                || !predicateEvaluationService.matchesCardPredicate(chosenCard, selected.predicate(), null,
                gameData, playerId, null, null, gameData.pendingEffectResolutionEntry.getXValue())) {
            return false;
        }
        CardEffect effect = selected.effect();
        if (effect instanceof com.github.laxika.magicalvibes.model.effect.ConditionalEffect conditional) {
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                    com.github.laxika.magicalvibes.service.effect.ConditionContext.forStackEntry(
                            gameData.pendingEffectResolutionEntry))) {
                return false;
            }
            effect = conditional.wrapped();
        }
        return effect instanceof com.github.laxika.magicalvibes.model.effect.MayPutSelectedCardOntoBattlefieldEffect;
    }

    private void insertSelectedCardFollowUp(GameData gameData, LibrarySearchFollowUp followUp,
            Card chosenCard, UUID playerId) {
        LibrarySearchFollowUp.SelectedCardFollowUp selectedCardFollowUp = followUp.selectedCardFollowUp();
        if (selectedCardFollowUp != null
                && selectedCardFollowUp.predicate() != null
                && gameData.pendingEffectResolutionEntry != null
                && predicateEvaluationService.matchesCardPredicate(
                        chosenCard, selectedCardFollowUp.predicate(), null, gameData, playerId, null, null,
                        gameData.pendingEffectResolutionEntry.getXValue())) {
            if (selectedCardFollowUp.useSelectedCardManaValue()) {
                gameData.pendingEffectResolutionEntry.setEventValue(chosenCard.getManaValue());
            }
            gameData.pendingEffectResolutionEntry.setChosenObjectCard(chosenCard);
            gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex,
                    List.of(selectedCardFollowUp.effect()));
        }
    }

    private void insertNoCardFollowUp(GameData gameData, LibrarySearchFollowUp followUp) {
        if (followUp.selectedCardFollowUp() == null
                || followUp.selectedCardFollowUp().effectIfNoCardChosen() == null
                || gameData.pendingEffectResolutionEntry == null) {
            return;
        }
        gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                gameData.pendingEffectResolutionIndex,
                List.of(followUp.selectedCardFollowUp().effectIfNoCardChosen()));
    }

    private void handleLibrarySearchCast(GameData gameData, Player player,
                                         PendingInteraction.LibrarySearch activeSearch, Card card) {
        LibrarySearchParams librarySearch = activeSearch.params();
        spellCastingService.castCardFromLibraryWhileSearching(gameData, player, librarySearch.playerId(), card);
        gameData.interaction.clearAwaitingInput();

        continueLibrarySearchAfterLibraryAction(gameData, player, activeSearch, card);
    }

    private void handleLibraryNinjutsu(GameData gameData, Player player,
                                       PendingInteraction.LibrarySearch activeSearch, Card card,
                                       LibraryNinjutsuEffect libraryNinjutsu) {
        UUID playerId = player.getId();
        List<UUID> attackers = unblockedAttackerIds(gameData, playerId);
        if (attackers.isEmpty()) {
            throw new IllegalStateException("Library ninjutsu requires an unblocked attacker you control");
        }

        playerInputService.beginPermanentChoice(
                gameData,
                playerId,
                attackers,
                new PermanentChoiceContext.LibraryNinjutsu(activeSearch, card, libraryNinjutsu.manaCost()),
                "Choose an unblocked attacker to shuffle into your library for library ninjutsu.");
    }

    private List<UUID> unblockedAttackerIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, new PermanentIsUnblockedAttackingPredicate()))
                .map(Permanent::getId)
                .toList();
    }

    /** Completes the attacker choice made after a library-ninjutsu card was selected. */
    public void completeLibraryNinjutsuChoice(GameData gameData, Player player, UUID attackerId,
                                              PermanentChoiceContext.LibraryNinjutsu choice) {
        Card shuffledAttacker = gameData.playerBattlefields.getOrDefault(player.getId(), List.of()).stream()
                .filter(permanent -> permanent.getId().equals(attackerId))
                .map(Permanent::getOriginalCard)
                .findFirst()
                .orElse(null);
        try {
            abilityActivationService.activateLibraryNinjutsu(
                    gameData, player, choice.card(), choice.manaCost(), attackerId);
        } catch (IllegalStateException exception) {
            List<UUID> attackers = gameData.playerBattlefields.getOrDefault(player.getId(), List.of()).stream()
                    .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, new PermanentIsUnblockedAttackingPredicate()))
                    .map(Permanent::getId)
                    .toList();
            if (!attackers.isEmpty()) {
                playerInputService.beginPermanentChoice(
                        gameData, player.getId(), attackers, choice,
                        "Choose an unblocked attacker to shuffle into your library for library ninjutsu.");
            }
            throw exception;
        }

        continueLibrarySearchAfterLibraryAction(gameData, player,
                withShuffledAttacker(gameData, player, choice.search(), shuffledAttacker), choice.card());
    }

    /**
     * The attacker shuffled into the library as library ninjutsu's cost is a card in the library the search is
     * still looking through, so a search it matches can find it.
     */
    private PendingInteraction.LibrarySearch withShuffledAttacker(GameData gameData, Player player,
                                                                   PendingInteraction.LibrarySearch search,
                                                                   Card shuffledAttacker) {
        LibrarySearchParams params = search.params();
        UUID deckOwnerId = params.targetPlayerId() != null ? params.targetPlayerId() : player.getId();
        if (shuffledAttacker == null || params.sourceCards() != null
                || params.cards().stream().anyMatch(card -> card.getId().equals(shuffledAttacker.getId()))) {
            return search;
        }
        Card inLibrary = gameData.playerDecks.getOrDefault(deckOwnerId, List.of()).stream()
                .filter(card -> card.getId().equals(shuffledAttacker.getId()))
                .findFirst()
                .orElse(null);
        if (inLibrary == null
                || params.filterCardTypes() != null && !params.filterCardTypes().isEmpty()
                && !com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport.matchesCardTypes(inLibrary, params.filterCardTypes())
                || params.filterCardName() != null && !params.filterCardName().equals(inLibrary.getName())
                || params.filterPredicate() != null && !predicateEvaluationService.matchesCardPredicate(
                inLibrary, params.filterPredicate(), null, gameData, deckOwnerId)) {
            return search;
        }
        List<Card> cards = new ArrayList<>(params.cards());
        cards.add(inLibrary);
        return new PendingInteraction.LibrarySearch(params.withCards(cards),
                search.messagePrompt(), search.messageCanFailToFind());
    }

    private void continueLibrarySearchAfterLibraryAction(GameData gameData, Player player,
                                                         PendingInteraction.LibrarySearch activeSearch, Card card) {
        // A card cast, or an ability activated, while searching: that player has chosen to search.
        activeSearch = markSearchChosen(gameData, activeSearch);
        LibrarySearchParams librarySearch = activeSearch.params();

        List<Card> remainingCards = librarySearch.cards().stream()
                .filter(searchCard -> !searchCard.getId().equals(card.getId()))
                .toList();
        if (remainingCards.isEmpty()) {
            UUID deckOwnerId = librarySearch.targetPlayerId() != null
                    ? librarySearch.targetPlayerId() : player.getId();
            if (librarySearch.shuffleAfterSelection()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            insertNoCardFollowUp(gameData, librarySearch.followUp());
            if (basicLandSearchQueueSupport.advance(gameData, librarySearch.followUp())) return;
            finishSearchAndResume(gameData);
            return;
        }

        beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                librarySearch.withCards(new ArrayList<>(remainingCards)),
                activeSearch.messagePrompt(), activeSearch.messageCanFailToFind()));
    }

    private void beginLibrarySearch(GameData gameData, PendingInteraction.LibrarySearch interaction) {
        LibrarySearchParams controlledParams = librarySearchSupport.applyOppositionAgentControl(
                gameData, interaction.params());
        if (controlledParams == null) {
            controlledParams = interaction.params();
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                controlledParams, interaction.messagePrompt(), interaction.messageCanFailToFind()));
    }

    /**
     * Shared completion tail for every flow in this handler that finishes a choice begun
     * mid-resolution: resume the stack entry parked in {@code pendingEffectResolutionEntry}
     * (e.g. Shefet Monitor's "search ... then draw", where the cycling draw follows the land
     * search), then auto-pass. The resume must run even when the choice was the effect list's
     * last effect — the past-the-end drain in {@code resolveEffectsFrom} is what clears the
     * parked entry, releases the deferred player-loss check, and runs the post-resolution
     * state-based actions; skipping it leaves the entry dangling. When nothing is parked this
     * behaves exactly like {@code resolveAutoPass}.
     */
    /**
     * Gifts Ungiven: the search is over ("Then shuffle" — the found cards are already out of the
     * library, so shuffling now is equivalent to shuffling at the end of resolution). The pool is
     * handed to the opponent recorded on the {@link PendingPileSeparation} queued when the search
     * began; they choose two of the cards for the controller's graveyard and the rest go to their
     * hand. An empty pool has nothing to choose from, so resolution just finishes.
     */
    /**
     * Signal the Clans: the search is over. Only a pool of exactly three revealed cards with three
     * different names puts a card into the searcher's hand, and which one is chosen at random; every
     * other revealed card is shuffled back into the library along with the rest of it.
     */
    private void finishSignalTheClansSearch(GameData gameData, UUID controllerId, List<Card> pool) {
        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        Card chosen = null;
        long distinctNames = pool.stream().map(Card::getName).distinct().count();
        if (pool.size() == 3 && distinctNames == 3) {
            chosen = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
            gameData.addCardToHand(controllerId, chosen);
            gameLogService.append(gameData, GameLog.textCardText(
                    controllerName + " chooses ", chosen, " at random and puts it into their hand."));
        } else if (!pool.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " did not reveal three cards with different names."));
        }

        for (Card card : pool) {
            if (card != chosen) {
                deck.add(card);
            }
        }

        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        gameLogService.append(gameData, GameLog.text(controllerName + "'s library is shuffled."));
        finishSearchAndResume(gameData);
    }

    private void finishGiftsUngivenSearch(GameData gameData, UUID controllerId, List<Card> pool) {
        // Threats Undetected's search has no shuffle of its own: the opponent's chosen cards are
        // shuffled into the library once, when they are put back (GraveyardReturnSupport).
        PendingPileSeparation pending = gameData.peekPendingInteraction(PendingPileSeparation.class);
        if (pending == null || pending.disposition() != CardPileDisposition.THREATS_UNDETECTED) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        }

        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        if (state == null || pool.isEmpty()) {
            finishSearchAndResume(gameData);
            return;
        }

        gameData.recordPileGroupingOrGuess();
        if (state.disposition() == CardPileDisposition.THREATS_UNDETECTED) {
            // "An opponent chooses two of those cards" names its opponent once the cards are revealed.
            chosenOpponentSupport.chooseThen(gameData, controllerId, "Threats Undetected",
                    new ChosenOpponentFollowUp.ThreatsUndetectedReveal(controllerId, pool));
            return;
        }
        beginGiftsStyleChoice(gameData, controllerId, state.targetPlayerId(), pool, state.disposition());
    }

    @Override
    public Class<ChosenOpponentFollowUp.ThreatsUndetectedReveal> followUpType() {
        return ChosenOpponentFollowUp.ThreatsUndetectedReveal.class;
    }

    /**
     * Threats Undetected's "an opponent chooses two of those cards": the chosen opponent chooses them. With no
     * opponent left the revealed cards go to the hand, as nothing is chosen.
     */
    @Override
    public void continueWithOpponent(GameData gameData, UUID opponentId,
                                     ChosenOpponentFollowUp.ThreatsUndetectedReveal reveal) {
        if (opponentId == null) {
            reveal.revealedCards().forEach(card -> gameData.addCardToHand(reveal.controllerId(), card));
            LibraryShuffleHelper.shuffleLibrary(gameData, reveal.controllerId());
            finishSearchAndResume(gameData);
            return;
        }
        beginGiftsStyleChoice(gameData, reveal.controllerId(), opponentId, reveal.revealedCards(),
                CardPileDisposition.THREATS_UNDETECTED);
    }

    private void beginGiftsStyleChoice(GameData gameData, UUID controllerId, UUID chooserId, List<Card> pool,
                                       CardPileDisposition disposition) {
        Map<UUID, UUID> cardOwners = new HashMap<>();
        for (Card card : pool) {
            cardOwners.put(card.getId(), controllerId);
        }
        gameData.queueInteraction(new PendingPileSeparation(controllerId, chooserId,
                List.of(), List.copyOf(pool), cardOwners, List.of(), List.of(), disposition));

        int count = Math.min(2, pool.size());
        String controllerName = gameData.playerIdToName.get(controllerId);
        String destination = disposition == CardPileDisposition.THREATS_UNDETECTED
                ? "shuffle into " + controllerName + "'s library"
                : "put into " + controllerName + "'s graveyard";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                chooserId, List.copyOf(pool), count,
                "Choose " + (count == 1 ? "a card" : count + " cards") + " to " + destination
                        + ". The rest go to their hand.", count));
    }

    private void handleD20BasicLandSearchChoice(GameData gameData, Player player, int cardIndex,
                                                 List<Card> searchCards, List<Card> deck,
                                                 int remainingCount, LibrarySearchFollowUp followUp,
                                                 com.github.laxika.magicalvibes.model.filter.CardPredicate filterPredicate,
                                                 List<Card> previouslyAccumulatedCards) {
        List<Card> accumulatedCards = new ArrayList<>(previouslyAccumulatedCards);

        if (cardIndex >= 0) {
            Card chosenCard = searchCards.get(cardIndex);
            // Where the card goes is decided by the roll that follows, so it is recorded with no zone.
            recordFoundCard(gameData, chosenCard, player.getId(), player.getId(), null);
            deck.removeIf(card -> card.getId().equals(chosenCard.getId()));
            accumulatedCards.add(chosenCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " reveals ", chosenCard, "."));

            List<Card> remainingMatches = deck.stream()
                    .filter(card -> filterPredicate == null
                            || predicateEvaluationService.matchesCardPredicate(
                            card, filterPredicate, null, gameData, player.getId()))
                    .toList();
            if (remainingCount > 1 && !remainingMatches.isEmpty()) {
                int newRemaining = remainingCount - 1;
                beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(player.getId(), new ArrayList<>(remainingMatches))
                                .remainingCount(newRemaining)
                                .reveals(true)
                                .canFailToFind(true)
                                .destination(LibrarySearchDestination.GIFTS_UNGIVEN_POOL)
                                .filterPredicate(filterPredicate)
                                .accumulatedCards(accumulatedCards)
                                .followUp(followUp)
                                .build(),
                        "Search your library for another basic land card to reveal ("
                                + newRemaining + " remaining).", true));
                return;
            }
        }

        finishD20BasicLandSearch(gameData, player.getId(), accumulatedCards, followUp);
    }

    private void finishD20BasicLandSearch(GameData gameData, UUID controllerId, List<Card> cards,
                                           LibrarySearchFollowUp followUp) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry != null) {
            pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                    List.of(followUp.selectedCardFollowUp().d20BasicLandSearch().rollEffect(cards)));
        }
        finishSearchAndResume(gameData);
    }

    /** Completes the destination choice for a revealed single-card library search. */
    public void handleLibrarySearchDestinationChosen(GameData gameData, Player player, Card card,
                                                     boolean toHand) {
        handleLibrarySearchDestinationChosen(gameData, player, card, toHand, false);
    }

    /** Completes a library search destination choice, including conditional battlefield entry. */
    public void handleLibrarySearchDestinationChosen(GameData gameData, Player player, Card card,
                                                     boolean toHand, boolean toBattlefieldTapped) {
        UUID playerId = player.getId();
        if (toBattlefieldTapped) {
            placeCardsOnBattlefieldSimultaneously(gameData, List.of(card), playerId,
                    true, false, false, false, null, null, null);
            performStateBasedActionsIfResolutionComplete(gameData);
        } else if (toHand) {
            gameData.addCardToHand(playerId, card);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", card, " into their hand."));
        } else {
            graveyardService.addCardToGraveyard(gameData, playerId, card, Zone.LIBRARY);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", card, " into their graveyard."));
        }

        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        gameLogService.append(gameData, GameLog.text(player.getUsername() + " shuffles their library."));
        gameData.interaction.clearAwaitingInput();
        finishSearchAndResume(gameData);
    }

    private void resolveManifestChoice(GameData gameData, UUID controllerId,
                                       List<Card> selectedCards, List<Card> remainingCards,
                                       boolean remainingToGraveyard) {
        if (selectedCards.size() != 1 || remainingCards.size() > 1) {
            throw new IllegalStateException("Manifest selection must choose exactly one of at most two cards");
        }

        StackEntry sourceEntry = gameData.pendingEffectResolutionEntry;
        Card sourceCard = sourceEntry == null ? selectedCards.getFirst() : sourceEntry.getCard();
        Permanent manifestedPermanent = manifestService.manifestCardAndReturnPermanent(
                gameData, controllerId, sourceCard, selectedCards.getFirst());
        if (sourceEntry != null && manifestedPermanent != null) {
            sourceEntry.setChosenPermanentId(manifestedPermanent.getId());
            // The manifest dread step ends with this answer: "that creature" is thisWay(MANIFEST) (CR 701.62a).
            sourceEntry.recordStep(StepKind.MANIFEST,
                    StepOutcome.ofObjects(List.of(manifestedPermanent.getId()), List.of(controllerId)));
        }

        if (remainingCards.isEmpty()) {
            if (remainingToGraveyard) {
                ActionEvents.manifestedDread(gameData, controllerId, null);
            }
            performStateBasedActionsIfResolutionComplete(gameData);
            finishSearchAndResume(gameData);
            return;
        }

        Card remainingCard = remainingCards.getFirst();
        if (remainingToGraveyard) {
            boolean enteredGraveyard = graveyardService.addCardToGraveyard(
                    gameData, controllerId, remainingCard, Zone.LIBRARY);
            ActionEvents.manifestedDread(gameData, controllerId,
                    enteredGraveyard ? remainingCard : null);
            performStateBasedActionsIfResolutionComplete(gameData);
            finishSearchAndResume(gameData);
            return;
        }

        gameData.playerDecks.get(controllerId).addFirst(remainingCard);
        // The other card was only looked at, so its name stays out of the public log (the choice's
        // card name is what the destination log line prints).
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.TargetLibraryDestinationChoice(
                controllerId, remainingCard.getId(), "a card"));
    }

    /**
     * Jace, Architect of Thought −8: the found nonland card (or nothing, on a failed find) is
     * exiled face up, the owner's library shuffles, and the queue moves on to the next player's
     * library. The exiled cards are only offered for casting once every library has been searched.
     * CR 701.23b: failing to find still shuffles the library and moves the queue on.
     */
    private void resolveEachPlayerNonlandExilePick(GameData gameData, Player player, UUID deckOwnerId,
                                                   Card chosenCard) {
        String exileOwnerName = gameData.playerIdToName.get(deckOwnerId);
        if (chosenCard == null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            gameLogService.append(gameData, GameLog.text(player.getUsername() + " fails to find a card in "
                    + exileOwnerName + "'s library. Library is shuffled."));
        } else {
            gameData.addToExile(deckOwnerId, chosenCard);
            LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " exiles ", chosenCard,
                    " from " + exileOwnerName + "'s library. Library is shuffled."));
        }

        PendingEachPlayerLibraryExile state = gameData.pollPendingInteraction(PendingEachPlayerLibraryExile.class);
        if (state != null) {
            List<UUID> exiled = new ArrayList<>(state.exiledCardIds());
            if (chosenCard != null) {
                exiled.add(chosenCard.getId());
            }
            gameData.queueInteraction(new PendingEachPlayerLibraryExile(
                    state.searcherId(), state.remainingPlayerIds(), exiled));
            if (librarySearchSupport.advanceEachPlayerNonlandExile(gameData)) return;
        }
        finishSearchAndResume(gameData);
    }

    /**
     * The card of the ability that searched the library, which keeps the source of any delayed trigger it creates.
     * If that source has already left the battlefield, the card of the resolving entry is used instead.
     */
    private Card delayedTriggerSourceCard(GameData gameData, LibrarySearchParams librarySearch) {
        if (librarySearch.sourcePermanentId() == null) {
            return null;
        }
        Permanent searchSource = gameQueryService.findPermanentById(gameData, librarySearch.sourcePermanentId());
        if (searchSource != null) {
            return searchSource.getCard();
        }
        return gameData.pendingEffectResolutionEntry != null ? gameData.pendingEffectResolutionEntry.getCard() : null;
    }

    private void finishSearchAndResume(GameData gameData) {
        StackEntry pending = gameData.pendingEffectResolutionEntry;
        if (pending != null) {
            // A found card's entry choice (e.g. "enter as a copy") is still open; answering it finishes
            // that entry and then resumes the rest of the effect.
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            effectResolutionService.resolveEffectsFrom(gameData, pending, gameData.pendingEffectResolutionIndex);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }
        }
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void performStateBasedActionsIfResolutionComplete(GameData gameData) {
        StackEntry pending = gameData.pendingEffectResolutionEntry;
        if (pending != null && gameData.pendingEffectResolutionIndex < pending.getEffectsToResolve().size()) {
            return;
        }
        stateBasedActionService.performStateBasedActions(gameData);
    }

    /**
     * Places multiple cards onto the battlefield simultaneously per CR 608.2f.
     * All permanents are added first, then ETB triggers are processed after all are on the battlefield.
     *
     * <p>{@code animateFound} (Nissa, Worldwaker) animates each placed permanent once every one of
     * them is on the battlefield: the cards enter as what they are printed as — so a basic land does
     * not fire creature-entered triggers — and only then become creatures. Its power/toughness are
     * evaluated without a stack entry, so they must be constant amounts.</p>
     */
    private void placeCardsOnBattlefieldSimultaneously(GameData gameData, List<Card> cards,
                                                        UUID ownerId, boolean tapped,
                                                        boolean grantHaste, boolean exileAtEndStep,
                                                        boolean returnToHandAtEndStep,
                                                        AnimatePermanentsEffect animateFound,
                                                        CounterType battlefieldCounter,
                                                        com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect enterWithCounters) {
        placeCardsOnBattlefieldSimultaneously(gameData, cards, ownerId, tapped, grantHaste, exileAtEndStep,
                returnToHandAtEndStep, animateFound, battlefieldCounter, enterWithCounters, false);
    }

    public void completeTappedEntryStateChoice(GameData gameData,
            com.github.laxika.magicalvibes.model.BattlefieldEntryRequest request) {
        if (battlefieldEntryBatchSupport.completeNativeChoice(gameData, request)) {
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }
        battlefieldPlacementService.place(gameData, request);
        finishResumedEntry(gameData, request);
    }

    /**
     * Applies the controller's choice of counter replacement order for an entering permanent and,
     * once its entry completes, handles it as having entered the battlefield (CR 616.1).
     */
    public void completeEntryCounterReplacementOrder(GameData gameData,
            com.github.laxika.magicalvibes.model.ChoiceContext.EntryCounterReplacementOrder order, String label) {
        if (battlefieldPlacementService.resolveEntryCounterReplacementOrder(gameData, order, label)) {
            finishResumedEntry(gameData, order.request());
        } else {
            inputCompletionService.publishStateAfterInput(gameData);
        }
    }

    private void finishResumedEntry(GameData gameData,
            com.github.laxika.magicalvibes.model.BattlefieldEntryRequest request) {
        StackEntry sourceEntry = request.sourceStackEntry();
        int etbMode = request.xValue();
        asEntersInteractionService.handleCreatureEnteredBattlefield(gameData, request.controllerId(),
                request.permanent().getCard(), sourceEntry == null ? null : sourceEntry.getTargetId(),
                request.permanent().getCastFromZone() == Zone.HAND,
                etbMode, request.xValue(), request.kicked(),
                sourceEntry == null ? List.of() : sourceEntry.getTargetIds(), request.repeatedAdditionalCosts(),
                sourceEntry == null ? List.of() : sourceEntry.getConvokeCreatureIds());
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private List<Permanent> placeCardsOnBattlefieldSimultaneously(GameData gameData, List<Card> cards,
                                                        UUID ownerId, boolean tapped,
                                                        boolean grantHaste, boolean exileAtEndStep,
                                                        boolean returnToHandAtEndStep,
                                                        AnimatePermanentsEffect animateFound,
                                                        CounterType battlefieldCounter,
                                                        com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect enterWithCounters,
                                                        boolean cloaked) {
        return placeCardsOnBattlefieldSimultaneously(gameData, cards, ownerId, tapped, grantHaste,
                exileAtEndStep, returnToHandAtEndStep, animateFound, battlefieldCounter,
                enterWithCounters, cloaked, ownerId);
    }

    private List<Permanent> placeCardsOnBattlefieldSimultaneously(GameData gameData, List<Card> cards,
                                                        UUID ownerId, boolean tapped,
                                                        boolean grantHaste, boolean exileAtEndStep,
                                                        boolean returnToHandAtEndStep,
                                                        AnimatePermanentsEffect animateFound,
                                                        CounterType battlefieldCounter,
                                                        com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect enterWithCounters,
                                                        boolean cloaked, UUID puttingPlayerId) {
        List<Permanent> permanents = new ArrayList<>();
        List<Card> placedCards = new ArrayList<>();
        String ownerName = gameData.playerIdToName.get(ownerId);

        // Snapshot enter-tapped types ONCE before any permanent enters (CR 608.2f)
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);

        // Grafdigger's Cage etc.: matching cards (e.g. creature cards) in libraries can't enter the
        // battlefield. Any blocked card stays in the library, which is then shuffled.
        boolean anyBlocked = false;

        // Phase 1: Place all permanents on the battlefield simultaneously. Carrying the batch keeps
        // them from applying their own replacement/static abilities to each other (CR 614.12), the
        // same way the enter-tapped snapshot above is frozen before any of them enters.
        List<Permanent> batch = new ArrayList<>();
        for (Card card : cards) {
            // A card already in exile (Sword of Hearth and Home's exiled creature) enters together with
            // the cards found in the library.
            boolean fromExile = gameData.findExiledCard(card.getId()) != null;
            if (!fromExile && gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)) {
                gameData.playerDecks.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(card);
                anyBlocked = true;
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " can't enter the battlefield from a library; it stays in the library."));
                continue;
            }
            if (fromExile) {
                gameData.removeFromExile(card.getId());
            }
            Permanent perm = new Permanent(card, fromExile ? Zone.EXILE : Zone.LIBRARY);
            if (fromExile) {
                perm.setEnteredFromExile(true);
            }
            var landEquilibriumPlan = puttingPlayerId.equals(ownerId) ? null
                    : landEquilibriumSupport.findPlan(gameData, puttingPlayerId, perm);
            if (cloaked) {
                perm.setFaceDownAsCloaked();
            }
            if (grantHaste) {
                gameData.addEffectOnPermanent(null, null, puttingPlayerId, new GrantKeywordEffect(Keyword.HASTE, Subjects.target()), perm.getId(), Duration.UNTIL_END_OF_TURN);
            }
            if (tapped) perm.tap();
            Runnable placeCard = () -> battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, ownerId, perm, enterTappedTypes, batch, enterWithCounters);
            if (puttingPlayerId.equals(ownerId)) {
                placeCard.run();
            } else {
                landEquilibriumSupport.runWithPutterOtherThanController(placeCard);
            }
            landEquilibriumSupport.applyPlan(gameData, puttingPlayerId, perm, landEquilibriumPlan, null);
            placeBattlefieldCounter(gameData, perm, battlefieldCounter);
            batch.add(perm);
            StackEntry creatingEntry = gameData.pendingEffectResolutionEntry;
            if (returnToHandAtEndStep) {
                // The delayed return belongs to the ability that searched (Nahiri, the Harbinger's -8),
                // not to the fetched permanent, even after control changes or the source has left.
                DelayedPermanentActions.scheduleForResolution(gameData, puttingPlayerId, perm,
                        new DelayedPermanentAction(perm.getId(), DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP),
                        ActivePlayer.ANY);
            } else if (exileAtEndStep) {
                // CR 603.7e: the delayed exile keeps the searching ability's source and controller.
                DelayedPermanentActions.scheduleForResolution(gameData, puttingPlayerId, perm,
                        new DelayedPermanentAction(perm.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP),
                        ActivePlayer.ANY);
            }
            permanents.add(perm);
            placedCards.add(card);

            if (cloaked) {
                gameLogService.append(gameData, GameLog.text(
                        ownerName + " puts a card onto the battlefield face down."));
            } else if (tapped) {
                gameLogService.append(gameData, GameLog.entersBattlefieldTappedUnder(card, ownerName));
            } else {
                gameLogService.append(gameData, GameLog.entersBattlefieldUnder(card, ownerName));
            }
        }

        if (cards.size() == 1 && permanents.size() == 1 && gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.setChosenPermanentId(permanents.getFirst().getId());
        }
        if (!permanents.isEmpty() && gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.setSearchedPermanentIds(
                    permanents.stream().map(Permanent::getId).toList());
        }

        if (anyBlocked) {
            java.util.Collections.shuffle(gameData.playerDecks.get(ownerId));
        }

        // Phase 2: Process ETB triggers after all permanents are on the battlefield (CR 608.2f)
        for (int i = 0; i < placedCards.size(); i++) {
            Card card = placedCards.get(i);
            Permanent perm = permanents.get(i);

            if (cloaked) {
                battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, ownerId, card);
            } else {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
            }
            if (!cloaked && card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                perm.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
                perm.setSummoningSick(false);
            }
        }

        // Animation follows the enter-the-battlefield triggers: the permanents enter as non-creature
        // lands and only then become creatures, so creature-entering triggers must not see them as creatures.
        if (animateFound != null && !permanents.isEmpty()) {
            StackEntry parked = gameData.pendingEffectResolutionEntry;
            String sourceName = parked != null && parked.getCard() != null ? parked.getCard().getName() : "";
            UUID sourcePermanentId = parked != null ? parked.getSourcePermanentId() : null;
            var ctx = com.github.laxika.magicalvibes.service.effect.AmountContext.forEstimation(ownerId);
            int power = amountEvaluationService.evaluate(gameData, animateFound.power(), ctx);
            int toughness = amountEvaluationService.evaluate(gameData, animateFound.toughness(), ctx);
            for (Permanent perm : permanents) {
                animationSupport.animatePermanently(gameData, perm, animateFound, power, toughness,
                        sourceName, sourcePermanentId, ownerId);
            }
        }

        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, ownerId);
        }
        return permanents;
    }

    private void placeBattlefieldCounter(GameData gameData, Permanent permanent, CounterType counterType) {
        if (counterType != null) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, gameData.pendingEffectResolutionEntry, permanent, counterType, 1);
        }
    }

    /**
     * If a pending basic-land-to-hand search is queued (e.g. Cultivate second pick),
     * starts the follow-up library search and returns true. Otherwise returns false.
     */
    private boolean startPendingBasicLandToHandSearch(GameData gameData, UUID playerId, LibrarySearchFollowUp followUp,
                                                       List<Card> originalSearchCards) {
        LibrarySearchFollowUp.BasicLandToHandPick pick = followUp.basicLandToHand();
        if (pick == null) return false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        List<Card> matchingCards = deck.stream()
                .filter(card -> pick.filter() != null
                        ? predicateEvaluationService.matchesCardPredicate(
                                card, pick.filter(), null, gameData, playerId)
                        : !pick.basicOnly()
                                || card.hasAllCardNames()
                                || (card.hasType(CardType.LAND)
                                && gameQueryService.cardHasSupertype(
                                card, CardSupertype.BASIC, gameData, playerId)))
                .filter(card -> pick.subtype() == null || card.getSubtypes().contains(pick.subtype()))
                .filter(originalSearchCards::contains)
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            String description = pick.description() != null
                    ? pick.description() + " cards"
                    : pick.subtype() == null
                    ? (pick.basicOnly() ? "basic land cards" : "land cards")
                    : (pick.basicOnly() ? "basic " : "") + pick.subtype().getDisplayName() + " cards";
            String logMsg = playerName + " finds no more " + description + ". Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return false;
        }

        String cardDescription = pick.description() != null
                ? pick.description()
                : pick.subtype() == null
                ? (pick.basicOnly() ? "basic land" : "land")
                : (pick.basicOnly() ? "basic " : "") + pick.subtype().getDisplayName();
        String prompt = "Search your library for a " + cardDescription + " card to put into your hand.";
        LibrarySearchFollowUp remaining = followUp.withBasicLandToHand(pick.decremented());
        LibrarySearchParams params = LibrarySearchParams.builder(playerId, new ArrayList<>(matchingCards))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.HAND)
                .shuffleAfterSelection(remaining.basicLandToHand() == null)
                .followUp(remaining)
                .build();

        beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));
        return true;
    }

    /**
     * If any card of the next bounded-pick category remains among the looked-at cards, begins that
     * pick and returns true. Missing subtype categories are skipped. Otherwise returns false so the
     * caller disposes the remaining cards immediately.
     */
    private boolean startSecondBoundedPick(GameData gameData, UUID controllerId, List<Card> lookedAtCards,
            List<Card> accumulatedCards, LibrarySearchFollowUp.SecondBoundedPick spec) {
        if (spec.type() == null && spec.subtype() == null && spec.predicate() == null) {
            return false;
        }

        List<Card> eligible = lookedAtCards.stream()
                .filter(card -> spec.predicate() != null
                        ? predicateEvaluationService.matchesCardPredicate(
                                card, spec.predicate(), null, gameData, controllerId)
                        : spec.subtype() != null
                                ? predicateEvaluationService.matchesCardPredicate(
                                        card, new CardSubtypePredicate(spec.subtype()), null, gameData, controllerId)
                                : card.hasType(spec.type()))
                .toList();
        if (eligible.isEmpty()) {
            if (!spec.remainingPredicatePicks().isEmpty()) {
                LibrarySearchFollowUp.SecondBoundedPick.PredicatePick next =
                        spec.remainingPredicatePicks().getFirst();
                return startSecondBoundedPick(gameData, controllerId, lookedAtCards, accumulatedCards,
                        LibrarySearchFollowUp.SecondBoundedPick.predicate(
                                next.predicate(), next.prompt(), spec.randomRest(), spec.destination(),
                                spec.remainingPredicatePicks().subList(1,
                                        spec.remainingPredicatePicks().size())));
            }
            if (!spec.remainingSubtypes().isEmpty()) {
                return startSecondBoundedPick(gameData, controllerId, lookedAtCards, accumulatedCards,
                        LibrarySearchFollowUp.SecondBoundedPick.subtype(
                                spec.remainingSubtypes().getFirst(),
                                spec.remainingSubtypes().subList(1, spec.remainingSubtypes().size()),
                                spec.randomRest(), spec.destination()));
            }
            if (!spec.remainingTypes().isEmpty()) {
                return startSecondBoundedPick(gameData, controllerId, lookedAtCards, accumulatedCards,
                        LibrarySearchFollowUp.SecondBoundedPick.cardType(
                                spec.remainingTypes().getFirst(),
                                spec.remainingTypes().subList(1, spec.remainingTypes().size())));
            }
            return false;
        }

        String category = spec.prompt() != null ? spec.prompt() : spec.subtype() != null
                ? spec.subtype().getDisplayName().toLowerCase()
                : spec.type().getDisplayName().toLowerCase();
        String destinationPhrase = spec.destination() == LibrarySearchDestination.BATTLEFIELD
                ? "onto the battlefield" : "into your hand";
        String prompt = isHoldOutThenDestination(spec.destination())
                ? "Choose " + (spec.prompt() != null ? category : "a " + category + " card") + " from among them."
                : spec.prompt() != null
                ? (spec.destination() == LibrarySearchDestination.HOLD_OUT
                        ? "Choose " + category + " from among them."
                        : "You may reveal " + category + " from among them and put it " + destinationPhrase + ".")
                : "You may reveal a " + category
                        + " card from among them and put it " + destinationPhrase + ".";
        LibrarySearchFollowUp nextFollowUp;
        if (spec.predicate() != null && !spec.remainingPredicatePicks().isEmpty()) {
            LibrarySearchFollowUp.SecondBoundedPick.PredicatePick next =
                    spec.remainingPredicatePicks().getFirst();
            nextFollowUp = LibrarySearchFollowUp.forBoundedPick(
                    LibrarySearchFollowUp.SecondBoundedPick.predicate(
                            next.predicate(), next.prompt(), spec.randomRest(), spec.destination(),
                            spec.remainingPredicatePicks().subList(1,
                                    spec.remainingPredicatePicks().size())));
        } else if (spec.subtype() != null) {
            nextFollowUp = LibrarySearchFollowUp.forSubtypeBoundedPick(
                    spec.remainingSubtypes(), spec.randomRest(), spec.destination());
        } else if (!spec.remainingTypes().isEmpty()) {
            nextFollowUp = LibrarySearchFollowUp.forCardTypeBoundedPick(spec.remainingTypes());
        } else if (spec.randomRest()) {
            nextFollowUp = LibrarySearchFollowUp.forBoundedPick(
                    LibrarySearchFollowUp.SecondBoundedPick.terminal(true, spec.destination()));
        } else {
            nextFollowUp = LibrarySearchFollowUp.NONE;
        }
        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(eligible))
                .reveals(true)
                .canFailToFind(spec.destination() != LibrarySearchDestination.HOLD_OUT
                        && !(isHoldOutThenDestination(spec.destination()) && accumulatedCards.isEmpty()))
                .destination(spec.destination())
                .sourceCards(new ArrayList<>(lookedAtCards))
                .accumulatedCards(new ArrayList<>(accumulatedCards))
                .reorderRemainingToBottom(!spec.restToGraveyard())
                .restToGraveyard(spec.restToGraveyard())
                .shuffleAfterSelection(false)
                .placeBattlefieldCardsSimultaneously(spec.destination() == LibrarySearchDestination.BATTLEFIELD)
                .followUp(nextFollowUp)
                .build();
        beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));
        return true;
    }

    private static boolean isHoldOutThenDestination(LibrarySearchDestination destination) {
        return destination == LibrarySearchDestination.HOLD_OUT_THEN_HAND
                || destination == LibrarySearchDestination.HOLD_OUT_THEN_BATTLEFIELD_OR_HAND;
    }

    /**
     * One pick of a bounded "choose a creature card and/or a land card" look: the chosen cards are held
     * out until every pick is made, the rest go to the bottom in a random order, and the held cards then
     * go into hand or, when the controller decides after choosing, all onto the battlefield or all into
     * hand. While nothing has been chosen the pick cannot be declined.
     */
    private void handleHeldCardsChoice(GameData gameData, Player player, int cardIndex, boolean canFailToFind,
                                       List<Card> searchCards, List<Card> sourceCards,
                                       List<Card> accumulatedCards, LibrarySearchFollowUp followUp,
                                       LibrarySearchDestination destination, UUID deckOwnerId) {
        if (cardIndex == -1) {
            if (!canFailToFind) {
                throw new IllegalStateException("At least one card must be chosen");
            }
        } else {
            if (cardIndex < 0 || cardIndex >= searchCards.size()) {
                throw new IllegalStateException("Invalid card choice");
            }
            Card chosenCard = searchCards.get(cardIndex);
            sourceCards.removeIf(card -> card.getId().equals(chosenCard.getId()));
            accumulatedCards.add(chosenCard);
        }

        if (followUp.secondBoundedPick() != null
                && startSecondBoundedPick(gameData, player.getId(), sourceCards, accumulatedCards,
                        followUp.secondBoundedPick())) {
            return;
        }

        if (!sourceCards.isEmpty()) {
            List<Card> rest = new ArrayList<>(sourceCards);
            Collections.shuffle(rest);
            gameData.playerDecks.get(deckOwnerId).addAll(rest);
            gameLogService.append(gameData, GameLog.text(player.getUsername()
                    + " puts the unchosen cards on the bottom of their library in a random order."));
        }
        if (accumulatedCards.isEmpty()) {
            finishSearchAndResume(gameData);
            return;
        }
        if (destination == LibrarySearchDestination.HOLD_OUT_THEN_HAND) {
            putHeldCardsIntoHand(gameData, player, accumulatedCards);
            finishSearchAndResume(gameData);
            return;
        }
        ChoiceContext.HeldCardsZoneChoice zoneChoice =
                new ChoiceContext.HeldCardsZoneChoice(player.getId(), new ArrayList<>(accumulatedCards));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                player.getId(), null, null, zoneChoice, List.of(HELD_TO_BATTLEFIELD, HELD_TO_HAND),
                "Put the chosen cards onto the battlefield or into your hand."));
    }

    private void putHeldCardsIntoHand(GameData gameData, Player player, List<Card> cards) {
        for (Card card : cards) {
            gameData.addCardToHand(player.getId(), card);
        }
        GameLog.Builder builder = GameLog.builder().text(player.getUsername() + " puts ");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) builder.text(", ");
            builder.card(cards.get(i));
        }
        gameLogService.append(gameData, builder.text(" into their hand.").build());
    }

    /** Applies the controller's battlefield-or-hand decision for the cards held out by a library look. */
    public void completeHeldCardsZoneChoice(GameData gameData, Player player,
                                            ChoiceContext.HeldCardsZoneChoice choice, String label) {
        if (!HELD_TO_BATTLEFIELD.equals(label) && !HELD_TO_HAND.equals(label)) {
            throw new IllegalArgumentException("Invalid choice: " + label);
        }
        gameData.interaction.clearAwaitingInput();
        if (HELD_TO_BATTLEFIELD.equals(label)) {
            placeCardsOnBattlefieldSimultaneously(gameData, choice.cards(), player.getId(), false,
                    false, false, false, null, null, null);
            performStateBasedActionsIfResolutionComplete(gameData);
        } else {
            putHeldCardsIntoHand(gameData, player, choice.cards());
        }
        finishSearchAndResume(gameData);
    }

    private void handleHeldOutCardChoice(GameData gameData, Player player, int cardIndex,
                                          List<Card> searchCards, List<Card> sourceCards,
                                          List<Card> accumulatedCards,
                                          LibrarySearchFollowUp followUp) {
        if (cardIndex < 0 || cardIndex >= searchCards.size()) {
            throw new IllegalStateException("A held-out card choice is mandatory");
        }

        Card chosenCard = searchCards.get(cardIndex);
        sourceCards.removeIf(card -> card.getId().equals(chosenCard.getId()));
        accumulatedCards.add(chosenCard);

        if (followUp.secondBoundedPick() != null
                && startSecondBoundedPick(gameData, player.getId(), sourceCards, accumulatedCards,
                        followUp.secondBoundedPick())) {
            return;
        }

        for (Card card : sourceCards) {
            graveyardService.addCardToGraveyard(gameData, player.getId(), card, Zone.LIBRARY);
        }

        if (accumulatedCards.isEmpty()) {
            finishSearchAndResume(gameData);
            return;
        }

        String prompt = "Choose one of the selected cards to put onto the battlefield; put the rest into your hand.";
        LibrarySearchParams params = LibrarySearchParams.builder(player.getId(), new ArrayList<>(accumulatedCards))
                .reveals(true)
                .canFailToFind(false)
                .destination(LibrarySearchDestination.BATTLEFIELD_ONE_AND_PUT_REST_INTO_HAND)
                .sourceCards(new ArrayList<>(accumulatedCards))
                .restToGraveyard(true)
                .shuffleAfterSelection(false)
                .prompt(prompt)
                .build();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, false));
    }

    private void handleBattlefieldOneAndPutRestIntoHandChoice(GameData gameData, Player player,
                                                               int cardIndex, List<Card> searchCards,
                                                               List<Card> sourceCards,
                                                               boolean tapped, boolean shuffleAfter) {
        if (cardIndex < 0 || cardIndex >= searchCards.size()) {
            throw new IllegalStateException("A battlefield card choice is mandatory");
        }

        Card chosenCard = searchCards.get(cardIndex);
        List<Card> handCards = sourceCards.stream()
                .filter(card -> !card.getId().equals(chosenCard.getId()))
                .toList();
        placeCardsOnBattlefieldSimultaneously(gameData, List.of(chosenCard), player.getId(),
                tapped, false, false, false, null, null, null);
        for (Card card : handCards) {
            gameData.addCardToHand(player.getId(), card);
        }
        if (!handCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " puts the other selected cards into their hand."));
        }
        if (shuffleAfter) {
            LibraryShuffleHelper.shuffleLibrary(gameData, player.getId());
        }
        performStateBasedActionsIfResolutionComplete(gameData);
        finishSearchAndResume(gameData);
    }

    /**
     * If a pending card-to-graveyard search is queued (Final Parting / Jarad's Orders second pick),
     * starts the follow-up library search and returns true. Otherwise returns false.
     */
    private boolean startPendingCardToGraveyardSearch(GameData gameData, UUID playerId, LibrarySearchFollowUp followUp) {
        LibrarySearchFollowUp.CardToGraveyardPick pick = followUp.cardToGraveyard();
        if (pick == null) return false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (deck.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            String logMsg = playerName + " finds no more cards. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return false;
        }

        List<Card> candidates;
        if (pick.filter() == null) {
            candidates = new ArrayList<>(deck);
        } else {
            candidates = deck.stream()
                    .filter(c -> predicateEvaluationService.matchesCardPredicate(c, pick.filter(), null, gameData, playerId))
                    .toList();
        }

        if (candidates.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            String logMsg = playerName + " finds no more matching cards. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return false;
        }

        String prompt = pick.filter() == null
                ? "Search your library for a card to put into your graveyard."
                : "Search your library for a " + CardPredicateUtils.describeFilter(pick.filter())
                        + " to put into your graveyard.";
        LibrarySearchParams params = LibrarySearchParams.builder(playerId, candidates)
                .reveals(pick.reveals())
                .canFailToFind(pick.canFailToFind())
                .destination(LibrarySearchDestination.GRAVEYARD)
                .filterPredicate(pick.filter())
                .followUp(followUp.clearCardToGraveyard())
                .build();

        beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(params, prompt, pick.canFailToFind()));
        return true;
    }

    public void handleLibraryRevealChoice(GameData gameData, Player player, List<UUID> cardIds) {
        PendingInteraction.LibraryRevealChoice libraryRevealChoice =
                gameData.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        if (libraryRevealChoice == null || !player.getId().equals(libraryRevealChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> validIds = libraryRevealChoice.validCardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }

        for (UUID cardId : cardIds) {
            if (!validIds.contains(cardId)) {
                throw new IllegalStateException("Invalid card: " + cardId);
            }
        }

        Set<UUID> uniqueIds = new HashSet<>(cardIds);
        if (uniqueIds.size() != cardIds.size()) {
            throw new IllegalStateException("Duplicate card IDs in selection");
        }
        if (cardIds.size() < libraryRevealChoice.minCount()
                || cardIds.size() > libraryRevealChoice.maxCount()) {
            throw new IllegalStateException("Invalid number of cards selected");
        }
        if (libraryRevealChoice.totalManaValueBound() != null) {
            int selectedManaValue = allCardsMatchingIds(libraryRevealChoice.allCards(), cardIds).stream()
                    .mapToInt(Card::getManaValue)
                    .sum();
            if (selectedManaValue > libraryRevealChoice.totalManaValueBound()) {
                throw new IllegalStateException("Selected cards exceed the total mana value limit of "
                        + libraryRevealChoice.totalManaValueBound());
            }
        }

        UUID controllerId = libraryRevealChoice.playerId();
        List<Card> allRevealedCards = libraryRevealChoice.allCards();
        if (libraryRevealChoice.requireDistinctPowers()) {
            Set<Integer> selectedPowers = new HashSet<>();
            for (Card card : allRevealedCards) {
                if (cardIds.contains(card.getId())) {
                    Integer currentPower = gameQueryService.getEffectiveCardPower(gameData, card);
                    int power = currentPower == null ? 0 : currentPower;
                    if (!selectedPowers.add(power)) {
                        throw new IllegalStateException("Selected cards must have different powers");
                    }
                }
            }
        }

        // Validate before touching interaction state, joining the three checks above: a rejected
        // answer must leave the prompt standing so the player can answer again. Clearing first and
        // then throwing destroys the only thing that would resume the entry parked in
        // pendingEffectResolutionEntry, wedging the game on a stale client answer. The life-payment
        // branches below read nothing the clear touches, so hoisting their affordability checks is
        // a pure move; the inner checks stay as defence.
        if (libraryRevealChoice.lifeCostPerSelection() > 0 && libraryRevealChoice.beneficiaryPlayerId() != null) {
            requirePunisherLifeAffordable(gameData, cardIds.size(),
                    libraryRevealChoice.beneficiaryPlayerId(), libraryRevealChoice.lifeCostPerSelection());
        }
        if (isControllerLifePaymentChoice(libraryRevealChoice)) {
            requireControllerLifeAffordable(gameData, cardIds.size(), controllerId,
                    libraryRevealChoice.lifeCostPerSelection());
        }

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();

        if (!asksSelectionLifeChangeFirst(gameData, player, libraryRevealChoice, cardIds)) {
            completeLibraryRevealChoice(gameData, player, libraryRevealChoice, cardIds);
        }
    }

    /**
     * Completes a validated library reveal choice ({@link #handleLibraryRevealChoice}).
     */
    private void completeLibraryRevealChoice(GameData gameData, Player player,
                                             PendingInteraction.LibraryRevealChoice libraryRevealChoice,
                                             List<UUID> cardIds) {
        UUID controllerId = libraryRevealChoice.playerId();
        List<Card> allRevealedCards = libraryRevealChoice.allCards();
        String playerName = gameData.playerIdToName.get(controllerId);

        PendingPsychoticEpisodeChoice psychoticEpisodeChoice =
                gameData.pollPendingInteraction(PendingPsychoticEpisodeChoice.class);
        if (psychoticEpisodeChoice != null) {
            handlePsychoticEpisodeChoice(gameData, player, psychoticEpisodeChoice,
                    allRevealedCards, cardIds);
            return;
        }

        if (gameData.hasPendingInteraction(PendingManifoldInsightsChoice.class)) {
            boolean finished = manifoldInsightsEffectHandler.completeCardChoice(
                    gameData, cardIds);
            if (finished) {
                finishSearchAndResume(gameData);
            }
            return;
        }

        if (gameData.hasPendingInteraction(PendingAllureOfTheUnknownChoice.class)) {
            handleAllureOfTheUnknownRevealChoice(gameData, allRevealedCards, cardIds);
            finishSearchAndResume(gameData);
            return;
        } else if (gameData.hasPendingInteraction(PendingMemoriesReturningChoice.class)) {
            handleMemoriesReturningChoice(gameData, allRevealedCards, cardIds);
            return;
        }

        PendingDubiousChallengeChoice dubiousChallenge =
                gameData.pollPendingInteraction(PendingDubiousChallengeChoice.class);
        if (dubiousChallenge != null) {
            if (dubiousChallenge.exiledCards().isEmpty()) {
                handleDubiousChallengeInitialChoice(gameData, allRevealedCards, cardIds, dubiousChallenge);
            } else {
                handleDubiousChallengeOpponentChoice(gameData, allRevealedCards, cardIds, dubiousChallenge);
            }
            return;
        }

        // Endless Horizons upkeep: controller chose which card exiled with the source to return.
        if (gameData.hasPendingInteraction(PendingReturnExiledWithSourceCard.class)) {
            handleReturnExiledWithSourceCard(gameData, allRevealedCards, cardIds, controllerId);
            return;
        }

        // Phyrexian Portal: controller searched the kept pile; the rest shuffles into their library.
        if (gameData.hasPendingInteraction(PendingPortalPileSearch.class)) {
            handlePortalPileSearch(gameData, allRevealedCards, cardIds, controllerId);
            return;
        }

        // Punisher reveal (Sword-Point Diplomacy etc.): opponent chose which cards to deny (paying life)
        if (libraryRevealChoice.lifeCostPerSelection() > 0 && libraryRevealChoice.beneficiaryPlayerId() != null) {
            handlePunisherRevealChoice(gameData, allRevealedCards, cardIds,
                    libraryRevealChoice.beneficiaryPlayerId(), libraryRevealChoice.lifeCostPerSelection());
            return;
        }

        // Separate selected cards from the rest
        Set<UUID> selectedIds = new HashSet<>(cardIds);
        List<Card> selectedCards = new ArrayList<>();
        List<Card> remainingCards = new ArrayList<>();
        for (Card card : allRevealedCards) {
            if (selectedIds.contains(card.getId())) {
                selectedCards.add(card);
            } else {
                remainingCards.add(card);
            }
        }

        if (libraryRevealChoice.selectLandsAfterHand()) {
            handleHandSelectionThenLandSelection(gameData, controllerId, playerName,
                    selectedCards, remainingCards);
            return;
        }

        if (libraryRevealChoice.selectedToTop()) {
            if (!selectedCards.isEmpty()) {
                GameLog.Builder revealBuilder = GameLog.builder().text(playerName + " reveals ");
                for (int i = 0; i < selectedCards.size(); i++) {
                    if (i > 0) revealBuilder.text(", ");
                    revealBuilder.card(selectedCards.get(i));
                }
                gameLogService.append(gameData, revealBuilder.text(".").build());
            }
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    controllerId, allRevealedCards, true, controllerId,
                    "Put the selected cards on top of your library and the rest on the bottom in any order.",
                    cardIds));
            return;
        }

        if (libraryRevealChoice.recordSelectedCount() && gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.setEventValue(selectedCards.size());
        }

        if (libraryRevealChoice.selectedToManifest()) {
            resolveManifestChoice(gameData, controllerId, selectedCards, remainingCards,
                    libraryRevealChoice.remainingToGraveyard());
            return;
        }

        if (libraryRevealChoice.selectedCardMayGoToHandIfBattlefieldDeclined()) {
            resolveMayPutSelectedCardOntoBattlefieldElseToHand(
                    gameData, libraryRevealChoice, controllerId, selectedCards, remainingCards);
            return;
        }

        if (libraryRevealChoice.selectedToHand()) {
            insertSelectedCardFollowUpIfApplicable(gameData, selectedCards, libraryRevealChoice);
            boolean controllerLifePayment = libraryRevealChoice.payLifePerSelection();
            resolveRevealChoiceToHand(gameData, controllerId, playerName, selectedCards, remainingCards,
                    libraryRevealChoice.reorderRemainingToBottom(), libraryRevealChoice.remainingToGraveyard(),
                    libraryRevealChoice.remainingToExile(), libraryRevealChoice.randomRemainingToBottom(),
                    libraryRevealChoice.gainLifeEqualToSelectedCardManaValue(),
                    gameData.pendingEffectResolutionEntry, libraryRevealChoice.effectIfNoCardChosen(),
                    controllerLifePayment ? 0 : libraryRevealChoice.lifeCostPerSelection(),
                    controllerLifePayment ? libraryRevealChoice.lifeCostPerSelection() : 0,
                    libraryRevealChoice.revealSelected());
            return;
        }

        if (libraryRevealChoice.selectedCardsToBattlefieldType() != null) {
            resolveRevealChoiceSplitByType(gameData, controllerId, playerName, selectedCards, remainingCards,
                    libraryRevealChoice.selectedCardsToBattlefieldType(),
                    libraryRevealChoice.selectedToBattlefieldTapped());
            return;
        }

        // Put selected cards onto the battlefield
        // A cloaked card is a 2/2 face-down creature card as it enters (CR 701.58a), so creature-only
        // prohibitions such as Grafdigger's Cage stop it even when the card itself is not a creature.
        // Cards exiled before the choice (Xenagos, the Reveler) enter from exile, so library-only
        // restrictions such as Grafdigger's Cage don't apply to them.
        List<Card> blockedCards = selectedCards.stream()
                .filter(card -> gameData.findExiledCard(card.getId()) == null)
                .filter(card -> libraryRevealChoice.selectedToBattlefieldCloaked()
                        ? gameQueryService.isManifestBlockedFromZone(gameData, card, Zone.LIBRARY)
                        : gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY))
                .toList();
        selectedCards.removeAll(blockedCards);
        remainingCards.addAll(blockedCards);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        List<UUID> selectedPermanentIds = new ArrayList<>();
        if (!libraryRevealChoice.selectedToBattlefieldCloaked()
                && libraryRevealChoice.battlefieldEntryReplacement() == null
                && libraryRevealChoice.battlefieldSelectionFollowUp() == null
                && selectedCards.stream().allMatch(card -> isTakenFromEveryZone(gameData, card))) {
            // The chosen cards enter in one event (CR 603.6a); each Aura's host is chosen as it enters
            // (CR 303.4f) among objects already on the battlefield. They go back into the library they
            // were revealed from so one that can't enter stays there.
            List<Card> library = gameData.playerDecks.get(controllerId);
            library.addAll(selectedCards);
            battlefieldEntryBatchSupport.begin(gameData, selectedCards.stream()
                    .map(card -> new com.github.laxika.magicalvibes.model.BattlefieldEntryCard(
                            controllerId, controllerId, card, Zone.LIBRARY, null)
                            .withTapped(libraryRevealChoice.selectedToBattlefieldTapped()))
                    .toList());
            if (!gameData.interaction.isAwaitingInput()) {
                List<UUID> enteredIds = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                        .filter(permanent -> selectedIds.contains(permanent.getOriginalCard().getId()))
                        .map(Permanent::getId)
                        .toList();
                StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
                if (pendingEntry != null && selectedCards.size() == 1 && enteredIds.size() == 1) {
                    pendingEntry.setChosenPermanentId(enteredIds.getFirst());
                }
                if (pendingEntry != null && !enteredIds.isEmpty()
                        && libraryRevealChoice.selectedToBattlefieldSimultaneously()) {
                    pendingEntry.setSearchedPermanentIds(enteredIds);
                }
                selectedCards.removeIf(library::contains);
            }
        } else if (selectedCards.size() == 1 && selectedCards.getFirst().isAura()
                && !libraryRevealChoice.selectedToBattlefieldCloaked()
                && !libraryRevealChoice.selectedToBattlefieldTapped()
                && libraryRevealChoice.battlefieldEntryReplacement() == null) {
            Card aura = selectedCards.getFirst();
            List<Card> library = gameData.playerDecks.get(controllerId);
            library.add(aura);
            battlefieldEntryBatchSupport.begin(gameData, List.of(
                    new com.github.laxika.magicalvibes.model.BattlefieldEntryCard(
                            controllerId, controllerId, aura, Zone.LIBRARY, null)));
        } else if (selectedCards.size() == 1 && selectedCards.getFirst().isAura()
                && !libraryRevealChoice.selectedToBattlefieldCloaked()
                && !libraryRevealChoice.selectedToBattlefieldTapped()
                && libraryRevealChoice.battlefieldEntryReplacement() != null) {
            Card aura = selectedCards.getFirst();
            if (!battlefieldEntryBatchSupport.beginSingleAuraHostChoice(
                    gameData, aura, controllerId, libraryRevealChoice.battlefieldEntryReplacement())) {
                remainingCards.add(aura);
            }
        } else if (libraryRevealChoice.selectedToBattlefieldSimultaneously()) {
            List<Permanent> entered = placeCardsOnBattlefieldSimultaneously(gameData, selectedCards, controllerId,
                    libraryRevealChoice.selectedToBattlefieldTapped(), false, false, false,
                    null, null, libraryRevealChoice.battlefieldEntryReplacement(),
                    libraryRevealChoice.selectedToBattlefieldCloaked());
            entered.stream().map(Permanent::getId).forEach(selectedPermanentIds::add);
        } else {
            List<Permanent> simultaneouslyEntered = new ArrayList<>();
            for (Card card : selectedCards) {
                Permanent perm = new Permanent(card, Zone.LIBRARY);
                if (libraryRevealChoice.selectedToBattlefieldCloaked()) {
                    perm.setFaceDownAsCloaked();
                }
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, perm, enterTappedTypesSnapshot, simultaneouslyEntered,
                        libraryRevealChoice.battlefieldEntryReplacement());
                simultaneouslyEntered.add(perm);
                selectedPermanentIds.add(perm.getId());
                if (selectedCards.size() == 1 && gameData.pendingEffectResolutionEntry != null) {
                    gameData.pendingEffectResolutionEntry.setChosenPermanentId(perm.getId());
                }
                if (libraryRevealChoice.selectedToBattlefieldTapped()) {
                    perm.tap();
                }

                if (libraryRevealChoice.selectedToBattlefieldCloaked()) {
                    gameLogService.append(gameData, GameLog.text(
                            playerName + " puts a card onto the battlefield face down."));
                } else {
                    gameLogService.append(gameData, GameLog.entersBattlefieldUnder(card, playerName));
                }

                if (libraryRevealChoice.selectedToBattlefieldCloaked()) {
                    battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, card);
                } else if (card.hasType(CardType.CREATURE) || card.hasType(CardType.ARTIFACT)) {
                    // Artifacts such as vehicles (uncrewed, non-creature) still have enter-the-battlefield abilities.
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
                }
                if (!libraryRevealChoice.selectedToBattlefieldCloaked()
                        && card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                    perm.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
                    perm.setSummoningSick(false);
                }
                if (!gameData.interaction.isAwaitingInput()) {
                    legendRuleService.checkLegendRule(gameData, controllerId);
                }
            }
        }

        queueLibrarySelectionFollowUp(gameData, libraryRevealChoice.battlefieldSelectionFollowUp(),
                selectedPermanentIds);

        // A placed card's "enter as a copy" choice is part of its entering (CR 614.1c), so the rest of
        // the revealed cards wait until it is answered.
        if (gameData.cloneOperation.card != null || !gameData.pendingEntryCopyChoices.isEmpty()) {
            gameData.queueInteraction(new PendingLibraryRevealRemainder(
                    libraryRevealChoice, selectedCards, remainingCards));
            return;
        }

        finishRevealedCards(gameData, libraryRevealChoice, selectedCards, remainingCards);
    }

    /** Whether a revealed card has been taken out of its library and sits in no other zone either. */
    private boolean isTakenFromEveryZone(GameData gameData, Card card) {
        UUID cardId = card.getId();
        return gameData.findExiledCard(cardId) == null
                && gameQueryService.findGraveyardOwnerById(gameData, cardId) == null
                && gameData.playerHands.values().stream().flatMap(List::stream).noneMatch(c -> c.getId().equals(cardId))
                && gameData.playerDecks.values().stream().flatMap(List::stream).noneMatch(c -> c.getId().equals(cardId));
    }

    /**
     * Handles the unchosen cards of a reveal choice once its chosen cards have finished entering
     * (their copy choices, or Whiskervale Forerunner's battlefield-or-hand choice, are answered).
     * Returns {@code false} when none is parked.
     */
    public boolean resumeLibraryRevealRemainder(GameData gameData) {
        PendingLibraryRevealRemainder remainder = gameData.pollPendingInteraction(PendingLibraryRevealRemainder.class);
        if (remainder == null) {
            return false;
        }
        if (remainder.choice().selectedCardMayGoToHandIfBattlefieldDeclined()) {
            putUnchosenOnBottomInRandomOrder(gameData, remainder.choice().playerId(),
                    new ArrayList<>(remainder.remainingCards()));
            finishSearchAndResume(gameData);
            return true;
        }
        finishRevealedCards(gameData, remainder.choice(), new ArrayList<>(remainder.selectedCards()),
                new ArrayList<>(remainder.remainingCards()));
        return true;
    }

    private void finishRevealedCards(GameData gameData, PendingInteraction.LibraryRevealChoice libraryRevealChoice,
                                     List<Card> selectedCards, List<Card> remainingCards) {
        UUID controllerId = libraryRevealChoice.playerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (libraryRevealChoice.remainingToGraveyard()) {
            for (Card card : remainingCards) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
            }

            if (selectedCards.isEmpty()) {
                String logEntry = playerName + " puts no cards onto the battlefield. The rest are put into their graveyard.";
                gameLogService.append(gameData, GameLog.text(logEntry));
            } else {
                gameLogService.append(gameData,
                        placedRevealedCardsLog(libraryRevealChoice, playerName, selectedCards)
                                .text(" onto the battlefield. The rest are put into their graveyard.").build());
            }
        } else if (libraryRevealChoice.remainingToExile()) {
            for (Card card : remainingCards) {
                if (gameData.findExiledCard(card.getId()) == null) {
                    exileService.exileCard(gameData, controllerId, card);
                }
            }

            if (selectedCards.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " puts no cards onto the battlefield. The rest are exiled."));
            } else {
                gameLogService.append(gameData,
                        placedRevealedCardsLog(libraryRevealChoice, playerName, selectedCards)
                                .text(" onto the battlefield. The rest are exiled.").build());
            }
        } else if (libraryRevealChoice.remainingToHand()) {
            for (Card card : remainingCards) {
                gameData.addCardToHand(controllerId, card);
            }

            if (selectedCards.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " puts the cards into their hand."));
            } else {
                gameLogService.append(gameData,
                        placedRevealedCardsLog(libraryRevealChoice, playerName, selectedCards)
                                .text(" onto the battlefield and the rest into their hand.").build());
            }
        } else if (libraryRevealChoice.randomRemainingToBottom()) {
            // Shuffle remaining cards and put them on the bottom of the library (Gishath, etc.)
            Collections.shuffle(remainingCards);
            List<Card> deck = gameData.playerDecks.get(controllerId);
            deck.addAll(remainingCards);

            if (selectedCards.isEmpty()) {
                String logEntry = playerName + " puts no cards onto the battlefield. The rest are put on the bottom of their library in a random order.";
                gameLogService.append(gameData, GameLog.text(logEntry));
            } else {
                gameLogService.append(gameData,
                        placedRevealedCardsLog(libraryRevealChoice, playerName, selectedCards)
                                .text(" onto the battlefield. The rest are put on the bottom of their library in a random order.").build());
            }
        } else if (libraryRevealChoice.reorderRemainingToBottom()) {
            List<Card> deck = gameData.playerDecks.get(controllerId);
            if (remainingCards.size() > 1) {
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                        controllerId, remainingCards, true, controllerId,
                        "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
                log.info("Game {} - {} puts {} card(s) onto the battlefield, reordering {} remaining",
                        gameData.id, playerName, selectedCards.size(), remainingCards.size());
                return;
            }
            deck.addAll(remainingCards);
            gameLogService.append(gameData,
                    placedRevealedCardsLog(libraryRevealChoice, playerName, selectedCards)
                            .text(" onto the battlefield. The rest are put on the bottom of their library.").build());
        } else {
            List<Card> deck = gameData.playerDecks.get(controllerId);
            deck.addAll(remainingCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

            if (selectedCards.isEmpty()) {
                String logEntry = playerName + " puts no cards onto the battlefield. Library is shuffled.";
                gameLogService.append(gameData, GameLog.text(logEntry));
            } else {
                gameLogService.append(gameData,
                        placedRevealedCardsLog(libraryRevealChoice, playerName, selectedCards)
                                .text(" onto the battlefield. Library is shuffled.").build());
            }
        }

        log.info("Game {} - {} resolves library reveal choice, {} cards to battlefield", gameData.id, playerName, selectedCards.size());

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        // Guild Feud: this was one of the two sequential reveals — hand back to the flow before state-based
        // actions, so a legend rule check waits until both reveals and the fight have resolved.
        if (gameData.hasPendingInteraction(PendingGuildFeud.class)
                && guildFeudSupport.onRevealAnswered(gameData, selectedCards)) {
            return;
        }

        performStateBasedActionsIfResolutionComplete(gameData);
        finishSearchAndResume(gameData);
    }

    /**
     * Opening of the "puts … onto the battlefield" log line. A cloaked selection enters face down, so the
     * cards' identities stay out of the public log.
     */
    private GameLog.Builder placedRevealedCardsLog(PendingInteraction.LibraryRevealChoice libraryRevealChoice,
                                                   String playerName, List<Card> selectedCards) {
        if (libraryRevealChoice.selectedToBattlefieldCloaked()) {
            return GameLog.builder().text(playerName + " puts "
                    + LibraryRevealSupport.pluralCards(selectedCards.size()) + " face down");
        }
        return appendCards(GameLog.builder().text(playerName + " puts "), selectedCards);
    }

    private void resolveRevealChoiceSplitByType(GameData gameData, UUID controllerId, String playerName,
                                                List<Card> selectedCards, List<Card> remainingCards,
                                                CardType battlefieldType, boolean battlefieldTapped) {
        // A land card that can't enter from the library (Grafdigger's Cage on a creature land) never leaves it,
        // so the "creature cards go to your hand" instruction still finds it there.
        List<Card> blockedCards = selectedCards.stream()
                .filter(card -> card.hasType(battlefieldType))
                .filter(card -> gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY))
                .toList();
        List<Card> battlefieldCards = selectedCards.stream()
                .filter(card -> card.hasType(battlefieldType))
                .filter(card -> !blockedCards.contains(card))
                .toList();
        List<Card> handCards = selectedCards.stream()
                .filter(card -> !card.hasType(battlefieldType)
                        || (blockedCards.contains(card) && card.hasType(CardType.CREATURE)))
                .toList();
        remainingCards.addAll(blockedCards.stream().filter(card -> !handCards.contains(card)).toList());

        // The rest go to the bottom before the revealed lands enter, so a land's "enter as a copy"
        // choice (CR 614.1c) is the last thing left when it opens.
        putUnchosenOnBottomInRandomOrder(gameData, controllerId, remainingCards);

        if (!battlefieldCards.isEmpty()) {
            placeCardsOnBattlefieldSimultaneously(gameData, battlefieldCards, controllerId,
                    battlefieldTapped, false, false, false, null, null, null);
        }
        for (Card card : handCards) {
            gameData.addCardToHand(controllerId, card);
        }
        if (!handCards.isEmpty()) {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(playerName + " reveals "), handCards)
                            .text(" and puts them into their hand.").build());
        }

        finishSearchAndResume(gameData);
    }

    private void putUnchosenOnBottomInRandomOrder(GameData gameData, UUID controllerId, List<Card> remainingCards) {
        if (remainingCards.isEmpty()) {
            return;
        }
        Collections.shuffle(remainingCards);
        gameData.playerDecks.get(controllerId).addAll(remainingCards);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " puts the unchosen cards on the bottom of their library in a random order."));
    }

    private void insertSelectedCardFollowUpIfApplicable(GameData gameData, List<Card> selectedCards,
                                                         PendingInteraction.LibraryRevealChoice choice) {
        CardPredicate predicate = choice.selectedCardPredicate();
        StackEntry sourceEntry = gameData.pendingEffectResolutionEntry;
        if (selectedCards.isEmpty() || predicate == null || choice.effectIfSelectedCardMatches() == null
                || sourceEntry == null) {
            return;
        }
        Card sourceCard = sourceEntry.getCard();
        UUID sourceCardId = sourceCard == null ? null : sourceCard.getId();
        if (predicateEvaluationService.matchesCardPredicate(selectedCards.getFirst(), predicate,
                sourceCardId, gameData, choice.playerId())) {
            sourceEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                    List.of(choice.effectIfSelectedCardMatches()));
        }
    }

    private static List<Card> allCardsMatchingIds(List<Card> cards, List<UUID> ids) {
        Set<UUID> selectedIds = new HashSet<>(ids);
        return cards.stream().filter(card -> selectedIds.contains(card.getId())).toList();
    }

    private void resolveMayPutSelectedCardOntoBattlefieldElseToHand(
            GameData gameData, PendingInteraction.LibraryRevealChoice choice, UUID controllerId,
            List<Card> selectedCards, List<Card> remainingCards) {
        if (!selectedCards.isEmpty()) {
            gameLogService.append(gameData, appendCards(GameLog.builder()
                    .text(gameData.playerIdToName.get(controllerId) + " reveals "), selectedCards).text(".").build());
        }
        // "If you don't put it onto the battlefield, put it into your hand": a card that can't enter
        // (Grafdigger's Cage) goes to hand the same way as on another player's turn.
        if (selectedCards.isEmpty() || !controllerId.equals(gameData.activePlayerId)
                || gameQueryService.isCardBlockedFromEnteringFromZone(gameData, selectedCards.getFirst(), Zone.LIBRARY)) {
            selectedCards.forEach(card -> gameData.addCardToHand(controllerId, card));
            putUnchosenOnBottomInRandomOrder(gameData, controllerId, remainingCards);
            finishSearchAndResume(gameData);
            return;
        }

        // The rest go to the bottom only after the revealed card has been put onto the battlefield,
        // including its "enter as a copy" choice (CR 614.1c), or into its owner's hand.
        gameData.queueInteraction(new PendingLibraryRevealRemainder(choice, selectedCards, remainingCards));
        Card selectedCard = selectedCards.getFirst();
        gameData.playerDecks.get(controllerId).addFirst(selectedCard);
        StackEntry pending = gameData.pendingEffectResolutionEntry;
        Card sourceCard = pending != null ? pending.getCard() : selectedCard;
        UUID sourcePermanentId = pending != null ? pending.getSourcePermanentId() : null;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                controllerId,
                List.of(new LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect(
                        new CardTruePredicate(), false)),
                "Put " + selectedCard.getName() + " onto the battlefield?",
                selectedCard.getId(), null, sourcePermanentId));
        playerInputService.processNextMayAbility(gameData);
    }

    private void handlePsychoticEpisodeChoice(GameData gameData, Player player,
                                              PendingPsychoticEpisodeChoice choice,
                                              List<Card> allRevealedCards,
                                              List<UUID> selectedCardIds) {
        if (selectedCardIds.size() != 1) {
            throw new IllegalStateException("Psychotic Episode requires one card");
        }

        UUID chosenId = selectedCardIds.getFirst();
        Card chosenCard = allRevealedCards.stream()
                .filter(card -> card.getId().equals(chosenId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid Psychotic Episode card"));
        List<Card> deck = gameData.playerDecks.get(choice.targetPlayerId());

        if (chosenId.equals(choice.topCardId())) {
            deck.removeIf(card -> card.getId().equals(chosenId));
        } else {
            gameData.playerHands.get(choice.targetPlayerId()).removeIf(
                    card -> card.getId().equals(chosenId));
        }
        deck.add(chosenCard);

        String targetName = gameData.playerIdToName.get(choice.targetPlayerId());
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses ", chosenCard,
                " to put on the bottom of " + targetName + "'s library."));
        finishSearchAndResume(gameData);
    }

    private void handleDubiousChallengeInitialChoice(
            GameData gameData, List<Card> allRevealedCards, List<UUID> selectedCardIds,
            PendingDubiousChallengeChoice pending) {
        Set<UUID> selectedIds = new HashSet<>(selectedCardIds);
        List<Card> selectedCards = allRevealedCards.stream()
                .filter(card -> selectedIds.contains(card.getId()))
                .toList();
        List<Card> deck = gameData.playerDecks.get(pending.controllerId());

        for (Card card : selectedCards) {
            deck.removeIf(libraryCard -> libraryCard.getId().equals(card.getId()));
            exileService.exileCard(gameData, pending.controllerId(), card);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, pending.controllerId());

        String controllerName = gameData.playerIdToName.get(pending.controllerId());
        if (selectedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " exiles no creature cards with Dubious Challenge. Library is shuffled."));
            finishSearchAndResume(gameData);
            return;
        }

        gameData.queueInteraction(new PendingDubiousChallengeChoice(
                pending.controllerId(), pending.opponentId(), selectedCards));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                pending.opponentId(), selectedCards, selectedCards.stream().map(Card::getId).toList(),
                false, false, false, false, false, 0, null, 1,
                "You may choose one of these exiled cards to put onto the battlefield under your control.",
                0, false));
        gameLogService.append(gameData,
                appendCards(GameLog.builder().text(controllerName + " exiles "), selectedCards)
                        .text(" with Dubious Challenge.").build());
    }

    private void handleDubiousChallengeOpponentChoice(
            GameData gameData, List<Card> allExiledCards, List<UUID> selectedCardIds,
            PendingDubiousChallengeChoice pending) {
        Set<UUID> selectedIds = new HashSet<>(selectedCardIds);
        List<Card> opponentCards = allExiledCards.stream()
                .filter(card -> selectedIds.contains(card.getId()))
                .toList();
        List<Card> controllerCards = allExiledCards.stream()
                .filter(card -> !selectedIds.contains(card.getId()))
                .toList();

        putDubiousChallengeCardsOntoBattlefield(gameData, opponentCards, pending.opponentId());
        putDubiousChallengeCardsOntoBattlefield(gameData, controllerCards, pending.controllerId());

        performStateBasedActionsIfResolutionComplete(gameData);
        finishSearchAndResume(gameData);
    }

    private void putDubiousChallengeCardsOntoBattlefield(
            GameData gameData, List<Card> cards, UUID controllerId) {
        List<Card> cardsToEnter = cards.stream()
                .filter(card -> gameData.removeFromExile(card.getId()))
                .toList();
        if (cardsToEnter.isEmpty()) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> batch = new ArrayList<>();
        String controllerName = gameData.playerIdToName.get(controllerId);
        for (Card card : cardsToEnter) {
            Permanent permanent = new Permanent(card);
            permanent.setEnteredFromExile(true);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, batch);
            batch.add(permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(card, controllerName));
            if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                permanent.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
                permanent.setSummoningSick(false);
            }
        }

        for (Card card : cardsToEnter) {
            if (card.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId,
                        card, null, false);
            }
        }
    }

    /**
     * Queues what a library-to-battlefield effect, or a draft onto the battlefield, does next with the permanents it put
     * onto the battlefield: an optional follow-up is a reflexive ability of its own, put on the stack; any other is the
     * next step of the resolving entry, which records the permanents as its picks.
     */
    public void queueLibrarySelectionFollowUp(GameData gameData,
                                               LibrarySelectionFollowUp followUp,
                                               List<UUID> selectedPermanentIds) {
        if (followUp == null || selectedPermanentIds.isEmpty()
                || !followUp.shouldOffer(gameData, selectedPermanentIds)
                || gameData.pendingEffectResolutionEntry == null) {
            return;
        }

        StackEntry sourceEntry = gameData.pendingEffectResolutionEntry;
        CardEffect followUpEffect = followUp.effect();
        if (followUp.optional()) {
            StackEntry record = ReflexiveAbilitySupport.record(gameData, sourceEntry,
                    List.of(new MayEffect(followUpEffect, followUp.prompt())));
            recordSelectedPermanents(record, selectedPermanentIds);
            // An optional follow-up is a reflexive ability of its own (CR 603.12), put on the stack through the stack
            // door; its "you may" is asked as it resolves (CR 603.5).
            triggeredAbilityStackService.putMade(gameData, record);
        } else {
            recordSelectedPermanents(sourceEntry, selectedPermanentIds);
            sourceEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, List.of(followUpEffect));
        }
    }

    /**
     * The permanents a library-to-battlefield effect put onto the battlefield, on the entry its follow-up resolves
     * in: its picks and, when there is one, its chosen permanent, which the follow-up's effect reads.
     */
    private static void recordSelectedPermanents(StackEntry entry, List<UUID> selectedPermanentIds) {
        entry.recordPicks(selectedPermanentIds);
        if (selectedPermanentIds.size() == 1) {
            entry.setChosenPermanentId(selectedPermanentIds.getFirst());
        }
    }

    private void handleHandSelectionThenLandSelection(GameData gameData, UUID controllerId,
                                                       String playerName, List<Card> selectedCards,
                                                       List<Card> remainingCards) {
        for (Card card : selectedCards) {
            gameData.addCardToHand(controllerId, card);
        }
        if (!selectedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " puts a card into their hand."));
        }

        List<Card> landCards = remainingCards.stream()
                .filter(card -> card.hasType(CardType.LAND))
                .toList();
        if (landCards.isEmpty()) {
            for (Card card : remainingCards) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
            }
            gameLogService.append(gameData, GameLog.text(
                    playerName + " puts the remaining cards into their graveyard."));
            finishSearchAndResume(gameData);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, new ArrayList<>(remainingCards),
                landCards.stream().map(Card::getId).toList(),
                true, false, false, false, false, 0, null, landCards.size(),
                "You may put any number of land cards from among them onto the battlefield tapped.",
                true, 0, false, null, false, null, false, true));
    }

    private void resolveRevealChoiceToHand(GameData gameData, UUID controllerId, String playerName,
                                              List<Card> selectedCards, List<Card> remainingCards,
                                              boolean reorderRemainingToBottom) {
        resolveRevealChoiceToHand(gameData, controllerId, playerName, selectedCards, remainingCards,
                reorderRemainingToBottom, false, false, false);
    }

    private void resolveRevealChoiceToHand(GameData gameData, UUID controllerId, String playerName,
                                              List<Card> selectedCards, List<Card> remainingCards,
                                              boolean reorderRemainingToBottom, boolean remainingToGraveyard,
                                              boolean remainingToExile) {
        resolveRevealChoiceToHand(gameData, controllerId, playerName, selectedCards, remainingCards,
                reorderRemainingToBottom, remainingToGraveyard, remainingToExile, false);
    }

    private void resolveRevealChoiceToHand(GameData gameData, UUID controllerId, String playerName,
                                              List<Card> selectedCards, List<Card> remainingCards,
                                              boolean reorderRemainingToBottom, boolean remainingToGraveyard,
                                              boolean remainingToExile, boolean randomRemainingToBottom) {
        resolveRevealChoiceToHand(gameData, controllerId, playerName, selectedCards, remainingCards,
                reorderRemainingToBottom, remainingToGraveyard, remainingToExile,
                randomRemainingToBottom, false, null, null, 0, 0, true);
    }

    private void resolveRevealChoiceToHand(GameData gameData, UUID controllerId, String playerName,
                                              List<Card> selectedCards, List<Card> remainingCards,
                                              boolean reorderRemainingToBottom, boolean remainingToGraveyard,
                                              boolean remainingToExile, boolean randomRemainingToBottom,
                                              boolean gainLifeEqualToSelectedCardManaValue,
                                              StackEntry sourceEntry, CardEffect effectIfNoCardChosen,
                                              int lifeLossPerSelectedCard,
                                              int lifePaymentPerSelectedCard, boolean revealSelected) {
        // Put selected cards into hand
        for (Card card : selectedCards) {
            gameData.addCardToHand(controllerId, card);
        }

        if (gainLifeEqualToSelectedCardManaValue && !selectedCards.isEmpty()) {
            Card chosenCard = selectedCards.getFirst();
            int manaValue = chosenCard.getManaValue();
            if (manaValue > 0) {
                if (sourceEntry != null && sourceEntry.getCard() != null) {
                    lifeSupport.applyGainLife(gameData, controllerId, manaValue,
                            sourceEntry.getCard().getName(), sourceEntry.getCard(), sourceEntry.getEntryType());
                } else {
                    lifeSupport.applyGainLife(gameData, controllerId, manaValue);
                }
            }
        }

        // Rest exiled (Browse): one card to hand, the others exiled face up.
        if (remainingToExile) {
            // A card put into a hand unrevealed stays hidden (CR 400.2): the line doesn't name it.
            if (!selectedCards.isEmpty() && revealSelected) {
                gameLogService.append(gameData,
                        appendCards(GameLog.builder().text(playerName + " puts "), selectedCards)
                                .text(" into their hand.").build());
            } else if (!selectedCards.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " puts "
                        + (selectedCards.size() == 1 ? "a card" : selectedCards.size() + " cards")
                        + " into their hand."));
            }
            for (Card card : remainingCards) {
                exileService.exileCard(gameData, controllerId, card);
            }
            if (!remainingCards.isEmpty()) {
                gameLogService.append(gameData,
                        appendCards(GameLog.builder().text(playerName + " exiles "), remainingCards)
                                .text(".").build());
            }
            log.info("Game {} - {} puts {} card(s) to hand, {} exiled", gameData.id, playerName, selectedCards.size(), remainingCards.size());
            finishSearchAndResume(gameData);
            return;
        }

        // Log the result
        if (remainingToGraveyard) {
            if (!selectedCards.isEmpty()) {
                String countWord = selectedCards.size() == 1 ? "one card" : selectedCards.size() + " cards";
                String logEntry = playerName + " puts " + countWord + " into their hand and the rest into their graveyard.";
                gameLogService.append(gameData, GameLog.text(logEntry));
            }
        } else if (selectedCards.isEmpty()) {
            String logEntry = playerName + " does not reveal any creature cards.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else if (!revealSelected) {
            gameLogService.append(gameData, GameLog.text(playerName + " puts " + selectedCards.size()
                    + " card(s) into their hand."));
        } else if (randomRemainingToBottom) {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(playerName + " puts "), selectedCards)
                            .text(" into their hand. The rest are put on the bottom of their library in a random order.")
                            .build());
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(playerName + " reveals "), selectedCards)
                            .text(" and puts them into their hand.").build());
        }

        // Handle remaining cards
        if (remainingToGraveyard) {
            List<Card> entered = graveyardService.addCardsFromLibraryToGraveyard(gameData, controllerId, remainingCards);
            if (sourceEntry != null && sourceEntry.isRecordLibraryGraveyardCount()) {
                sourceEntry.setEventValue(entered.size());
            }
            gainLifeForGreatestPowerOfGraveyardCards(gameData, controllerId, entered, sourceEntry);
            applySelectionLifeLoss(gameData, controllerId, selectedCards.size(),
                    lifeLossPerSelectedCard, sourceEntry);
            applySelectionLifePayment(gameData, controllerId, selectedCards.size(),
                    lifePaymentPerSelectedCard, sourceEntry);
            log.info("Game {} - {} puts {} card(s) to hand, {} to graveyard", gameData.id, playerName, selectedCards.size(), remainingCards.size());

            // Resume resolving remaining effects on the same spell/ability
            // (e.g. Dark Bargain: "Look at top 3, put 2 to hand, rest to graveyard. Deals 2 damage to you.")
            finishSearchAndResume(gameData);
            return;
        }

        if (randomRemainingToBottom) {
            if (!remainingCards.isEmpty()) {
                Collections.shuffle(remainingCards);
                gameData.playerDecks.get(controllerId).addAll(remainingCards);
            }
            if (selectedCards.isEmpty() && effectIfNoCardChosen != null && sourceEntry != null) {
                sourceEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                        List.of(effectIfNoCardChosen));
            }
            log.info("Game {} - {} puts {} card(s) to hand, {} to bottom randomly",
                    gameData.id, playerName, selectedCards.size(), remainingCards.size());
            finishSearchAndResume(gameData);
            return;
        }

        if (reorderRemainingToBottom && remainingCards.size() > 1) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    controllerId, remainingCards, true, controllerId,
                    "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
            log.info("Game {} - {} reveals {} creature cards to hand, reordering {} remaining",
                    gameData.id, playerName, selectedCards.size(), remainingCards.size());
            return;
        }

        if (!remainingCards.isEmpty()) {
            List<Card> deck = gameData.playerDecks.get(controllerId);
            deck.addAll(remainingCards);
        }

        log.info("Game {} - {} reveals {} creature cards to hand", gameData.id, playerName, selectedCards.size());
        finishSearchAndResume(gameData);
    }

    /**
     * The life a library reveal choice's answer makes a player pay or lose for the cards chosen: Sword-Point
     * Diplomacy's opponent paying for each card denied, Moonlight Bargain's controller paying for each card kept, or
     * Search for Blex's controller losing life for the cards put into hand. Paying life is losing it (CR 119.4), and
     * Lich's Duel Mastery replaces each such loss with its sacrifice once its shields are gone, whose death
     * replacements are chosen before anything changes (CR 616.1). So when the answer has such a life change, it is
     * completed in a window that asks those questions before the first card moves, and is answered again once they
     * are chosen.
     *
     * @return {@code true} when the answer was completed here (or stopped to ask)
     */
    private boolean asksSelectionLifeChangeFirst(GameData gameData, Player player,
                                                 PendingInteraction.LibraryRevealChoice choice, List<UUID> cardIds) {
        int cost = choice.lifeCostPerSelection();
        if (cost <= 0 || cardIds.isEmpty()) {
            return false;
        }
        UUID payerId;
        int amount;
        int times;
        if (choice.beneficiaryPlayerId() != null) {
            payerId = gameData.orderedPlayerIds.stream()
                    .filter(id -> !id.equals(choice.beneficiaryPlayerId()))
                    .findFirst()
                    .orElse(null);
            amount = cost;
            times = cardIds.size();
        } else if (choice.selectedToHand() && choice.remainingToGraveyard() && !choice.remainingToExile()
                && !choice.selectLandsAfterHand() && !choice.selectedToTop() && !choice.selectedToManifest()
                && !choice.selectedCardMayGoToHandIfBattlefieldDeclined()) {
            payerId = choice.playerId();
            amount = choice.payLifePerSelection() ? cost : cost * cardIds.size();
            times = choice.payLifePerSelection() ? cardIds.size() : 1;
        } else {
            return false;
        }
        if (payerId == null) {
            return false;
        }
        permanentChoiceHandlerService.runAskingDeathReplacementsFirst(gameData, () -> {
            lifeSupport.askBeforeRepeatedLifeLossesThatFollowAChange(gameData, payerId, amount, times);
            completeLibraryRevealChoice(gameData, player, choice, cardIds);
        }, new GameData.InteractionAnswerReplay(player, choice, new InteractionAnswer.CardsChosen(cardIds)));
        return true;
    }

    private void applySelectionLifeLoss(GameData gameData, UUID controllerId, int selectedCount,
                                        int lifeLossPerSelectedCard, StackEntry sourceEntry) {
        int lifeLoss = selectedCount * lifeLossPerSelectedCard;
        if (lifeLoss <= 0) {
            return;
        }
        String sourceName = sourceEntry != null && sourceEntry.getCard() != null
                ? sourceEntry.getCard().getName()
                : "library choice";
        lifeSupport.applyLifeLoss(gameData, controllerId, lifeLoss, sourceName);
    }

    private void gainLifeForGreatestPowerOfGraveyardCards(
            GameData gameData, UUID controllerId, List<Card> cards, StackEntry sourceEntry) {
        if (sourceEntry == null
                || !sourceEntry.isGainLifeEqualToGreatestPowerOfCardsPutIntoGraveyard()) {
            return;
        }
        int greatestPower = cards.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(card -> gameQueryService.getEffectiveCardPower(gameData, card))
                .filter(java.util.Objects::nonNull)
                .mapToInt(power -> Math.max(0, power))
                .max()
                .orElse(0);
        if (greatestPower > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, greatestPower,
                    sourceEntry.getCard().getName(), sourceEntry.getCard(), sourceEntry.getEntryType());
        }
    }

    private void applySelectionLifePayment(GameData gameData, UUID controllerId, int selectedCount,
                                           int lifePaymentPerSelectedCard, StackEntry sourceEntry) {
        if (lifePaymentPerSelectedCard <= 0) {
            return;
        }
        String sourceName = sourceEntry != null && sourceEntry.getCard() != null
                ? sourceEntry.getCard().getName()
                : "library choice";
        for (int i = 0; i < selectedCount; i++) {
            lifeSupport.applyLifePayment(gameData, controllerId, lifePaymentPerSelectedCard, sourceName);
        }
    }

    private void handleAllureOfTheUnknownRevealChoice(GameData gameData, List<Card> allRevealedCards,
                                                       List<UUID> selectedCardIds) {
        allureOfTheUnknownEffectHandler.completeCardChoice(gameData, allRevealedCards, selectedCardIds);
    }

    private void handleMemoriesReturningChoice(GameData gameData, List<Card> allRevealedCards,
                                                List<UUID> selectedCardIds) {
        memoriesReturningEffectHandler.completeCardChoice(gameData, allRevealedCards, selectedCardIds);
        finishSearchAndResume(gameData);
    }

    /**
     * Phyrexian Portal: the controller has searched the pile they kept. The selected card goes to their
     * hand and every other card of the pile goes back into their library, which is then shuffled. The
     * search names no quality (CR 701.23b does not apply), so one card must be found from a nonempty
     * pile. The searched card is not named in the log, since the library is hidden.
     */
    private void handlePortalPileSearch(GameData gameData, List<Card> pileCards,
                                        List<UUID> selectedCardIds, UUID controllerId) {
        gameData.clearPendingInteractions(PendingPortalPileSearch.class);

        String playerName = gameData.playerIdToName.get(controllerId);
        Set<UUID> chosenIds = new HashSet<>(selectedCardIds);
        List<Card> deck = gameData.playerDecks.computeIfAbsent(controllerId, k -> new ArrayList<>());
        for (Card card : pileCards) {
            if (chosenIds.contains(card.getId())) {
                gameData.addCardToHand(controllerId, card);
                gameLogService.append(gameData, GameLog.text(playerName + " puts a card into their hand."));
            } else {
                deck.add(card);
            }
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        gameLogService.append(gameData,
                GameLog.text(playerName + " shuffles the rest of the pile into their library."));

        finishSearchAndResume(gameData);
    }

    private void handleReturnExiledWithSourceCard(GameData gameData, List<Card> allRevealedCards,
                                                  List<UUID> selectedCardIds, UUID controllerId) {
        PendingReturnExiledWithSourceCard pending =
                gameData.peekPendingInteraction(PendingReturnExiledWithSourceCard.class);
        gameData.clearPendingInteractions(PendingReturnExiledWithSourceCard.class);

        if (pending != null && pending.toBattlefield()) {
            UUID returnControllerId = pending.controllerId() != null ? pending.controllerId() : controllerId;
            Set<UUID> chosenIds = new HashSet<>(selectedCardIds);
            for (Card card : allRevealedCards) {
                if (chosenIds.contains(card.getId())) {
                    returnCardExiledWithSourceToBattlefieldEffectHandler.returnToBattlefield(
                            gameData, returnControllerId, card, "exile", pending.grantedSubtype(),
                            pending.enterTapped(), pending.enterAttacking(), pending.grantHaste(),
                            pending.battlefieldEntryReplacement());
                    break;
                }
            }
            finishSearchAndResume(gameData);
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        Set<UUID> selectedIds = new HashSet<>(selectedCardIds);
        for (Card card : allRevealedCards) {
            if (selectedIds.contains(card.getId())) {
                UUID destinationId = pending != null && pending.toOwnersHand()
                        ? gameData.exiledCards.stream().filter(exiled -> exiled.card().getId().equals(card.getId()))
                                .map(com.github.laxika.magicalvibes.model.ExiledCardEntry::ownerId)
                                .findFirst().orElse(controllerId)
                        : controllerId;
                gameData.removeFromExile(card.getId());
                gameData.addCardToHand(destinationId, card);
                gameLogService.append(gameData, GameLog.textCardText(
                        controllerName + " puts ", card, " from exile into their hand."));
                log.info("Game {} - {} returns {} from exile to hand",
                        gameData.id, controllerName, card.getName());
                break;
            }
        }

        finishSearchAndResume(gameData);
    }

    /**
     * Rejects a punisher reveal answer (Sword-Point Diplomacy) that denies more cards than the
     * choosing opponent can pay life for. Called both before the prompt is cleared — so a rejected
     * answer leaves it standing — and again inside the branch as defence.
     */
    private void requirePunisherLifeAffordable(GameData gameData, int selectedCount,
                                               UUID controllerId, int lifeCost) {
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElseThrow();
        int totalLifeCost = selectedCount * lifeCost;
        int opponentLife = gameData.playerLifeTotals.get(opponentId);
        if (totalLifeCost > opponentLife) {
            throw new IllegalStateException("Not enough life to pay for " + selectedCount
                    + " cards (need " + totalLifeCost + ", have " + opponentLife + ")");
        }
    }

    private boolean isControllerLifePaymentChoice(PendingInteraction.LibraryRevealChoice choice) {
        return choice.lifeCostPerSelection() > 0
                && choice.payLifePerSelection();
    }

    private void requireControllerLifeAffordable(GameData gameData, int selectedCount,
                                                 UUID controllerId, int lifeCost) {
        int totalLifeCost = selectedCount * lifeCost;
        if (!gameQueryService.canPlayerLoseLife(gameData, controllerId)) {
            throw new IllegalStateException("Not enough life to pay for " + selectedCount
                    + " cards (need " + totalLifeCost + ", have " + gameData.getLife(controllerId) + ")");
        }
        // Each card is a separate payment (CR 119.4): it needs the life total to cover its own amount at
        // that moment, and a life-loss multiplier such as Bloodletter of Aclazotz's shrinks what is left.
        int multiplier = gameQueryService.opponentLifeLossMultiplier(gameData, controllerId);
        int remainingLife = gameData.getLife(controllerId);
        for (int i = 0; i < selectedCount; i++) {
            if (lifeCost > remainingLife) {
                throw new IllegalStateException("Not enough life to pay for " + selectedCount
                        + " cards (need " + totalLifeCost + ", have " + gameData.getLife(controllerId) + ")");
            }
            remainingLife -= lifeCost * multiplier;
        }
    }

    private void handlePunisherRevealChoice(GameData gameData, List<Card> allRevealedCards,
                                              List<UUID> selectedCardIds,
                                              UUID controllerId, int lifeCost) {

        String controllerName = gameData.playerIdToName.get(controllerId);

        // Determine the opponent (the player who made the choice)
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElseThrow();
        String opponentName = gameData.playerIdToName.get(opponentId);

        requirePunisherLifeAffordable(gameData, selectedCardIds.size(), controllerId, lifeCost);
        int totalLifeCost = selectedCardIds.size() * lifeCost;

        // Separate selected (denied) cards from unselected (to hand)
        Set<UUID> deniedIds = new HashSet<>(selectedCardIds);
        List<Card> toHand = new ArrayList<>();
        List<Card> toExile = new ArrayList<>();
        for (Card card : allRevealedCards) {
            if (deniedIds.contains(card.getId())) {
                toExile.add(card);
            } else {
                toHand.add(card);
            }
        }

        // Opponent pays life for each denied card; each card is its own payment, so life-payment
        // triggers and replacements apply per card.
        StackEntry punisherSource = gameData.pendingEffectResolutionEntry;
        String punisherSourceName = punisherSource != null && punisherSource.getCard() != null
                ? punisherSource.getCard().getName() : "a denied card";
        for (int i = 0; i < toExile.size(); i++) {
            lifeSupport.applyLifePayment(gameData, opponentId, lifeCost, punisherSourceName);
        }

        // Cards not denied go to controller's hand
        for (Card card : toHand) {
            gameData.addCardToHand(controllerId, card);
        }
        if (!toHand.isEmpty()) {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(controllerName + " puts "), toHand)
                            .text(" into their hand.").build());
        }

        // Denied cards are exiled
        for (Card card : toExile) {
            exileService.exileCard(gameData, controllerId, card);
        }
        if (!toExile.isEmpty()) {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(controllerName + " exiles "), toExile)
                            .text(".").build());
        }

        log.info("Game {} - Punisher reveal resolved: {} to hand, {} exiled ({} paid {} life)",
                gameData.id, toHand.size(), toExile.size(), opponentName,
                totalLifeCost * gameQueryService.opponentLifeLossMultiplier(gameData, opponentId));

        performStateBasedActionsIfResolutionComplete(gameData);
        finishSearchAndResume(gameData);
    }

    /**
     * Handles the player's choice from a Sunbird's Invocation (or cascade) "cast without paying"
     * reveal. If a card is chosen, it is cast without paying its mana cost; every card not cast goes
     * to the bottom of the library in a random order — Sunbird's "Put the rest on the bottom of your
     * library in a random order" and cascade's CR 702.85a "put all cards exiled this way that
     * weren't cast on the bottom of your library in a random order".
     *
     * <p>A chosen card with no legal target can't be cast at all (CR 601.2c), so it is one of the
     * cards not cast: it stays in the pile and is bottomed along with the rest, rather than being
     * singled out into some other zone.
     */
    private void handleCastWithoutPayingChoice(GameData gameData, Player player, int cardIndex,
                                                boolean canFailToFind, List<Card> searchCards,
                                                List<Card> sourceCards, List<Card> deck, UUID deckOwnerId,
                                                boolean grantHaste, boolean sacrificeAtEndStep,
                                                int spellManaValueLimit, boolean restToExile,
                                                boolean restStays, Map<UUID, Set<SpellFace>> offeredFaces) {
        String playerName = player.getUsername();
        Set<UUID> exiledSourceIds = sourceCards.stream()
                .filter(card -> gameData.findExiledCard(card.getId()) != null)
                .map(Card::getId).collect(java.util.stream.Collectors.toSet());
        // Cards that were only looked at (Svella, Ice Shaper) are still in the library (CR 701.20b); they leave it
        // now that the player has chosen, and the uncast ones are put back on the bottom below, unless nothing
        // moves them (restStays: Galvanoth), when only the cast card leaves.
        Set<UUID> sourceCardIds = sourceCards.stream().map(Card::getId).collect(java.util.stream.Collectors.toSet());
        if (!restStays) {
            deck.removeIf(card -> sourceCardIds.contains(card.getId()));
        }

        boolean chosenCardCastable = cardIndex == -1 || cardIndex < -1 || cardIndex >= searchCards.size()
                || canCastWithoutPaying(gameData, player.getId(), searchCards.get(cardIndex), spellManaValueLimit);
        if (!sourceCards.isEmpty() && exiledSourceIds.size() == sourceCards.size()
                && !grantHaste && !sacrificeAtEndStep && !restToExile && chosenCardCastable) {
            if (cardIndex == -1 && !canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
            if (cardIndex < -1 || cardIndex >= searchCards.size()) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }
            if (cardIndex >= 0) {
                Card hit = searchCards.get(cardIndex);
                if (hit.getBackFaceCard() != null && hit.getCastingOption(AdventureCast.class).isPresent()) {
                    // The caster picks which face to cast (the creature or its Adventure); the uncast cards
                    // are put on the bottom now because the face choice ends the search flow.
                    exileFreeCastQueueSupport.queueRemainderToLibraryBottom(gameData, sourceCards.stream()
                            .map(Card::getId).filter(id -> !id.equals(hit.getId())).toList());
                    exileFreeCastQueueSupport.putRemainderIntoLibraryBottom(gameData);
                    gameData.interaction.clearAwaitingInput();
                    exileFreeCastSupport.castWithManaValueLimit(gameData, player, hit.getId(),
                            spellManaValueLimit);
                    return;
                }
            }
            exileFreeCastQueueSupport.queueRemainderToLibraryBottom(gameData,
                    sourceCards.stream().map(Card::getId).toList());
            exileFreeCastQueueSupport.castChosenSpellsWithoutPaying(gameData, player,
                    cardIndex == -1 ? List.of() : List.of(searchCards.get(cardIndex).getId()));
            return;
        }

        Card chosenCard = null;
        Set<SpellFace> chosenFaces = Set.of();
        boolean uncastable = false;
        if (cardIndex == -1) {
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
        } else {
            if (cardIndex < 0 || cardIndex >= searchCards.size()) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }
            Card picked = searchCards.get(cardIndex);
            Set<SpellFace> allowedFaces = offeredFaces.getOrDefault(picked.getId(), Set.of());
            chosenFaces = castableFaces(gameData, player.getId(), picked, spellManaValueLimit, allowedFaces);
            String refusal = chosenFaces.isEmpty()
                    ? freeCastRefusal(gameData, player.getId(), picked, spellManaValueLimit, allowedFaces)
                    : null;
            if (refusal == null && chosenFaces.isEmpty()) {
                refusal = " can't be cast";
            }
            if (refusal == null) {
                chosenCard = picked;
                // Remove chosen card from sourceCards
                for (int i = 0; i < sourceCards.size(); i++) {
                    if (sourceCards.get(i).getId().equals(chosenCard.getId())) {
                        sourceCards.remove(i);
                        break;
                    }
                }
            } else {
                uncastable = true;
                gameLogService.append(gameData, GameLog.cardThen(picked, refusal + "."));
                log.info("Game {} - {} cast-without-paying can't be cast; card is bottomed",
                        gameData.id, picked.getName());
            }
        }

        // Put remaining cards on the bottom of the library in a random order, unless they stay exiled or stay put
        if (!restToExile && !restStays && !sourceCards.isEmpty()) {
            Collections.shuffle(sourceCards);
            sourceCards.forEach(card -> gameData.removeFromExile(card.getId()));
            deck.addAll(sourceCards);
            log.info("Game {} - {} remaining revealed cards shuffled to bottom of library",
                    gameData.id, sourceCards.size());
        }

        if (chosenCard == null) {
            if (!uncastable) {
                String logEntry = playerName + " declines to cast a spell.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} declines Sunbird's Invocation cast", gameData.id, playerName);
            }
            finishSearchAndResume(gameData);
            return;
        }

        // A hit that stays exiled when declined (Jodah's cascade) is not in sourceCards but is still exiled.
        boolean castFromExile = exiledSourceIds.contains(chosenCard.getId())
                || gameData.findExiledCard(chosenCard.getId()) != null;
        UUID chosenCardId = chosenCard.getId();
        deck.removeIf(card -> card.getId().equals(chosenCardId));
        // "Do this only once each turn" (CR 603.2h; Planetarium of Wan Shi Tong): choosing to cast takes the action.
        if (gameData.pendingEffectResolutionEntry != null) {
            com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService
                    .markActionTaken(gameData, gameData.pendingEffectResolutionEntry);
        }
        // A card whose cost increase isn't paid goes where the effect puts the cards it doesn't cast.
        IfNotCast ifNotCast = castFromExile && (restToExile || restStays) ? IfNotCast.STAYS_EXILED
                : !castFromExile && restStays ? IfNotCast.LIBRARY_TOP : IfNotCast.LIBRARY_BOTTOM;
        Zone sourceZone = castFromExile ? Zone.EXILE : Zone.LIBRARY;
        if (chosenFaces.size() > 1 && isAdventurer(chosenCard)) {
            // Both the card and its Adventure may be cast, so the caster picks which (CR 716.3).
            beginFaceChoice(gameData, player, chosenCard, sourceZone, new PayFreeCastCostIncreaseEffect.LibraryCast(
                    deckOwnerId, null, grantHaste, sacrificeAtEndStep, spellManaValueLimit, ifNotCast));
            return;
        }
        castCardWithoutPaying(gameData, player, chosenCard, null, grantHaste, sacrificeAtEndStep,
                sourceZone, spellManaValueLimit, ifNotCast, deckOwnerId, chosenFaces);
    }

    private static boolean isAdventurer(Card card) {
        return card.getBackFaceCard() != null && card.getCastingOption(AdventureCast.class).isPresent();
    }

    private static Card adventureFace(Card card) {
        return card.createRuntimeCopyWithFace(card.getBackFaceCard());
    }

    /**
     * The faces of {@code card} it can be cast as now without paying its mana cost, among {@code allowedFaces} (empty
     * for any): an adventurer card as the card or its Adventure, each judged by its own characteristics (CR 716.3a); a
     * modal double-faced card by the faces its legal modes cast (CR 712.8b); any other card as itself.
     */
    private Set<SpellFace> castableFaces(GameData gameData, UUID controllerId, Card card, int spellManaValueLimit,
                                         Set<SpellFace> allowedFaces) {
        Set<SpellFace> faces = java.util.EnumSet.noneOf(SpellFace.class);
        if (isAdventurer(card)) {
            if (allows(allowedFaces, SpellFace.CARD) && !card.hasType(CardType.LAND)
                    && card.getManaValue() <= spellManaValueLimit
                    && freeCastRefusal(gameData, controllerId, card, spellManaValueLimit) == null) {
                faces.add(SpellFace.CARD);
            }
            Card adventure = adventureFace(card);
            if (allows(allowedFaces, SpellFace.ADVENTURE) && adventure.getManaValue() <= spellManaValueLimit
                    && freeCastRefusal(gameData, controllerId, adventure, spellManaValueLimit) == null) {
                faces.add(SpellFace.ADVENTURE);
            }
            return faces;
        }
        if (freeCastRefusal(gameData, controllerId, card, spellManaValueLimit, allowedFaces) == null) {
            faces.addAll(!allowedFaces.isEmpty() ? allowedFaces
                    : card.isModalDoubleFaced() && card.getBackFaceCard() != null
                    ? java.util.EnumSet.of(SpellFace.CARD, SpellFace.BACK_FACE) : java.util.EnumSet.of(SpellFace.CARD));
        }
        return faces;
    }

    private static boolean allows(Set<SpellFace> allowedFaces, SpellFace face) {
        return allowedFaces.isEmpty() || allowedFaces.contains(face);
    }

    /** Asks which face of an adventurer card both of whose faces may be cast is cast (CR 716.3). */
    private void beginFaceChoice(GameData gameData, Player player, Card card, Zone sourceZone,
                                 PayFreeCastCostIncreaseEffect.LibraryCast cast) {
        Map<String, Boolean> faces = new java.util.LinkedHashMap<>();
        faces.put("Cast " + card.getName(), false);
        faces.put("Cast " + card.getBackFaceCard().getName(), true);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(player.getId(), null, null,
                new ChoiceContext.LibraryFreeCastFaceChoice(card, sourceZone, cast, faces),
                new ArrayList<>(faces.keySet()), "Choose which spell to cast."));
    }

    /** Casts the face the caster chose for a card {@link #beginFaceChoice} asked about. */
    public void completeFaceChoice(GameData gameData, Player player, String choice,
                                   ChoiceContext.LibraryFreeCastFaceChoice context) {
        Boolean adventure = context.faces().get(choice);
        if (adventure == null) {
            throw new IllegalStateException("Invalid spell face choice");
        }
        gameData.interaction.clearAwaitingInput();
        PayFreeCastCostIncreaseEffect.LibraryCast cast = context.cast();
        castCardWithoutPaying(gameData, player, context.card(), cast.discoverValue(), cast.grantHaste(),
                cast.sacrificeAtEndStep(), context.sourceZone(), cast.spellManaValueLimit(), cast.ifNotCast(),
                cast.ownerId(), Set.of(adventure ? SpellFace.ADVENTURE : SpellFace.CARD));
    }

    private void handleCastWithoutPayingAndShuffleLibraryChoice(
            GameData gameData, Player player, int cardIndex, boolean canFailToFind,
            List<Card> searchCards, List<Card> sourceCards, List<Card> deck, UUID deckOwnerId) {
        Card chosenCard = null;
        if (cardIndex == -1) {
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
        } else {
            if (cardIndex < 0 || cardIndex >= searchCards.size()) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }
            Card picked = searchCards.get(cardIndex);
            String refusal = freeCastRefusal(gameData, player.getId(), picked);
            if (refusal == null) {
                chosenCard = picked;
                UUID chosenCardId = chosenCard.getId();
                sourceCards.removeIf(card -> card.getId().equals(chosenCardId));
            } else {
                gameLogService.append(gameData, GameLog.cardThen(picked, refusal + " and stays in the library."));
            }
        }

        if (!sourceCards.isEmpty()) {
            deck.addAll(sourceCards);
        }

        if (chosenCard == null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            finishSearchAndResume(gameData);
            return;
        }
        // The shuffle follows the cast: the spell and its targets are chosen first and the shuffle
        // triggers then go on the stack above the spell.
        gameData.libraryToShuffleAfterFreeCast = deckOwnerId;
        castCardWithoutPaying(gameData, player, chosenCard, IfNotCast.LIBRARY_SHUFFLED_IN, deckOwnerId);
    }

    private void handleExileAndMayCastWithoutPayingChoice(GameData gameData, Player player,
                                                           int cardIndex, boolean canFailToFind,
                                                           List<Card> searchCards,
                                                           List<Card> sourceCards, List<Card> deck,
                                                           UUID deckOwnerId) {
        Card chosenCard = null;
        if (cardIndex == -1) {
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
        } else {
            chosenCard = searchCards.get(cardIndex);
            Card selectedCard = chosenCard;
            sourceCards.removeIf(card -> card.getId().equals(selectedCard.getId()));
            exileService.exileCard(gameData, deckOwnerId, selectedCard);
        }

        if (!sourceCards.isEmpty()) {
            Collections.shuffle(sourceCards);
            deck.addAll(sourceCards);
        }

        if (chosenCard != null) {
            Card sourceCard = gameData.pendingEffectResolutionEntry != null
                    ? gameData.pendingEffectResolutionEntry.getCard() : chosenCard;
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    sourceCard,
                    player.getId(),
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                    "Cast " + chosenCard.getName() + " without paying its mana cost?",
                    chosenCard.getId()));
        }

        finishSearchAndResume(gameData);
    }

    /**
     * Whether a searched card may be cast under a "spell with mana value N or less" limit. Only the
     * half of a split card being cast is checked (CR 709.3a), so its cheapest half counts.
     */
    private boolean canCastSearchedFace(Card card, Set<CardType> castableTypes, Integer maxManaValue) {
        if (maxManaValue == null) return false;
        if (!card.hasType(CardType.LAND) && card.getSmallestSpellManaValue() <= maxManaValue
                && (castableTypes == null || castableTypes.isEmpty()
                || castableTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(castableTypes::contains))) return true;
        Card adventure = card.getBackFaceCard();
        return card.getCastingOption(com.github.laxika.magicalvibes.model.AdventureCast.class).isPresent()
                && adventure != null && adventure.getManaValue() <= maxManaValue
                && (castableTypes == null || castableTypes.isEmpty()
                || castableTypes.contains(adventure.getType())
                || adventure.getAdditionalTypes().stream().anyMatch(castableTypes::contains));
    }

    private void handleExileFaceDownAndMayCastOrPutIntoHandChoice(
            GameData gameData, Player player, int cardIndex, boolean canFailToFind,
            List<Card> searchCards, List<Card> sourceCards, List<Card> deck, UUID deckOwnerId,
            Set<CardType> castableTypes, Integer maxManaValue) {
        Card chosenCard = null;
        if (cardIndex == -1) {
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
        } else {
            chosenCard = searchCards.get(cardIndex);
            Card selectedCard = chosenCard;
            sourceCards.removeIf(card -> card.getId().equals(selectedCard.getId()));
            exileService.exileCardFaceDown(gameData, deckOwnerId, chosenCard, null);
        }

        if (!sourceCards.isEmpty()) {
            Collections.shuffle(sourceCards);
            deck.addAll(sourceCards);
        }

        if (chosenCard != null) {
            boolean mayCast = canCastSearchedFace(chosenCard, castableTypes, maxManaValue);
            if (mayCast) {
                Card sourceCard = gameData.pendingEffectResolutionEntry != null
                        ? gameData.pendingEffectResolutionEntry.getCard() : chosenCard;
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        sourceCard,
                        player.getId(),
                        List.of(new MayCastExiledCardWithoutPayingManaCostOrPutIntoHandEffect(maxManaValue)),
                        "Cast " + chosenCard.getName()
                                + " without paying its mana cost? If you don't, put it into your hand.",
                        chosenCard.getId()));
            } else {
                gameData.removeFromExile(chosenCard.getId());
                gameData.addCardToHand(deckOwnerId, chosenCard);
            }

            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " exiles a card face down", chosenCard,
                    mayCast ? "." : " and puts it into their hand."));
        }

        finishSearchAndResume(gameData);
    }

    /**
     * Handles discover's choice: cast the qualifying card for free or put it into the controller's
     * hand. All other revealed cards are put on the bottom of the library in a random order.
     */
    private void handleDiscoverChoice(GameData gameData, Player player, int cardIndex,
                                      List<Card> searchCards, List<Card> sourceCards, List<Card> deck,
                                      Integer discoverValue) {
        if (searchCards.size() != 1) {
            throw new IllegalStateException("Discover must offer exactly one card");
        }

        Card discovered = searchCards.getFirst();
        boolean cast = cardIndex >= 0 && canCastWithoutPaying(gameData, player.getId(), discovered);
        if (cardIndex >= 0 && !cast) {
            gameLogService.append(gameData, GameLog.cardThen(discovered,
                    " can't be cast without paying its mana cost and is put into "
                            + player.getUsername() + "'s hand."));
        } else if (cardIndex < 0) {
            gameLogService.append(gameData, GameLog.cardThen(discovered,
                    " is put into " + player.getUsername() + "'s hand."));
        }

        sourceCards.removeIf(card -> card.getId().equals(discovered.getId()));
        if (cast && discovered.getCastingCostsAndSpellEffects().stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.CostEffect.class::isInstance)) {
            // A spell with additional costs is cast through the free-cast queue, which makes the player pay
            // them; if the cast can't be completed the card is put into the discoverer's hand instead.
            putExiledCardsOnBottom(gameData, deck, sourceCards);
            gameData.pendingExileFreeCastRemainderToHand.add(discovered.getId());
            exileFreeCastQueueSupport.castChosenSpellsWithoutPaying(gameData, player, List.of(discovered.getId()));
            checkDiscoverTriggers(gameData, player.getId(), discoverValue);
            return;
        }
        putExiledCardsOnBottom(gameData, deck, sourceCards);

        if (!cast) {
            gameData.removeFromExile(discovered.getId());
            gameData.addCardToHand(player.getId(), discovered);
            checkDiscoverTriggers(gameData, player.getId(), discoverValue);
            finishSearchAndResume(gameData);
            return;
        }

        castCardWithoutPaying(gameData, player, discovered, discoverValue, false, false, Zone.EXILE,
                Integer.MAX_VALUE, IfNotCast.HAND, player.getId());
    }

    /**
     * Aladdin's Lamp — the player has chosen which looked-at card to keep. Put the chosen card back
     * on top of the library and the rest on the bottom in a random order, then draw a card (the kept
     * one, now on top). The final draw is a real draw event, routed through {@code resolveDrawCard}.
     */
    private void handleCastOneAndPutOtherIntoHandChoice(GameData gameData, Player player, int cardIndex,
                                                         List<Card> searchCards, List<Card> sourceCards,
                                                         List<Card> deck) {
        if (cardIndex == -1) {
            beginPutOneIntoHandChoice(gameData, searchCards, sourceCards, player.getId());
            return;
        }

        Card chosenCard = searchCards.get(cardIndex);
        String refusal = freeCastRefusal(gameData, player.getId(), chosenCard);
        if (refusal != null) {
            gameLogService.append(gameData, GameLog.cardThen(chosenCard, refusal + "."));
            beginPutOneIntoHandChoice(gameData, searchCards, sourceCards, player.getId());
            return;
        }

        Card handCard = searchCards.stream()
                .filter(card -> !card.getId().equals(chosenCard.getId()))
                .findFirst().orElse(null);
        sourceCards.removeIf(card -> card.getId().equals(chosenCard.getId())
                || handCard != null && card.getId().equals(handCard.getId()));
        gameData.removeFromExile(chosenCard.getId());
        if (handCard != null) {
            gameData.removeFromExile(handCard.getId());
            gameData.addCardToHand(player.getId(), handCard);
        }
        putExiledCardsOnBottom(gameData, deck, sourceCards);

        if (handCard != null) {
            gameLogService.append(gameData, GameLog.cardThen(handCard,
                    " is put into " + player.getUsername() + "'s hand."));
        }
        castCardWithoutPaying(gameData, player, chosenCard, IfNotCast.LIBRARY_BOTTOM, player.getId());
    }

    private void handleCastOneAndPutRestIntoHandChoice(GameData gameData, Player player, int cardIndex,
                                                        List<Card> searchCards, List<Card> sourceCards,
                                                        UUID handOwnerId) {
        if (cardIndex == -1) {
            putAllExiledCardsIntoHand(gameData, sourceCards, handOwnerId);
            finishSearchAndResume(gameData);
            return;
        }

        Card chosenCard = searchCards.get(cardIndex);
        String refusal = freeCastRefusal(gameData, player.getId(), chosenCard);
        if (refusal != null) {
            gameLogService.append(gameData, GameLog.cardThen(chosenCard, refusal + "."));
            putAllExiledCardsIntoHand(gameData, sourceCards, handOwnerId);
            finishSearchAndResume(gameData);
            return;
        }

        for (Card card : new ArrayList<>(sourceCards)) {
            if (card.getId().equals(chosenCard.getId())) {
                gameData.removeFromExile(card.getId());
            } else if (gameData.removeFromExile(card.getId())) {
                gameData.addCardToHand(handOwnerId, card);
            }
        }
        castCardWithoutPaying(gameData, player, chosenCard, IfNotCast.HAND, handOwnerId);
    }

    private void putAllExiledCardsIntoHand(GameData gameData, List<Card> cards, UUID handOwnerId) {
        for (Card card : cards) {
            if (gameData.removeFromExile(card.getId())) {
                gameData.addCardToHand(handOwnerId, card);
            }
        }
    }

    private void handlePutOneIntoHandRestToBottomRandom(GameData gameData, int cardIndex,
                                                         List<Card> searchCards, List<Card> sourceCards,
                                                         List<Card> deck, UUID deckOwnerId) {
        if (cardIndex < 0 || cardIndex >= searchCards.size()) {
            throw new IllegalStateException("A card must be chosen for the hand");
        }
        Card chosenCard = searchCards.get(cardIndex);
        sourceCards.removeIf(card -> card.getId().equals(chosenCard.getId())
                || searchCards.stream().anyMatch(qualifyingCard ->
                !qualifyingCard.getId().equals(chosenCard.getId())
                        && card.getId().equals(qualifyingCard.getId())));
        gameData.removeFromExile(chosenCard.getId());
        gameData.addCardToHand(deckOwnerId, chosenCard);
        putExiledCardsOnBottom(gameData, deck, sourceCards);
        finishSearchAndResume(gameData);
    }

    private void beginPutOneIntoHandChoice(GameData gameData, List<Card> searchCards,
                                            List<Card> sourceCards, UUID playerId) {
        String prompt = "Choose one of these cards to put into your hand.";
        beginLibrarySearch(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(playerId, new ArrayList<>(searchCards))
                        .reveals(true)
                        .canFailToFind(false)
                        .sourceCards(sourceCards)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.PUT_ONE_INTO_HAND_REST_TO_BOTTOM_RANDOM)
                        .build(), prompt, false));
    }

    private void putExiledCardsOnBottom(GameData gameData, List<Card> deck, List<Card> cards) {
        cards.forEach(card -> gameData.removeFromExile(card.getId()));
        if (!cards.isEmpty()) {
            Collections.shuffle(cards);
            deck.addAll(cards);
        }
    }

    private void handleDrawChosenRestToBottomRandom(GameData gameData, int cardIndex,
                                                    List<Card> searchCards, List<Card> sourceCards,
                                                    List<Card> deck, UUID deckOwnerId) {
        if (cardIndex < 0 || cardIndex >= searchCards.size()) {
            throw new IllegalStateException("Aladdin's Lamp requires choosing one card to keep");
        }
        Card chosenCard = searchCards.get(cardIndex);
        sourceCards.removeIf(c -> c.getId().equals(chosenCard.getId()));

        // The rest go to the bottom of the library in a random order.
        int bottomed = sourceCards.size();
        if (!sourceCards.isEmpty()) {
            Collections.shuffle(sourceCards);
            deck.addAll(sourceCards);
        }
        // Chosen card back on top, then draw it (fires draw triggers, counts as a draw).
        deck.addFirst(chosenCard);

        String playerName = gameData.playerIdToName.get(deckOwnerId);
        gameLogService.append(gameData, GameLog.text(playerName + " puts " + bottomed
                + " card" + (bottomed != 1 ? "s" : "") + " on the bottom of their library in a random order."));
        log.info("Game {} - {} keeps a card and bottoms {} (Aladdin's Lamp)", gameData.id, playerName, bottomed);

        drawService.resolveDrawCard(gameData, deckOwnerId);

        if (!gameData.interaction.isAwaitingInput()) {
            finishSearchAndResume(gameData);
        }
    }

    /**
     * The legal targets for casting {@code card} without paying its mana cost, or an empty list
     * when the card needs a target and none is legal. Only meaningful for cards where
     * {@link EffectResolution#needsTarget} holds.
     */
    private List<UUID> computeCastWithoutPayingTargets(GameData gameData, Card card, UUID controllerId) {
        Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(card);
        List<UUID> validTargets = new ArrayList<>();

        if (allowedTargets.contains(TargetType.PERMANENT)) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    boolean matchesFilter = card.getTargetFilter() instanceof PermanentPredicateTargetFilter filter
                            ? predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter.predicate())
                            : gameQueryService.isCreature(gameData, p);
                    if (matchesFilter && targetLegalityService.checkSpellTargeting(
                            gameData, card, p.getId(), null, controllerId).isEmpty()) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            validTargets.addAll(gameData.orderedPlayerIds);
        }

        if (allowedTargets.contains(TargetType.SPELL_ON_STACK)) {
            for (StackEntry stackEntry : gameData.stack) {
                UUID targetId = stackEntry.getTargetableId();
                if (targetLegalityService.checkSpellTargetOnStack(gameData, targetId,
                        card.getTargetFilter(), controllerId, null, 0).isEmpty()) {
                    validTargets.add(targetId);
                }
            }
        }

        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            for (UUID pid : gameData.orderedPlayerIds) {
                for (Card graveyardCard : gameData.playerGraveyards.getOrDefault(pid, List.of())) {
                    try {
                        targetLegalityService.validateGraveyardEffectTargetOnly(gameData, card,
                                card.getCastingCostsAndSpellEffects(), graveyardCard.getId(), 0, controllerId);
                        validTargets.add(graveyardCard.getId());
                    } catch (IllegalStateException ignored) {
                        // The graveyard card does not satisfy the spell's target restriction.
                    }
                }
            }
        }

        return validTargets;
    }

    /**
     * The legal targets for a spell cast without paying its mana cost from a non-library zone
     * (Wild Evocation): permanents, players, spells on the stack and graveyard cards it can target.
     */
    public List<UUID> legalTargetsForCastWithoutPaying(GameData gameData, UUID controllerId, Card card) {
        return computeCastWithoutPayingTargets(gameData, card, controllerId);
    }

    private List<UUID> computeCastWithoutPayingTargets(GameData gameData, Card card,
                                                       List<CardEffect> spellEffects, UUID controllerId) {
        List<UUID> validTargets = new ArrayList<>();

        List<UUID> candidateTargets = new ArrayList<>(gameData.orderedPlayerIds);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield != null) {
                candidateTargets.addAll(battlefield.stream().map(Permanent::getId).toList());
            }
        }

        for (UUID targetId : candidateTargets) {
            try {
                targetLegalityService.validateSpellTargeting(gameData, card, spellEffects, targetId, null,
                        controllerId, true, 0);
                validTargets.add(targetId);
            } catch (IllegalStateException ignored) {
                // The mode's target filter or effect restrictions reject this candidate.
            }
        }

        return validTargets;
    }

    private ChooseOneEffect spellModal(Card card) {
        return card.getSpellEffects().stream()
                .filter(ChooseOneEffect::isCastersModal)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .orElse(null);
    }

    private LegalModalOptions legalModalOptions(GameData gameData, Card card, UUID controllerId) {
        return legalModalOptions(gameData, card, controllerId, Integer.MAX_VALUE);
    }

    /**
     * The modes of {@code card}'s modal spell that can be cast. Under a "spell with mana value N or
     * less" limit only a split card's half is checked (CR 709.3a), so a half over the limit isn't
     * offered.
     */
    private LegalModalOptions legalModalOptions(GameData gameData, Card card, UUID controllerId,
                                                int spellManaValueLimit) {
        return legalModalOptions(gameData, card, controllerId, spellManaValueLimit, Set.of());
    }

    /**
     * As above, among the modes that cast one of {@code faces} (empty for any): a modal double-faced card's first mode
     * casts its front face and its second its back face, each judged by that face's mana value (CR 712.8b).
     */
    private LegalModalOptions legalModalOptions(GameData gameData, Card card, UUID controllerId,
                                                int spellManaValueLimit, Set<SpellFace> faces) {
        ChooseOneEffect modal = spellModal(card);
        if (modal == null || modal.choicesRequired() != 1 || modal.choicesMax() != 1) {
            return null;
        }

        List<ChooseOneEffect.ChooseOneOption> legalOptions = new ArrayList<>();
        List<Integer> modeIndices = new ArrayList<>();
        for (int modeIndex = 0; modeIndex < modal.options().size(); modeIndex++) {
            // These cards are cast from a library or exile, never a hand, so a split card can't be fused.
            if (SplitCardCastSupport.isFusedMode(card, modeIndex)
                    || castingPermissionService.isSplitModeForbiddenByName(gameData, controllerId, card, modeIndex)) {
                continue;
            }
            boolean backFaceMode = card.isModalDoubleFaced() && card.getBackFaceCard() != null && modeIndex == 1;
            if (card.isModalDoubleFaced() && !allows(faces, backFaceMode ? SpellFace.BACK_FACE : SpellFace.CARD)) {
                continue;
            }
            ChooseOneEffect.ChooseOneOption option = modal.options().get(modeIndex);
            Card evaluationCard = card.createRuntimeCopy();
            List<CardEffect> effects = new ArrayList<>(evaluationCard.getCastingCostsAndSpellEffects());
            spellCastingService.prepareModalSpellCast(evaluationCard, effects, modeIndex);
            int modeManaValue = backFaceMode ? card.getBackFaceCard().getManaValue() : evaluationCard.getManaValue();
            if (modeManaValue > spellManaValueLimit) {
                continue;
            }
            boolean needsTarget = EffectResolution.needsSpellCastTarget(
                    effects, evaluationCard.isAuraThatRequiresAttachment(), evaluationCard.isEnchantPlayer());
            if (!needsTarget || !computeCastWithoutPayingTargets(gameData, evaluationCard, effects, controllerId).isEmpty()) {
                legalOptions.add(option);
                modeIndices.add(modeIndex);
            }
        }

        if (legalOptions.isEmpty()) {
            return null;
        }

        return new LegalModalOptions(
                new ChooseOneEffect(legalOptions, modal.optional(), modal.choicesRequired(), modal.choicesMax(),
                        modal.allModesWhenOptionalCostPaid(), modal.modesMayRepeat(),
                        modal.choicesMaxCondition()),
                modeIndices);
    }

    public void handleLibraryCastModeChoice(GameData gameData, Player player, String chosenLabel,
                                            ChoiceContext.LibraryCastModeChoice ctx) {
        int selectedOptionIndex = -1;
        for (int i = 0; i < ctx.effect().options().size(); i++) {
            if (ctx.effect().options().get(i).label().equals(chosenLabel)) {
                selectedOptionIndex = i;
                break;
            }
        }
        if (selectedOptionIndex < 0) {
            throw new IllegalArgumentException("Invalid mode: " + chosenLabel);
        }

        int modeIndex = ctx.modeIndices().get(selectedOptionIndex);
        Card card = ctx.cardToCast();
        List<CardEffect> spellEffects = new ArrayList<>(card.getCastingCostsAndSpellEffects());
        int effectiveXValue = spellCastingService.prepareModalSpellCast(card, spellEffects, modeIndex);
        StackEntryType spellType = ctx.spellType();
        // A modal double-faced card cast by its back face (encoded as 1, as in the hand cast path) is
        // cast as that face, so the back face's spell and permanent are the ones that get created.
        if (card.isModalDoubleFaced() && card.getBackFaceCard() != null && effectiveXValue == 1) {
            card = card.getBackFaceCard();
            spellEffects = new ArrayList<>(card.getCastingCostsAndSpellEffects());
            spellType = mapCardTypeToSpellType(card);
            effectiveXValue = 0;
        }
        gameData.interaction.clearAwaitingInput();

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses \"" + chosenLabel + "\" for ", card, "."));

        boolean needsTarget = EffectResolution.needsSpellCastTarget(
                spellEffects, card.isAuraThatRequiresAttachment(), card.isEnchantPlayer());
        if (needsTarget) {
            List<UUID> validTargets = computeCastWithoutPayingTargets(gameData, card, spellEffects,
                    ctx.controllerId());
            if (validTargets.isEmpty()) {
                throw new IllegalStateException("No legal target remains for " + card.getName()
                        + " after choosing a mode");
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.LibraryCastSpellTarget(card, ctx.controllerId(), spellEffects,
                            spellType, null, ctx.discoverValue(),
                            searchedCardOwnerOverride(gameData, card, ctx.controllerId()), ctx.sourceZone()));
            playerInputService.beginPermanentChoice(gameData, ctx.controllerId(), validTargets,
                    "Choose a target for " + card.getName() + ".");

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " casts ", card,
                    " without paying its mana cost — choosing target."));
            log.info("Game {} - {} casts {} without paying mana after choosing mode, choosing target",
                    gameData.id, player.getUsername(), card.getName());
            return;
        }

        StackEntry modalSpell = new StackEntry(
                spellType, card, ctx.controllerId(), card.getName(),
                spellEffects, effectiveXValue, (UUID) null, null
        );
        modalSpell.setSourceZone(ctx.sourceZone());
        modalSpell.setOwnerIdOverride(searchedCardOwnerOverride(gameData, card, ctx.controllerId()));
        gameData.stack.add(modalSpell);
        gameData.recordSpellCast(ctx.controllerId(), card);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " casts ", card, " without paying its mana cost."));
        log.info("Game {} - {} casts {} without paying mana after choosing mode",
                gameData.id, player.getUsername(), card.getName());

        spellEvents.cast(gameData, card, ctx.controllerId(), ctx.sourceZone());
        checkDiscoverTriggers(gameData, ctx.controllerId(), ctx.discoverValue());
        LibraryShuffleHelper.shuffleLibraryPendingAfterFreeCast(gameData);
        finishSearchAndResume(gameData);
    }

    public void castTopCardFromLibraryForMana(GameData gameData, UUID controllerId, Card cardToCast,
                                               String manaCost) {
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty() || !deck.getFirst().getId().equals(cardToCast.getId())) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                    " is no longer on top of the library and cannot be cast."));
            return;
        }

        deck.removeFirst();
        String playerName = gameData.playerIdToName.get(controllerId);
        StackEntryType spellType = mapCardTypeToSpellType(cardToCast);

        LegalModalOptions modalOptions = legalModalOptions(gameData, cardToCast, controllerId);
        if (modalOptions != null) {
            Card runtimeCard = cardToCast.createRuntimeCopy();
            playerInputService.beginLibraryCastModeChoice(gameData, controllerId, runtimeCard,
                    modalOptions.effect(), spellType, modalOptions.modeIndices());
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " casts ", runtimeCard, " by paying " + manaCost + " — choosing mode."));
            return;
        }

        List<CardEffect> spellEffects = cardToCast.getType().isPermanentType()
                ? List.of()
                : new ArrayList<>(cardToCast.getCastingCostsAndSpellEffects());
        if (EffectResolution.needsTarget(cardToCast) || EffectResolution.needsSpellTarget(cardToCast)) {
            List<UUID> validTargets = computeCastWithoutPayingTargets(
                    gameData, cardToCast, spellEffects, controllerId);
            if (validTargets.isEmpty()) {
                deck.addFirst(cardToCast);
                gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                        " has no legal targets and stays on top of the library."));
                return;
            }
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.LibraryCastSpellTarget(
                            cardToCast, controllerId, spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, controllerId, validTargets,
                    "Choose a target for " + cardToCast.getName() + ".");
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " casts ", cardToCast,
                    " by paying " + manaCost + " — choosing target."));
            return;
        }

        gameData.stack.add(new StackEntry(
                spellType, cardToCast, controllerId, cardToCast.getName(),
                spellEffects, 0, (UUID) null, null));
        gameData.recordSpellCast(controllerId, cardToCast);
        gameData.priorityPassedBy.clear();
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " casts ", cardToCast, " by paying " + manaCost + "."));
        log.info("Game {} - {} casts {} from the top of the library by paying {}",
                gameData.id, playerName, cardToCast.getName(), manaCost);
        spellEvents.cast(gameData, cardToCast, controllerId, false);
    }

    /**
     * Whether a "you may cast this without paying its mana cost" permission can actually be taken up
     * for {@code card}. A spell that needs a target but has no legal one can't be cast at all
     * (CR 601.2c), so the permission simply does nothing and the card stays wherever the effect
     * leaves the cards it didn't cast — which differs per effect, so the caller decides.
     *
     * <p>Callers must consult this before moving the card, and only then call
     * {@link #castCardWithoutPaying}.
     */
    private boolean canCastWithoutPaying(GameData gameData, UUID controllerId, Card card) {
        return canCastWithoutPaying(gameData, controllerId, card, Integer.MAX_VALUE);
    }

    /**
     * {@link #canCastWithoutPaying(GameData, UUID, Card)} under a "spell with mana value N or less"
     * limit, which only the half of a split card being cast has to meet (CR 709.3a).
     */
    private boolean canCastWithoutPaying(GameData gameData, UUID controllerId, Card card, int spellManaValueLimit) {
        Zone sourceZone = gameData.findExiledCard(card.getId()) == null ? Zone.LIBRARY : Zone.EXILE;
        return canCastWithoutPaying(gameData, controllerId, card, spellManaValueLimit, sourceZone);
    }

    /**
     * Whether {@code card}, revealed from {@code controllerId}'s hand, can be cast without paying its
     * mana cost (CR 601.2): the cast must be permitted (e.g. not silenced) and its own "cast this spell only
     * during ..." restriction must hold (CR 601.3; only the timing of its card type is ignored), every mandatory additional
     * cost must be payable, and a required target must be legal. Used by Wild Evocation.
     */
    public boolean canCastRevealedFromHandWithoutPaying(GameData gameData, UUID controllerId, Card card) {
        return canCastWithoutPaying(gameData, controllerId, card, Integer.MAX_VALUE, Zone.HAND);
    }

    /**
     * {@link #canCastRevealedFromHandWithoutPaying} for a card looked at or revealed in {@code controllerId}'s
     * library, under a "spell with mana value N or less" limit: a spell that couldn't be cast now is no option of a
     * "you may cast a spell from among them" (CR 608.2d). Used by {@code LookAtTopCardsAndMayCastEffect}.
     */
    public boolean canCastFromLibraryWithoutPaying(GameData gameData, UUID controllerId, Card card,
                                                   int spellManaValueLimit) {
        return canCastWithoutPaying(gameData, controllerId, card, spellManaValueLimit, Zone.LIBRARY);
    }

    private boolean canCastWithoutPaying(GameData gameData, UUID controllerId, Card card, int spellManaValueLimit,
                                         Zone sourceZone) {
        return freeCastRefusal(gameData, controllerId, card, spellManaValueLimit, sourceZone) == null;
    }

    /** {@link #freeCastRefusal(GameData, UUID, Card, int, Zone)} with no mana value limit, from its zone. */
    private String freeCastRefusal(GameData gameData, UUID controllerId, Card card) {
        return freeCastRefusal(gameData, controllerId, card, Integer.MAX_VALUE);
    }

    private String freeCastRefusal(GameData gameData, UUID controllerId, Card card, int spellManaValueLimit) {
        return freeCastRefusal(gameData, controllerId, card, spellManaValueLimit, Set.of());
    }

    /** As above, for a card that may be cast only as one of {@code faces} (empty for any). */
    private String freeCastRefusal(GameData gameData, UUID controllerId, Card card, int spellManaValueLimit,
                                   Set<SpellFace> faces) {
        Zone sourceZone = gameData.findExiledCard(card.getId()) == null ? Zone.LIBRARY : Zone.EXILE;
        return freeCastRefusal(gameData, controllerId, card, spellManaValueLimit, sourceZone, faces);
    }

    /**
     * Why {@code card} can't be cast without paying its mana cost from {@code sourceZone} now, as the log puts it
     * after the card's name, or null when it can (see {@link #canCastWithoutPaying(GameData, UUID, Card)}). A cast
     * from a library or exile must also be able to pay its cost increases, with the mana abilities its caster may
     * activate (CR 601.2f, 601.2g); a cast from a hand asks for them itself.
     */
    private String freeCastRefusal(GameData gameData, UUID controllerId, Card card, int spellManaValueLimit,
                                   Zone sourceZone) {
        return freeCastRefusal(gameData, controllerId, card, spellManaValueLimit, sourceZone, Set.of());
    }

    private String freeCastRefusal(GameData gameData, UUID controllerId, Card card, int spellManaValueLimit,
                                   Zone sourceZone, Set<SpellFace> faces) {
        if (!gameQueryService.canCastSpellFromZone(gameData, card, sourceZone, controllerId)
                || !castingPermissionService.canCastDuringResolution(gameData, controllerId, card)
                || !castingPermissionService.canCastWithSpellTimingRestriction(gameData, controllerId, card)) {
            return " can't be cast";
        }
        if (!castingCostService.canPayAdditionalSpellCosts(gameData, controllerId, card)) {
            return " can't be cast: its additional cost can't be paid";
        }
        boolean legalChoices = spellModal(card) != null
                ? legalModalOptions(gameData, card, controllerId, spellManaValueLimit, faces) != null
                : !(EffectResolution.needsTarget(card) || EffectResolution.needsSpellTarget(card))
                || !computeCastWithoutPayingTargets(gameData, card, controllerId).isEmpty();
        if (!legalChoices) {
            return " has no legal targets, so it can't be cast";
        }
        if (sourceZone != Zone.HAND
                && !freeCastCostIncreaseSupport.payableWithManaAbilities(gameData, controllerId, card, sourceZone)) {
            return " can't be cast: its cost increase can't be paid";
        }
        return null;
    }

    /**
     * Casts {@code chosenCard} (already removed from its library) under {@code player}'s control
     * without paying its mana cost. Targeted spells begin a target choice; non-targeted spells go
     * straight onto the stack. Shared by Sunbird's Invocation and the CAST_WITHOUT_PAYING library
     * search destination (e.g. Knowledge Exploitation).
     *
     * <p>Requires {@link #canCastWithoutPaying} to hold — an uncastable card must never reach here,
     * because where it goes instead is the calling effect's decision, not this method's.
     */
    /**
     * Returns the card's owner when a card cast from another player's library or graveyard is not
     * controlled by its owner, so the resolved spell is disposed of in its owner's zones (CR 400.3).
     * Returns null when the caster owns the card.
     */
    private UUID searchedCardOwnerOverride(GameData gameData, Card card, UUID controllerId) {
        UUID ownerId = card.getOwnerId();
        if (ownerId == null) {
            ownerId = gameData.startingDeckCardIds.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(card.getId()))
                    .map(java.util.Map.Entry::getKey)
                    .findFirst().orElse(null);
        }
        return ownerId == null || ownerId.equals(controllerId) ? null : ownerId;
    }

    private void castCardWithoutPaying(GameData gameData, Player player, Card chosenCard, IfNotCast ifNotCast,
                                       UUID ifNotCastZoneOwnerId) {
        castCardWithoutPaying(gameData, player, chosenCard, null, false, false, Zone.LIBRARY, Integer.MAX_VALUE,
                ifNotCast, ifNotCastZoneOwnerId);
    }

    private void castCardWithoutPaying(GameData gameData, Player player, Card chosenCard,
                                       Integer discoverValue, boolean grantHaste,
                                       boolean sacrificeAtEndStep, Zone sourceZone, int spellManaValueLimit,
                                       IfNotCast ifNotCast, UUID ifNotCastZoneOwnerId) {
        castCardWithoutPaying(gameData, player, chosenCard, discoverValue, grantHaste, sacrificeAtEndStep, sourceZone,
                spellManaValueLimit, ifNotCast, ifNotCastZoneOwnerId, Set.of());
    }

    /**
     * Casts {@code chosenCard}, already out of its library (a card cast from exile leaves exile once its cost
     * increases are paid), without paying its mana cost; {@link #canCastWithoutPaying} must hold.
     *
     * @param ifNotCast            where the card goes when its cost increase isn't paid
     * @param ifNotCastZoneOwnerId the player whose library or hand {@code ifNotCast} names
     * @param faces                the faces it may be cast as (empty for any): {@code ADVENTURE} alone casts an
     *                             adventurer card as its Adventure (CR 716.3); a modal double-faced card is offered
     *                             only the modes casting these faces (CR 712.8b)
     */
    private void castCardWithoutPaying(GameData gameData, Player player, Card chosenCard,
                                       Integer discoverValue, boolean grantHaste,
                                       boolean sacrificeAtEndStep, Zone sourceZone, int spellManaValueLimit,
                                       IfNotCast ifNotCast, UUID ifNotCastZoneOwnerId, Set<SpellFace> faces) {
        boolean asAdventure = isAdventurer(chosenCard) && faces.equals(Set.of(SpellFace.ADVENTURE));
        Card face = asAdventure ? adventureFace(chosenCard) : chosenCard;
        // Its total cost is its cost increases (CR 601.2f), paid once nothing else can stop the cast, from the pool or
        // after a question during which mana abilities may be activated (CR 601.2g).
        FreeCastCostIncreaseSupport.Payment payment = freeCastCostIncreaseSupport.payOrAsk(gameData, player.getId(),
                face, chosenCard.getId(), sourceZone, new PayFreeCastCostIncreaseEffect.LibraryCast(
                        ifNotCastZoneOwnerId, discoverValue, grantHaste, sacrificeAtEndStep, spellManaValueLimit,
                        ifNotCast).castAs(chosenCard, faces));
        if (payment == FreeCastCostIncreaseSupport.Payment.ASKED) {
            return;
        }
        if (payment == FreeCastCostIncreaseSupport.Payment.UNPAYABLE) {
            abandonForUnpaidCostIncrease(gameData, player, chosenCard, discoverValue, ifNotCast,
                    ifNotCastZoneOwnerId);
            return;
        }
        if (sourceZone == Zone.EXILE) {
            gameData.removeFromExile(chosenCard.getId());
        }
        UUID playerId = player.getId();
        String playerName = player.getUsername();
        boolean creatureSpell = face.hasType(CardType.CREATURE);
        Card castCard = face;
        if (creatureSpell && sacrificeAtEndStep) {
            castCard = chosenCard.createRuntimeCopy();
            castCard.setSacrificeAtEndStep(true);
        }
        if (creatureSpell && grantHaste) {
            gameData.spellsGrantedHasteOnEntry.add(castCard.getId());
        }

        // Cast the chosen card without paying its mana cost
        StackEntryType spellType = mapCardTypeToSpellType(castCard);

        LegalModalOptions modalOptions = legalModalOptions(gameData, castCard, playerId, spellManaValueLimit, faces);
        if (modalOptions != null) {
            Card runtimeCard = castCard.createRuntimeCopy();
            playerInputService.beginLibraryCastModeChoice(gameData, playerId, runtimeCard,
                    modalOptions.effect(), spellType, modalOptions.modeIndices(), discoverValue, sourceZone);
            gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", runtimeCard,
                    " without paying its mana cost — choosing mode."));
            log.info("Game {} - {} casts {} without paying mana, choosing mode",
                    gameData.id, playerName, runtimeCard.getName());
            return;
        }

        List<CardEffect> spellEffects = new ArrayList<>(castCard.getCastingCostsAndSpellEffects());

        if (EffectResolution.needsTarget(castCard) || EffectResolution.needsSpellTarget(castCard)) {
            List<UUID> validTargets = computeCastWithoutPayingTargets(gameData, castCard, playerId);

            if (validTargets.isEmpty()) {
                throw new IllegalStateException("Uncastable card reached castCardWithoutPaying: "
                        + castCard.getName() + " — callers must check canCastWithoutPaying first");
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.LibraryCastSpellTarget(
                            castCard, playerId, spellEffects, spellType, null, discoverValue,
                            searchedCardOwnerOverride(gameData, castCard, playerId), sourceZone,
                            asAdventure ? chosenCard : null));
            playerInputService.beginPermanentChoice(gameData, playerId, validTargets,
                    "Choose a target for " + castCard.getName() + ".");

            gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ",
                    castCard, " without paying its mana cost — choosing target."));
            log.info("Game {} - {} casts {} (Sunbird's Invocation), choosing target",
                    gameData.id, playerName, castCard.getName());
            return;
        }

        // Non-targeted spell — put directly on stack
        StackEntry freeSpell = new StackEntry(
                spellType, castCard, playerId, castCard.getName(),
                spellEffects, 0, (UUID) null, null
        );
        freeSpell.setSourceZone(sourceZone);
        freeSpell.setOwnerIdOverride(searchedCardOwnerOverride(gameData, castCard, playerId));
        if (asAdventure) {
            freeSpell.setPhysicalCard(chosenCard);
            freeSpell.setCastWithAdventure(true);
        }
        if (exileFreeCastQueueSupport.beginSacrificeCostIfNeeded(gameData, freeSpell)) return;
        gameData.stack.add(freeSpell);

        gameData.recordSpellCast(playerId, castCard);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " casts ", castCard, " without paying its mana cost."));
        log.info("Game {} - {} casts {} (Sunbird's Invocation) without paying mana",
                gameData.id, playerName, castCard.getName());

        spellEvents.cast(gameData, castCard, playerId, sourceZone);
        checkDiscoverTriggers(gameData, playerId, discoverValue);
        LibraryShuffleHelper.shuffleLibraryPendingAfterFreeCast(gameData);
        // The free spell now sits on the stack, but the ability that cast it is still parked
        // mid-resolution. Only the shared tail drains that park; a bare view invalidation leaves it
        // dangling, which strands any later effect on the same ability and wedges
        // deferPlayerLossCheck so no player can lose to a state-based action again.
        finishSearchAndResume(gameData);
    }

    /**
     * Answers the cost-increase payment question of a search cast {@link #castCardWithoutPaying} paused on: paid (and
     * still castable), the card is cast again, paying from the pool; otherwise it isn't cast and goes where the cast
     * said a card it doesn't cast goes.
     */
    public void resumeAfterCostIncreaseDecision(GameData gameData, Player player, Card card, Zone sourceZone,
                                                PayFreeCastCostIncreaseEffect.LibraryCast cast, boolean paid) {
        // The question was asked about the face being cast; the card is the one cast.
        Card physicalCard = cast.physicalCard() != null ? cast.physicalCard() : card;
        if (paid && freeCastRefusal(gameData, player.getId(), card, cast.spellManaValueLimit(), sourceZone,
                cast.faces()) == null) {
            castCardWithoutPaying(gameData, player, physicalCard, cast.discoverValue(), cast.grantHaste(),
                    cast.sacrificeAtEndStep(), sourceZone, cast.spellManaValueLimit(), cast.ifNotCast(),
                    cast.ownerId(), cast.faces());
            return;
        }
        abandonForUnpaidCostIncrease(gameData, player, physicalCard, cast.discoverValue(), cast.ifNotCast(),
                cast.ownerId());
    }

    private void abandonForUnpaidCostIncrease(GameData gameData, Player player, Card card, Integer discoverValue,
                                              IfNotCast ifNotCast, UUID zoneOwnerId) {
        if (ifNotCast != IfNotCast.STAYS_EXILED) {
            gameData.removeFromExile(card.getId());
        }
        List<Card> library = gameData.playerDecks.computeIfAbsent(zoneOwnerId, ignored -> new ArrayList<>());
        String destination = switch (ifNotCast) {
            case LIBRARY_BOTTOM -> {
                library.add(card);
                yield " It is put on the bottom of its owner's library.";
            }
            case LIBRARY_TOP -> {
                library.addFirst(card);
                yield " It stays on top of its owner's library.";
            }
            case LIBRARY_SHUFFLED_IN -> {
                library.add(java.util.concurrent.ThreadLocalRandom.current().nextInt(library.size() + 1), card);
                yield " It stays in its owner's library.";
            }
            case STAYS_EXILED -> " It stays exiled.";
            case HAND -> {
                gameData.addCardToHand(zoneOwnerId, card);
                yield " It is put into " + gameData.playerIdToName.get(zoneOwnerId) + "'s hand.";
            }
        };
        gameLogService.append(gameData, GameLog.cardThen(card, " can't be cast: its cost increase isn't paid."
                + destination));
        checkDiscoverTriggers(gameData, player.getId(), discoverValue);
        LibraryShuffleHelper.shuffleLibraryPendingAfterFreeCast(gameData);
        finishSearchAndResume(gameData);
    }

    private void checkDiscoverTriggers(GameData gameData, UUID playerId, Integer discoverValue) {
        if (discoverValue != null) {
            ActionEvents.discovered(gameData, playerId, discoverValue);
        }
    }

    private static void addCardAtLibraryPosition(List<Card> deck, Card card, int position) {
        deck.add(Math.min(position, deck.size()), card);
    }

    private void addCardFromLibraryToHand(GameData gameData, UUID handOwnerId, Card card,
                                          UUID deckOwnerId, boolean fromLibrary) {
        gameData.addCardToHand(handOwnerId, card);
    }

    private static String topLibraryPositionText(int position) {
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

    /** Appends {@code cards} as comma-separated card segments (each hoverable) to {@code builder}. */
    /**
     * Power of a card that is outside the battlefield, for "different powers" comparisons. A
     * characteristic-defining power (Lhurgoyf) applies in every zone (CR 604.3), so it is evaluated
     * for the searching player rather than read as the card's unset printed value.
     */
    private Integer searchPower(GameData gameData, Card card, UUID controllerId) {
        var cda = card.getStaticEffects().stream()
                .filter(com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect.class::isInstance)
                .map(com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (cda == null) {
            return card.getPower();
        }
        return amountEvaluationService.evaluate(gameData, cda.power(),
                com.github.laxika.magicalvibes.service.effect.AmountContext.forEstimation(controllerId));
    }

    private static GameLog.Builder appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
        return builder;
    }

    private static StackEntryType mapCardTypeToSpellType(Card card) {
        return switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    }

    private record LegalModalOptions(ChooseOneEffect effect, List<Integer> modeIndices) {}
}
