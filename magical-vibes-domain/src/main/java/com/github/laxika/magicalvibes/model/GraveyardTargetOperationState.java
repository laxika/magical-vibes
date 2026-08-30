package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

public class GraveyardTargetOperationState {
    public int independentTargetGroupIndex = -1;
    public final java.util.List<UUID> independentTargetCardIds = new java.util.ArrayList<>();
    public final java.util.List<Integer> independentTargetGroupSizes = new java.util.ArrayList<>();
    public boolean resolutionTimeExileThenMayBecomeCopyResume;
    public boolean resolutionTimeKayaSpiritsJusticeResume;
    /** Whether a resolution-time collect-evidence choice is awaiting completion. */
    public boolean resolutionTimeCollectEvidenceResume;
    /** Resolution-time selection of cards to return for an aggregate mana-value effect. */
    public boolean resolutionTimeReturnCardsToBattlefieldResume;

    public Card card;
    public UUID controllerId;
    public List<CardEffect> effects;
    public StackEntryType entryType;
    public int xValue;
    public boolean anyNumber;
    public boolean giftPromised;
    /**
     * Whether all chosen targets must come from one graveyard ("... from a single graveyard",
     * Scarab Feast). Enforced in {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen}.
     */
    public boolean singleGraveyard;
    /** In-progress cumulative-upkeep payments, one single-graveyard choice per age counter. */
    public CumulativeUpkeepPaymentContext cumulativeUpkeepPayment;
    /** In-progress payment that moves a fixed number of cards from the controller's graveyard. */
    public ControllerGraveyardPaymentContext controllerGraveyardPayment;

    public record CumulativeUpkeepPaymentContext(UUID sourceControllerId, Card sourceCard,
                                                  UUID sourcePermanentId, ForcedCostOrElseEffect forcedCost,
                                                  int cardsPerPayment, int remainingPayments,
                                                  List<UUID> selectedCardIds) {
        public CumulativeUpkeepPaymentContext {
            selectedCardIds = List.copyOf(selectedCardIds);
        }
    }

    public record ControllerGraveyardPaymentContext(UUID sourceControllerId, Card sourceCard,
                                                     UUID sourcePermanentId,
                                                     ForcedCostOrElseEffect forcedCost, int count) {
    }
    /** Target player for effects like "Target player shuffles ... from their graveyard" */
    public UUID targetPlayerId;
    /** Remaining cast-time graveyard target groups for spells with more than one such group. */
    public List<CardEffect> pendingSpellGraveyardChoiceEffects = List.of();
    /** The cast-time graveyard target group currently being answered. */
    public CardEffect activeSpellGraveyardChoiceEffect;
    /** Selected graveyard cards kept separately for each cast-time target group. */
    public final Map<CardEffect, List<UUID>> spellGraveyardCardIdsByEffect = new IdentityHashMap<>();
    /** Whether the spell is being cast with flashback */
    public boolean flashback;
    /** Physical hand card for an alternate-face spell whose cast face is stored in {@link #card}. */
    public Card physicalCard;
    /** Whether the pending spell was cast as an Adventure. */
    public boolean castWithAdventure;
    /** Source permanent ID for saga chapter graveyard targets (used in SBA check CR 714.4). */
    public UUID sourcePermanentId;
    /** Effective power of an attack-trigger source, captured before graveyard target selection. */
    public Integer triggeringPermanentPowerAtTrigger;
    /** Chapter name for saga chapter graveyard targets (e.g. "I", "II"). */
    public String chapterName;
    /**
     * Spell-on-stack target (a counter) chosen at cast time for a modal mode that pairs a counter
     * with an interactive graveyard return (e.g. Soul Manipulation's "both" mode). Carried through
     * the graveyard-choice flow so the counter survives onto the resulting stack entry's targetId.
     */
    public UUID spellCounterTargetId;
    /** Battlefield/player targets selected for a modal spell paired with this graveyard choice. */
    public List<UUID> permanentTargetIds;
    /**
     * Resolution-time "exile up to one target card from a graveyard" (Grixis Sojourners' death and
     * cycling triggers). When set, {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen}
     * exiles the chosen card and resumes the paused ability resolution (e.g. the cycling draw)
     * instead of pushing a new stack entry. Set by
     * {@code ExileUpToOneCardFromGraveyardEffectHandler}.
     */
    public boolean resolutionTimeExileResume;
    /**
     * Resolution-time "you may exile a creature card from your graveyard. If you do, create a 4/4
     * black Zombie token copy with haste until end of turn" (God-Pharaoh's Gift). When set,
     * {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen} exiles the chosen card,
     * creates the transformed token copy, and resumes the paused ability resolution. Set by
     * {@code ExileOwnCreatureFromGraveyardCreateZombieTokenCopyEffectHandler}.
     */
    public boolean resolutionTimeExileCreateZombieTokenCopyResume;
    public boolean resolutionTimeExileNCardsThenEffectResume;
    public List<UUID> resolutionTimeExileNCardsThenEffectChosenCardIds;
    public boolean resolutionTimeDragonApproachResume;
    /** Resolution-time choice for Chandra, Heart of Fire's graveyard-and-library exile. */
    public ExileMatchingCardsFromGraveyardAndLibraryContext resolutionTimeExileMatchingCardsResume;
    /** Resolution-time optional filtered exile whose successful choice has a life-loss rider. */
    public boolean resolutionTimeExileThenEachOpponentLosesLifeResume;
    /** Whether the optional filtered exile choice has been answered. */
    public boolean resolutionTimeExileThenEachOpponentLosesLifeChoiceMade;
    /** The card chosen by the optional filtered exile choice, or {@code null} for decline. */
    public UUID resolutionTimeExileThenEachOpponentLosesLifeChosenCardId;
    /** Resolution-time land exile followed by a target-creature counter placement. */
    public boolean resolutionTimeExileThenPutCounterOnTargetCreatureResume;
    public ExileUpToOneMatchingCardFromEachGraveyardContext
            resolutionTimeExileUpToOneMatchingCardFromEachGraveyardResume;
    public boolean resolutionTimeShuffleUpToThreeCardsFromEachGraveyardResume;
    public boolean resolutionTimeExileThenPutCountersOnSharedTypeCreaturesResume;
    public boolean resolutionTimeExileThenPutCountersOnSharedTypeCreaturesChoiceMade;
    public UUID resolutionTimeExileThenPutCountersOnSharedTypeCreaturesChosenCardId;
    /** Whether the land-exile choice has been answered. */
    public boolean resolutionTimeExileThenPutCounterOnTargetCreatureChoiceMade;
    /** The land chosen by the resolution-time exile choice, or {@code null} for decline. */
    public UUID resolutionTimeExileThenPutCounterOnTargetCreatureChosenCardId;
    public boolean resolutionTimeExileOwnGraveyardCardPutCountersResume;
    public boolean resolutionTimeExileOwnGraveyardCardPutCountersChoiceMade;
    public UUID resolutionTimeExileOwnGraveyardCardPutCountersChosenCardId;
    public boolean resolutionTimeExileOneOfDiscardedCardsResume;
    public boolean resolutionTimeExileOneOfDiscardedCardsChoiceMade;
    public List<UUID> resolutionTimeExileOneOfDiscardedCardsCandidateIds = List.of();
    public UUID resolutionTimeExileOneOfDiscardedCardsChosenCardId;
    public UUID resolutionTimeExileOneOfDiscardedCardsSourcePermanentId;
    public boolean resolutionTimePutOnBottomThenExileTopCardsResume;
    public boolean resolutionTimePutOnBottomThenExileTopCardsChoiceMade;
    public UUID resolutionTimePutOnBottomThenExileTopCardsChosenCardId;
    /** Whether an optional graveyard exile with a follow-up is awaiting its answer. */
    public boolean resolutionTimeExileThenEffectResume;
    /** Whether the optional graveyard exile with a follow-up has been answered. */
    public boolean resolutionTimeExileThenEffectChoiceMade;
    /** The card chosen for the optional graveyard exile, or {@code null} for a decline. */
    public UUID resolutionTimeExileThenEffectChosenCardId;
    /**
     * Resolution-time "target opponent chooses a card in your graveyard" (Forgotten Lore or Shrouded
     * Lore). When set,
     * {@code GraveyardChoiceHandlerService.handleGraveyardCardChosen} only records the chosen card on
     * {@code GameData.forgottenLore} and resumes the paused resolution — the card is not moved. Set by
     * {@code ForgottenLoreEffectHandler}.
     */
    public boolean resolutionTimeForgottenLoreResume;
    /**
     * Resolution-time "target opponent chooses one of the top two cards of your graveyard"
     * (Phyrexian Grimoire). When set, {@code GraveyardChoiceHandlerService.handleGraveyardCardChosen}
     * only records the chosen card on {@link #phyrexianGrimoireChosenCardId} and resumes the paused
     * resolution — the card is not moved. Set by
     * {@code OpponentChoosesOneOfTopTwoGraveyardCardsEffectHandler}.
     */
    public boolean resolutionTimePhyrexianGrimoireResume;
    /** Card the opponent just picked for the above, consumed on the next re-entry. */
    public UUID phyrexianGrimoireChosenCardId;
    /** Whether Wake to Slaughter is awaiting the opponent's choice between its two targets. */
    public boolean resolutionTimeWakeToSlaughterResume;
    /** Card the opponent just chose to return to hand for Wake to Slaughter. */
    public UUID wakeToSlaughterChosenCardId;
    /** Card chosen by the target opponent for a resolution-time opponent graveyard choice. */
    public UUID scroungeChosenCardId;
    /** Whether a resolution-time target-opponent graveyard choice is awaiting an answer. */
    public boolean resolutionTimeScroungeResume;
    /** Whether an opponent's resolution-time graveyard choice returns the card to its owner's hand. */
    public boolean resolutionTimeOpponentChoosesCardToHandResume;
    /** Opponent selected by the controller for the resolution-time graveyard choice. */
    public UUID opponentChoosesCardToHandChosenOpponentId;
    /** Card selected by the opponent for the resolution-time graveyard choice. */
    public UUID opponentChoosesCardToHandChosenCardId;
    /**
     * As-enters "exile any number of creature cards from your graveyard" (CR 614.1c, Sutured
     * Ghoul). When set, {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen} exiles the
     * chosen cards tracked with the entering permanent and then resumes the entry by running its
     * ETB triggers, instead of pushing a new stack entry. Set by
     * {@code BattlefieldEntryService.handleCreatureEnteredBattlefield}.
     */
    public AsEntersGraveyardExileContext asEntersExile;
    public MilledCreatureReturnContext milledCreatureReturn;

    /**
     * The entry context needed to resume {@code BattlefieldEntryService.processCreatureETBEffects}
     * after an as-enters graveyard exile choice.
     */
    public record AsEntersGraveyardExileContext(UUID enteringPermanentId, UUID controllerId, Card card,
                                                UUID targetId, boolean wasCastFromHand, int etbMode,
                                                int xValue, boolean kicked, List<UUID> targetIds,
                                                int countersPerCard, List<CounterType> counterTypes) {
        public AsEntersGraveyardExileContext(UUID enteringPermanentId, UUID controllerId, Card card,
                                             UUID targetId, boolean wasCastFromHand, int etbMode,
                                             int xValue, boolean kicked, List<UUID> targetIds,
                                             int countersPerCard) {
            this(enteringPermanentId, controllerId, card, targetId, wasCastFromHand, etbMode,
                    xValue, kicked, targetIds, countersPerCard, List.of());
        }
    }

    public record ExileMatchingCardsFromGraveyardAndLibraryContext(UUID controllerId, CardPredicate filter) {
    }

    public record DeadlyCoverUpContext(UUID chosenCardId) {
    }

    /** Resolution-time choice state for Deadly Cover-Up's non-targeting graveyard exile. */
    public DeadlyCoverUpContext resolutionTimeDeadlyCoverUp;

    public record ExileUpToOneMatchingCardFromEachGraveyardContext(
            UUID controllerId, UUID sourcePermanentId, CardPredicate filter) {
    }

    public record MilledCreatureReturnContext(List<UUID> chosenCardIds) {
        public MilledCreatureReturnContext {
            chosenCardIds = chosenCardIds == null ? null : List.copyOf(chosenCardIds);
        }
    }
}
