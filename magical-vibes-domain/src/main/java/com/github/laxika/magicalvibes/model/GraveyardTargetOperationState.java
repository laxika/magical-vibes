package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.UUID;

public class GraveyardTargetOperationState {

    public Card card;
    public UUID controllerId;
    public List<CardEffect> effects;
    public StackEntryType entryType;
    public int xValue;
    public boolean anyNumber;
    /**
     * Whether all chosen targets must come from one graveyard ("... from a single graveyard",
     * Scarab Feast). Enforced in {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen}.
     */
    public boolean singleGraveyard;
    /** Target player for effects like "Target player shuffles ... from their graveyard" */
    public UUID targetPlayerId;
    /** Whether the spell is being cast with flashback */
    public boolean flashback;
    /** Source permanent ID for saga chapter graveyard targets (used in SBA check CR 714.4). */
    public UUID sourcePermanentId;
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
    /** Resolution-time choice for Chandra, Heart of Fire's graveyard-and-library exile. */
    public ExileMatchingCardsFromGraveyardAndLibraryContext resolutionTimeExileMatchingCardsResume;
    /** Resolution-time optional filtered exile whose successful choice has a life-loss rider. */
    public boolean resolutionTimeExileThenEachOpponentLosesLifeResume;
    /** Whether the optional filtered exile choice has been answered. */
    public boolean resolutionTimeExileThenEachOpponentLosesLifeChoiceMade;
    /** The card chosen by the optional filtered exile choice, or {@code null} for decline. */
    public UUID resolutionTimeExileThenEachOpponentLosesLifeChosenCardId;
    /**
     * Resolution-time "target opponent chooses a card in your graveyard" (Forgotten Lore). When set,
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
    /** Card chosen by the target opponent for a resolution-time opponent graveyard choice. */
    public UUID scroungeChosenCardId;
    /** Whether a resolution-time target-opponent graveyard choice is awaiting an answer. */
    public boolean resolutionTimeScroungeResume;
    /**
     * As-enters "exile any number of creature cards from your graveyard" (CR 614.1c, Sutured
     * Ghoul). When set, {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen} exiles the
     * chosen cards tracked with the entering permanent and then resumes the entry by running its
     * ETB triggers, instead of pushing a new stack entry. Set by
     * {@code BattlefieldEntryService.handleCreatureEnteredBattlefield}.
     */
    public AsEntersGraveyardExileContext asEntersExile;

    /**
     * The entry context needed to resume {@code BattlefieldEntryService.processCreatureETBEffects}
     * after an as-enters graveyard exile choice.
     */
    public record AsEntersGraveyardExileContext(UUID enteringPermanentId, UUID controllerId, Card card,
                                                UUID targetId, boolean wasCastFromHand, int etbMode,
                                                boolean kicked) {
    }

    public record ExileMatchingCardsFromGraveyardAndLibraryContext(UUID controllerId, CardPredicate filter) {
    }
}
