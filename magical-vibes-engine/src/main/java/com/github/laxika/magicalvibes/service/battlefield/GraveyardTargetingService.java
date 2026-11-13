package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.IndependentlyTargetedGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class GraveyardTargetingService {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    @Autowired @Lazy private TargetLegalityService targetLegalityService;
    @Autowired @Lazy private TriggeredAbilityStackService triggeredAbilityStackService;

    /**
     * Returns the given player's graveyard as a pool of legal targets, or {@code null} when no card
     * in a graveyard can be targeted (Ground Seal). Every targeting handler in this class reads
     * graveyards through here, so the restriction applies uniformly and an emptied pool makes the
     * trigger fizzle or the spell find no legal target.
     */
    /** How a prompt names the graveyards {@code scope} searches. */
    private String zoneLabel(GraveyardSearchScope scope) {
        return switch (scope) {
            case ALL_GRAVEYARDS -> "a graveyard";
            case OPPONENT_GRAVEYARD -> "an opponent's graveyard";
            case CONTROLLERS_GRAVEYARD -> "your graveyard";
        };
    }

    private List<Card> targetableGraveyard(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (!gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            return null;
        }
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            return null;
        }
        return graveyard.stream()
                .filter(card -> !gameQueryService.isLandCardTargetRestricted(gameData, card, sourceControllerId))
                .toList();
    }

    /**
     * Begins the one selection across the battlefield and graveyards that {@code record}'s ability makes as it goes on
     * the stack (CR 603.3d), an ability the stack door parked for it ({@code CardEffect.battlefieldAndGraveyardChoice}:
     * "up to three other target creatures from the battlefield and/or creature cards from graveyards", Angel of
     * Serenity). The chosen cards finish the parked ability ({@code GraveyardChoiceHandlerService}); with nothing to
     * choose it goes on the stack with no target, as "up to" allows.
     */
    public void beginBattlefieldAndGraveyardChoice(GameData gameData, StackEntry record,
                                                   BattlefieldAndGraveyardCardChoosingEffect choosingEffect) {
        Card card = record.getCard();
        UUID controllerId = record.getControllerId();
        UUID sourcePermanentId = record.getSourcePermanentId();
        List<Card> pool = battlefieldAndGraveyardPool(gameData, controllerId, card, sourcePermanentId,
                choosingEffect);
        if (pool.isEmpty()) {
            triggeredAbilityStackService.finishParked(gameData, record, entry -> {
            });
            gameLogService.append(gameData, GameLog.cardThen(card, "'s ability triggers targeting no cards."));
            return;
        }
        int maxTargets = Math.min(choosingEffect.mixedZoneMaxTargets(record.getXValue()), pool.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(record.getEffectsToResolve());
        gameData.graveyardTargetOperation.sourcePermanentId = sourcePermanentId;
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        gameData.graveyardTargetOperation.attackedTargetId = sourcePermanent == null
                ? null : sourcePermanent.getAttackTarget();
        gameData.graveyardTargetOperation.xValue = record.getXValue();
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.triggerRecord = record;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, pool, maxTargets,
                card.getName() + "'s ability — Choose up to " + maxTargets + " "
                        + choosingEffect.mixedZoneChoiceDescription(maxTargets));
    }

    /**
     * Begins the player target of {@code record}'s ability, which the stack door parked for a target player and then
     * target cards in that player's graveyard ({@code choice}, Loaming Shaman), as it goes on the stack (CR 603.3d);
     * {@link #chooseTargetPlayersGraveyardCards} takes the answer. With no player it can target, the ability is removed.
     */
    public void beginTargetPlayerGraveyardChoice(GameData gameData, StackEntry record,
                                                 GraveyardCardChoosingEffect choice) {
        Card card = record.getCard();
        UUID controllerId = record.getControllerId();
        List<UUID> players = gameData.orderedPlayerIds.stream()
                .filter(playerId -> targetLegalityService.playerUntargetableReason(gameData, playerId, controllerId,
                        card) == null)
                .toList();
        if (players.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s ability has no valid targets."));
            return;
        }
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TargetPlayerGraveyardCardsTrigger(card, controllerId, record, choice));
        playerInputService.beginAnyTargetChoice(gameData, controllerId, List.of(), players,
                card.getName() + "'s ability — Choose target player.");
    }

    /**
     * Takes the player target {@code targetPlayerId} of the ability {@code pending} is about and begins the choice of
     * target cards in that player's graveyard; with none to choose, the ability goes on the stack targeting the player
     * alone.
     */
    public void chooseTargetPlayersGraveyardCards(GameData gameData, UUID targetPlayerId,
                                                  PermanentChoiceContext.TargetPlayerGraveyardCardsTrigger pending) {
        StackEntry record = pending.record();
        Card card = pending.sourceCard();
        UUID controllerId = pending.controllerId();
        GraveyardCardChoosingEffect choice = pending.choice();
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, targetPlayerId, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (choice.graveyardChoiceFilter() == null || predicateEvaluationService.matchesCardPredicate(
                        graveyardCard, choice.graveyardChoiceFilter(), card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }
        if (matchingCards.isEmpty()) {
            triggeredAbilityStackService.finishParked(gameData, record, entry -> {
                entry.setTargetId(targetPlayerId);
                entry.setNonTargeting(false);
            });
            gameLogService.append(gameData, GameLog.cardThen(card, "'s ability targets "
                    + gameData.playerIdToName.get(targetPlayerId) + "."));
            return;
        }
        int maxTargets = Math.min(choice.graveyardChoiceMaxTargets(), matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(record.getEffectsToResolve());
        gameData.graveyardTargetOperation.targetPlayerId = targetPlayerId;
        gameData.graveyardTargetOperation.sourcePermanentId = record.getSourcePermanentId();
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.triggerRecord = record;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose any number of target cards from "
                        + gameData.playerIdToName.get(targetPlayerId) + "'s graveyard.");
    }

    /**
     * The battlefield permanents (as their cards) and graveyard cards {@code choosingEffect} of {@code card}'s ability
     * may choose, each a legal target of the ability.
     */
    private List<Card> battlefieldAndGraveyardPool(GameData gameData, UUID controllerId, Card card,
                                                   UUID sourcePermanentId,
                                                   BattlefieldAndGraveyardCardChoosingEffect choosingEffect) {
        PermanentPredicate battlefieldPredicate = choosingEffect.mixedZoneBattlefieldPredicate();
        CardPredicate graveyardPredicate = choosingEffect.mixedZoneGraveyardPredicate();
        List<Card> pool = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (choosingEffect.mixedZoneExcludesSourcePermanent()
                        && permanent.getId().equals(sourcePermanentId)) continue;
                if (targetLegalityService.checkTriggeredPermanentTargetableReason(
                        gameData, permanent, card, controllerId).isPresent()) continue;
                if (battlefieldPredicate == null
                        ? gameQueryService.isCreature(gameData, permanent)
                        : predicateEvaluationService.matchesPermanentPredicate(gameData, permanent,
                        battlefieldPredicate)) {
                    pool.add(permanent.getCard());
                }
            }
        });
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId, controllerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (!gameQueryService.isLandCardTargetRestricted(gameData, graveyardCard, controllerId)
                        && (graveyardPredicate == null
                        ? graveyardCard.hasType(CardType.CREATURE)
                        : predicateEvaluationService.matchesCardPredicate(graveyardCard, graveyardPredicate,
                        card.getId(), gameData, playerId))) {
                    pool.add(graveyardCard);
                }
            }
        }
        return pool;
    }


    private UUID sourcePermanentId(GameData gameData, UUID controllerId, Card card) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .map(Permanent::getId)
                .findFirst()
                .orElse(null);
    }

    public void handleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                               StackEntryType entryType, int xValue) {
        handleExactNGraveyardSpellTargeting(gameData, controllerId, card, entryType, xValue,
                new com.github.laxika.magicalvibes.model.filter.CardTypePredicate(CardType.CREATURE),
                "to exile", card.getCastingCostsAndSpellEffects(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    public void handleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                               StackEntryType entryType, int xValue,
                                               List<CardEffect> spellEffects, GraveyardSearchScope scope) {
        handleExactNGraveyardSpellTargeting(gameData, controllerId, card, entryType, xValue,
                new com.github.laxika.magicalvibes.model.filter.CardTypePredicate(CardType.CREATURE),
                "to exile", spellEffects, scope);
    }

    public void handleExactNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                     StackEntryType entryType, int targetCount,
                                                     CardPredicate filter, String destination) {
        handleExactNGraveyardSpellTargeting(gameData, controllerId, card, entryType, targetCount,
                filter, destination, card.getCastingCostsAndSpellEffects(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    public void handleExactNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                     StackEntryType entryType, int targetCount,
                                                     CardPredicate filter, String destination,
                                                     List<CardEffect> spellEffects, GraveyardSearchScope scope) {
        handleExactNGraveyardSpellTargeting(gameData, controllerId, card, entryType, targetCount,
                filter, destination, spellEffects, scope, null);
    }

    public void handleExactNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                     StackEntryType entryType, int targetCount,
                                                     CardPredicate filter, String destination,
                                                     List<CardEffect> spellEffects, GraveyardSearchScope scope,
                                                     List<UUID> eligibleBeforeCosts) {
        List<Card> matchingCards = new ArrayList<>();
        for (UUID graveyardOwner : scope.graveyardOwners(gameData.orderedPlayerIds, controllerId)) {
            List<Card> graveyard = targetableGraveyard(gameData, graveyardOwner, controllerId);
            if (graveyard != null) {
                for (Card graveyardCard : graveyard) {
                    if ((eligibleBeforeCosts == null || eligibleBeforeCosts.contains(graveyardCard.getId()))
                            && predicateEvaluationService.matchesCardPredicate(
                            graveyardCard, filter, card.getId(), gameData, graveyardOwner)) {
                        matchingCards.add(graveyardCard);
                    }
                }
            }
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = targetCount;
        gameData.graveyardTargetOperation.anyNumber = false;
        String searchZone = scope == GraveyardSearchScope.ALL_GRAVEYARDS ? "graveyards" : zoneLabel(scope);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, targetCount,
                targetCount, "Choose " + targetCount + " target " + CardPredicateUtils.describeFilter(filter)
                        + (targetCount != 1 ? "s" : "") + " from " + searchZone + " " + destination + ".");
    }

    public void handleAnyNumberGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                        StackEntryType entryType, CardPredicate filter) {
        handleAnyNumberGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter,
                card.getCastingCostsAndSpellEffects());
    }

    public void handleAnyNumberGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                        StackEntryType entryType, CardPredicate filter,
                                                        List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, controllerId, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        int maxTargets = matchingCards.size();
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose any number of target " + filterLabel + "s from your graveyard.");
    }

    /** Targets up to any number of matching cards from one chosen player's graveyard. */
    public void handleUpToNSingleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                         StackEntryType entryType, CardPredicate filter,
                                                         int maxTargetsCap, List<CardEffect> spellEffects) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter, maxTargetsCap,
                spellEffects, 0, false, false, GraveyardSearchScope.ALL_GRAVEYARDS, true, null, null);
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                    List<CardEffect> spellEffects) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter, maxTargetsCap,
                spellEffects, 0, false, false, GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                false, null, null);
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                    List<CardEffect> spellEffects, GraveyardSearchScope source,
                                                    boolean singleGraveyard) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter, maxTargetsCap,
                spellEffects, 0, false, false, source, singleGraveyard, null, null);
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                    List<CardEffect> spellEffects, GraveyardSearchScope source) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter, maxTargetsCap,
                spellEffects, 0, false, false, source, false, null, null);
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType,
                                                    ReturnTargetCardsFromGraveyardToHandEffect returnEffect,
                                                    int maxTargetsCap, List<CardEffect> spellEffects) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, returnEffect.filter(),
                maxTargetsCap, spellEffects, returnEffect.minTargets(),
                returnEffect.requireSharedCreatureType(), false,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, null, null);
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType,
                                                    ReturnTargetCardsFromGraveyardToHandEffect returnEffect,
                                                    int maxTargetsCap, List<CardEffect> spellEffects,
                                                    int minTargets) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, returnEffect.filter(),
                maxTargetsCap, spellEffects, minTargets,
                returnEffect.requireSharedCreatureType(), false,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, null);
    }

    /**
     * Targets the union of several independently optional graveyard target groups. The answer
     * handler validates that the selected cards can be assigned one-to-one to the effect's filters.
     */
    public void handleUpToOneOfEachFilterGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                                                   Card card, StackEntryType entryType,
                                                                   ReturnUpToOneOfEachFilterFromGraveyardToHandEffect effect,
                                                                   List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, controllerId, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (effect.filters().stream().anyMatch(filter ->
                        predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId()))) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards,
                effect.filters().size(),
                "Choose up to " + effect.filters().size()
                        + " target cards from your graveyard to return to your hand.");
    }

    /** Begins the ordered, independently optional graveyard target groups of a spell. */
    public boolean beginIndependentGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                                            IndependentlyTargetedGraveyardCardsEffect effect) {
        return beginIndependentGraveyardSpellTargeting(gameData, controllerId, effect, false);
    }

    /** Begins independent graveyard target groups, optionally enabling kicked-only groups. */
    public boolean beginIndependentGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                                            IndependentlyTargetedGraveyardCardsEffect effect,
                                                            boolean kicked) {
        if (effect.targetScopes().size() != effect.targetFilters().size()
                || effect.targetGroupsOnlyWhenKicked().size() != effect.targetFilters().size()) {
            throw new IllegalArgumentException("Independent graveyard target metadata must have equal sizes");
        }
        while (gameData.graveyardTargetOperation.independentTargetGroupIndex
                < effect.targetFilters().size()) {
            int groupIndex = gameData.graveyardTargetOperation.independentTargetGroupIndex;
            if (effect.targetGroupsOnlyWhenKicked().get(groupIndex) && !kicked) {
                gameData.graveyardTargetOperation.independentTargetGroupSizes.add(0);
                gameData.graveyardTargetOperation.independentTargetGroupIndex++;
                continue;
            }

            List<Card> matchingCards = matchingIndependentGraveyardCards(gameData, controllerId, effect, groupIndex);

            if (matchingCards.isEmpty()) {
                if (effect.minimumTargetCounts().get(groupIndex) > 0) {
                    throw new IllegalStateException("No legal target for required graveyard target group");
                }
                gameData.graveyardTargetOperation.independentTargetGroupSizes.add(0);
                gameData.graveyardTargetOperation.independentTargetGroupIndex++;
                continue;
            }

            // A required target that cannot be chosen means the whole ability is not put on the stack,
            // so no earlier group is prompted for before a later required group is known to have a target.
            for (int laterIndex = groupIndex + 1; laterIndex < effect.targetFilters().size(); laterIndex++) {
                if (effect.minimumTargetCounts().get(laterIndex) > 0
                        && !(effect.targetGroupsOnlyWhenKicked().get(laterIndex) && !kicked)
                        && matchingIndependentGraveyardCards(gameData, controllerId, effect, laterIndex).isEmpty()) {
                    throw new IllegalStateException("No legal target for required graveyard target group");
                }
            }

            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, 1,
                    effect.minimumTargetCounts().get(groupIndex),
                    "Choose up to one target " + effect.targetDescriptions().get(groupIndex)
                            + " from " + zoneLabel(effect.targetScopes().get(groupIndex)) + ".");
            return true;
        }

        gameData.graveyardTargetOperation.independentTargetGroupIndex = -1;
        return false;
    }

    private List<Card> matchingIndependentGraveyardCards(GameData gameData, UUID controllerId,
                                                         IndependentlyTargetedGraveyardCardsEffect effect,
                                                         int groupIndex) {
        GraveyardSearchScope scope = effect.targetScopes().get(groupIndex);
        List<Card> matchingCards = new ArrayList<>();
        for (UUID graveyardOwnerId : scope.graveyardOwners(gameData.orderedPlayerIds, controllerId)) {
            List<Card> graveyard = targetableGraveyard(gameData, graveyardOwnerId, controllerId);
            if (graveyard == null) {
                continue;
            }
            for (Card graveyardCard : graveyard) {
                if (effect.requiresDistinctTargets()
                        && gameData.graveyardTargetOperation.independentTargetCardIds
                        .contains(graveyardCard.getId())) {
                    continue;
                }
                if (predicateEvaluationService.matchesCardPredicate(
                        graveyardCard, effect.targetFilters().get(groupIndex),
                        gameData.graveyardTargetOperation.card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }
        return matchingCards;
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                    List<CardEffect> spellEffects, boolean fromBattlefieldThisTurn) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter, maxTargetsCap,
                spellEffects, 0, false, fromBattlefieldThisTurn,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, null, null);
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType,
                                                    ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect,
                                                    List<CardEffect> spellEffects) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType,
                returnEffect.filter(), returnEffect.maxTargets(), spellEffects, returnEffect.minTargets(), false,
                returnEffect.fromBattlefieldThisTurn(), returnEffect.source(),
                returnEffect.singleGraveyard(), null,
                returnEffect.hasTotalManaValueCap() ? returnEffect.maxTotalManaValue() : null,
                graveyardTargetFilterFor(card, returnEffect));
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType,
                                                    ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect,
                                                    int maxTargetsCap, Integer xValue,
                                                    List<CardEffect> spellEffects) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType,
                returnEffect.filter(), maxTargetsCap, spellEffects, returnEffect.minTargets(), false,
                returnEffect.fromBattlefieldThisTurn(), returnEffect.source(),
                returnEffect.singleGraveyard(), xValue,
                returnEffect.hasTotalManaValueCap() ? returnEffect.maxTotalManaValue() : null,
                graveyardTargetFilterFor(card, returnEffect));
    }

    private void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                     StackEntryType entryType, CardPredicate filter,
                                                     int maxTargetsCap, List<CardEffect> spellEffects,
                                                     int minTargets, boolean requireSharedCreatureType,
                                                     boolean fromBattlefieldThisTurn,
                                                     GraveyardSearchScope source,
                                                     boolean singleGraveyard,
                                                     Integer xValue) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter, maxTargetsCap,
                spellEffects, minTargets, requireSharedCreatureType, fromBattlefieldThisTurn, source,
                singleGraveyard, xValue, null, null);
    }

    private void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                     StackEntryType entryType, CardPredicate filter,
                                                     int maxTargetsCap, List<CardEffect> spellEffects,
                                                     int minTargets, boolean requireSharedCreatureType,
                                                      boolean fromBattlefieldThisTurn,
                                                      GraveyardSearchScope source,
                                                      boolean singleGraveyard,
                                                      Integer xValue,
                                                      Integer maxTotalManaValue) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter, maxTargetsCap,
                spellEffects, minTargets, requireSharedCreatureType, fromBattlefieldThisTurn, source,
                singleGraveyard, xValue, maxTotalManaValue, null);
    }

    private void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                     StackEntryType entryType, CardPredicate filter,
                                                     int maxTargetsCap, List<CardEffect> spellEffects,
                                                     int minTargets, boolean requireSharedCreatureType,
                                                      boolean fromBattlefieldThisTurn,
                                                      GraveyardSearchScope source,
                                                      boolean singleGraveyard,
                                                      Integer xValue,
                                                      Integer maxTotalManaValue,
                                                      GraveyardCardPredicateTargetFilter targetGroupFilter) {
        List<Card> matchingCards = new ArrayList<>();
        List<UUID> graveyardOwners = switch (source) {
            case CONTROLLERS_GRAVEYARD -> List.of(controllerId);
            case OPPONENT_GRAVEYARD -> gameData.orderedPlayerIds.stream()
                    .filter(playerId -> !playerId.equals(controllerId))
                    .toList();
            case ALL_GRAVEYARDS -> gameData.orderedPlayerIds;
        };
        for (UUID graveyardOwner : graveyardOwners) {
            if (targetGroupFilter != null && targetGroupFilter.minimumPoisonCounters() != null
                    && !graveyardOwner.equals(controllerId)
                    && gameData.playerPoisonCounters.getOrDefault(graveyardOwner, 0)
                    < targetGroupFilter.minimumPoisonCounters()) {
                continue;
            }
            List<Card> graveyard = targetableGraveyard(gameData, graveyardOwner, controllerId);
            var trackedIds = fromBattlefieldThisTurn
                    ? gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(
                            graveyardOwner, java.util.Set.of())
                    : null;
            if (graveyard != null) {
                for (Card graveyardCard : graveyard) {
                    if ((trackedIds == null || trackedIds.contains(graveyardCard.getId()))
                            && (maxTotalManaValue == null || graveyardCard.getManaValue() <= maxTotalManaValue)
                            && predicateEvaluationService.matchesCardPredicate(
                                    graveyardCard, filter, card.getId(), gameData, graveyardOwner,
                                    null, null, xValue)) {
                        matchingCards.add(graveyardCard);
                    }
                }
            }
        }

        if (requireSharedCreatureType) {
            matchingCards.removeIf(candidate -> matchingCards.stream()
                    .noneMatch(other -> !other.getId().equals(candidate.getId())
                            && gameQueryService.shareCreatureType(gameData, candidate, other)));
        }

        if (matchingCards.size() < minTargets) {
            throw new IllegalStateException("Not enough legal graveyard targets");
        }

        if (matchingCards.isEmpty() && minTargets == 0) {
            return;
        }
        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = xValue == null ? 0 : xValue;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.singleGraveyard = singleGraveyard;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        String prompt = minTargets > 0
                ? "Choose " + minTargets + " target " + filterLabel + "s that share a creature type from your graveyard."
                : maxTargetsCap == Integer.MAX_VALUE
                ? "Choose any number of target " + filterLabel + "s from " + zoneLabel(source) + "."
                : "Choose up to " + maxTargetsCap + " target " + filterLabel + "s"
                + (singleGraveyard ? " from a single graveyard" : " from " + zoneLabel(source)) + ".";
        if (maxTotalManaValue != null) {
            prompt = prompt.substring(0, prompt.length() - 1)
                    + " with total mana value " + maxTotalManaValue + " or less.";
        }
        if (maxTotalManaValue != null) {
            playerInputService.beginMultiGraveyardChoiceWithMaximumManaValue(
                    gameData, controllerId, matchingCards, maxTargets, minTargets, maxTotalManaValue, prompt);
        } else {
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                    minTargets, prompt);
        }
    }

    private GraveyardCardPredicateTargetFilter graveyardTargetFilterFor(Card card, CardEffect effect) {
        int targetGroupIndex = card.getEffectTargetIndex(effect);
        if (targetGroupIndex < 0 || targetGroupIndex >= card.getTargetGroups().size()) {
            return null;
        }
        if (card.getTargetGroups().get(targetGroupIndex).getFilter()
                instanceof GraveyardCardPredicateTargetFilter filter) {
            return filter;
        }
        return null;
    }

    /**
     * "Put up to N target cards from an opponent's graveyard on top of their library"
     * (Misinformation). Pools every opponent's graveyard as a legal target but flags the choice
     * {@code singleGraveyard}, so all chosen cards must come from the same opponent's graveyard.
     */
    public void handleUpToNOpponentGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                            StackEntryType entryType, CardPredicate filter,
                                                            int maxTargetsCap, List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (!gameQueryService.isLandCardTargetRestricted(gameData, graveyardCard, controllerId)
                        && predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.singleGraveyard = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose up to " + maxTargetsCap + " target card" + (maxTargetsCap != 1 ? "s" : "")
                        + " from an opponent's graveyard.");
    }

    public void handleUpToNTargetPlayerGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                                                UUID targetPlayerId, Card card,
                                                                StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                                List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, targetPlayerId, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.targetPlayerId = targetPlayerId;
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from " + targetPlayerName + "'s graveyard.");
    }

    /**
     * Begins the next cast-time graveyard target group for a spell that has more than one such
     * group. Empty groups are recorded without opening a pointless prompt.
     */
    public boolean beginNextSpellGraveyardChoice(GameData gameData) {
        var operation = gameData.graveyardTargetOperation;
        while (true) {
            if (operation.activeSpellGraveyardChoiceEffect == null) {
                if (operation.pendingSpellGraveyardChoiceEffects == null
                        || operation.pendingSpellGraveyardChoiceEffects.isEmpty()) {
                    return false;
                }
                operation.activeSpellGraveyardChoiceEffect =
                    operation.pendingSpellGraveyardChoiceEffects.remove(0);
            }

            CardEffect choiceEffect = operation.activeSpellGraveyardChoiceEffect;
            if (choiceEffect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnEffect) {
                List<Card> matchingCards = matchingCardsInGraveyard(gameData, operation.controllerId,
                        returnEffect.filter(), operation.card.getId(), operation.controllerId);
                if (returnEffect.requireSharedCreatureType()) {
                    matchingCards.removeIf(candidate -> matchingCards.stream()
                            .noneMatch(other -> !other.getId().equals(candidate.getId())
                                    && gameQueryService.shareCreatureType(gameData, candidate, other)));
                }
                if (!matchingCards.isEmpty()) {
                    handleUpToNGraveyardSpellTargeting(gameData, operation.controllerId, operation.card,
                            operation.entryType, returnEffect, returnEffect.maxTargets(), operation.effects);
                    return true;
                }
            } else if (choiceEffect instanceof ShuffleTargetCardsFromGraveyardIntoLibraryEffect shuffleEffect) {
                UUID targetPlayerId = operation.targetPlayerId;
                if (targetPlayerId == null) {
                    throw new IllegalStateException("Must target a player");
                }
                List<Card> matchingCards = matchingCardsInGraveyard(gameData, targetPlayerId,
                        shuffleEffect.filter(), operation.card.getId(), operation.controllerId);
                if (!matchingCards.isEmpty()) {
                    handleUpToNTargetPlayerGraveyardSpellTargeting(gameData, operation.controllerId,
                            targetPlayerId, operation.card, operation.entryType, shuffleEffect.filter(),
                            shuffleEffect.maxTargets(), operation.effects);
                    return true;
                }
            } else {
                throw new IllegalStateException("Unsupported spell graveyard target group: "
                        + choiceEffect.getClass().getSimpleName());
            }

            operation.spellGraveyardCardIdsByEffect.put(choiceEffect, List.of());
            operation.activeSpellGraveyardChoiceEffect = null;
        }
    }

    private List<Card> matchingCardsInGraveyard(GameData gameData, UUID playerId,
                                                 CardPredicate filter, UUID sourceCardId,
                                                 UUID sourceControllerId) {
        List<Card> graveyard = targetableGraveyard(gameData, playerId, sourceControllerId);
        if (graveyard == null) {
            return new ArrayList<>();
        }
        return graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, sourceCardId))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    public void handleExactNTargetPlayerGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                                                 UUID targetPlayerId, Card card,
                                                                 StackEntryType entryType, int targetCount,
                                                                 CardPredicate filter, List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, targetPlayerId, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = targetCount;
        gameData.graveyardTargetOperation.anyNumber = false;
        gameData.graveyardTargetOperation.targetPlayerId = targetPlayerId;
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, targetCount,
                targetCount, "Choose " + targetCount + " target " + filterLabel + ""
                        + (targetCount != 1 ? "s" : "") + " from " + targetPlayerName + "'s graveyard.");
    }

    /**
     * "Exile up to N target cards from a single graveyard" (Scarab Feast), or exactly X target cards
     * from a single graveyard (Rats' Feast). Pools every card in every graveyard that matches the
     * effect's filter, and flags the choice {@code singleGraveyard} so
     * {@code GraveyardChoiceHandlerService} rejects a selection spanning more than one graveyard.
     */
    public void handleUpToNSingleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                         StackEntryType entryType, int maxTargetsCap,
                                                         com.github.laxika.magicalvibes.model.filter.CardPredicate filter,
                                                         List<CardEffect> spellEffects, boolean exactTargets) {
        List<Card> matchingCards = new ArrayList<>();
        boolean hasEnoughCardsInOneGraveyard = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId, controllerId);
            if (graveyard == null) continue;
            List<Card> matchingGraveyardCards = graveyard.stream()
                    .filter(candidate -> filter == null
                            || predicateEvaluationService.matchesCardPredicate(candidate, filter, card.getId()))
                    .toList();
            matchingCards.addAll(matchingGraveyardCards);
            if (matchingGraveyardCards.size() >= maxTargetsCap) {
                hasEnoughCardsInOneGraveyard = true;
            }
        }

        if (exactTargets && !hasEnoughCardsInOneGraveyard) {
            throw new IllegalStateException("Not enough cards in a single graveyard");
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = exactTargets ? maxTargetsCap : 0;
        gameData.graveyardTargetOperation.anyNumber = !exactTargets;
        gameData.graveyardTargetOperation.singleGraveyard = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                exactTargets ? maxTargets : 0,
                (exactTargets ? "Choose exactly " : "Choose up to ") + maxTargetsCap + " target card"
                        + (maxTargetsCap != 1 ? "s" : "") + " from a single graveyard to exile.");
    }

    public void handleUpToNAllGraveyardsSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                        StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                        List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId, controllerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.singleGraveyard = false;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from graveyards.");
    }
}
