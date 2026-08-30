package com.github.laxika.magicalvibes.service.spell;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.effect.cost.AdditionalSpellCostService;
import com.github.laxika.magicalvibes.service.effect.WaterbendPaymentService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LandCopyOnEnterService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.DiscardXCardsCastingCost;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.ExileCardFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.ExileTopCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.ExileXCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.EachOpponentGainsLifeCastingCost;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.RemoveCountersFromControlledCreaturesCastingCost;
import com.github.laxika.magicalvibes.model.RemoveXCountersFromControlledPermanentsCastingCost;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.SacrificeXPermanentsCastingCost;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.RevealCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTimeCreatureTypeChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.CastTimeXValueEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToKickedSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromGraveyardOrExileToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGraveyardExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardsAndSeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.effect.DeliverUntoEvilEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.IndependentlyTargetedGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAndReturnTargetCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileCreatureCost;
import com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayLifeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrSacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.EscalateDiscardCost;
import com.github.laxika.magicalvibes.model.effect.EscalateSacrificeCost;
import com.github.laxika.magicalvibes.model.effect.EscalateTapCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ForageOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAnyNumberOfPermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeOrSacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.BlightCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnControlledCreatureOrPayManaCost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardCost;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesForCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.effect.WebSlingingEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastingAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSourceActivatedAbilitiesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpellCastingService {

    private final CardRevealService cardRevealService;
    private final BattlefieldEntryService battlefieldEntryService;
    @Lazy private final CloneService cloneService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameActionAvailabilityService actionAvailabilityService;
    private final GameLogService gameLogService;
    private final CastingCostService castingCostService;
    private final CastingPermissionService castingPermissionService;
    private final TurnProgressionService turnProgressionService;
    private final TargetLegalityService targetLegalityService;
    private final com.github.laxika.magicalvibes.service.target.TargetGroupAssignmentService targetGroupAssignmentService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyardService;
    private final ExileService exileService;
    private final com.github.laxika.magicalvibes.service.effect.AmountEvaluationService amountEvaluationService;
    private final com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService conditionEvaluationService;
    private final AdditionalSpellCostService additionalSpellCostService;
    private final WaterbendPaymentService waterbendPaymentService;
    private final GameMutationCoordinator mutationCoordinator;
    private final StateBasedActionService stateBasedActionService;
    private final LifeSupport lifeSupport;
    private final LandCopyOnEnterService landCopyOnEnterService;
    @Lazy private final PlayerInputService playerInputService;

    // --- Helper records ---

    private record ManaRestrictionFlags(boolean isArtifact, boolean isMyr, boolean hasRestrictedRedContext, boolean kickedOnlyGreen, boolean instantSorceryOnlyColorless, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnly, boolean legendarySpellOnly, boolean manaValueAtLeastFour, Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext, Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext, Set<CardSubtype> subtypeSpellOnlyContext) {
        boolean hasRestricted() {
            return isArtifact || isMyr || hasRestrictedRedContext || kickedOnlyGreen || instantSorceryOnlyColorless || creatureSpellOnly || legendarySpellOnly || manaValueAtLeastFour
                    || (subtypeCreatureContext != null && !subtypeCreatureContext.isEmpty())
                    || (subtypeSpellOrAbilityContext != null && !subtypeSpellOrAbilityContext.isEmpty())
                    || (subtypeSpellOnlyContext != null && !subtypeSpellOnlyContext.isEmpty())
                    || (subtypeCreatureSourceSpellOrAbilityContext != null && !subtypeCreatureSourceSpellOrAbilityContext.isEmpty())
                    || (subtypeOrPlaneswalkerSpellContext != null && !subtypeOrPlaneswalkerSpellContext.isEmpty());
        }
    }

    private record BeheldCardPayment(Card card, UUID ownerId) {}

    // --- Helper methods ---

    /**
     * Whether a permanent the casting player controls grants {@code ability} to {@code card} via a
     * {@link SpellCastingAbilityGrantingEffect} static ability. Lets casting assistance be paid on spells
     * that lack the innate ability keyword.
     */
    private boolean hasSpellCastingAbilityGrantForCard(GameData gameData, UUID playerId, Card card, Keyword ability) {
        return hasSpellCastingAbilityGrantForCard(gameData, playerId, card, ability, Zone.HAND);
    }

    private boolean hasSpellCastingAbilityGrantForCard(GameData gameData, UUID playerId, Card card,
                                                        Keyword ability, Zone sourceZone) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof SpellCastingAbilityGrantingEffect grant
                        && grant.grantedAbility() == ability
                        && grant.appliesToSourceZone(sourceZone)
                        && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Pays a spell's "as an additional cost to cast this spell, put a -1/-1 counter on a creature you
     * control" cost (e.g. Scarscale Ritual). The creature is supplied via {@code sacrificePermanentId}.
     * The counter is placed directly; the creature dies later via state-based actions if its toughness
     * reaches 0.
     */
    private void payPutCounterOnControlledCreatureCost(GameData gameData, Player player, Card card,
                                                       PutCounterOnControlledCreatureCost cost, UUID creatureId) {
        if (cost == null || (cost.optional() && creatureId == null)) return;
        Permanent creature = additionalSpellCostService.validatePutCounterOnControlledCreatureCost(gameData, player, card, cost, creatureId);
        putCountersAsCost(gameData, player, card, creature, cost.counterType(), cost.count());
    }

    private void payBlightCost(GameData gameData, Player player, Card card,
                               BlightCost cost, UUID creatureId, int announcedX) {
        if (cost == null) return;
        Permanent creature = additionalSpellCostService.validateBlightCost(gameData, player, card, creatureId);
        putCountersAsCost(gameData, player, card, creature, CounterType.MINUS_ONE_MINUS_ONE, announcedX);
    }

    private void payPutCountersOnControlledCreatureOrPayManaCost(
            GameData gameData, Player player, Card card,
            PutCountersOnControlledCreatureOrPayManaCost cost, UUID creatureId,
            ManaPool preManaPaymentPool) {
        if (cost == null) return;
        if (creatureId != null) {
            Permanent creature = additionalSpellCostService.validatePutCountersOnControlledCreatureOrPayManaCost(
                    gameData, player, card, cost, creatureId);
            putCountersAsCost(gameData, player, card, creature, cost.counterType(), cost.count());
            return;
        }
        try {
            ManaCost extra = new ManaCost(cost.manaCost());
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            int before = pool.getTotalAllMana();
            if (!extra.canPay(pool)) {
                throw new IllegalStateException("Not enough mana to pay " + cost.manaCost() + " for " + card.getName());
            }
            extra.pay(pool);
            gameData.addSpellCastManaSpent(card.getId(), before - pool.getTotalAllMana());
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " pays " + cost.manaCost() + " for ")
                    .card(card)
                    .text(".")
                    .build());
        } catch (IllegalStateException e) {
            if (preManaPaymentPool != null) {
                gameData.playerManaPools.put(player.getId(), preManaPaymentPool);
            }
            throw e;
        }
    }

    private void putCountersAsCost(GameData gameData, Player player, Card card, Permanent creature,
                                   CounterType type, int count) {
        count = gameQueryService.replaceCounters(gameData, creature, type, count);
        if (count <= 0) {
            return;
        }
        creature.setCounterCount(type, creature.getCounterCount(type) + count);
        triggerCollectionService.checkYouPutCountersTriggers(gameData, player.getId(), count);
        gameData.playersWhoPutCountersOnCreaturesThisTurn.add(player.getId());
        if (type == CounterType.PLUS_ONE_PLUS_ONE) {
            gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(player.getId());
        }
        String counterName = type == CounterType.MINUS_ONE_MINUS_ONE ? "-1/-1"
                : type == CounterType.PLUS_ONE_PLUS_ONE ? "+1/+1"
                : type.name().toLowerCase();
        String counterText = count == 1 ? "a " + counterName + " counter" : count + " " + counterName + " counters";
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " puts " + counterText + " on ")
                .card(creature.getCard())
                .text(" for ")
                .card(card)
                .text(".")
                .build());
    }

    private void payRemoveCountersFromControlledCreaturesCost(
            GameData gameData, Player player, Card card,
            RemoveCountersFromControlledCreaturesCastingCost cost, List<UUID> permanentIds) {
        if (cost == null) {
            return;
        }
        additionalSpellCostService.validateRemoveCountersFromControlledCreaturesCost(
                gameData, player, card, cost, permanentIds);
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            removeOneCounter(gameData, permanent, cost.counterType());
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " removes a ")
                    .text(counterLabel(cost.counterType()))
                    .text(" counter from ")
                    .card(permanent.getCard())
                    .text(" to cast ")
                    .card(card)
                    .text(".")
                    .build());
        }
    }

    private void payRemoveXCountersFromControlledPermanentsCost(
            GameData gameData, Player player, Card card,
            RemoveXCountersFromControlledPermanentsCastingCost cost, int xValue, List<UUID> permanentIds) {
        if (cost == null) {
            return;
        }
        additionalSpellCostService.validateRemoveXCountersFromControlledPermanentsCost(
                gameData, player, card, cost, xValue, permanentIds);
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            removeOneCounter(gameData, permanent, cost.counterType());
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " removes a ")
                    .text(counterLabel(cost.counterType()))
                    .text(" counter from ")
                    .card(permanent.getCard())
                    .text(" to cast ")
                    .card(card)
                    .text(" by flashback.")
                    .build());
        }
    }

    private void removeOneCounter(GameData gameData, Permanent permanent, CounterType counterType) {
        if (counterType == CounterType.ANY) {
            CounterType actualType = permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0
                    ? CounterType.MINUS_ONE_MINUS_ONE : CounterType.PLUS_ONE_PLUS_ONE;
            permanent.setCounterCount(actualType, permanent.getCounterCount(actualType) - 1);
            if (actualType == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, 1);
            }
            return;
        }
        permanent.setCounterCount(counterType, permanent.getCounterCount(counterType) - 1);
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(permanent, 1);
        }
    }

    private String counterLabel(CounterType counterType) {
        return switch (counterType) {
            case PLUS_ONE_PLUS_ONE -> "+1/+1";
            case MINUS_ONE_MINUS_ONE -> "-1/-1";
            case CHARGE -> "charge";
            default -> counterType.name().toLowerCase();
        };
    }

    /**
     * Pays a spell's "as an additional cost to cast this spell, return a creature you control to its
     * owner's hand" cost (e.g. Familiar's Ruse). The creature is supplied via {@code sacrificePermanentId}.
     */
    private void payReturnCreatureToHandCost(GameData gameData, Player player, Card card, UUID returnPermanentId) {
        Permanent toReturn = additionalSpellCostService.validateReturnCreatureToHandCost(gameData, player, card, returnPermanentId);
        permanentRemovalService.removePermanentToHand(gameData, toReturn);
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " returns ")
                .card(toReturn.getCard())
                .text(" to hand for ")
                .card(card)
                .text(".")
                .build());
    }


    /**
     * Pays a spell's "as an additional cost to cast this spell, discard a card" cost
     * (e.g. Seize the Spoils). {@code discardHandCardIndex} is the index into the caster's hand
     * as it was <em>before</em> the spell left the hand (i.e. the index the caller/UI sees), so it
     * is adjusted for the already-removed spell at {@code spellCardIndex}. The chosen card must
     * match the cost's predicate and cannot be the spell being cast. Fires discard triggers.
     */
    private void payDiscardCost(GameData gameData, Player player, Card card, DiscardCardTypeCost cost,
                                Integer discardHandCardIndex, int spellCardIndex) {
        if (cost == null) return;
        int effectiveIndex = additionalSpellCostService.validateDiscardCost(gameData, player, card, cost, discardHandCardIndex, spellCardIndex);
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card toDiscard = hand.get(effectiveIndex);
        hand.remove(effectiveIndex);
        graveyardService.addCardToGraveyard(gameData, playerId, toDiscard);
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " discards ")
                .card(toDiscard)
                .text(" to cast ")
                .card(card)
                .text(".")
                .build());
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, toDiscard);
    }

    /** Pays an additional cost that reveals a matching card from the caster's hand. */
    private int payRevealCardFromHandCost(GameData gameData, Player player, Card card,
                                          RevealCardFromHandCost cost, Integer handCardIndex,
                                          int spellCardIndex, int resolvedXValue) {
        if (cost == null) return resolvedXValue;
        int effectiveIndex = additionalSpellCostService.validateRevealCardCost(
                gameData, player, card, cost, handCardIndex, spellCardIndex);
        Card toReveal = gameData.playerHands.get(player.getId()).get(effectiveIndex);
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " reveals ")
                .card(toReveal)
                .text(" from their hand to cast ")
                .card(card)
                .text(".")
                .build());
        return cost.trackManaValue() ? toReveal.getManaValue() : resolvedXValue;
    }

    /** Pays a fixed-count discard additional cast cost, removing the selected cards from highest index first. */
    private void payDiscardCardsCost(GameData gameData, Player player, Card card, DiscardCardTypeCost cost,
                                     List<Integer> discardHandCardIndices, int spellCardIndex) {
        List<Integer> effectiveIndices = new ArrayList<>(
                additionalSpellCostService.validateDiscardCardsCost(
                        gameData, player, card, cost, discardHandCardIndices, spellCardIndex));
        effectiveIndices.sort(java.util.Collections.reverseOrder());
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        for (int effectiveIndex : effectiveIndices) {
            Card toDiscard = hand.get(effectiveIndex);
            hand.remove(effectiveIndex);
            graveyardService.addCardToGraveyard(gameData, playerId, toDiscard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " discards ")
                    .card(toDiscard)
                    .text(" to cast ")
                    .card(card)
                    .text(".")
                    .build());
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, toDiscard);
        }
    }

    /** Pays a spell's random-discard additional cast cost (e.g. Sonic Burst). */
    private void payRandomDiscardCost(GameData gameData, Player player, Card card, DiscardRandomCardCost cost) {
        if (cost == null) return;
        additionalSpellCostService.validateRandomDiscardCost(gameData, player, card);
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> eligibleCards = hand.stream()
                .filter(candidate -> !candidate.getId().equals(card.getId()))
                .toList();
        Card discarded = eligibleCards.get(ThreadLocalRandom.current().nextInt(eligibleCards.size()));
        hand.remove(discarded);
        graveyardService.addCardToGraveyard(gameData, playerId, discarded);
        gameData.discardCausedByOpponent = false;
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " discards ")
                .card(discarded)
                .text(" at random to cast ")
                .card(card)
                .text(".")
                .build());
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, discarded);
    }

    /**
     * Pays the "discard X cards" additional cast cost (Abandon Hope) for the announced X. Discards
     * highest post-removal hand indices first so earlier removals do not shift later ones; legality
     * is re-checked by {@code AdditionalSpellCostService.validateDiscardXCardsCost}, which the cast
     * path already ran before any cost was consumed.
     */
    private int payDiscardXCardsCost(GameData gameData, Player player, Card card, DiscardXCardsCost cost,
                                     int announcedX, List<Integer> discardHandCardIndices, int spellCardIndex) {
        if (cost.randomSelection()) {
            additionalSpellCostService.validateDiscardXCardsCost(
                    gameData, player, card, cost, announcedX, List.of(), spellCardIndex);
            UUID playerId = player.getId();
            List<Card> hand = gameData.playerHands.get(playerId);
            int discardedManaValue = 0;
            for (int i = 0; i < announcedX; i++) {
                List<Card> eligibleCards = hand.stream()
                        .filter(candidate -> !candidate.getId().equals(card.getId()))
                        .filter(candidate -> cost.predicate() == null
                                || predicateEvaluationService.matchesCardPredicate(
                                candidate, cost.predicate(), candidate.getId()))
                        .toList();
                Card toDiscard = eligibleCards.get(ThreadLocalRandom.current().nextInt(eligibleCards.size()));
                discardedManaValue += toDiscard.getManaValue();
                hand.remove(toDiscard);
                graveyardService.addCardToGraveyard(gameData, playerId, toDiscard);
                gameData.discardCausedByOpponent = false;
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " discards ")
                        .card(toDiscard)
                        .text(" at random to cast ")
                        .card(card)
                        .text(".")
                        .build());
                triggerCollectionService.checkDiscardTriggers(gameData, playerId, toDiscard);
            }
            return cost.trackManaValue() ? discardedManaValue : 0;
        }
        List<Integer> effectiveIndices = new ArrayList<>(
                additionalSpellCostService.validateDiscardXCardsCost(
                        gameData, player, card, cost, announcedX, discardHandCardIndices, spellCardIndex));
        effectiveIndices.sort(java.util.Collections.reverseOrder());
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        int discardedManaValue = 0;
        for (int effectiveIndex : effectiveIndices) {
            Card toDiscard = hand.get(effectiveIndex);
            discardedManaValue += toDiscard.getManaValue();
            hand.remove(effectiveIndex);
            graveyardService.addCardToGraveyard(gameData, playerId, toDiscard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " discards ")
                    .card(toDiscard)
                    .text(" to cast ")
                    .card(card)
                    .text(".")
                    .build());
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, toDiscard);
        }
        return cost.trackManaValue() ? discardedManaValue : 0;
    }

    /**
     * Pays escalate's discard-per-extra-mode cost. Discards highest post-removal hand indices
     * first so earlier removals do not shift later ones.
     */
    private void payEscalateDiscardCost(GameData gameData, Player player, Card card, EscalateDiscardCost cost,
                                        int modesChosen, List<Integer> discardHandCardIndices, int spellCardIndex) {
        if (cost == null) return;
        List<Integer> effectiveIndices = new ArrayList<>(
                additionalSpellCostService.validateEscalateDiscardCost(
                        gameData, player, card, modesChosen, discardHandCardIndices, spellCardIndex));
        effectiveIndices.sort(java.util.Collections.reverseOrder());
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        for (int effectiveIndex : effectiveIndices) {
            Card toDiscard = hand.get(effectiveIndex);
            hand.remove(effectiveIndex);
            graveyardService.addCardToGraveyard(gameData, playerId, toDiscard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " discards ")
                    .card(toDiscard)
                    .text(" to escalate ")
                    .card(card)
                    .text(".")
                    .build());
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, toDiscard);
        }
    }


    /**
     * True when casting this card writes mode-dependent state onto it: modal spells
     * (ChooseOneEffect) rebuild the card's spell targets and cast-time target filter at cast
     * time ({@link #unwrapChooseOneEffect}, {@link #applyModalEtbTargetFilter}). Live cards are
     * frozen and shared with AI simulation copies, so every modal cast must first swap in an
     * unfrozen {@link Card#createRuntimeCopy()} that replaces the original in the casting zone.
     */
    private void validateSuppliedGraveyardTargetBeforePlayabilityFailure(
            GameData gameData, Card card, UUID controllerId, UUID targetId, int xValue) {
        if (targetId == null) {
            return;
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetId);
        if (graveyardOwnerId == null) {
            return;
        }

        List<CardEffect> graveyardEffects = EffectResolution
                .expandConditionalTargetingEffects(card.getEffects(EffectSlot.SPELL)).stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                .toList();
        if (graveyardEffects.isEmpty()) {
            return;
        }

        Set<GraveyardSearchScope> scopes = graveyardEffects.stream()
                .flatMap(effect -> effect.targetSpec().graveyardScope().stream())
                .collect(java.util.stream.Collectors.toSet());
        if (scopes.contains(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                && !scopes.contains(GraveyardSearchScope.ALL_GRAVEYARDS)
                && !graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in your graveyard");
        }
        if (scopes.contains(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                && !scopes.contains(GraveyardSearchScope.ALL_GRAVEYARDS)
                && graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in an opponent's graveyard");
        }
        targetLegalityService.validateEffectTargetInZone(
                gameData, card, graveyardEffects, targetId, Zone.GRAVEYARD, xValue, controllerId);
    }

    private static boolean isModalSpell(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream().anyMatch(ChooseOneEffect.class::isInstance)
                || card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream().anyMatch(ChooseOneEffect.class::isInstance);
    }

    private static Card selectedModalDoubleFacedLandFace(Card card, int modeIndex) {
        if (!card.isModalDoubleFaced()) {
            return card;
        }
        if (modeIndex < 0 || modeIndex > 1 || card.getBackFaceCard() == null) {
            throw new IllegalStateException("Invalid modal double-faced land face");
        }
        return modeIndex == 0 ? card : card.getBackFaceCard();
    }

    /**
     * Unwraps a {@link ConditionalEffect} so graveyard-targeting detection can see the inner effect
     * (e.g. a "if {B} was spent" reanimate on Torrent of Souls). Non-conditional effects pass through.
     */
    private static CardEffect unwrapConditional(CardEffect effect) {
        return effect instanceof ConditionalEffect conditional ? conditional.wrapped() : effect;
    }

    /**
     * Returns the card at {@code cardIndex}, swapping in an unfrozen runtime copy (replacing the
     * original in hand) first when the spell is modal — see {@link #isModalSpell}.
     */
    private static Card modalRuntimeCopyForHandCast(List<Card> hand, int cardIndex) {
        Card card = hand.get(cardIndex);
        if (isModalSpell(card)) {
            card = card.createRuntimeCopy();
            hand.set(cardIndex, card);
        }
        return card;
    }

    private int resolveCastTimeXValue(GameData gameData, Card card, UUID controllerId, int announcedXValue) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(CastTimeXValueEffect.class::isInstance)
                .map(CastTimeXValueEffect.class::cast)
                .map(CastTimeXValueEffect::castTimeXValue)
                .filter(amount -> amount != null)
                .findFirst()
                .map(amount -> amountEvaluationService.evaluate(gameData, amount,
                        com.github.laxika.magicalvibes.service.effect.AmountContext.forCasting(
                                controllerId, announcedXValue, card)))
                .orElse(announcedXValue);
    }

    private void validateXValueCap(GameData gameData, Card card, UUID controllerId, int xValue) {
        if (card.getXValueCap() == null) {
            return;
        }
        int cap = amountEvaluationService.evaluate(gameData, card.getXValueCap(),
                com.github.laxika.magicalvibes.service.effect.AmountContext.forCasting(
                        controllerId, xValue, card));
        if (xValue > cap) {
            throw new IllegalStateException("X can't be greater than " + cap);
        }
    }

    private static Card bestowRuntimeCopyForHandCast(List<Card> hand, int cardIndex) {
        Card card = hand.get(cardIndex).createRuntimeCopy();
        card.setType(CardType.ENCHANTMENT);
        card.setAdditionalTypes(Set.of());
        card.setSubtypes(List.of(com.github.laxika.magicalvibes.model.CardSubtype.AURA));
        card.setPower(null);
        card.setToughness(null);
        card.target(com.github.laxika.magicalvibes.model.filter.TargetFilters.creature());
        hand.set(cardIndex, card);
        return card;
    }

    private static Card prototypeRuntimeCopyForHandCast(List<Card> hand, int cardIndex) {
        Card card = hand.get(cardIndex).createRuntimeCopy();
        AlternateHandCast prototype = card.getCastingOption(AlternateHandCast.class)
                .filter(AlternateHandCast::isPrototype)
                .orElseThrow(() -> new IllegalStateException("Card does not have a prototype casting cost"));
        String manaCost = prototype.getCost(ManaCastingCost.class)
                .orElseThrow(() -> new IllegalStateException("Prototype has no mana cost"))
                .manaCost();
        card.setManaCost(manaCost);
        card.setColor(prototype.prototypeColor());
        card.setColors(List.of(prototype.prototypeColor()));
        card.setPower(prototype.prototypePower());
        card.setToughness(prototype.prototypeToughness());
        hand.set(cardIndex, card);
        return card;
    }

    private int unwrapChooseOneEffect(GameData gameData, UUID controllerId, Card card,
                                      List<CardEffect> effects, int effectiveXValue) {
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) instanceof ChooseOneEffect coe) {
                boolean additionalModesAllowed = coe.additionalModesCondition() == null
                        || conditionEvaluationService.isMet(gameData, coe.additionalModesCondition(),
                        com.github.laxika.magicalvibes.service.effect.ConditionContext.forCasting(controllerId));
                List<Integer> chosenModeIndices = coe.decodeModeIndices(
                        effectiveXValue, coe.effectiveChoicesMax(additionalModesAllowed));
                List<ChooseOneEffect.ChooseOneOption> chosenModes = chosenModeIndices.stream()
                        .map(idx -> coe.options().get(idx))
                        .toList();

                effects.remove(i);
                int insertAt = i;
                for (ChooseOneEffect.ChooseOneOption chosen : chosenModes) {
                    effects.addAll(insertAt, chosen.effects());
                    insertAt += chosen.effects().size();
                }

                // Split cards with fuse are modes with their own total cost (CR 709.3, CR 702.102c):
                // the chosen half's cost replaces the card's printed cost on the runtime copy, so
                // every downstream cost modifier / payment step sees the cost actually being paid.
                applyModeManaCost(card, chosenModes);

                card.clearRuntimeSpellTargets();
                card.setCastTimeTargetFilter(null);

                // Classic choose-one with a single target uses castTimeTargetFilter + targetId.
                // Choose-multiple, "choose one or more", and modes with custom target counts declare
                // target() slots so targets ride in targetIds uniformly.
                boolean useTargetSlots = coe.variableModeCount() || coe.choicesRequired() > 1
                        || chosenModes.stream().anyMatch(chosen ->
                        chosen.minTargets() != 1 || chosen.maxTargets() != 1);
                if (!useTargetSlots && chosenModes.size() == 1) {
                    ChooseOneEffect.ChooseOneOption chosen = chosenModes.getFirst();
                    if (chosen.targetFilters() != null) {
                        for (int t = 0; t < chosen.targetFilters().size(); t++) {
                            SpellTarget spellTarget = card.target(chosen.targetFilters().get(t));
                            if (t < chosen.effects().size()) {
                                card.registerEffectTargetIndex(chosen.effects().get(t), spellTarget.getIndex());
                            }
                        }
                    } else if (chosen.targetFilter() != null) {
                        card.setCastTimeTargetFilter(chosen.targetFilter());
                    }
                } else {
                    for (ChooseOneEffect.ChooseOneOption chosen : chosenModes) {
                        if (chosen.targetFilters() != null) {
                            for (int t = 0; t < chosen.targetFilters().size(); t++) {
                                SpellTarget spellTarget = card.target(chosen.targetFilters().get(t));
                                if (t < chosen.effects().size()) {
                                    card.registerEffectTargetIndex(chosen.effects().get(t), spellTarget.getIndex());
                                }
                            }
                        } else if (chosen.targetFilter() != null
                                || needsFlatModalTargetGroup(chosen.effects())) {
                            SpellTarget spellTarget = declareModeTarget(card, chosen);
                            for (CardEffect modeEffect : chosen.effects()) {
                                card.registerEffectTargetIndex(modeEffect, spellTarget.getIndex());
                            }
                        }
                    }
                }
                return card.isModalDoubleFaced() ? chosenModeIndices.getFirst() : 0;
            }
        }
        return effectiveXValue;
    }

    public int prepareModalSpellCast(GameData gameData, UUID controllerId, Card card,
                                     List<CardEffect> effects, int modeEncoding) {
        return unwrapChooseOneEffect(gameData, controllerId, card, effects, modeEncoding);
    }

    public int prepareModalSpellCast(Card card, List<CardEffect> effects, int modeEncoding) {
        return unwrapChooseOneEffect(null, null, card, effects, modeEncoding);
    }

    private static void validateOptionalCostModalSelection(
            ChooseOneEffect modal,
            AdditionalSpellCostService.ExtractedCosts additionalCosts,
            AdditionalSpellCostService.CostSelection costSelection,
            int modeEncoding) {
        if (!modal.allModesWhenOptionalCostPaid()
                || additionalCosts.putCounterCost() == null
                || !additionalCosts.putCounterCost().optional()) {
            return;
        }
        int chosenModeCount = modal.decodeModeIndices(modeEncoding).size();
        boolean optionalCostPaid = costSelection.sacrificePermanentId() != null;
        int requiredModeCount = optionalCostPaid ? modal.choicesMax() : modal.choicesRequired();
        if (chosenModeCount != requiredModeCount) {
            throw new IllegalStateException(optionalCostPaid
                    ? "Paying the optional cost requires choosing all modes"
                    : "Without paying the optional cost, choose exactly one mode");
        }
    }

    private void validateConditionalModalSelection(GameData gameData, UUID controllerId,
                                                    ChooseOneEffect modal, int modeEncoding) {
        if (modal.choicesMaxCondition() == null) {
            return;
        }
        int chosenModeCount = modal.decodeModeIndices(modeEncoding).size();
        boolean conditionMet = conditionEvaluationService.isMet(
                gameData, modal.choicesMaxCondition(),
                com.github.laxika.magicalvibes.service.effect.ConditionContext.forCasting(controllerId));
        int maximumAllowedModes = conditionMet ? modal.choicesMax() : modal.choicesRequired();
        if (chosenModeCount > maximumAllowedModes) {
            throw new IllegalStateException("The additional modal modes are not available");
        }
    }

    private void validateSharedCreatureTypeGraveyardTargets(
            GameData gameData, UUID playerId, ReturnTargetCardsFromGraveyardToHandEffect effect) {
        if (effect.minTargets() <= 0 && !effect.requireSharedCreatureType()) {
            return;
        }
        List<Card> matchingCards = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, effect.filter(), null))
                .toList();
        if (matchingCards.size() < effect.minTargets()) {
            throw new IllegalStateException("The chosen mode requires " + effect.minTargets()
                    + " matching cards in your graveyard");
        }
        if (!effect.requireSharedCreatureType()) {
            return;
        }
        boolean hasSharedPair = matchingCards.stream().anyMatch(first -> matchingCards.stream()
                .anyMatch(second -> !first.getId().equals(second.getId())
                        && gameQueryService.shareCreatureType(first, second)));
        if (!hasSharedPair) {
            throw new IllegalStateException("The chosen mode requires two creature cards that share a creature type");
        }
    }

    /**
     * Rejects a player handed to a modal mode that only targets permanents.
     * <p>
     * {@code TargetLegalityService} skips its target-type check for modal cards because the card's
     * SPELL slot still holds the raw {@code ChooseOneEffect}, which exposes no target types. Here the
     * chosen mode's effects are already unwrapped, so their {@code targetSpec()}s and any explicit
     * mode target filter answer the question — a mode that admits no player (Far's bounce) must not
     * accept one.
     */
    private static void validateModalTargetKind(GameData gameData, boolean wasModal, Card card,
                                                List<CardEffect> resolvedSpellEffects, UUID targetId) {
        if (!wasModal || targetId == null || !gameData.playerIds.contains(targetId)) {
            return;
        }
        Set<TargetType> allowed = EffectResolution.computeAllowedTargets(
                resolvedSpellEffects, List.of(), false, false);
        if (!allowed.contains(TargetType.PLAYER)
                && !targetFilterAllowsPlayer(card.getCastTimeTargetFilter())) {
            throw new IllegalStateException("This spell cannot target players");
        }
    }

    private static boolean targetFilterAllowsPlayer(TargetFilter targetFilter) {
        return targetFilter instanceof AnyTargetPredicateTargetFilter
                || targetFilter instanceof PlayerPredicateTargetFilter;
    }

    /**
     * Applies a chosen mode's own total mana cost to the card being cast, when it declares one.
     * Only single-mode selections carry a cost (split halves and the fuse mode are mutually
     * exclusive choices of one {@code ChooseOneEffect}); ordinary modals leave the cost untouched.
     */
    private static void applyModeManaCost(Card card, List<ChooseOneEffect.ChooseOneOption> chosenModes) {
        if (chosenModes.size() != 1) {
            return;
        }
        String modeCost = chosenModes.getFirst().manaCost();
        if (modeCost != null) {
            card.setManaCost(modeCost);
        }
    }

    private static String selectedModalManaCost(List<CardEffect> effects, int modeEncoding) {
        for (CardEffect effect : effects) {
            if (effect instanceof ChooseOneEffect modal) {
                List<Integer> selectedModes = modal.decodeModeIndices(modeEncoding);
                if (selectedModes.size() != 1) {
                    return null;
                }
                return modal.options().get(selectedModes.getFirst()).manaCost();
            }
        }
        return null;
    }

    private static String spreeManaSuffix(SpreeAdditionalManaCost spreeCost,
                                          List<CardEffect> effects, int modeEncoding) {
        if (spreeCost == null) {
            return "";
        }
        ChooseOneEffect modal = effects.stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Spree cost has no modal choices"));
        return modal.decodeModeIndices(modeEncoding).stream()
                .map(index -> {
                    if (index < 0 || index >= spreeCost.modeManaCosts().size()) {
                        throw new IllegalStateException("Spree mode has no matching additional cost");
                    }
                    return spreeCost.modeManaCosts().get(index);
                })
                .reduce("", String::concat);
    }

    /**
     * Declares the cast-time target group for a chosen modal mode from its {@code targetFilter}
     * and optional min/max/X-scaled bounds (Profane Command's "up to X target creatures").
     */
    private SpellTarget declareModeTarget(Card card, ChooseOneEffect.ChooseOneOption chosen) {
        if (chosen.xScaledTargets()) {
            if (chosen.minTargets() == chosen.maxTargets() && chosen.minTargets() > 0) {
                return card.targetExactlyX(chosen.targetFilter(), chosen.maxTargets());
            }
            return card.targetX(chosen.targetFilter(), chosen.maxTargets());
        }
        if (chosen.minTargets() != 1 || chosen.maxTargets() != 1) {
            return card.target(chosen.targetFilter(), chosen.minTargets(), chosen.maxTargets());
        }
        return card.target(chosen.targetFilter());
    }

    private static boolean needsFlatModalTargetGroup(List<CardEffect> effects) {
        return effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || EffectResolution.targetsSpellOnStack(effect));
    }

    private void applyModalEtbTargetFilter(Card card, int effectiveXValue) {
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof ChooseOneEffect coe) {
                if (effectiveXValue >= 0 && effectiveXValue < coe.options().size()) {
                    TargetFilter filter = coe.options().get(effectiveXValue).targetFilter();
                    if (filter != null) {
                        card.setCastTimeTargetFilter(filter);
                    }
                }
                return;
            }
        }
    }

    private boolean isOptionalModalEtbSkip(Card card, int effectiveXValue) {
        if (effectiveXValue >= 0) {
            return false;
        }
        return card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseOneEffect coe && coe.optional());
    }

    private ManaRestrictionFlags computeManaRestrictionFlags(GameData gameData, UUID playerId, Card card) {
        return computeManaRestrictionFlags(gameData, playerId, card, false);
    }

    private ManaRestrictionFlags computeManaRestrictionFlags(GameData gameData, UUID playerId, Card card, boolean kicked) {
        boolean isArtifact = gameQueryService.cardHasType(card, CardType.ARTIFACT, gameData, playerId);
        boolean isMyr = gameQueryService.cardHasSubtype(card, CardSubtype.MYR, gameData, playerId);
        boolean hasRestrictedRedContext = isArtifact || card.hasType(CardType.CREATURE);
        boolean instantSorceryOnlyColorless = card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY);
        Set<CardSubtype> subtypeCreatureContext = card.hasType(CardType.CREATURE)
                ? nullToEmpty(gameQueryService.getCardSubtypes(card, gameData, playerId))
                : Set.of();
        // Spell-or-ability restricted mana (e.g. Smokebraider) can pay for any spell of the matching
        // subtype, so compute subtypes for every spell (Elemental spells are creatures in practice).
        Set<CardSubtype> subtypeSpellOrAbilityContext = new HashSet<>(
                nullToEmpty(gameQueryService.getCardSubtypes(card, gameData, playerId)));
        Set<CardSubtype> subtypeSpellOnlyContext = new HashSet<>(subtypeSpellOrAbilityContext);
        if (!gameQueryService.getEffectiveCardColors(gameData, card).isEmpty()) {
            subtypeSpellOrAbilityContext.remove(CardSubtype.ELDRAZI);
        }
        boolean creatureSpellOnly = card.hasType(CardType.CREATURE);
        boolean legendarySpellOnly = card.getSupertypes().contains(CardSupertype.LEGENDARY);
        boolean manaValueAtLeastFour = card.getManaValue() >= 4;
        Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext =
                new HashSet<>();
        if (card.hasType(CardType.PLANESWALKER)) {
            subtypeOrPlaneswalkerSpellContext.add(new ManaRestriction.SubtypeOrPlaneswalkerSpells());
        }
        if (subtypeSpellOrAbilityContext.contains(CardSubtype.ELEMENTAL)
                || (card.hasType(CardType.PLANESWALKER)
                && subtypeSpellOrAbilityContext.contains(CardSubtype.CHANDRA))) {
            subtypeOrPlaneswalkerSpellContext.add(new ManaRestriction.SubtypeOrPlaneswalkerSpells(
                    CardSubtype.ELEMENTAL, CardSubtype.CHANDRA));
        }
        return new ManaRestrictionFlags(isArtifact, isMyr, hasRestrictedRedContext, kicked, instantSorceryOnlyColorless,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnly, legendarySpellOnly,
                manaValueAtLeastFour, subtypeOrPlaneswalkerSpellContext, subtypeCreatureContext,
                subtypeSpellOnlyContext);
    }

    private static Set<CardSubtype> nullToEmpty(Set<CardSubtype> subtypes) {
        return subtypes != null ? subtypes : Set.of();
    }

    private StackEntryType cardTypeToStackEntryType(CardType type) {
        return switch (type) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            default -> throw new IllegalStateException("Unsupported card type: " + type);
        };
    }

    // --- Main methods ---

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, null, null, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, null, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, false, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                null, null, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex,
                  List<Integer> discardHandCardIndices) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                discardHandCardIndices, null, null);
    }

    /**
     * Cast entry point that threads a modal spell's real {@code {X}} value separately from the mode
     * selection (which rides in {@code xValue}). Only modal {X} spells (e.g. Alabaster Potion) supply
     * {@code modalXValue}; everything else passes {@code null} and behaves exactly as before.
     */
    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex, Integer modalXValue) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                null, modalXValue, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex,
                  List<Integer> discardHandCardIndices, Integer modalXValue,
                  List<UUID> imposedSacrificePermanentIds) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                discardHandCardIndices, modalXValue, imposedSacrificePermanentIds, null);
    }

    /**
     * Full cast entry point. {@code additionalCostSacrificePermanentIds} pays a multi-permanent
     * additional cast cost (Phyrexian Tribute's "sacrifice two creatures"); single-permanent
     * sacrifice costs keep using {@code sacrificePermanentId}.
     */
    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex,
                  List<Integer> discardHandCardIndices, Integer modalXValue,
                  List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                discardHandCardIndices, modalXValue, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, List.of(), false);
    }

    /**
     * Full cast entry point including {@code repeatedAdditionalCosts} — the caster's chosen
     * payments for a {@link com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost}
     * ("you may pay {1}{R} and/or {1}{G} any number of times"), one entry per repetition — and
     * {@code buyback} — whether the caster pays the spell's optional buyback cost
     * ({@link com.github.laxika.magicalvibes.model.effect.BuybackEffect}, CR 702.27).
     */
    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex,
                  List<Integer> discardHandCardIndices, Integer modalXValue,
                  List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds,
                  List<String> repeatedAdditionalCosts, boolean buyback) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                discardHandCardIndices, modalXValue, imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                repeatedAdditionalCosts, buyback, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex,
                  List<Integer> discardHandCardIndices, Integer modalXValue,
                  List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds,
                  List<String> repeatedAdditionalCosts, boolean buyback, Integer sharedColorDiscardHandCardIndex) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = !fromGraveyard && hand != null && cardIndex >= 0 && cardIndex < hand.size()
                ? hand.get(cardIndex) : null;
        if (beginOpponentChosenTargetChoiceIfNeeded(
                gameData, player, cardIndex, xValue, targetId, attempted, buyback)) {
            return;
        }
        try {
            playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                    convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                    alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, kicked,
                    discardHandCardIndex, discardHandCardIndices, false, List.of(), modalXValue,
                    List.of(), imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                    repeatedAdditionalCosts, buyback, false, List.of(), sharedColorDiscardHandCardIndex);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    public void playCardWithGift(GameData gameData, Player player, int cardIndex, Integer xValue,
                                 UUID targetId, Map<UUID, Integer> damageAssignments,
                                 List<UUID> targetIds, boolean giftPromised) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = hand != null && cardIndex >= 0 && cardIndex < hand.size() ? hand.get(cardIndex) : null;
        try {
            playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(), List.of(), false, null, null, List.of(),
                    null, null, false, null, List.of(), false, List.of(), null, List.of(), null, null,
                    List.of(), false, false, List.of(), null, null, null, List.of(), List.of(), null,
                    null, false, null, false, giftPromised);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    public void playCardAfterOpponentChosenTarget(GameData gameData, Player player, int cardIndex,
                                                  Integer xValue, UUID targetId, UUID chosenOpponentId,
                                                  boolean buyback) {
        Card card = gameData.playerHands.getOrDefault(player.getId(), List.of()).get(cardIndex);
        playCard(gameData, player, cardIndex, xValue, targetId, Map.of(), List.of(), List.of(),
                false, null, null, null, null, null, false, null, null, null, null, null,
                List.of(), buyback);
        gameData.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(card.getId()))
                .findFirst()
                .ifPresent(entry -> entry.setOpponentChosenTargetPlayerId(chosenOpponentId));
    }

    private boolean isOpponentChosenTargetSpell(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(GainControlOfTargetEffect.class::isInstance)
                .map(GainControlOfTargetEffect.class::cast)
                .anyMatch(GainControlOfTargetEffect::opponentChoosesTarget);
    }

    private boolean beginOpponentChosenTargetChoiceIfNeeded(
            GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
            Card attempted, boolean buyback) {
        if (attempted == null || targetId == null || !gameData.playerIds.contains(targetId)
                || !isOpponentChosenTargetSpell(attempted)) {
            return false;
        }
        beginOpponentChosenTargetChoice(gameData, player, cardIndex, xValue, attempted, buyback);
        return true;
    }

    private void beginOpponentChosenTargetChoice(GameData gameData, Player player, int cardIndex,
                                                 Integer xValue, Card card, boolean buyback) {
        int announcedXValue = xValue != null ? xValue : 0;
        List<UUID> validOpponentIds = targetLegalityService.computeValidOpponentChosenTargetPlayers(
                gameData, card, player.getId(), announcedXValue, false);
        if (validOpponentIds.isEmpty()) {
            throw new IllegalStateException("No legal opponent can choose a target");
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.OpponentChosenSpellTarget(
                player, card, cardIndex, xValue, buyback, null));
        playerInputService.beginPermanentChoice(gameData, player.getId(), validOpponentIds,
                "Choose an opponent to choose a creature for " + card.getName() + ".");
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                         Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                         List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                         Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds,
                         Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                         boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                         Integer modalXValue, List<UUID> imposedSacrificePermanentIds,
                         List<UUID> additionalCostSacrificePermanentIds, List<String> repeatedAdditionalCosts,
                         boolean buyback, UUID beholdPermanentId, Integer beholdHandCardIndex,
                         List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                         CardSubtype beholdChosenSubtype) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, modalXValue,
                imposedSacrificePermanentIds, additionalCostSacrificePermanentIds, repeatedAdditionalCosts,
                buyback, beholdPermanentId, beholdHandCardIndex, beholdPermanentIds, beholdHandCardIndices,
                beholdChosenSubtype, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                         Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                         List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                         Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds,
                         Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                         boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                         Integer modalXValue, List<UUID> imposedSacrificePermanentIds,
                         List<UUID> additionalCostSacrificePermanentIds, List<String> repeatedAdditionalCosts,
                         boolean buyback, UUID beholdPermanentId, Integer beholdHandCardIndex,
                         List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                         CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, modalXValue,
                imposedSacrificePermanentIds, additionalCostSacrificePermanentIds, repeatedAdditionalCosts,
                buyback, beholdPermanentId, beholdHandCardIndex, beholdPermanentIds, beholdHandCardIndices,
                beholdChosenSubtype, chosenCreatureType,
                additionalCostSacrificePermanentIds != null && !additionalCostSacrificePermanentIds.isEmpty());
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                         Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                         List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                         Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds,
                         Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                         boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                         Integer modalXValue, List<UUID> imposedSacrificePermanentIds,
                         List<UUID> additionalCostSacrificePermanentIds, List<String> repeatedAdditionalCosts,
                         boolean buyback, UUID beholdPermanentId, Integer beholdHandCardIndex,
                  List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                  CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType,
                  boolean waterbendPaid) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                discardHandCardIndices, modalXValue, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback, beholdPermanentId,
                beholdHandCardIndex, beholdPermanentIds, beholdHandCardIndices, beholdChosenSubtype,
                chosenCreatureType, waterbendPaid, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds,
                  Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                  boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                  Integer modalXValue, List<UUID> imposedSacrificePermanentIds,
                  List<UUID> additionalCostSacrificePermanentIds, List<String> repeatedAdditionalCosts,
                  boolean buyback, UUID beholdPermanentId, Integer beholdHandCardIndex,
                  List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                  CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType,
                  boolean waterbendPaid, Boolean payLifeForAdditionalCost) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = !fromGraveyard && hand != null && cardIndex >= 0 && cardIndex < hand.size()
                ? hand.get(cardIndex) : null;
        if (beginOpponentChosenTargetChoiceIfNeeded(
                gameData, player, cardIndex, xValue, targetId, attempted, buyback)) {
            return;
        }
        try {
            playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                    convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                    alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, kicked,
                    discardHandCardIndex, discardHandCardIndices, false, List.of(), modalXValue,
                    List.of(), imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                    repeatedAdditionalCosts, buyback, false, List.of(), null,
                    beholdPermanentId, beholdHandCardIndex, beholdPermanentIds, beholdHandCardIndices,
                    beholdChosenSubtype, chosenCreatureType, waterbendPaid, payLifeForAdditionalCost, false);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    /**
     * Casts a card and pays splice costs (CR 702.47): as an Arcane (or other splice-quality) spell
     * is cast, cards with matching splice may be revealed from hand; their splice costs become part
     * of the total cost and their SPELL effects are added to the spell. Spliced cards remain in hand.
     */
    public void playCardWithSplice(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                   Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                                   List<Integer> spliceHandCardIndices) {
        playCardWithSplice(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                spliceHandCardIndices, List.of());
    }

    public void playCardWithSplice(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                   Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                                   List<Integer> spliceHandCardIndices, List<UUID> spliceCostPermanentIds) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = hand != null && cardIndex >= 0 && cardIndex < hand.size() ? hand.get(cardIndex) : null;
        try {
            playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(), List.of(), false, null, null, List.of(),
                    null, null, false, null, null, false, List.of(), null,
                    spliceHandCardIndices != null ? spliceHandCardIndices : List.of(), null, null, List.of(), false, false,
                    spliceCostPermanentIds != null ? spliceCostPermanentIds : List.of(), null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    /**
     * Casts a card for its evoke (or other pure-mana alternate) cost (CR 702.75). Unlike the
     * sacrifice/tap-based alternate casts, evoke's alternate cost has no permanent components, so
     * it cannot be inferred from a non-empty sacrifice list — this entry point forces the alternate
     * cost explicitly. {@code targetId} carries any target the spell's ETB ability requires.
     */
    public void playCardWithEvoke(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                  Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = hand != null && cardIndex >= 0 && cardIndex < hand.size() ? hand.get(cardIndex) : null;
        try {
            playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(), List.of(), false, null, null, List.of(),
                    null, null, false, null, null, true, List.of(), null, List.of(), null, null, List.of(), false, false, List.of(), null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    /**
     * Casts a card for an alternative cost whose components carry no cast-request payload — one the
     * caster either pays in full or not at all, with nothing to choose (e.g. Spinning Darkness's
     * "exile the top three black cards of your graveyard"). Like evoke and prowl, such a cost cannot
     * be inferred from the request, so this entry point forces it explicitly.
     */
    public void playCardWithAlternateCost(GameData gameData, Player player, int cardIndex, Integer xValue,
                                          UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        playCardWithAlternateCost(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, null);
    }

    public void playCardWithAlternateCost(GameData gameData, Player player, int cardIndex, Integer xValue,
                                          UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                                          Integer handCardIndex) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = hand != null && cardIndex >= 0 && cardIndex < hand.size() ? hand.get(cardIndex) : null;
        try {
            if (attempted != null && attempted.getCastingOption(AdventureCast.class).isPresent()) {
                playAdventureCardInternal(gameData, player, cardIndex, xValue, targetId, targetIds);
                return;
            }
            if (attempted != null && attempted.getCastingOption(OmenCast.class).isPresent()) {
                playOmenCard(gameData, player, cardIndex, xValue, targetId, targetIds, false);
                return;
            }
            if (attempted != null && attempted.getKeywords().contains(Keyword.PLOT)) {
                plotCardFromHand(gameData, player, cardIndex, targetId, targetIds);
                return;
            }
            playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(), List.of(), false, null, null, List.of(),
                    null, null, false, handCardIndex, null, true, List.of(), null, List.of(), null, null, List.of(), false, false, List.of(), null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    public void playCardWithAdventure(GameData gameData, Player player, int cardIndex, Integer xValue,
                                      UUID targetId, Map<UUID, Integer> damageAssignments,
                                      List<UUID> targetIds) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = hand != null && cardIndex >= 0 && cardIndex < hand.size() ? hand.get(cardIndex) : null;
        try {
            if (attempted == null || attempted.getCastingOption(AdventureCast.class).isEmpty()
                    || attempted.getBackFaceCard() == null) {
                throw new IllegalStateException("Card does not have an Adventure face");
            }
            playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(), List.of(), false, null, null, List.of(),
                    null, null, false, null, null, false, List.of(), null, List.of(), null, null,
                    List.of(), false, false, List.of(), null, true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    private void plotCardFromHand(GameData gameData, Player player, int cardIndex,
                                  UUID targetId, List<UUID> targetIds) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalArgumentException("Invalid card index");
        }
        if (!playerId.equals(gameData.activePlayerId)
                || (gameData.currentStep != TurnStep.PRECOMBAT_MAIN
                && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN)
                || !gameData.stack.isEmpty()) {
            throw new IllegalStateException("A card can be plotted only at sorcery speed");
        }
        if (targetId != null || (targetIds != null && !targetIds.isEmpty())) {
            throw new IllegalStateException("Plotting a card does not use targets");
        }

        Card card = hand.get(cardIndex);
        AlternateHandCast plotOption = card.getCastingOption(AlternateHandCast.class)
                .orElseThrow(() -> new IllegalStateException("Plot cost is unavailable"));
        if (plotOption.costs().stream().anyMatch(cost -> !(cost instanceof ManaCastingCost))) {
            throw new IllegalStateException("Unsupported non-mana plot cost");
        }
        ManaCost plotCost = plotOption.getCost(ManaCastingCost.class)
                .map(cost -> new ManaCost(cost.manaCost()))
                .orElseGet(() -> new ManaCost("{0}"));
        int modifier = castingCostService.getPlotCostModifier(gameData, playerId, card);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (pool == null || !plotCost.canPay(pool, modifier)) {
            throw new IllegalStateException("Not enough mana to pay plot cost");
        }
        plotCost.pay(pool, modifier);

        hand.remove(cardIndex);
        markCardPlotted(gameData, playerId, card);
        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " plots ", card, "."));
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    private void markCardPlotted(GameData gameData, UUID playerId, Card card) {
        gameData.addToExile(playerId, card);
        gameData.plottedCardIds.add(card.getId());
        gameData.exilePlayPermissions.put(card.getId(), playerId);
        gameData.exilePlayWithoutPayingManaCost.add(card.getId());
        triggerCollectionService.checkPlotTriggers(gameData, playerId, card);
        if (gameData.hasPendingInteraction(PermanentChoiceContext.PlotTriggerAnyTarget.class)) {
            triggerCollectionService.processNextPlotTrigger(gameData);
        }
    }

    private void playAdventureCardLegacy(GameData gameData, Player player, int cardIndex, Integer xValue,
                                         UUID targetId, List<UUID> targetIds) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalArgumentException("Invalid card index");
        }

        Card physicalCard = hand.get(cardIndex);
        Card adventureCard = physicalCard.getBackFaceCard();
        AdventureCast adventureCast = physicalCard.getCastingOption(AdventureCast.class)
                .orElseThrow(() -> new IllegalStateException("Card does not have Adventure"));
        if (adventureCast.getCost(ManaCastingCost.class).isEmpty()) {
            throw new IllegalStateException("Adventure cost is not a mana cost");
        }
        if (adventureCast.costs().stream().anyMatch(cost -> !(cost instanceof ManaCastingCost))) {
            throw new IllegalStateException("Unsupported non-mana Adventure cost");
        }
        if (adventureCard == null || (!adventureCard.hasType(CardType.SORCERY)
                && !adventureCard.hasType(CardType.INSTANT))) {
            throw new IllegalStateException("Card does not have a castable Adventure face");
        }
        if (castingPermissionService.isSpellCastingFromHandRestricted(gameData, playerId)
                || !castingPermissionService.canCastWithSpellTimingRestriction(gameData, playerId, adventureCard)) {
            throw new IllegalStateException("Card is not playable");
        }
        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        if (!castingPermissionService.canCastWithTiming(
                gameData, playerId, adventureCard, isActivePlayer, isMainPhase, gameData.stack.isEmpty())) {
            throw new IllegalStateException("Card is not playable");
        }

        int effectiveXValue = xValue != null ? xValue : 0;
        effectiveXValue = resolveCastTimeXValue(gameData, adventureCard, playerId, effectiveXValue);
        validateXValueCap(gameData, adventureCard, playerId, effectiveXValue);
        if (adventureCard.getParsedManaCost() != null && adventureCard.getParsedManaCost().hasX()
                && effectiveXValue < 0) {
            throw new IllegalStateException("X value cannot be negative");
        }

        List<UUID> declaredTargetIds = targetIds != null ? targetIds : List.of();
        if (targetId != null) {
            if (EffectResolution.needsSpellTarget(adventureCard.getEffects(EffectSlot.SPELL))) {
                targetLegalityService.validateSpellTargetOnStack(
                        gameData, targetId, adventureCard.getTargetFilter(), playerId, effectiveXValue);
            } else {
                targetLegalityService.validateSpellTargeting(
                        gameData, adventureCard, targetId, null, playerId, true, effectiveXValue);
            }
        }
        if (!actionAvailabilityService.isCardPlayable(
                gameData, playerId, adventureCard, gameData.playerManaPools.get(playerId), 0)) {
            throw new IllegalStateException("Card is not playable");
        }
        List<CardEffect> effects = adventureCard.getEffects(EffectSlot.SPELL);
        if (targetId == null && declaredTargetIds.isEmpty() && adventureCard.getMinTargets() > 0) {
            throw new IllegalStateException("Spell requires a target");
        }

        paySpellManaCost(gameData, playerId, adventureCard, effectiveXValue, List.of());
        int manaSpent = gameData.getSpellCastManaSpent(adventureCard.getId());
        gameData.clearSpellCastManaSpent(adventureCard.getId());
        gameData.addSpellCastManaSpent(physicalCard.getId(), manaSpent);
        hand.remove(cardIndex);

        StackEntryType entryType = adventureCard.hasType(CardType.INSTANT)
                ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;
        Zone adventureTargetZone = EffectResolution.needsSpellTarget(effects) ? Zone.STACK : null;
        StackEntry entry;
        if (!declaredTargetIds.isEmpty()) {
            entry = new StackEntry(entryType, physicalCard, playerId, adventureCard.getName(), effects,
                    effectiveXValue, null, null, Map.of(), adventureTargetZone, List.of(), declaredTargetIds);
        } else if (targetId != null) {
            entry = new StackEntry(entryType, physicalCard, playerId, adventureCard.getName(), effects,
                    effectiveXValue, targetId, null, Map.of(), adventureTargetZone, List.of(), List.of());
        } else {
            entry = new StackEntry(entryType, physicalCard, playerId, adventureCard.getName(), effects, effectiveXValue);
        }
        entry.setCastWithAdventure(true);
        entry.setSourceZone(Zone.HAND);
        gameData.stack.add(entry);
        finishSpellCast(gameData, playerId, player, hand, physicalCard);
    }

    private void playOmenCard(GameData gameData, Player player, int cardIndex, Integer xValue,
                              UUID targetId, List<UUID> targetIds, boolean adventure) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalArgumentException("Invalid card index");
        }

        Card physicalCard = hand.get(cardIndex);
        Card omenCard = physicalCard.getBackFaceCard();
        if (omenCard == null || (!omenCard.hasType(CardType.SORCERY) && !omenCard.hasType(CardType.INSTANT))) {
            throw new IllegalStateException("Card does not have a castable alternate face");
        }
        if (castingPermissionService.isSpellCastingFromHandRestricted(gameData, playerId)
                || !castingPermissionService.canCastWithSpellTimingRestriction(gameData, playerId, omenCard)) {
            throw new IllegalStateException("Card is not playable");
        }
        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        if (!castingPermissionService.canCastWithTiming(
                gameData, playerId, omenCard, isActivePlayer, isMainPhase, gameData.stack.isEmpty())) {
            throw new IllegalStateException("Card is not playable");
        }
        int effectiveXValue = xValue != null ? xValue : 0;
        effectiveXValue = resolveCastTimeXValue(gameData, omenCard, playerId, effectiveXValue);
        validateXValueCap(gameData, omenCard, playerId, effectiveXValue);
        if (omenCard.getParsedManaCost() != null && omenCard.getParsedManaCost().hasX()
                && effectiveXValue < 0) {
            throw new IllegalStateException("X value cannot be negative");
        }

        List<UUID> declaredTargetIds = targetIds != null ? targetIds : List.of();
        if (targetId != null) {
            if (EffectResolution.needsSpellTarget(omenCard.getEffects(EffectSlot.SPELL))) {
                targetLegalityService.validateSpellTargetOnStack(
                        gameData, targetId, omenCard.getTargetFilter(), playerId, effectiveXValue);
            } else {
                targetLegalityService.validateSpellTargeting(
                        gameData, omenCard, targetId, null, playerId, true, effectiveXValue);
            }
        }
        if (!actionAvailabilityService.isCardPlayable(
                gameData, playerId, omenCard, gameData.playerManaPools.get(playerId), 0)) {
            throw new IllegalStateException("Card is not playable");
        }
        List<CardEffect> effects = omenCard.getEffects(EffectSlot.SPELL);
        if (targetId == null && declaredTargetIds.isEmpty()
                && omenCard.getMinTargets() > 0) {
            throw new IllegalStateException("Spell requires a target");
        }

        paySpellManaCost(gameData, playerId, omenCard, effectiveXValue, List.of());
        int manaSpent = gameData.getSpellCastManaSpent(omenCard.getId());
        gameData.clearSpellCastManaSpent(omenCard.getId());
        gameData.addSpellCastManaSpent(physicalCard.getId(), manaSpent);
        hand.remove(cardIndex);

        StackEntryType entryType = omenCard.hasType(CardType.INSTANT)
                ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;
        Zone omenTargetZone = EffectResolution.needsSpellTarget(effects) ? Zone.STACK : null;
        StackEntry entry;
        if (!declaredTargetIds.isEmpty()) {
            entry = new StackEntry(entryType, physicalCard, playerId, omenCard.getName(), effects,
                    effectiveXValue, null, null, Map.of(), omenTargetZone, List.of(), declaredTargetIds);
        } else if (targetId != null) {
            entry = new StackEntry(entryType, physicalCard, playerId, omenCard.getName(), effects,
                    effectiveXValue, targetId, null, Map.of(), omenTargetZone, List.of(), List.of());
        } else {
            entry = new StackEntry(entryType, physicalCard, playerId, omenCard.getName(), effects, effectiveXValue);
        }
        entry.setCastWithOmen(!adventure);
        entry.setCastWithAdventure(adventure);
        entry.setSourceZone(Zone.HAND);
        gameData.stack.add(entry);
        finishSpellCast(gameData, playerId, player, hand, physicalCard);
    }

    public void playAdventureCard(GameData gameData, Player player, int cardIndex, Integer xValue,
                                  UUID targetId, List<UUID> targetIds) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = hand != null && cardIndex >= 0 && cardIndex < hand.size() ? hand.get(cardIndex) : null;
        try {
            playAdventureCardInternal(gameData, player, cardIndex, xValue, targetId, targetIds);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    private void playAdventureCardInternal(GameData gameData, Player player, int cardIndex, Integer xValue,
                                           UUID targetId, List<UUID> targetIds) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalArgumentException("Invalid card index");
        }

        Card physicalCard = hand.get(cardIndex);
        if (physicalCard.getCastingOption(AdventureCast.class).isEmpty()) {
            throw new IllegalStateException("Card does not have Adventure");
        }
        Card adventureCard = physicalCard.getBackFaceCard();
        if (adventureCard == null || (!adventureCard.hasType(CardType.SORCERY)
                && !adventureCard.hasType(CardType.INSTANT))) {
            throw new IllegalStateException("Card does not have a castable Adventure face");
        }
        if (castingPermissionService.isSpellCastingFromHandRestricted(gameData, playerId)
                || !castingPermissionService.canCastWithSpellTimingRestriction(gameData, playerId, adventureCard)) {
            throw new IllegalStateException("Card is not playable");
        }
        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        if (!castingPermissionService.canCastWithTiming(
                gameData, playerId, adventureCard, isActivePlayer, isMainPhase, gameData.stack.isEmpty())) {
            throw new IllegalStateException("Card is not playable");
        }

        int effectiveXValue = xValue != null ? xValue : 0;
        effectiveXValue = resolveCastTimeXValue(gameData, adventureCard, playerId, effectiveXValue);
        validateXValueCap(gameData, adventureCard, playerId, effectiveXValue);
        if (adventureCard.getParsedManaCost() != null && adventureCard.getParsedManaCost().hasX()
                && effectiveXValue < 0) {
            throw new IllegalStateException("X value cannot be negative");
        }

        List<UUID> declaredTargetIds = targetIds != null ? targetIds : List.of();
        List<CardEffect> effects = adventureCard.getEffects(EffectSlot.SPELL);
        boolean adventureNeedsGraveyardTargeting = effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        boolean adventureNeedsExileTargeting = effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD));
        boolean adventureNeedsCastTarget = EffectResolution.needsSpellCastTarget(
                effects, adventureCard.isAura(), adventureCard.isEnchantPlayer());
        if (!declaredTargetIds.isEmpty()) {
            targetLegalityService.validateMultiSpellTargets(
                    gameData, adventureCard, declaredTargetIds, playerId, effectiveXValue);
        } else if (targetId != null) {
            if (adventureNeedsExileTargeting) {
                targetLegalityService.validateEffectTargetInZone(
                        gameData, adventureCard, effects, targetId, Zone.EXILE, effectiveXValue, playerId);
            } else if (adventureNeedsGraveyardTargeting) {
                targetLegalityService.validateEffectTargetInZone(
                        gameData, adventureCard, effects, targetId, Zone.GRAVEYARD, effectiveXValue, playerId);
            } else if (EffectResolution.needsSpellTarget(effects)) {
                targetLegalityService.validateSpellTargetOnStack(
                        gameData, targetId, adventureCard.getTargetFilter(), playerId, effectiveXValue);
            } else {
                targetLegalityService.validateSpellTargeting(
                        gameData, adventureCard, targetId, null, playerId, true, effectiveXValue);
            }
        }
        if (!actionAvailabilityService.isCardPlayable(
                gameData, playerId, adventureCard, gameData.playerManaPools.get(playerId), 0)) {
            throw new IllegalStateException("Card is not playable");
        }
        if (targetId == null && declaredTargetIds.isEmpty()
                && (adventureNeedsCastTarget || adventureCard.getMinTargets() > 0)) {
            throw new IllegalStateException("Spell requires a target");
        }

        paySpellManaCost(gameData, playerId, adventureCard, effectiveXValue, List.of());
        int manaSpent = gameData.getSpellCastManaSpent(adventureCard.getId());
        gameData.clearSpellCastManaSpent(adventureCard.getId());
        gameData.addSpellCastManaSpent(physicalCard.getId(), manaSpent);
        hand.remove(cardIndex);

        StackEntryType entryType = adventureCard.hasType(CardType.INSTANT)
                ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;
        Zone adventureTargetZone = adventureNeedsExileTargeting
                ? Zone.EXILE
                : adventureNeedsGraveyardTargeting
                ? Zone.GRAVEYARD
                : EffectResolution.needsSpellTarget(effects) ? Zone.STACK : null;
        StackEntry entry;
        if (!declaredTargetIds.isEmpty()) {
            entry = new StackEntry(entryType, physicalCard, playerId, adventureCard.getName(), effects,
                    effectiveXValue, declaredTargetIds);
        } else if (targetId != null) {
            entry = new StackEntry(entryType, physicalCard, playerId, adventureCard.getName(), effects,
                    effectiveXValue, targetId, null, Map.of(), adventureTargetZone, List.of(), List.of());
        } else {
            entry = new StackEntry(entryType, physicalCard, playerId, adventureCard.getName(), effects,
                    effectiveXValue);
        }
        entry.setCastWithAdventure(true);
        entry.setSourceZone(Zone.HAND);
        gameData.stack.add(entry);
        finishSpellCast(gameData, playerId, player, hand, physicalCard);
    }

    public void playCardWithMorph(GameData gameData, Player player, int cardIndex, Integer xValue,
                                  UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        playCardWithMorph(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, null);
    }

    public void playCardWithMorph(GameData gameData, Player player, int cardIndex, Integer xValue,
                                  UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                                  Integer revealedHandCardIndex) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        if (hand == null || cardIndex < 0 || cardIndex >= hand.size() || hand.get(cardIndex).getMorphCost() == null) {
            throw new IllegalStateException("Card does not have morph");
        }
        playCardWithAlternateCost(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                revealedHandCardIndex);
    }

    /** Validates the permanent choices for a morph face-up return cost before any cost is paid. */
    public void validateMorphAdditionalCost(GameData gameData, Player player, ReturnPermanentsCost cost,
                                             List<UUID> permanentIds) {
        List<UUID> ids = permanentIds != null ? permanentIds : List.of();
        if (ids.size() != cost.count()) {
            throw new IllegalStateException("Must return exactly " + cost.count() + " permanents");
        }
        Set<UUID> uniqueIds = new HashSet<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
        for (UUID permanentId : ids) {
            if (!uniqueIds.add(permanentId)) {
                throw new IllegalStateException("Each permanent may be chosen only once");
            }
            Permanent toReturn = battlefield == null ? null : battlefield.stream()
                    .filter(permanent -> permanent.getId().equals(permanentId))
                    .findFirst()
                    .orElse(null);
            if (toReturn == null) {
                throw new IllegalStateException("Return target not found on your battlefield");
            }
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, toReturn, cost.filter())) {
                throw new IllegalStateException("Return target does not match the required filter");
            }
        }
    }

    /** Returns the already-validated permanents for a morph face-up cost and records the payment. */
    public void payMorphAdditionalCost(GameData gameData, Player player, Card card, ReturnPermanentsCost cost,
                                        List<UUID> permanentIds) {
        for (UUID permanentId : permanentIds) {
            Permanent toReturn = gameQueryService.findPermanentById(gameData, permanentId);
            if (toReturn != null && permanentRemovalService.removePermanentToHand(gameData, toReturn)) {
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " returns ")
                        .card(toReturn.getCard())
                        .text(" to hand for ")
                        .card(card)
                        .text(".")
                        .build());
            }
        }
    }

    /** Validates the discard component of a morph face-up cost before any cost is paid. */
    public void validateMorphDiscardCost(GameData gameData, Player player, Card card,
                                         DiscardCardTypeCost cost, Integer discardHandCardIndex) {
        additionalSpellCostService.validateDiscardCost(
                gameData, player, card, cost, discardHandCardIndex, -1);
    }

    /** Pays the already-validated discard component of a morph face-up cost. */
    public void payMorphDiscardCost(GameData gameData, Player player, Card card,
                                    DiscardCardTypeCost cost, Integer discardHandCardIndex) {
        int effectiveIndex = additionalSpellCostService.validateDiscardCost(
                gameData, player, card, cost, discardHandCardIndex, -1);
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card toDiscard = hand.remove(effectiveIndex);
        graveyardService.addCardToGraveyard(gameData, playerId, toDiscard);
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " discards ")
                .card(toDiscard)
                .text(" to turn ")
                .card(card)
                .text(" face up.")
                .build());
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, toDiscard);
    }

    /**
     * Casts a card for its prowl cost (CR 702.75). Like evoke, prowl is a pure-mana alternate hand
     * cost that must be forced explicitly. The prowl availability condition (dealt combat damage
     * with the required creature type this turn) is validated inside {@code playCardInternal}.
     */
    public void playCardWithProwl(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                  Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = hand != null && cardIndex >= 0 && cardIndex < hand.size() ? hand.get(cardIndex) : null;
        try {
            playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(), List.of(), false, null, null, List.of(),
                    null, null, false, null, null, true, List.of(), null, List.of(), null, null, List.of(), false, false, List.of(), null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    /**
     * Casts a card for its overload cost (CR 702.96a). Overload is an alternative cost with no
     * permanent components, so like evoke and prowl it must be forced explicitly. Paying it also
     * changes the spell's text — every "target" becomes "each" — which per CR 702.96b means the
     * spell takes no targets at all, so no {@code targetId} is accepted here.
     */
    public void playCardWithOverload(GameData gameData, Player player, int cardIndex, Integer xValue) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = hand != null && cardIndex >= 0 && cardIndex < hand.size() ? hand.get(cardIndex) : null;
        try {
            playCardInternal(gameData, player, cardIndex, xValue, null, Map.of(),
                    List.of(), List.of(), false, null, null, List.of(),
                    null, null, false, null, null, true, List.of(), null, List.of(), null, null, List.of(), false, true, List.of(), null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    /**
     * Casts a card and pays its conspire cost (CR 702.78): as the spell is cast, two untapped
     * creatures the caster controls that each share a color with the spell are tapped. Paying the
     * cost queues a "when you do, copy it and you may choose a new target for the copy" trigger.
     * Unlike evoke/prowl, conspire is an <em>additional</em> cost — the spell's normal mana cost is
     * still paid.
     */
    public void playCardWithConspire(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                     Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> conspireCreatureIds) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card attempted = hand != null && cardIndex >= 0 && cardIndex < hand.size() ? hand.get(cardIndex) : null;
        try {
            playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(), List.of(), false, null, null, List.of(),
                    null, null, false, null, null, false,
                    conspireCreatureIds != null ? conspireCreatureIds : List.of(), null, List.of(), null, null, List.of(), false, false, List.of(), null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            restoreAttemptedCardAfterFailedCast(gameData, hand, attempted, cardIndex);
            throw e;
        }
    }

    private static void restoreAttemptedCardAfterFailedCast(GameData gameData, List<Card> hand,
                                                             Card attempted, int cardIndex) {
        if (attempted == null || hand == null || gameData.stack.stream()
                .anyMatch(entry -> attempted.getId().equals(entry.getCard().getId()))) {
            return;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (attempted.getId().equals(hand.get(i).getId())) {
                hand.set(i, attempted);
                return;
            }
        }
        hand.add(Math.min(cardIndex, hand.size()), attempted);
    }

    private void playCardInternal(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                  Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds,
                  boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount,
                  List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex,
                  List<Integer> discardHandCardIndices, boolean forceAlternateCost,
                  List<UUID> conspireCreatureIds, Integer modalXValue, List<Integer> spliceHandCardIndices,
                  List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds,
                  List<String> repeatedAdditionalCosts, boolean buyback, boolean overloaded,
                  List<UUID> spliceCostPermanentIds, Integer sharedColorDiscardHandCardIndex) {
        playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, forceAlternateCost, conspireCreatureIds,
                modalXValue, spliceHandCardIndices, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback, overloaded,
                spliceCostPermanentIds, sharedColorDiscardHandCardIndex, null, null, List.of(), List.of(), null,
                null, false, null, false);
    }

    private void playCardInternal(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                  Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds,
                  boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount,
                  List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex,
                  List<Integer> discardHandCardIndices, boolean forceAlternateCost,
                  List<UUID> conspireCreatureIds, Integer modalXValue, List<Integer> spliceHandCardIndices,
                  List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds,
                  List<String> repeatedAdditionalCosts, boolean buyback, boolean overloaded,
                  List<UUID> spliceCostPermanentIds, Integer sharedColorDiscardHandCardIndex,
                  boolean adventure) {
        playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, forceAlternateCost, conspireCreatureIds,
                modalXValue, spliceHandCardIndices, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback, overloaded,
                spliceCostPermanentIds, sharedColorDiscardHandCardIndex, null, null, List.of(), List.of(), null,
                null, false, null, adventure);
    }

    private void playCardInternal(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                  Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds,
                  boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount,
                  List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex,
                  List<Integer> discardHandCardIndices, boolean forceAlternateCost,
                  List<UUID> conspireCreatureIds, Integer modalXValue, List<Integer> spliceHandCardIndices,
                  List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds,
                  List<String> repeatedAdditionalCosts, boolean buyback, boolean overloaded,
                  List<UUID> spliceCostPermanentIds, Integer sharedColorDiscardHandCardIndex,
                  UUID beholdPermanentId, Integer beholdHandCardIndex,
                  List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                  CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType,
                  boolean waterbendPaid, Boolean payLifeForAdditionalCost, boolean adventure) {
        playCardInternal(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, forceAlternateCost, conspireCreatureIds,
                modalXValue, spliceHandCardIndices, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback, overloaded,
                spliceCostPermanentIds, sharedColorDiscardHandCardIndex, beholdPermanentId, beholdHandCardIndex,
                beholdPermanentIds, beholdHandCardIndices, beholdChosenSubtype, chosenCreatureType,
                waterbendPaid, payLifeForAdditionalCost, adventure, false);
    }

    private void playCardInternal(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex,
                  List<Integer> discardHandCardIndices, boolean forceAlternateCost,
                  List<UUID> conspireCreatureIds, Integer modalXValue, List<Integer> spliceHandCardIndices,
                  List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds,
                  List<String> repeatedAdditionalCosts, boolean buyback, boolean overloaded,
                  List<UUID> spliceCostPermanentIds, Integer sharedColorDiscardHandCardIndex,
                  UUID beholdPermanentId, Integer beholdHandCardIndex,
                  List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                  CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType,
                  boolean waterbendPaid, Boolean payLifeForAdditionalCost, boolean adventure,
                  boolean giftPromised) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (repeatedAdditionalCosts == null) repeatedAdditionalCosts = List.of();
        if (additionalCostSacrificePermanentIds == null) additionalCostSacrificePermanentIds = List.of();
        if (targetIds == null) targetIds = List.of();
        if (convokeCreatureIds == null) convokeCreatureIds = List.of();
        if (conspireCreatureIds == null) conspireCreatureIds = List.of();
        if (spliceHandCardIndices == null) spliceHandCardIndices = List.of();
        if (spliceCostPermanentIds == null) spliceCostPermanentIds = List.of();
        if (alternateCostSacrificePermanentIds == null) alternateCostSacrificePermanentIds = List.of();
        if (imposedSacrificePermanentIds == null) imposedSacrificePermanentIds = List.of();
        if (beholdPermanentIds == null) beholdPermanentIds = List.of();
        if (beholdHandCardIndices == null) beholdHandCardIndices = List.of();
        if (discardHandCardIndices == null) discardHandCardIndices = List.of();
        List<Integer> dividedDamageTargetGroupSizes = List.of();
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();

        // Refresh the caster's "spend white as red" permission (Sunglasses of Urza) from current board
        // state so this cast's affordability checks and payment honor it.
        ManaPool casterPool = gameData.playerManaPools.get(playerId);
        if (casterPool != null) {
            casterPool.setWhiteSpendableAsRed(gameQueryService.canSpendWhiteManaAsRed(gameData, playerId));
            casterPool.setWhiteSpendableAsAnyColor(gameQueryService.canSpendWhiteManaAsAnyColor(gameData, playerId));
            casterPool.setWhiteSpendableAsAnyColorWithoutRestriction(
                    gameQueryService.canSpendWhiteManaAsAnyColorUntilEndOfTurn(gameData, playerId));
            casterPool.setAllManaSpendableAsAnyColor(gameQueryService.canSpendManaAsAnyColor(gameData, playerId));
        }

        List<Card> handEarly = gameData.playerHands.get(playerId);
        if (!fromGraveyard && (cardIndex < 0 || cardIndex >= handEarly.size())) {
            throw new IllegalArgumentException("Invalid card index");
        }

        List<UUID> costReductionSacrificeIds = !alternateCostSacrificePermanentIds.isEmpty()
                ? alternateCostSacrificePermanentIds : additionalCostSacrificePermanentIds;
        boolean hasSacrificeForCostReduction = !costReductionSacrificeIds.isEmpty() && !fromGraveyard
                && handEarly.get(cardIndex).getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(SacrificeCreaturesForCostReductionEffect.class::isInstance);
        boolean hasExileHandAlternateCost = !fromGraveyard
                && handEarly.get(cardIndex).getCastingOption(AlternateHandCast.class)
                        .flatMap(a -> a.getCost(ExileCardsFromHandCastingCost.class))
                        .isPresent();
        List<DiscardCardCastingCost> alternateDiscardCosts = !fromGraveyard
                ? handEarly.get(cardIndex).getCastingOption(AlternateHandCast.class)
                        .map(a -> a.getCosts(DiscardCardCastingCost.class))
                        .orElse(List.of())
                : List.of();
        boolean hasDiscardHandAlternateCost = !alternateDiscardCosts.isEmpty();
        List<Integer> alternateDiscardHandCardIndices = List.of();
        int additionalAlternateDiscardCount = Math.max(0, alternateDiscardCosts.size() - 1);
        if (additionalAlternateDiscardCount > 0
                && (forceAlternateCost || discardHandCardIndex != null || sharedColorDiscardHandCardIndex != null)) {
            int splitIndex = Math.min(additionalAlternateDiscardCount, discardHandCardIndices.size());
            alternateDiscardHandCardIndices = List.copyOf(discardHandCardIndices.subList(0, splitIndex));
            discardHandCardIndices = List.copyOf(discardHandCardIndices.subList(splitIndex,
                    discardHandCardIndices.size()));
        }
        boolean hasBestowCost = !fromGraveyard
                && handEarly.get(cardIndex).getCastingOption(BestowCast.class).isPresent();
        WebSlingingEffect webSlingingEffect = null;
        if (!fromGraveyard && alternateCostSacrificePermanentIds.size() == 1
                && !hasSacrificeForCostReduction) {
            Card webSlingingFace = handEarly.get(cardIndex);
            if (webSlingingFace.isModalDoubleFaced()
                    && webSlingingFace.getBackFaceCard() != null && effectiveXValue == 1) {
                webSlingingFace = webSlingingFace.getBackFaceCard();
            }
            webSlingingEffect = castingCostService.findWebSlingingEffectFromBattlefield(
                    gameData, playerId, webSlingingFace);
        }
        boolean usingWebSlingingCost = webSlingingEffect != null
                && !handEarly.get(cardIndex).getCastingOption(AlternateHandCast.class).isPresent()
                && !handEarly.get(cardIndex).getCastingOption(BestowCast.class).isPresent();
        boolean hasGraveyardExileAlternateCost = !fromGraveyard
                && exileGraveyardCardIndex != null
                && handEarly.get(cardIndex).getCastingOption(AlternateHandCast.class)
                .flatMap(a -> a.getCost(ExileCardFromGraveyardCastingCost.class))
                .isPresent();
        boolean usingBestowCost = hasBestowCost && (forceAlternateCost || targetId != null);
        boolean usingSharedColorDiscardAlternativeCost = !fromGraveyard
                && sharedColorDiscardHandCardIndex != null
                && handEarly.get(cardIndex).getCastingOption(AlternateHandCast.class).isEmpty()
                && castingCostService.hasSharedColorDiscardAlternativeCostFromBattlefield(
                        gameData, playerId, handEarly.get(cardIndex));
        CollectEvidenceCost collectEvidenceAlternativeCost = !fromGraveyard
                && exileGraveyardCardIndices != null
                && !exileGraveyardCardIndices.isEmpty()
                ? castingCostService.findCollectEvidenceAlternativeCostFromBattlefield(
                        gameData, playerId, handEarly.get(cardIndex))
                : null;
        boolean usingCollectEvidenceAlternativeCost = collectEvidenceAlternativeCost != null;
        Integer alternateDiscardHandCardIndex = hasDiscardHandAlternateCost
                ? discardHandCardIndex != null ? discardHandCardIndex : sharedColorDiscardHandCardIndex
                : null;
        boolean usingAlternateCost = usingBestowCost || forceAlternateCost
                || hasGraveyardExileAlternateCost
                || usingCollectEvidenceAlternativeCost
                || (!alternateCostSacrificePermanentIds.isEmpty() && !hasSacrificeForCostReduction)
                || (hasExileHandAlternateCost && discardHandCardIndex != null)
                || alternateDiscardHandCardIndex != null
                || usingSharedColorDiscardAlternativeCost
                || usingWebSlingingCost;
        Integer webSlingingReturnedCreatureManaValue = null;

        // Handle playing a land from graveyard (e.g. via Crucible of Worlds)
        if (fromGraveyard) {
            List<Integer> playableGraveyard = actionAvailabilityService.getPlayableGraveyardLandIndices(gameData, playerId);
            if (!playableGraveyard.contains(cardIndex)) {
                throw new IllegalStateException("Card is not playable from graveyard");
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            playGraveyardLandFromLocation(gameData, player, graveyard, cardIndex, effectiveXValue);
            return;
        }

        Card physicalHandCard = gameData.playerHands.get(playerId).get(cardIndex);
        boolean castingFaceDown = usingAlternateCost && physicalHandCard.getMorphCost() != null;
        boolean selectingModalBackFace = physicalHandCard.isModalDoubleFaced()
                && physicalHandCard.getBackFaceCard() != null
                && effectiveXValue == 1;
        if (adventure && (physicalHandCard.getBackFaceCard() == null
                || physicalHandCard.getCastingOption(AdventureCast.class).isEmpty())) {
            throw new IllegalStateException("Card does not have an Adventure face");
        }
        Card handCardForTiming = adventure
                ? physicalHandCard.getBackFaceCard()
                : selectingModalBackFace
                ? physicalHandCard.getBackFaceCard()
                : physicalHandCard;
        if (handCardForTiming.isCastOnlyFromGraveyard()) {
            throw new IllegalStateException("Card cannot be cast from hand");
        }
        if (handCardForTiming.hasType(CardType.LAND) && !castingFaceDown
                ? castingPermissionService.isLandPlayFromHandRestricted(gameData, playerId)
                : castingPermissionService.isSpellCastingFromHandRestricted(gameData, playerId)) {
            throw new IllegalStateException("Card is not playable");
        }
        if (!castingPermissionService.canCastWithSpellTimingRestriction(gameData, playerId, handCardForTiming)) {
            throw new IllegalStateException("Card is not playable");
        }

        if (!usingAlternateCost && castingPermissionService.flashTimingRequiresAlternateCast(
                gameData, playerId, gameData.playerHands.get(playerId).get(cardIndex))) {
            throw new IllegalStateException("Card is not playable");
        }

        List<Integer> playable = actionAvailabilityService.getPlayableCardIndices(gameData, playerId);
        if (!playable.contains(cardIndex)) {
            // Re-check with convoke if card has convoke keyword
            List<Card> handCheck = gameData.playerHands.get(playerId);
            Card cardCheck = handCheck.get(cardIndex);
            Card selectedFaceCheck = selectingModalBackFace ? cardCheck.getBackFaceCard() : cardCheck;
            boolean suppliedExiledCardTarget = targetId != null
                    && cardCheck.getEffects(EffectSlot.SPELL).stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD));
            boolean suppliedCollectEvidenceAdditionalCost = exileGraveyardCardIndices != null
                    && !exileGraveyardCardIndices.isEmpty()
                    && cardCheck.getEffects(EffectSlot.SPELL).stream()
                    .anyMatch(CollectEvidenceCost.class::isInstance);
            boolean suppliedGiftTarget = giftPromised
                    && (targetId != null || targetIds != null && !targetIds.isEmpty())
                    && actionAvailabilityService.isCardPlayableWithDeclaredTargets(
                    gameData, playerId, selectedFaceCheck, gameData.playerManaPools.get(playerId), 0);
            if (suppliedGiftTarget) {
                // Gift can change the spell's target filter. The selected target is validated
                // against the gifted filter below after the prepared effects are known.
            } else if (selectingModalBackFace && actionAvailabilityService.isCardPlayableWithDeclaredTargets(
                    gameData, playerId, selectedFaceCheck, gameData.playerManaPools.get(playerId), 0)) {
                // The generic hand query admits either face; casting validates the selected face.
            } else if (adventure && cardCheck.getBackFaceCard() != null
                    && actionAvailabilityService.isCardPlayableWithDeclaredTargets(
                    gameData, playerId, cardCheck.getBackFaceCard(), gameData.playerManaPools.get(playerId), 0)) {
                // The selected Adventure face is playable even when the normal face is not.
            } else if (suppliedExiledCardTarget) {
                // Exiled-card targets are validated after the prepared spell effects are resolved.
            } else if (suppliedCollectEvidenceAdditionalCost) {
                // Allow optional evidence payment; its graveyard selection and mana reduction are validated below.
            } else if (usingAlternateCost && (usingCollectEvidenceAlternativeCost
                    || cardCheck.getCastingOption(AlternateHandCast.class).isPresent()
                    || cardCheck.getCastingOption(BestowCast.class).isPresent()
                    || usingSharedColorDiscardAlternativeCost
                    || usingWebSlingingCost)) {
                // Allow — alternate cost bypasses mana check; validated below
            } else if ((cardCheck.getKeywords().contains(Keyword.CONVOKE)
                    || cardCheck.getKeywords().contains(Keyword.IMPROVISE)
                    || hasSpellCastingAbilityGrantForCard(gameData, playerId, cardCheck, Keyword.CONVOKE)
                    || hasSpellCastingAbilityGrantForCard(gameData, playerId, cardCheck, Keyword.IMPROVISE))
                    && !convokeCreatureIds.isEmpty()) {
                // Allow assisted casting even if not in basic playable list
                List<Integer> convokePlayable = actionAvailabilityService.getPlayableCardIndices(gameData, playerId, convokeCreatureIds.size());
                if (!convokePlayable.contains(cardIndex)) {
                    throw new IllegalStateException("Card is not playable even with casting assistance");
                }
            } else if (hasSacrificeForCostReduction) {
                // Allow — sacrifice cost reduction will be validated during casting
            } else {
                boolean playableWithTargetingReduction = false;
                Permanent targetingReductionTarget = targetId != null
                        ? gameQueryService.findPermanentById(gameData, targetId)
                        : null;
                boolean explicitExileTarget = targetId != null
                        && gameData.findExiledCard(targetId) != null
                        && cardCheck.getEffects(EffectSlot.SPELL).stream()
                        .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD));
                if (targetId != null
                        && cardCheck.getTargetFilter() != null
                        && (targetingReductionTarget != null || gameData.playerIds.contains(targetId))) {
                    targetLegalityService.validateSpellTargeting(
                            gameData, cardCheck, targetId, null, playerId, true,
                            effectiveXValue, false, giftPromised);
                }
                if (targetingReductionTarget != null) {
                    int targetingCostModifier = castingCostService.getTargetingSpellCostModifier(
                            gameData, playerId, cardCheck, targetId, targetIds);
                    if (targetingCostModifier < 0) {
                        playableWithTargetingReduction = actionAvailabilityService.isCardPlayable(
                                gameData, playerId, cardCheck, gameData.playerManaPools.get(playerId),
                                targetingCostModifier);
                    }
                }
                if (playableWithTargetingReduction || explicitExileTarget) {
                    // Allow — the chosen target supplies a cost reduction that the target-free
                    // playability query cannot see.
                } else {
                    validateSuppliedGraveyardTargetBeforePlayabilityFailure(
                            gameData, selectedFaceCheck, playerId, targetId, effectiveXValue);
                    throw new IllegalStateException("Card is not playable");
                }
            }
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        Card bestowOriginalCard = usingBestowCost ? hand.get(cardIndex) : null;
        boolean usingPrototypeCost = usingAlternateCost && !usingBestowCost
                && hand.get(cardIndex).getCastingOption(AlternateHandCast.class)
                .map(AlternateHandCast::isPrototype)
                .orElse(false);
        Card preparedCard = usingBestowCost
                ? bestowRuntimeCopyForHandCast(hand, cardIndex)
                : usingPrototypeCost
                ? prototypeRuntimeCopyForHandCast(hand, cardIndex)
                : modalRuntimeCopyForHandCast(hand, cardIndex);
        if (!spliceHandCardIndices.isEmpty()) {
            preparedCard = preparedCard.createRuntimeCopy();
            hand.set(cardIndex, preparedCard);
        }
        final Card card = adventure ? preparedCard.getBackFaceCard() : preparedCard;
        if (card == null) {
            throw new IllegalStateException("Card does not have an Adventure face");
        }
        boolean hasGiftChoice = card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(GiftEffect.class::isInstance);
        if (giftPromised && !hasGiftChoice) {
            throw new IllegalStateException("Gift was promised for a card without Gift");
        }
        effectiveXValue = resolveCastTimeXValue(gameData, card, playerId, effectiveXValue);
        validateXValueCap(gameData, card, playerId, effectiveXValue);
        int extraTargetCount = Math.max(0, targetIds.size() - 1);
        int perTargetCost = card.getAdditionalCostPerExtraTarget() * extraTargetCount;
        boolean hasXCost = card.getManaCost() != null && new ManaCost(card.getManaCost()).hasX();
        String perTargetManaCost = repeatAdditionalTargetManaCost(card, extraTargetCount);
        boolean hasModalEtb = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(ChooseOneEffect.class::isInstance);
        applyModalEtbTargetFilter(card, effectiveXValue);
        List<CardEffect> filteredSpellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        boolean hasCreatureSpellAdditionalCountersCost = additionalSpellCostService
                .hasCreatureSpellAdditionalCountersCost(gameData, playerId, card);
        AdditionalSpellCostService.ExtractedCosts additionalCosts =
                additionalSpellCostService.extractAndRemove(gameData, playerId, card, filteredSpellEffects);
        List<SpliceEffect> pendingSpliceCosts = resolveAndAppendSpliceEffects(
                gameData, playerId, hand, card, cardIndex, spliceHandCardIndices, filteredSpellEffects);
        int escalateModeCount = additionalCosts.hasEscalate()
                ? additionalSpellCostService.countChosenModes(card, effectiveXValue) : 0;
        String tieredManaSuffix = additionalSpellCostService.tieredManaSuffix(
                additionalCosts.tieredManaCost(), effectiveXValue);
        String escalateManaSuffix = tieredManaSuffix
                + additionalSpellCostService.escalateManaSuffix(
                additionalCosts.escalateManaCost(), escalateModeCount)
                + additionalSpellCostService.repeatedAdditionalCostSuffix(
                        card, additionalCosts.repeatableManaCost(), repeatedAdditionalCosts)
                + spreeManaSuffix(additionalCosts.spreeAdditionalManaCost(),
                        filteredSpellEffects, effectiveXValue);
        // "Destroy target artifact. For each additional payment, destroy another target artifact":
        // the announced X is the number of targets the payments buy, so it must match the payments
        // actually made (one base target plus one per repetition). Spells whose repeatable cost
        // buys no targets (Taste of Paradise) read the payment count straight off the stack entry,
        // so their X is unconstrained.
        if (additionalCosts.repeatableManaCost() != null && card.hasXScaledTargets()
                && effectiveXValue != 1 + repeatedAdditionalCosts.size()) {
            throw new IllegalStateException("X must be " + (1 + repeatedAdditionalCosts.size())
                    + " for the additional costs paid");
        }
        AdditionalSpellCostService.CostSelection costSelection = new AdditionalSpellCostService.CostSelection(
                sacrificePermanentId, exileGraveyardCardIndex, exileGraveyardCardIndices,
                discardHandCardIndex, discardHandCardIndices, escalateModeCount, cardIndex,
                additionalCostSacrificePermanentIds, beholdPermanentId, beholdHandCardIndex,
                beholdPermanentIds, beholdHandCardIndices, beholdChosenSubtype,
                payLifeForAdditionalCost);
        boolean collectEvidenceCostPaid = usingCollectEvidenceAlternativeCost
                || isCollectEvidenceCostPaid(additionalCosts, costSelection);
        if (usingCollectEvidenceAlternativeCost) {
            additionalSpellCostService.validateCollectEvidenceCost(
                    gameData, player, card, collectEvidenceAlternativeCost, exileGraveyardCardIndices);
        }

        // Handle modal spells (Choose one): unwrap at cast time per MTG CR 700.2a
        boolean wasModal = filteredSpellEffects.stream().anyMatch(ChooseOneEffect.class::isInstance);
        int modeEncoding = effectiveXValue;
        filteredSpellEffects.stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .forEach(modal -> validateConditionalModalSelection(gameData, playerId, modal, modeEncoding));
        int modalModeCount = filteredSpellEffects.stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .map(modal -> modal.decodeModeIndices(modeEncoding).size())
                .orElse(0);
        String selectedModeManaCost = selectedModalManaCost(filteredSpellEffects, modeEncoding);
        filteredSpellEffects.stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .forEach(modal -> validateOptionalCostModalSelection(
                        modal, additionalCosts, costSelection, modeEncoding));
        effectiveXValue = unwrapChooseOneEffect(gameData, playerId, card, filteredSpellEffects, effectiveXValue);
        DealDividedDamageEffect castTimeDividedDamage = findChosenDividedDamageEffect(filteredSpellEffects);
        if (castTimeDividedDamage != null && damageAssignments != null && !damageAssignments.isEmpty()
                && card.getSpellTargets().size() > 1) {
            List<UUID> damageTargetIds = List.copyOf(damageAssignments.keySet());
            int otherTargetCount = targetIds.size();
            List<UUID> combinedTargetIds = new ArrayList<>(damageTargetIds);
            combinedTargetIds.addAll(targetIds);
            targetIds = List.copyOf(combinedTargetIds);
            dividedDamageTargetGroupSizes = List.of(damageTargetIds.size(), otherTargetCount);
        }
        boolean castModalBackFace = selectingModalBackFace && modeEncoding == 1;
        // A mode that brought its own total cost (a split card's half, or its fuse mode) was never
        // the cost the playability pre-check cleared — that check only needs *some* mode to be
        // affordable — so the mode actually chosen has to be paid for here.
        if (!usingAlternateCost && selectedModeManaCost != null) {
            ManaPool modePool = gameData.playerManaPools.get(playerId);
            ManaCost modeCost = card.getParsedManaCost();
            int modeAdditionalCost = castingCostService.getCastCostModifier(
                    gameData, playerId, card, effectiveXValue, null, collectEvidenceCostPaid);
            if (modeCost != null && !modeCost.canPay(modePool, modeAdditionalCost)) {
                throw new IllegalStateException("Not enough mana to pay " + card.getManaCost()
                        + " for " + card.getName());
            }
        }
        // Overload (CR 702.96a) rewrites "target" to "each" as the spell is cast, so the resolved
        // effect list — not the card's printed union targeting — decides whether a target is needed.
        if (overloaded) {
            filteredSpellEffects = EffectResolution.resolveEffects(filteredSpellEffects, kicked, true, null);
        }
        // The mode selection is carried by xValue (consumed above). For a modal spell that also has an
        // {X} cost (e.g. Alabaster Potion), the real X paid is threaded separately via modalXValue so it
        // drives mana payment / XValue resolution — while non-{X} modal spells keep X = 0 as before.
        if ((wasModal || hasModalEtb) && modalXValue != null) {
            effectiveXValue = modalXValue;
        }
        effectiveXValue = additionalSpellCostService.resolveXValue(
                additionalCosts, costSelection, effectiveXValue);
        if (castingPermissionService.isOpponentsChosenColorSpellCastRestricted(gameData, playerId, card)
                || castingPermissionService.isOpponentsSpellMatchingPredicateRestricted(gameData, playerId, card)
                || castingPermissionService.isOpponentsManaValueSpellCastRestricted(gameData, playerId, card, effectiveXValue)) {
            throw new IllegalStateException("Card is not playable");
        }

        // Kicker can add a target to a spell that is otherwise targetless (e.g. Unstable Footing),
        // so cast-time targeting must use the branch selected by the kicker choice. The raw effect
        // list remains on the stack entry so resolution can still evaluate other conditions there.
        List<CardEffect> targetingSpellEffects = EffectResolution.resolveEffects(
                filteredSpellEffects, kicked, overloaded, null);
        validateCastTimeCreatureTypeChoice(gameData, playerId, targetId, targetIds,
                targetingSpellEffects, chosenCreatureType);
        boolean unwrappedNeedsSpellTarget = targetingSpellEffects.stream()
                .anyMatch(EffectResolution::targetsSpellOnStack);
        // ETB triggered abilities choose targets after a permanent enters; this helper only sees
        // the spell's effects and therefore does not make ETB targets cast-time requirements.
        boolean modalHasBattlefieldOrPlayerTarget = wasModal && card.getSpellTargets().stream()
                .map(SpellTarget::getFilter)
                .anyMatch(filter -> filter != null
                        && !(filter instanceof StackEntryPredicateTargetFilter)
                        && !(filter instanceof GraveyardCardPredicateTargetFilter));
        TargetFilter castTimeTargetFilter = card.getCastTimeTargetFilter();
        modalHasBattlefieldOrPlayerTarget |= wasModal && castTimeTargetFilter != null
                && !(castTimeTargetFilter instanceof StackEntryPredicateTargetFilter)
                && !(castTimeTargetFilter instanceof GraveyardCardPredicateTargetFilter);
        boolean unwrappedNeedsTarget = EffectResolution.needsSpellCastTarget(
                targetingSpellEffects, card.isAura(), card.isEnchantPlayer())
                || modalHasBattlefieldOrPlayerTarget;
        boolean allSpellTargetsAlsoAllowPermanents = targetingSpellEffects.stream()
                .filter(EffectResolution::targetsSpellOnStack)
                .allMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        // Targets multiple distinct spells on the stack. Two shapes qualify:
        //  - a modal mode that declares more than one spell target() slot (Choreographed Sparks' "both":
        //    one instant/sorcery spell + one creature spell);
        //  - a non-modal "counter up to N target spells" (Double Negative): a single spell-on-stack
        //    target group with max > 1 (bound to a CounterEachTargetSpellEffect) and no permanent/player
        //    targets. In both cases the chosen targets ride in the flat targetIds list.
        boolean multipleSpellTargets = unwrappedNeedsSpellTarget && (wasModal
                ? targetId == null && !targetIds.isEmpty() && !allSpellTargetsAlsoAllowPermanents
                : !unwrappedNeedsTarget && card.getMaxTargets() > 1);

        // A "spell or permanent" single-target chooser (e.g. Glamerdye) can target either zone. Infer
        // which one this cast is using from the actual target id so the right validation/entry path runs.
        boolean mixedSpellOrPermanentTarget = unwrappedNeedsSpellTarget && unwrappedNeedsTarget
                && !multipleSpellTargets && targetIds.isEmpty();
        boolean mixedSpellAndPermanentTargets = unwrappedNeedsSpellTarget && unwrappedNeedsTarget
                && !multipleSpellTargets && !targetIds.isEmpty();
        boolean firstDeclaredGroupTargetsSpell = card.getSpellTargets().stream()
                .filter(group -> group.getIndex() == 0)
                .findFirst()
                .map(group -> targetingSpellEffects.stream()
                        .filter(effect -> card.getEffectTargetIndex(effect) == group.getIndex())
                        .anyMatch(EffectResolution::targetsSpellOnStack))
                .orElse(false);
        if (mixedSpellAndPermanentTargets && targetId == null) {
            UUID selectedStackTargetId = targetIds.stream()
                    .filter(candidateId -> targetLegalityService.isSpellOnStack(gameData, candidateId))
                    .findFirst()
                    .orElse(null);
            if (selectedStackTargetId != null) {
                targetId = selectedStackTargetId;
                targetIds = targetIds.stream()
                        .filter(candidateId -> !candidateId.equals(selectedStackTargetId))
                        .toList();
            }
        }
        boolean targetingSpellOnStack = allSpellTargetsAlsoAllowPermanents
                ? targetLegalityService.isSpellOnStack(gameData, targetId)
                : mixedSpellOrPermanentTarget
                ? targetLegalityService.isSpellOnStack(gameData, targetId)
                : mixedSpellAndPermanentTargets
                ? targetLegalityService.isSpellOnStack(gameData, targetId)
                : unwrappedNeedsSpellTarget;
        if (mixedSpellOrPermanentTarget && targetId == null) {
            throw new IllegalStateException("Spell requires a target");
        }

        filteredSpellEffects.stream()
                .filter(ReturnTargetCardsFromGraveyardToHandEffect.class::isInstance)
                .map(ReturnTargetCardsFromGraveyardToHandEffect.class::cast)
                .findFirst()
                .ifPresent(effect -> validateSharedCreatureTypeGraveyardTargets(gameData, playerId, effect));

        // Validate alternate casting cost if used (e.g. Demon of Death's Gate)
        if (usingAlternateCost) {
            if (usingWebSlingingCost) {
                validateWebSlingingCreature(gameData, playerId, alternateCostSacrificePermanentIds);
            } else if (usingBestowCost) {
                // Bestow's mana cost and required Aura target are validated below.
            } else if (usingSharedColorDiscardAlternativeCost) {
                castingCostService.validateSharedColorDiscardAlternativeCost(
                        gameData, playerId, card, sharedColorDiscardHandCardIndex, cardIndex);
            } else if (usingCollectEvidenceAlternativeCost) {
                additionalSpellCostService.validateCollectEvidenceCost(
                        gameData, player, card, collectEvidenceAlternativeCost, exileGraveyardCardIndices);
            } else {
            AlternateHandCast altCast = card.getCastingOption(AlternateHandCast.class)
                    .orElseThrow(() -> new IllegalStateException("Card does not have an alternate casting cost"));

            // Prowl: the alternate cost may only be used if the caster dealt combat damage to a
            // player this turn with a creature of the required subtype.
            if (!altCast.prowlDamageSubtypes().isEmpty()
                    && !castingCostService.prowlConditionMet(gameData, playerId, altCast.prowlDamageSubtypes())) {
                throw new IllegalStateException("Prowl cost is not available — no combat damage dealt with the required creature type this turn");
            }

            // General availability gate (e.g. Qasali Ambusher's "if a creature is attacking you and
            // you control a Forest and a Plains").
            if (altCast.availabilityCondition() != null
                    && !conditionEvaluationService.isMet(gameData, altCast.availabilityCondition(),
                            com.github.laxika.magicalvibes.service.effect.ConditionContext.forCasting(playerId))) {
                throw new IllegalStateException("Alternate casting cost is not available — its condition is not met");
            }

            var sacCost = altCast.getCost(SacrificePermanentsCost.class);
            if (sacCost.isPresent()) {
                if (alternateCostSacrificePermanentIds.size() != sacCost.get().count()) {
                    throw new IllegalStateException("Must sacrifice exactly " + sacCost.get().count() + " permanents");
                }
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                for (UUID sacId : alternateCostSacrificePermanentIds) {
                    Permanent toSacrifice = battlefield.stream()
                            .filter(p -> p.getId().equals(sacId))
                            .findFirst()
                            .orElse(null);
                    if (toSacrifice == null) {
                        throw new IllegalStateException("Sacrifice target not found on your battlefield");
                    }
                    if (!predicateEvaluationService.matchesPermanentPredicate(gameData, toSacrifice, sacCost.get().filter())) {
                        throw new IllegalStateException("Sacrifice target does not match the required filter");
                    }
                }
            }

            var lifeCost = altCast.getCost(LifeCastingCost.class);
            if (lifeCost.isPresent()) {
                int currentLife = gameData.getLife(playerId);
                if (currentLife < lifeCost.get().amount()) {
                    throw new IllegalStateException("Not enough life to pay alternate cost");
                }
            }

            var tapCost = altCast.getCost(TapUntappedPermanentsCost.class);
            if (tapCost.isPresent()) {
                int requiredCount = tapCost.get().count();
                // When sacrifice cost is also present, the IDs are used for sacrifice; tap IDs come separately
                // When only tap cost is present, all IDs are for tapping
                int sacCount = sacCost.map(SacrificePermanentsCost::count).orElse(0);
                int tapIdCount = alternateCostSacrificePermanentIds.size() - sacCount;
                if (tapIdCount != requiredCount) {
                    throw new IllegalStateException("Must tap exactly " + requiredCount + " permanents");
                }
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                List<UUID> tapIds = alternateCostSacrificePermanentIds.subList(sacCount, alternateCostSacrificePermanentIds.size());
                for (UUID tapId : tapIds) {
                    Permanent toTap = battlefield.stream()
                            .filter(p -> p.getId().equals(tapId))
                            .findFirst()
                            .orElse(null);
                    if (toTap == null) {
                        throw new IllegalStateException("Tap target not found on your battlefield");
                    }
                    if (toTap.isTapped()) {
                        throw new IllegalStateException("Permanent is already tapped");
                    }
                    if (!predicateEvaluationService.matchesPermanentPredicate(toTap, tapCost.get().filter(),
                            FilterContext.of(gameData).withSourceControllerId(playerId))) {
                        throw new IllegalStateException("Tap target does not match the required filter");
                    }
                }
            }

            var returnCost = altCast.getCost(ReturnPermanentsCost.class);
            if (returnCost.isPresent()) {
                int requiredCount = returnCost.get().count();
                // Return IDs occupy the tail of the list, after any sacrifice and tap IDs.
                int sacCount = sacCost.map(SacrificePermanentsCost::count).orElse(0);
                int tapCount = tapCost.map(TapUntappedPermanentsCost::count).orElse(0);
                int returnIdCount = alternateCostSacrificePermanentIds.size() - sacCount - tapCount;
                if (returnIdCount != requiredCount) {
                    throw new IllegalStateException("Must return exactly " + requiredCount + " permanents");
                }
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                List<UUID> returnIds = alternateCostSacrificePermanentIds.subList(sacCount + tapCount, alternateCostSacrificePermanentIds.size());
                for (UUID returnId : returnIds) {
                    Permanent toReturn = battlefield == null ? null : battlefield.stream()
                            .filter(p -> p.getId().equals(returnId))
                            .findFirst()
                            .orElse(null);
                    if (toReturn == null) {
                        throw new IllegalStateException("Return target not found on your battlefield");
                    }
                    if (!predicateEvaluationService.matchesPermanentPredicate(gameData, toReturn, returnCost.get().filter())) {
                        throw new IllegalStateException("Return target does not match the required filter");
                    }
                }
            }

            var exileHandCost = altCast.getCost(ExileCardsFromHandCastingCost.class);
            if (exileHandCost.isPresent()) {
                validateExileFromHandAlternateCost(gameData, playerId, card, exileHandCost.get(),
                        discardHandCardIndex, discardHandCardIndices, cardIndex, effectiveXValue);
            }

            validateAlternateDiscardCosts(gameData, playerId, card, alternateDiscardCosts,
                    alternateDiscardHandCardIndex, alternateDiscardHandCardIndices, cardIndex);

            var revealHandCost = altCast.getCost(RevealCardsFromHandCastingCost.class);
            if (revealHandCost.isPresent()) {
                validateRevealFromHandAlternateCost(gameData, playerId, card, revealHandCost.get(),
                        discardHandCardIndex, cardIndex);
            }

            var exileGraveyardCost = altCast.getCost(ExileTopCardsFromGraveyardCastingCost.class);
            if (exileGraveyardCost.isPresent()) {
                findTopMatchingGraveyardCards(gameData, playerId, card, exileGraveyardCost.get());
            }

            var chosenGraveyardExileCost = altCast.getCost(ExileCardFromGraveyardCastingCost.class);
            if (chosenGraveyardExileCost.isPresent()) {
                validateChosenGraveyardExileCost(gameData, playerId, card,
                        chosenGraveyardExileCost.get(), exileGraveyardCardIndex);
            }

            var manaCost = altCast.getCost(ManaCastingCost.class);
            if (manaCost.isPresent()) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                ManaCost cost = castingCostService.applyColoredManaCostReductions(
                        gameData, playerId, card, new ManaCost(manaCost.get().manaCost()));
                if (cost.hasX() && effectiveXValue < 0) {
                    throw new IllegalStateException("X value cannot be negative");
                }
                ManaCost sacrificedManaCost = computeSacrificedManaCost(
                        gameData, altCast, alternateCostSacrificePermanentIds);
                ManaPool.NoncreatureSpellManaState noncreatureMana = !card.hasType(CardType.CREATURE)
                        ? pool.promoteNoncreatureSpellOnlyMana()
                        : null;
                ManaPool.MulticoloredSpellManaState multicoloredMana =
                        gameQueryService.getEffectiveCardColors(gameData, card).size() >= 2
                                ? pool.promoteMulticoloredSpellOnlyMana() : null;
                int faceDownCostReduction = card.getMorphCost() != null
                        ? castingCostService.getCastCostModifierForFaceDownSpell(gameData, playerId, card) : 0;
                ManaPool.FaceDownSpellsOrTurnFaceUpManaState restrictedMana = card.getMorphCost() != null
                        ? pool.promoteFaceDownSpellsOrTurnFaceUpMana() : null;
                boolean canPay;
                try {
                    canPay = altCast.reduceManaBySacrificedManaCost()
                            ? cost.canPayAfterReduction(pool, sacrificedManaCost)
                            : cost.canPay(pool, alternateCostXArgument(cost, effectiveXValue,
                                    computeEmergeManaReduction(gameData, altCast, alternateCostSacrificePermanentIds))
                                    + faceDownCostReduction);
                } finally {
                    if (noncreatureMana != null) {
                        pool.restorePromotedNoncreatureSpellOnlyMana(noncreatureMana);
                    }
                    if (restrictedMana != null) {
                        pool.restorePromotedFaceDownSpellsOrTurnFaceUpMana(restrictedMana);
                    }
                    if (multicoloredMana != null) {
                        pool.restorePromotedMulticoloredSpellOnlyMana(multicoloredMana);
                    }
                }
                if (!canPay) {
                    throw new IllegalStateException("Not enough mana to pay alternate casting cost");
                }
            }
            }
        }

        // Compute targeting tax from effects like Kopala, Warden of Waves and Kaervek's Torch
        int targetingTax = castingCostService.getTargetingSpellCostModifier(gameData, playerId, card, targetId, targetIds)
                + castingCostService.getTargetingStackEntryTax(gameData, targetId, targetIds);
        int targetingLifeTax = castingCostService.getTargetingLifeTax(gameData, playerId, targetId, targetIds);
        if (targetingLifeTax > 0 && targetingLifeTax > gameData.getLife(playerId)) {
            throw new IllegalStateException("Not enough life to pay the targeting life cost");
        }
        int selectedDelveReduction = additionalSpellCostService.delveReduction(
                additionalCosts, costSelection.exileGraveyardCardIndices());

        if (usingBestowCost) {
            validateBestowManaCost(gameData, playerId, card, targetingTax);
        }
        if (usingWebSlingingCost) {
            validateWebSlingingManaCost(gameData, playerId, card, webSlingingEffect,
                    targetingTax, hasXCost ? 0 : perTargetCost,
                    perTargetManaCost + escalateManaSuffix);
        }

        if (!usingAlternateCost && !castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, card)) {
            // Check if a non-zero alternative cost from the battlefield is affordable (e.g. Jodah)
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int additionalCost = castingCostService.getCastCostModifier(
                    gameData, playerId, card, effectiveXValue, null, collectEvidenceCostPaid)
                    + targetingTax + (hasXCost ? 0 : perTargetCost);
            boolean usingBattlefieldAlternativeCost = false;
            ManaPool.NoncreatureSpellManaState noncreatureMana = !card.hasType(CardType.CREATURE)
                    ? pool.promoteNoncreatureSpellOnlyMana()
                    : null;
            ManaPool.MulticoloredSpellManaState multicoloredMana =
                    gameQueryService.getEffectiveCardColors(gameData, card).size() >= 2
                            ? pool.promoteMulticoloredSpellOnlyMana() : null;
            try {
                String manaCostString = card.getManaCost() != null
                        ? card.getManaCost() + perTargetManaCost + escalateManaSuffix : escalateManaSuffix;
                if (!manaCostString.isEmpty()) {
                    ManaCost normalCost = castingCostService.applyColoredManaCostReductions(
                            gameData, playerId, card, new ManaCost(manaCostString));
                    boolean normalCostPayable = normalCost.hasX()
                            ? normalCost.canPayWithAdditionalGenericCost(
                                    pool, effectiveXValue, additionalCost - selectedDelveReduction)
                            : normalCost.canPayWithAdditionalGenericCost(pool, 0, additionalCost);
                    if (!normalCostPayable) {
                        usingBattlefieldAlternativeCost = castingCostService.canAffordAlternativeCostFromBattlefield(
                                gameData, playerId, card, pool, additionalCost);
                    }
                }

                if (!usingBattlefieldAlternativeCost) {
                // For X-cost spells, validate that player can pay colored + generic + xValue + any cost increases
                if (card.getManaCost() != null) {
                    ManaCost cost = castingCostService.applyColoredManaCostReductions(
                            gameData, playerId, card, new ManaCost(card.getManaCost() + escalateManaSuffix));
                    if (cost.hasX()) {
                        if (effectiveXValue < 0) {
                            throw new IllegalStateException("X value cannot be negative");
                        }
                        int totalAdditionalCost = additionalCost + perTargetCost;
                        ManaRestrictionFlags flags = computeManaRestrictionFlags(gameData, playerId, card, kicked);
                        List<ManaColor> plannedConvoke = planConvokeContributions(gameData, playerId, card, convokeCreatureIds);
                        if (!plannedConvoke.isEmpty()) {
                            // Convoke on an {X} spell (Chord of Calling): tapped creatures pay part of
                            // the colored cost and of X, so the plain pool check would reject it.
                            if (!cost.canPayWithConvoke(pool, effectiveXValue + totalAdditionalCost, plannedConvoke)) {
                                throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                            }
                        } else if (card.hasXColorRestriction()) {
                            if (!cost.canPay(pool, effectiveXValue, card.getXColorRestrictions(),
                                    totalAdditionalCost - selectedDelveReduction)) {
                                throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                            }
                        } else if (flags.hasRestricted()) {
                            if (!cost.canPayWithAdditionalGenericCost(pool, effectiveXValue,
                                    totalAdditionalCost - selectedDelveReduction,
                                    flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext(),
                                    flags.kickedOnlyGreen(), flags.instantSorceryOnlyColorless(),
                                    flags.subtypeCreatureContext(), flags.subtypeSpellOrAbilityContext(),
                                    flags.creatureSpellOnly(), false, flags.legendarySpellOnly(),
                                    flags.manaValueAtLeastFour(),
                                    flags.subtypeOrPlaneswalkerSpellContext(),
                                    flags.subtypeCreatureSourceSpellOrAbilityContext(), false,
                                    flags.subtypeSpellOnlyContext())) {
                                throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                            }
                        } else if (!cost.canPayWithAdditionalGenericCost(
                                pool, effectiveXValue, totalAdditionalCost - selectedDelveReduction)) {
                            throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                        }
                    } else if (!escalateManaSuffix.isEmpty()
                            && !cost.canPayWithAdditionalGenericCost(pool, 0, additionalCost)) {
                        throw new IllegalStateException("Not enough mana to pay escalate cost");
                    }
                }

                // Validate creature-only mana restriction (e.g. Myr Superion)
                if (card.isRequiresCreatureMana()) {
                    ManaCost creatureCost = castingCostService.applyColoredManaCostReductions(
                            gameData, playerId, card, new ManaCost(card.getManaCost() + escalateManaSuffix));
                    int additionalCostForCreature = castingCostService.getCastCostModifier(
                            gameData, playerId, card, effectiveXValue, null, collectEvidenceCostPaid);
                    if (!creatureCost.canPayCreatureOnly(pool, additionalCostForCreature)) {
                        throw new IllegalStateException("Can only spend mana produced by creatures to cast this spell");
                    }
                }
                if (card.isRequiresBasicLandMana()) {
                    ManaCost basicLandCost = castingCostService.applyColoredManaCostReductions(
                            gameData, playerId, card, new ManaCost(card.getManaCost() + escalateManaSuffix));
                    int additionalCostForBasicLand = castingCostService.getCastCostModifier(
                            gameData, playerId, card, effectiveXValue);
                    if (!basicLandCost.canPayBasicLandOnly(pool, effectiveXValue, additionalCostForBasicLand)) {
                        throw new IllegalStateException("Can only spend mana produced by basic lands to cast this spell");
                    }
                }
                }
            } finally {
                if (noncreatureMana != null) {
                    pool.restorePromotedNoncreatureSpellOnlyMana(noncreatureMana);
                }
                if (multicoloredMana != null) {
                    pool.restorePromotedMulticoloredSpellOnlyMana(multicoloredMana);
                }
            }
        } else if (!usingAlternateCost && !escalateManaSuffix.isEmpty()) {
            // Free cast from battlefield (e.g. As Foretold): mana cost is waived, but escalate is still paid.
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int additionalCost = castingCostService.getCastCostModifier(
                    gameData, playerId, card, effectiveXValue, null, collectEvidenceCostPaid)
                    + targetingTax;
            ManaCost escalateOnly = new ManaCost(escalateManaSuffix);
            withMulticoloredSpellMana(gameData, playerId, card, () -> {
                if (!escalateOnly.canPayWithAdditionalGenericCost(pool, 0, additionalCost)) {
                    throw new IllegalStateException("Not enough mana to pay escalate cost");
                }
                return null;
            });
        }

        // Validate spell target (targeting a spell on the stack)
        if (unwrappedNeedsSpellTarget && targetingSpellOnStack) {
            if (multipleSpellTargets) {
                targetLegalityService.validateMultiSpellTargetsOnStack(
                        gameData, card, targetIds, playerId, kicked, giftPromised);
            } else {
                targetLegalityService.validateSpellTargetOnStack(
                        gameData, targetId, card.getTargetFilter(), playerId, effectiveXValue, kicked,
                        giftPromised);
            }
        }

        // For modal spells, graveyard-targeting is determined by the chosen mode's unwrapped effects
        // (the raw SPELL slot holds only the ChooseOneEffect, which reports no graveyard targeting).
        List<CardEffect> graveyardTargetingSource = EffectResolution
                .expandConditionalTargetingEffects(targetingSpellEffects);

        ReturnCardFromGraveyardEffect graveyardReturnEffect = (ReturnCardFromGraveyardEffect) graveyardTargetingSource.stream()
                .map(SpellCastingService::unwrapConditional)
                .filter(e -> e instanceof ReturnCardFromGraveyardEffect returnEffect
                        && returnEffect.targetGraveyard())
                .findFirst().orElse(null);
        boolean needsSingleGraveyardTargeting = graveyardReturnEffect != null;

        // Detect any effect that targets a graveyard card (e.g. PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect)
        boolean needsGraveyardEffectTargeting = !needsSingleGraveyardTargeting
                && graveyardTargetingSource.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        boolean needsImmediateGraveyardEffectTargeting = graveyardTargetingSource.stream()
                .filter(e -> !(e instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect)
                        && !(e instanceof ReturnUpToOneOfEachFilterFromGraveyardToHandEffect)
                        && !(e instanceof ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect))
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        Set<GraveyardSearchScope> graveyardScopes = graveyardTargetingSource.stream()
                .flatMap(e -> e.targetSpec().graveyardScope().stream())
                .collect(java.util.stream.Collectors.toSet());
        boolean canTargetAnyGraveyard = graveyardScopes.contains(GraveyardSearchScope.ALL_GRAVEYARDS);
        boolean targetsControllersGraveyardOnly =
                graveyardScopes.contains(GraveyardSearchScope.CONTROLLERS_GRAVEYARD);

        // Detect effects that target cards in exile (e.g. Runic Repetition and Darkpact).
        CardEffect exileTargetingEffect = targetingSpellEffects.stream()
                .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD))
                .findFirst().orElse(null);
        ReturnTargetCardFromExileToHandEffect exileReturnEffect =
                exileTargetingEffect instanceof ReturnTargetCardFromExileToHandEffect returnEffect
                        ? returnEffect : null;
        ReturnTargetCardFromGraveyardOrExileToHandEffect graveyardOrExileReturnEffect =
                graveyardTargetingSource.stream()
                        .filter(e -> e instanceof ReturnTargetCardFromGraveyardOrExileToHandEffect)
                        .map(e -> (ReturnTargetCardFromGraveyardOrExileToHandEffect) e)
                        .findFirst().orElse(null);
        boolean needsGraveyardOrExileTargeting = graveyardOrExileReturnEffect != null;
        Zone graveyardOrExileTargetZone = null;
        boolean needsExileTargeting = exileTargetingEffect != null;
        boolean targetIsExiledCard = targetId != null && gameData.findExiledCard(targetId) != null;
        boolean hasGraveyardTargetInTargetIds = targetIds.stream().anyMatch(candidateId ->
                gameData.orderedPlayerIds.stream()
                        .flatMap(ownerId -> gameData.playerGraveyards.getOrDefault(ownerId, List.of()).stream())
                        .anyMatch(graveyardCard -> graveyardCard.getId().equals(candidateId)));
        boolean hasExileTargetInTargetIds = targetIds.stream().anyMatch(candidateId ->
                gameData.orderedPlayerIds.stream()
                        .flatMap(ownerId -> gameData.getPlayerExiledCards(ownerId).stream())
                        .anyMatch(exiledCard -> exiledCard.getId().equals(candidateId)));

        if (targetId == null && targetIds.isEmpty()
                && unwrappedNeedsTarget && !unwrappedNeedsSpellTarget
                && (card.getMaxTargets() == 0
                || card.getEffectiveMinTargets(effectiveXValue, kicked, giftPromised) > 0)
                && !EffectResolution.needsDamageDistribution(targetingSpellEffects)
                && !(kicked && findKickedDividedDamageEffect(filteredSpellEffects) != null)
                && !needsExileTargeting && !needsGraveyardOrExileTargeting
                && !needsSingleGraveyardTargeting && !needsGraveyardEffectTargeting
                && !isOptionalModalEtbSkip(card, effectiveXValue)) {
            throw new IllegalStateException("Spell requires a target");
        }

        UUID validatedTargetId = targetId;

        // Validate target if specified (can be a permanent or a player)
        if (targetId != null && !targetingSpellOnStack) {
            if (needsGraveyardOrExileTargeting) {
                graveyardOrExileTargetZone = findOwnedGraveyardOrExileTargetZone(gameData, playerId, targetId);
                if (graveyardOrExileTargetZone == null) {
                    throw new IllegalStateException("Target must be an instant or sorcery in your graveyard or an exiled card with flashback you own");
                }
                targetLegalityService.validateEffectTargetInZone(
                        gameData, card, targetId, graveyardOrExileTargetZone);
            } else if (needsExileTargeting && targetIsExiledCard) {
                if (exileReturnEffect != null && exileReturnEffect.ownedOnly()) {
                    boolean inControllersExile = gameData.getPlayerExiledCards(playerId)
                            .stream()
                            .anyMatch(c -> c.getId().equals(validatedTargetId));
                    if (!inControllersExile) {
                        throw new IllegalStateException("Target must be an exiled card you own");
                    }
                }
                targetLegalityService.validateEffectTargetInZone(gameData, card, targetingSpellEffects,
                        targetId, Zone.EXILE, effectiveXValue, playerId);
            } else if (needsSingleGraveyardTargeting) {
                String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                if (graveyardReturnEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
                    boolean inControllersGraveyard = gameData.playerGraveyards
                            .getOrDefault(playerId, List.of())
                            .stream()
                            .anyMatch(c -> c.getId().equals(validatedTargetId));
                    if (!inControllersGraveyard) {
                        throw new IllegalStateException("Target must be a " + filterLabel + " in your graveyard");
                    }
                } else if (graveyardReturnEffect.source() == GraveyardSearchScope.OPPONENT_GRAVEYARD) {
                    boolean inControllersGraveyard = gameData.playerGraveyards
                            .getOrDefault(playerId, List.of())
                            .stream()
                            .anyMatch(c -> c.getId().equals(validatedTargetId));
                    if (inControllersGraveyard) {
                        throw new IllegalStateException("Target must be in an opponent's graveyard");
                    }
                }
                if (card.getMaxTargets() > 0) {
                    // Mixed graveyard + permanent targeting: validate only graveyard effects
                    // (use the modal-unwrapped effect list + paid X so MV≤X / MV=X gates fire)
                    targetLegalityService.validateGraveyardEffectTargetOnly(
                            gameData, card, graveyardTargetingSource, targetId, effectiveXValue, playerId);
                } else {
                    targetLegalityService.validateEffectTargetInZone(gameData, card, graveyardTargetingSource,
                            targetId, Zone.GRAVEYARD, effectiveXValue, playerId);
                }
            } else if (needsGraveyardEffectTargeting) {
                boolean inControllersGraveyard = gameData.playerGraveyards
                        .getOrDefault(playerId, List.of())
                        .stream()
                        .anyMatch(c -> c.getId().equals(validatedTargetId));
                if (targetsControllersGraveyardOnly) {
                    if (!inControllersGraveyard) {
                        throw new IllegalStateException("Target must be in your graveyard");
                    }
                } else if (!canTargetAnyGraveyard && inControllersGraveyard) {
                    throw new IllegalStateException("Target must be in an opponent's graveyard");
                }
                if (card.getMaxTargets() > 0) {
                    // Mixed graveyard + permanent targeting: validate only graveyard effects
                    targetLegalityService.validateGraveyardEffectTargetOnly(
                            gameData, card, graveyardTargetingSource, targetId, effectiveXValue, playerId);
                } else {
                    targetLegalityService.validateEffectTargetInZone(gameData, card, graveyardTargetingSource,
                            targetId, Zone.GRAVEYARD, effectiveXValue, playerId);
                }
            } else {
                validateModalTargetKind(gameData, wasModal, card, filteredSpellEffects, targetId);
                int primaryTargetPosition = mixedSpellAndPermanentTargets ? 0 : targetIds.size();
                List<CardEffect> primaryTargetEffects = effectsForTargetPosition(
                        card, targetingSpellEffects, primaryTargetPosition);
                targetLegalityService.validateSpellTargeting(gameData, card, primaryTargetEffects,
                        targetId, null, playerId, unwrappedNeedsTarget, effectiveXValue, kicked,
                        giftPromised);
            }
        } else if (unwrappedNeedsTarget && needsGraveyardOrExileTargeting) {
            throw new IllegalStateException("Must target an instant or sorcery in your graveyard or an exiled card with flashback you own");
        } else if (unwrappedNeedsTarget && needsExileTargeting && !hasExileTargetInTargetIds) {
            if (exileReturnEffect != null) {
                String exileFilterLabel = CardPredicateUtils.describeFilter(exileReturnEffect.filter());
                throw new IllegalStateException(exileReturnEffect.ownedOnly()
                        ? "Must target an exiled " + exileFilterLabel + " you own"
                        : "Must target an exiled " + exileFilterLabel);
            }
            throw new IllegalStateException("Must target a card in exile");
        } else if (unwrappedNeedsTarget && needsSingleGraveyardTargeting && !hasGraveyardTargetInTargetIds) {
            // "Up to one" graveyard targets (Yawgmoth) may be omitted; mandatory ones (Crawl) may not,
            // even when the spell also has optional permanent target groups.
            if (!graveyardReturnEffect.upTo()) {
                String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                throw new IllegalStateException("Must target a " + filterLabel + " in your graveyard");
            }
        } else if (unwrappedNeedsTarget && needsImmediateGraveyardEffectTargeting
                && !hasGraveyardTargetInTargetIds) {
            // Non-ReturnCard graveyard targets with no permanent groups still require a target.
            if (card.getMaxTargets() == 0) {
                throw new IllegalStateException("Must target a card in a graveyard");
            }
        }

        if (wasModal && card.getCastTimeTargetFilter() != null
                && targetId == null && !targetIds.isEmpty()) {
            if (targetIds.size() != 1) {
                throw new IllegalStateException("The chosen mode requires exactly one target");
            }
            targetLegalityService.validateSpellTargeting(
                    gameData, card, targetingSpellEffects, targetIds.getFirst(), null,
                    playerId, true, effectiveXValue, kicked, giftPromised);
        }

        // Validate multi-target permanent targeting (skip when the targets are spells on the stack)
        if (kicked && targetId != null && card.getSpellTargets().size() > 1
                && !multipleSpellTargets) {
            if (!card.isAllowSharedTargets() && targetIds.contains(targetId)) {
                throw new IllegalStateException("All targets must be different");
            }
            targetLegalityService.validateSpellTargetGroupsAfterPrimary(
                    gameData, card, targetIds, playerId, effectiveXValue, true);
        } else if (card.getMaxTargets() > 0 && !targetIds.isEmpty() && !multipleSpellTargets) {
            if (!dividedDamageTargetGroupSizes.isEmpty()) {
                targetLegalityService.validateMultiSpellTargets(
                        gameData, card, targetIds, playerId, effectiveXValue, kicked,
                        targetingSpellEffects, dividedDamageTargetGroupSizes);
            } else if (mixedSpellAndPermanentTargets) {
                targetLegalityService.validateMixedSpellAndPermanentTargets(
                        gameData, card, targetIds, playerId, effectiveXValue, giftPromised,
                        targetingSpellEffects);
            } else {
                targetLegalityService.validateMultiSpellTargets(
                        gameData, card, targetIds, playerId, effectiveXValue, kicked, giftPromised,
                        targetingSpellEffects);
            }
        }

        // Validate permanent targets for spells that also target a spell on the stack (e.g. Lost in the Mist)
        if (unwrappedNeedsSpellTarget && unwrappedNeedsTarget && !targetIds.isEmpty()
                && !mixedSpellAndPermanentTargets) {
            for (UUID permTargetId : targetIds) {
                targetLegalityService.validateSpellTargeting(
                        gameData, card, permTargetId, null, playerId, true, 0, false, giftPromised);
            }
        }

        targetLegalityService.validateFlagbearerTargetChoiceForSpellCast(
                gameData, card, targetingSpellEffects, targetId, targetIds, playerId, effectiveXValue, kicked);

        // Validate and apply convoke or improvise
        List<ManaColor> convokeContributions = List.of();
        boolean hasConvoke = card.getKeywords().contains(Keyword.CONVOKE)
                || hasSpellCastingAbilityGrantForCard(gameData, playerId, card, Keyword.CONVOKE);
        boolean hasImprovise = card.getKeywords().contains(Keyword.IMPROVISE)
                || hasSpellCastingAbilityGrantForCard(gameData, playerId, card, Keyword.IMPROVISE);
        if (!convokeCreatureIds.isEmpty() && (hasConvoke || hasImprovise)) {
            List<ManaColor> contributions = collectConvokeContributions(
                    gameData, playerId, convokeCreatureIds, hasConvoke, hasImprovise);
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            List<Permanent> validatedSources = convokeCreatureIds.stream()
                    .map(creatureId -> battlefield.stream().filter(p -> p.getId().equals(creatureId)).findFirst().orElseThrow())
                    .toList();
            // Tap all casting-assistance sources after validation to ensure atomic failure.
            // CR 603.2 + 603.3: any "whenever enchanted permanent becomes tapped" triggers
            // are deferred so they don't sit on the stack mid-cast; finishSpellCast()
            // flushes pendingManaAbilityTriggers onto the stack above the spell.
            int stackBeforeTriggers = gameData.stack.size();
            triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
            try {
                for (Permanent source : validatedSources) {
                    source.tap();
                    triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, source);
                }
            } finally {
                triggerCollectionService.endPermanentTapTriggerBatch(gameData);
            }
            if (gameData.stack.size() > stackBeforeTriggers) {
                List<StackEntry> deferred = new ArrayList<>(
                        gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()));
                gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()).clear();
                gameData.pendingManaAbilityTriggers.addAll(deferred);
            }
            convokeContributions = contributions;
        }

        // Validate and apply conspire (CR 702.78): tap two untapped creatures you control that each
        // share a color with this spell. Conspire is an additional cost, so the normal mana cost is
        // still paid below. Paying it flags the spell (gameData.conspiredSpellIds) so that a single
        // "copy it, you may choose a new target for the copy" trigger is queued above the spell when
        // spell-cast triggers are collected in finishSpellCast().
        if (!conspireCreatureIds.isEmpty()) {
            if (!card.getKeywords().contains(Keyword.CONSPIRE)
                    && !hasSpellCastingAbilityGrantForCard(gameData, playerId, card, Keyword.CONSPIRE)) {
                throw new IllegalStateException(card.getName() + " doesn't have conspire");
            }
            if (conspireCreatureIds.size() != 2 || conspireCreatureIds.get(0).equals(conspireCreatureIds.get(1))) {
                throw new IllegalStateException("Conspire requires two different untapped creatures you control");
            }
            List<CardColor> spellColors = card.getColors();
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            List<Permanent> conspireCreatures = new ArrayList<>();
            for (UUID creatureId : conspireCreatureIds) {
                Permanent creature = battlefield.stream()
                        .filter(p -> p.getId().equals(creatureId))
                        .findFirst()
                        .orElse(null);
                if (creature == null) {
                    throw new IllegalStateException("Conspire creature not found on your battlefield");
                }
                if (!gameQueryService.isCreature(gameData, creature)) {
                    throw new IllegalStateException(creature.getCard().getName() + " is not a creature");
                }
                if (creature.isTapped()) {
                    throw new IllegalStateException(creature.getCard().getName() + " is already tapped");
                }
                if (spellColors != null && !spellColors.isEmpty()) {
                    Set<CardColor> creatureColors = gameQueryService.getEffectiveColors(gameData, creature);
                    if (spellColors.stream().noneMatch(creatureColors::contains)) {
                        throw new IllegalStateException(creature.getCard().getName()
                                + " does not share a color with " + card.getName());
                    }
                }
                conspireCreatures.add(creature);
            }
            // Tap after validation for atomic failure; defer any tap triggers (see convoke above).
            int stackBeforeTriggers = gameData.stack.size();
            triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
            try {
                for (Permanent creature : conspireCreatures) {
                    creature.tap();
                    triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, creature);
                }
            } finally {
                triggerCollectionService.endPermanentTapTriggerBatch(gameData);
            }
            if (gameData.stack.size() > stackBeforeTriggers) {
                List<StackEntry> deferred = new ArrayList<>(
                        gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()));
                gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()).clear();
                gameData.pendingManaAbilityTriggers.addAll(deferred);
            }
            gameData.conspiredSpellIds.add(card.getId());
        }

        // Validate graveyard targets for spells that target creature cards in graveyard
        ExileCreaturesFromGraveyardAndCreateTokensEffect graveyardCreatureTargetingEffect =
                card.getEffects(EffectSlot.SPELL).stream()
                        .filter(ExileCreaturesFromGraveyardAndCreateTokensEffect.class::isInstance)
                        .map(ExileCreaturesFromGraveyardAndCreateTokensEffect.class::cast)
                        .findFirst().orElse(null);
        boolean needsGraveyardCreatureTargeting = graveyardCreatureTargetingEffect != null;
        ReturnTargetCardsFromGraveyardToBattlefieldEffect returnToBattlefieldEffect =
                filteredSpellEffects.stream()
                        .filter(ReturnTargetCardsFromGraveyardToBattlefieldEffect.class::isInstance)
                        .map(ReturnTargetCardsFromGraveyardToBattlefieldEffect.class::cast)
                        .findFirst().orElse(null);
        SacrificePermanentAndReturnTargetCardsFromGraveyardEffect sacrificeAndReturnEffect =
                filteredSpellEffects.stream()
                        .filter(SacrificePermanentAndReturnTargetCardsFromGraveyardEffect.class::isInstance)
                        .map(SacrificePermanentAndReturnTargetCardsFromGraveyardEffect.class::cast)
                        .findFirst().orElse(null);
        if (needsGraveyardCreatureTargeting && effectiveXValue > 0) {
            long creatureCount = graveyardCreatureTargetingEffect.graveyardScope()
                    .graveyardOwners(gameData.orderedPlayerIds, playerId).stream()
                    .flatMap(ownerId -> gameData.playerGraveyards.getOrDefault(ownerId, List.of()).stream())
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .count();
            if (effectiveXValue > creatureCount) {
                throw new IllegalStateException("Not enough creature cards in graveyard (need " + effectiveXValue + ", have " + creatureCount + ")");
            }
        }
        if (returnToBattlefieldEffect != null && returnToBattlefieldEffect.xScaled() && effectiveXValue > 0) {
            Set<UUID> trackedIds = returnToBattlefieldEffect.fromBattlefieldThisTurn()
                    ? gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(playerId, Set.of())
                    : null;
            long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                    .filter(c -> trackedIds == null || trackedIds.contains(c.getId()))
                    .filter(c -> predicateEvaluationService.matchesCardPredicate(
                            c, returnToBattlefieldEffect.filter(), card.getId()))
                    .count();
            if (effectiveXValue > matchingCount) {
                throw new IllegalStateException("Not enough matching creature cards in graveyard (need "
                        + effectiveXValue + ", have " + matchingCount + ")");
            }
        }
        if (sacrificeAndReturnEffect != null) {
            long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                    .filter(c -> predicateEvaluationService.matchesCardPredicate(
                            c, sacrificeAndReturnEffect.returnFilter(), card.getId()))
                    .count();
            if (sacrificeAndReturnEffect.targetCount() > matchingCount) {
                throw new IllegalStateException("Not enough matching creature cards in graveyard (need "
                        + sacrificeAndReturnEffect.targetCount() + ", have " + matchingCount + ")");
            }
        }
        ReturnTargetCardsFromGraveyardToHandEffect xScaledToHandEffect =
                card.getEffects(EffectSlot.SPELL).stream()
                        .filter(ReturnTargetCardsFromGraveyardToHandEffect.class::isInstance)
                        .map(ReturnTargetCardsFromGraveyardToHandEffect.class::cast)
                        .filter(ReturnTargetCardsFromGraveyardToHandEffect::xScaled)
                        .findFirst().orElse(null);
        if (xScaledToHandEffect != null && effectiveXValue > 0) {
            long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                    .filter(c -> predicateEvaluationService.matchesCardPredicate(
                            c, xScaledToHandEffect.filter(), card.getId()))
                    .count();
            if (effectiveXValue > matchingCount) {
                throw new IllegalStateException("Not enough matching cards in graveyard (need "
                        + effectiveXValue + ", have " + matchingCount + ")");
            }
        }
        ReturnTargetCardsFromGraveyardToHandEffect exactToHandEffect =
                card.getEffects(EffectSlot.SPELL).stream()
                        .filter(ReturnTargetCardsFromGraveyardToHandEffect.class::isInstance)
                        .map(ReturnTargetCardsFromGraveyardToHandEffect.class::cast)
                        .filter(ReturnTargetCardsFromGraveyardToHandEffect::exactTargets)
                        .findFirst().orElse(null);
        if (exactToHandEffect != null) {
            long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                    .filter(c -> predicateEvaluationService.matchesCardPredicate(
                            c, exactToHandEffect.filter(), card.getId()))
                    .count();
            if (exactToHandEffect.maxTargets() > matchingCount) {
                throw new IllegalStateException("Not enough matching cards in graveyard (need "
                        + exactToHandEffect.maxTargets() + ", have " + matchingCount + ")");
            }
        }

        if (usingSharedColorDiscardAlternativeCost) {
            validateSharedColorDiscardDoesNotOverlapAdditionalCosts(
                    additionalCosts, costSelection, sharedColorDiscardHandCardIndex, card);
        }
        BuybackEffect buybackEffect = findBuybackEffect(card);
        if (buyback && buybackEffect != null && buybackEffect.hasLifeCost()) {
            additionalSpellCostService.validatePayLifeCost(gameData, player, card, buybackEffect.lifeCost());
        }
        hand.remove(cardIndex);
        int stackBeforeCastingCosts = gameData.stack.size();
        if (buyback && buybackEffect != null && buybackEffect.hasDiscardCost()) {
            if (buybackEffect.hasRandomDiscardCost()) {
                if (discardHandCardIndices != null && !discardHandCardIndices.isEmpty()) {
                    throw new IllegalStateException("Random buyback discard does not accept a card choice");
                }
                if (hand.size() < buybackEffect.discardCount()) {
                    throw new IllegalStateException("Must discard a card at random to pay buyback cost");
                }
            } else {
                additionalSpellCostService.validateDiscardXCardsCost(
                        gameData, player, card, new DiscardXCardsCost(), buybackEffect.discardCount(),
                        discardHandCardIndices, cardIndex);
            }
        }
        AdditionalSpellCostService.CostSelection paymentCostSelection = usingSharedColorDiscardAlternativeCost
                ? adjustCostSelectionAfterSharedColorDiscard(costSelection, sharedColorDiscardHandCardIndex)
                : costSelection;
        Card castCharacteristics = castModalBackFace ? card.getBackFaceCard() : card;

        if (card.hasType(CardType.LAND) && !castingFaceDown) {
            // Lands bypass the stack — go directly onto battlefield
            Card landFace = selectedModalDoubleFacedLandFace(card, effectiveXValue);
            if (landCopyOnEnterService.prepare(gameData, playerId, card, landFace,
                    true, false, null, Zone.HAND)) {
                return;
            }
            Permanent permanent = new Permanent(card);
            permanent.setCard(landFace);
            gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);
            gameLogService.append(gameData, GameLog.playerPlays(player.getUsername(), landFace));
            log.info("Game {} - {} plays {}", gameData.id, player.getUsername(), landFace.getName());
            if (cloneService.prepareCloneReplacementEffect(gameData, playerId, landFace, null, effectiveXValue, true)) {
                return;
            }
            battlefieldEntryService.putLandOntoBattlefield(gameData, playerId, permanent, Zone.HAND);

            // A land whose entry is replaced by "sacrifice an untapped [land] instead" (Balduvian
            // Trading Post) parks the entry on a permanent choice; ETB effects and auto-pass must
            // wait until that choice resumes the entry.
            if (!gameData.interaction.isAwaitingInput()) {
                // Process ETB effects for lands (e.g. Glimmerpost)
                battlefieldEntryService.processLandETBEffects(gameData, playerId, landFace);
                if (!gameData.interaction.isAwaitingInput()) {
                    triggerCollectionService.checkControllerPlaysLandTriggers(gameData, playerId, landFace);
                    turnProgressionService.resolveAutoPass(gameData);
                }
            }
        } else if (castingFaceDown
                || castCharacteristics.hasType(CardType.CREATURE)
                || castCharacteristics.hasType(CardType.ENCHANTMENT)
                || gameQueryService.cardHasType(castCharacteristics, CardType.ARTIFACT, gameData, playerId)
                || castCharacteristics.hasType(CardType.PLANESWALKER)
                || castCharacteristics.hasType(CardType.BATTLE)) {
            // Permanent spells: pay mana (or alternate cost), put on stack, finish.
            // {X} costs (hydras, Meathook Massacre, Chimeric Mass, Nissa, …): pay and snapshot X
            // onto the stack entry. Non-X permanents ignore manaCostX in the pay path; modal casts
            // reuse the same wire field as mode index (snapshotted for ETB ChooseOne unwrap).
            int manaCostX = effectiveXValue;
            int stackX = effectiveXValue;
            UUID stackTarget = castCharacteristics.hasType(CardType.PLANESWALKER) ? null : targetId;
            Card stackCard = castModalBackFace ? castCharacteristics : card;
            StackEntryType permanentEntryType = castingFaceDown
                    ? StackEntryType.CREATURE_SPELL
                    : cardTypeToStackEntryType(castCharacteristics.getType());
            List<UUID> costReductionTargetIds = !targetIds.isEmpty() ? targetIds
                    : (targetId != null ? List.of(targetId) : List.of());
            int targetSubtypeCostReduction = castingCostService.computeTargetBasedCostReduction(
                    gameData, playerId, card, costReductionTargetIds);

            // Validate the chosen target when playability was admitted because another legal
            // target could use the reduction.
            if (targetSubtypeCostReduction == 0 && !usingAlternateCost
                    && castingCostService.hasTargetBasedCastCostReduction(card)) {
                ManaCost validationCost = castingCostService.applyColoredManaCostReductions(
                        gameData, playerId, card, new ManaCost(card.getManaCost()));
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int costModifier = castingCostService.getCastCostModifier(gameData, playerId, card, effectiveXValue);
                if (!validationCost.canPay(pool, costModifier)) {
                    throw new IllegalStateException("Not enough mana — target does not qualify for cost reduction");
                }
            }

            // CR 601.2h: a cast either completes or leaves the game state untouched — reject an
            // unpayable non-mana additional cost up front, before any cost is consumed. A throw
            // later in the pay chain would keep the mana (and costs) already paid.
            KickerEffect kickerEffect = findKickerEffect(card);
            int collectEvidenceMinimumManaValue = additionalSpellCostService
                    .resolveCollectEvidenceMinimumManaValue(
                            gameData, additionalCosts.collectEvidenceCost(), targetId, targetIds);
            validateCardFlashAdditionalCost(gameData, player, card, additionalCosts, costSelection);
            boolean waterbendCostPaid = isWaterbendCostPaid(additionalCosts, waterbendPaid);
            additionalSpellCostService.validateAll(gameData, player, card, additionalCosts, costSelection,
                    additionalCosts.tieredManaCost() != null ? modeEncoding : effectiveXValue,
                    waterbendCostPaid, collectEvidenceMinimumManaValue);
            validateImposedSacrificeTax(gameData, player, card, imposedSacrificePermanentIds);
            if (kicked && kickerEffect != null && kickerEffect.hasLifeCost()) {
                additionalSpellCostService.validatePayLifeCost(gameData, player, card, kickerEffect.lifeCost());
            }
            if (kicked && kickerEffect != null && kickerEffect.hasSacrificeCost()) {
                validateKickerSacrificeCost(gameData, player, card, kickerEffect, sacrificePermanentId,
                        additionalCostSacrificePermanentIds);
            }
            if (kicked && kickerEffect != null && kickerEffect.hasTapCost()) {
                additionalSpellCostService.validateSingleTapCost(gameData, player, card,
                        kickerEffect.tapPredicate(), sacrificePermanentId);
            }
            if (kicked && kickerEffect != null && kickerEffect.hasReturnCost()) {
                additionalSpellCostService.validateReturnPermanentToHandCost(gameData, player, card,
                        new ReturnPermanentToHandCost(kickerEffect.returnPredicate()), sacrificePermanentId);
            }
            if (kicked && kickerEffect != null && kickerEffect.hasDiscardCost()) {
                additionalSpellCostService.validateDiscardCost(gameData, player, card,
                        new DiscardCardTypeCost(kickerEffect.discardPredicate(), kickerEffect.discardDescription()),
                        discardHandCardIndex, cardIndex);
            }
            if (buyback && buybackEffect != null && buybackEffect.hasSacrificeCost()) {
                validateBuybackSacrificeCost(gameData, player, card, buybackEffect, sacrificePermanentId,
                        additionalCostSacrificePermanentIds);
            }

            int waterbendAdditionalGenericCost = waterbendCostPaid
                    ? additionalCosts.waterbendCost().effectiveAmount(effectiveXValue) : 0;
            convokeContributions = appendManaContributions(convokeContributions,
                    applyWaterbendCost(gameData, player, card, additionalCosts.waterbendCost(),
                            costSelection.sacrificePermanentIds(), effectiveXValue, waterbendCostPaid));

            int sacrificeCostReduction = 0;
            if (hasSacrificeForCostReduction) {
                sacrificeCostReduction = paySacrificeCreaturesForCostReduction(
                        gameData, player, card, costReductionSacrificeIds);
            }
            int maximumDelveReduction = castingCostService.maximumDelveReduction(
                    gameData, playerId, card, manaCostX,
                    castingCostService.getCastCostModifier(
                            gameData, playerId, card, effectiveXValue, null, collectEvidenceCostPaid)
                            - sacrificeCostReduction - targetSubtypeCostReduction + targetingTax);
            if (additionalCosts.delveCost() != null) {
                additionalSpellCostService.validateDelveCost(gameData, player, card, additionalCosts.delveCost(),
                        costSelection.exileGraveyardCardIndices(), maximumDelveReduction);
            }
            int delveReduction = additionalSpellCostService.delveReduction(additionalCosts,
                    costSelection.exileGraveyardCardIndices());

            ManaPool preManaPaymentPool = (kicked && kickerEffect != null && kickerEffect.hasManaCost())
                    || (buyback && buybackEffect != null)
                    || (additionalCosts.payLifeOrPayManaCost() != null
                    && !Boolean.TRUE.equals(payLifeForAdditionalCost))
                    || (additionalCosts.sacrificePermanentOrPayManaCost() != null && sacrificePermanentId == null)
                    || (additionalCosts.forageOrPayManaCost() != null && sacrificePermanentId == null
                    && (exileGraveyardCardIndices == null || exileGraveyardCardIndices.isEmpty()))
                    || (additionalCosts.putCountersOrPayManaCost() != null && sacrificePermanentId == null)
                    || (additionalCosts.discardCardOrPayManaCost() != null && discardHandCardIndex == null)
                    ? new ManaPool(gameData.playerManaPools.get(playerId)) : null;
            // Converge or Sunburst on a permanent: snapshot the pool before payment so the number
            // of distinct colors spent can be counted after it, then carry that count as the stack
            // entry's X — which is what an ON_ENTER_BATTLEFIELD EnterWithCountersEffect(XValue)
            // reads. Sunburst still needs this snapshot when the card also has a real {X} cost.
            boolean needsConvergeValue = card.getKeywords().contains(Keyword.SUNBURST)
                    || EffectResolution.hasColorsSpentCounterEffect(card)
                    || (EffectResolution.hasConvergeEffect(card)
                            && (card.getManaCost() == null || !new ManaCost(card.getManaCost()).hasX()));
            java.util.EnumMap<ManaColor, Integer> convergeSnapshot = needsConvergeValue
                    ? gameData.playerManaPools.get(playerId).getColoredManaTotals()
                    : null;
            java.util.EnumMap<ManaColor, Integer> colorsSpentSnapshot =
                    EffectResolution.hasColorSpentCondition(card)
                            || EffectResolution.hasColorManaPairsSpentToCastAmount(card)
                            ? gameData.playerManaPools.get(playerId).getColoredManaTotals()
                            : null;
            int phyrexianManaPaidWithLife = 0;
            if (usingAlternateCost) {
                if (usingWebSlingingCost) {
                    webSlingingReturnedCreatureManaValue = payWebSlingingCost(gameData, player, card, webSlingingEffect,
                            alternateCostSacrificePermanentIds.getFirst(), targetingTax,
                            hasXCost ? 0 : perTargetCost, perTargetManaCost + escalateManaSuffix);
                } else if (usingBestowCost) {
                    payBestowCastingCost(gameData, player, card, targetingTax);
                } else if (usingSharedColorDiscardAlternativeCost) {
                    paySharedColorDiscardAlternativeCost(gameData, player, card,
                    sharedColorDiscardHandCardIndex, cardIndex);
                } else if (usingCollectEvidenceAlternativeCost) {
                    payCollectEvidenceCost(gameData, player, card, collectEvidenceAlternativeCost,
                            paymentCostSelection.exileGraveyardCardIndices());
                } else {
                    webSlingingReturnedCreatureManaValue = payAlternateCastingCost(gameData, player, card, alternateCostSacrificePermanentIds,
                            hasDiscardHandAlternateCost ? alternateDiscardHandCardIndex : discardHandCardIndex,
                            hasDiscardHandAlternateCost ? alternateDiscardHandCardIndices : discardHandCardIndices,
                            exileGraveyardCardIndex, cardIndex, manaCostX);
                }
                if (!usingWebSlingingCost) {
                    payEscalateManaOnly(gameData, playerId, card, escalateManaSuffix, targetingTax);
                }
            } else {
                phyrexianManaPaidWithLife = paySpellManaCost(gameData, playerId, castCharacteristics, manaCostX, convokeContributions, phyrexianLifeCount, kicked,
                        sacrificeCostReduction + delveReduction + targetSubtypeCostReduction, targetingTax,
                        (hasXCost ? 0 : perTargetCost) + waterbendAdditionalGenericCost,
                        perTargetManaCost, escalateManaSuffix, collectEvidenceCostPaid);
            }
            if (kicked && kickerEffect != null) {
                payKickerCost(gameData, player, card, kickerEffect, sacrificePermanentId, discardHandCardIndex,
                        additionalCostSacrificePermanentIds, cardIndex, preManaPaymentPool, effectiveXValue);
            }
            if (buyback && buybackEffect != null) {
                payBuybackCost(gameData, player, card, buybackEffect, sacrificePermanentId,
                        additionalCostSacrificePermanentIds, discardHandCardIndices, cardIndex, preManaPaymentPool);
            }
            AdditionalCostPayment additionalCostPayment = payAdditionalCosts(
                    gameData, player, card, additionalCosts, paymentCostSelection, 0, preManaPaymentPool,
                    effectiveXValue, collectEvidenceMinimumManaValue);
            addCreatureSpellAdditionalCounters(gameData, card, repeatedAdditionalCosts,
                    hasCreatureSpellAdditionalCountersCost);
            BeheldCardPayment beholdPayment = payBeholdCost(
                    gameData, player, card, additionalCosts.beholdCost(), costSelection);
            payImposedSacrificeTax(gameData, player, card, imposedSacrificePermanentIds);
            payTargetingLifeCost(gameData, player, card, targetingLifeTax);
            if (convergeSnapshot != null) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int converge = ManaPool.countDistinctColoredManaSpent(
                        convergeSnapshot, pool.getColoredManaTotals(), convokeContributions);
                gameData.setSpellCastConvergeValue(card.getId(), converge);
                stackX = converge;
            }
            if (colorsSpentSnapshot != null) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                gameData.setSpellCastColorsSpent(card.getId(), ManaPool.coloredManaColorsSpent(
                        colorsSpentSnapshot, pool.getColoredManaTotals(), convokeContributions));
                gameData.setSpellCastManaSpentByColor(card.getId(), ManaPool.coloredManaSpent(
                        colorsSpentSnapshot, pool.getColoredManaTotals(), convokeContributions));
            }
            deferSpellCastCostTriggers(gameData, stackBeforeCastingCosts);
            StackEntry entry;
            if (card.isAura() && (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting)) {
                // Reanimation Aura (e.g. Animate Dead): the target is a creature card in a graveyard.
                // Mark the stack entry's target zone as GRAVEYARD so resolution reanimates the enchanted
                // card and attaches the Aura to it (StackResolutionService.resolveEnchantmentSpell), and
                // so on-resolution fizzle checks look in the graveyard rather than on the battlefield.
                entry = new StackEntry(
                        permanentEntryType, stackCard, playerId, castCharacteristics.getName(),
                        List.of(), stackX, targetId, null, Map.of(), Zone.GRAVEYARD, List.of(), List.of()
                );
            } else if (!targetIds.isEmpty() && card.isAura()) {
                // Aura with ETB targeting (e.g. New Horizons): first target is the aura attachment,
                // remaining targets are for ETB effects
                UUID auraTarget = targetIds.getFirst();
                List<UUID> etbTargets = targetIds.size() > 1
                        ? new ArrayList<>(targetIds.subList(1, targetIds.size())) : List.of();
                entry = new StackEntry(
                        permanentEntryType, stackCard, playerId, castCharacteristics.getName(),
                        List.of(), stackX, auraTarget, null, Map.of(), null, List.of(), etbTargets
                );
            } else if (!targetIds.isEmpty()) {
                // Multi-target creature (e.g. Burning Sun's Avatar ETB with multiple targets)
                entry = new StackEntry(
                        permanentEntryType, stackCard, playerId, castCharacteristics.getName(),
                        List.of(), stackX, targetIds
                );
            } else {
                entry = new StackEntry(
                        permanentEntryType, stackCard, playerId, castCharacteristics.getName(),
                        List.of(), stackX, stackTarget, null
                );
            }
            if (castModalBackFace) {
                entry.setPhysicalCard(card);
            }
            entry.setPhyrexianManaPaidWithLife(phyrexianManaPaidWithLife);
            entry.setAlternateCost(usingAlternateCost);
            entry.setWebSlingingReturnedCreatureManaValue(webSlingingReturnedCreatureManaValue);
            entry.setCastTransformed(castModalBackFace);
            if (kicked && kickerEffect != null) {
                entry.setKicked(true);
            }
            if (buyback && buybackEffect != null) {
                entry.setBuyback(true);
            }
            if (castingFaceDown) {
                entry.setCastFaceDown(true);
            }
            // Evoke (CR 702.75): a permanent cast for its alternate (evoke) cost is flagged so its
            // "when it enters, sacrifice it" ETB trigger fires. Harmless for non-evoke alternate
            // casts (e.g. Demon of Death's Gate), which have no evoke sacrifice ETB effect.
            if (usingAlternateCost && !usingWebSlingingCost && !usingBestowCost
                    && !usingCollectEvidenceAlternativeCost && card.getMorphCost() == null) {
                entry.setEvoked(true);
                // Prowl (CR 702.75): flag the entry so a creature's "if its prowl cost was paid" ETB
                // trigger can gate on it. Only set for actual prowl alternate casts.
                AlternateHandCast altHandCast = card.getCastingOption(AlternateHandCast.class).orElse(null);
                if (altHandCast != null && !altHandCast.prowlDamageSubtypes().isEmpty()) {
                    entry.setProwl(true);
                }
                if (altHandCast != null && altHandCast.spectacle()) {
                    entry.setSpectacle(true);
                }
            }
            if (usingBestowCost) {
                entry.setBestowOriginalCard(bestowOriginalCard);
            }
            if (overloaded) {
                entry.setOverloaded(true);
            }
            stampSacrificedCostSnapshot(entry, additionalCostPayment);
            if (additionalCostPayment.sacrificedCardId() != null) {
                entry.setSacrificedCardId(additionalCostPayment.sacrificedCardId());
            }
            if (additionalCostPayment.sacrificedCardSnapshot() != null) {
                entry.setSacrificedCard(additionalCostPayment.sacrificedCardSnapshot());
            }
            if (additionalCostPayment.exiledCostCardId() != null) {
                entry.setExiledCostCardId(additionalCostPayment.exiledCostCardId());
                entry.setExiledCostCardSnapshot(additionalCostPayment.exiledCostCardSnapshot());
            }
            entry.setPutCounterCostPaid(isPutCounterCostPaid(additionalCosts, paymentCostSelection));
            entry.setCollectEvidenceCostPaid(
                    usingCollectEvidenceAlternativeCost
                            || isCollectEvidenceCostPaid(additionalCosts, paymentCostSelection));
            entry.setBeholdCostPaid(isBeholdCostPaid(additionalCosts, paymentCostSelection));
            entry.setWaterbendCostPaid(waterbendCostPaid);
            if (!repeatedAdditionalCosts.isEmpty()) {
                entry.setRepeatedAdditionalCosts(List.copyOf(repeatedAdditionalCosts));
            }
            if (hasModalEtb) {
                entry.setEtbMode(xValue != null ? xValue : 0);
            }
            if (wasModal) {
                entry.setModalModeCount(modalModeCount);
            }
            entry.setSourceZone(Zone.HAND);
            // Mirage flash clause: the permanent this becomes is sacrificed at the next cleanup step
            // if the spell was cast any time a sorcery couldn't have been cast.
            entry.setCastWhenSorceryCouldNotBeCast(
                    !castingPermissionService.sorceryTimingAvailable(gameData, playerId));
            if (beholdPayment != null) {
                entry.setBeheldCard(beholdPayment.card());
                entry.setBeheldCardOwnerId(beholdPayment.ownerId());
            }
            entry.setBeholdChosenSubtype(beholdChosenSubtype);
            entry.setChosenCreatureType(chosenCreatureType);
            entry.setConvokeCreatureIds(convokeCreatureIds);
            entry.setGiftPromised(giftPromised);
            gameData.stack.add(entry);
            gameData.stack.stream()
                    .filter(stackEntry -> stackEntry.getCard().getId().equals(card.getId()))
                    .findFirst()
                    .ifPresent(stackEntry -> stackEntry.setGiftPromised(giftPromised));
            finishSpellCast(gameData, playerId, player, hand, card);
        } else if (castCharacteristics.hasType(CardType.SORCERY)
                || castCharacteristics.hasType(CardType.INSTANT)) {
            // Sorcery/Instant spells: pay mana + sacrifice costs, handle targeting, put on stack
            StackEntryType entryType = cardTypeToStackEntryType(castCharacteristics.getType());
            int resolvedXValue = effectiveXValue;
            List<UUID> costReductionTargetIds = !targetIds.isEmpty() ? targetIds
                    : (targetId != null ? List.of(targetId) : List.of());
            int perTargetLifeCost = card.getAdditionalLifeCostPerTarget() * costReductionTargetIds.size();
            if (perTargetLifeCost > 0 && perTargetLifeCost > gameData.getLife(playerId)) {
                throw new IllegalStateException("Not enough life to pay the per-target life cost");
            }
            int targetSubtypeCostReduction = castingCostService.computeTargetBasedCostReduction(gameData, playerId, card, costReductionTargetIds);
            boolean needsConvergeValue = card.getKeywords().contains(Keyword.SUNBURST)
                    || EffectResolution.hasColorsSpentCounterEffect(card)
                    || (EffectResolution.hasConvergeEffect(card)
                            && (card.getManaCost() == null || !new ManaCost(card.getManaCost()).hasX()));
            boolean needsColorsSpent = EffectResolution.hasColorSpentCondition(card)
                    || EffectResolution.hasColorManaPairsSpentToCastAmount(card);
            java.util.EnumMap<ManaColor, Integer> colorsSpentSnapshot = needsColorsSpent
                    ? gameData.playerManaPools.get(playerId).getColoredManaTotals()
                    : null;
            java.util.EnumMap<ManaColor, Integer> convergeSnapshot = needsConvergeValue
                    ? gameData.playerManaPools.get(playerId).getColoredManaTotals()
                    : null;
            // Validate mana when target-based cost reduction doesn't apply but playability
            // check passed optimistically (e.g. Savage Stomp targeting a non-Dinosaur)
            if (targetSubtypeCostReduction == 0 && !usingAlternateCost && castingCostService.hasTargetBasedCastCostReduction(card)) {
                ManaCost validationCost = castingCostService.applyColoredManaCostReductions(
                        gameData, playerId, card, new ManaCost(card.getManaCost()));
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int costModifier = castingCostService.getCastCostModifier(
                        gameData, playerId, card, effectiveXValue, null, collectEvidenceCostPaid);
                if (!validationCost.canPay(pool, costModifier)) {
                    throw new IllegalStateException("Not enough mana — target does not qualify for cost reduction");
                }
            }
            // CR 601.2h: a cast either completes or leaves the game state untouched — reject an
            // unpayable non-mana additional cost up front, before any cost is consumed. A throw
            // later in the pay chain would keep the mana (and costs) already paid.
            KickerEffect kickerEffect = findKickerEffect(card);
            int collectEvidenceMinimumManaValue = additionalSpellCostService
                    .resolveCollectEvidenceMinimumManaValue(
                            gameData, additionalCosts.collectEvidenceCost(), targetId, targetIds);
            validateCardFlashAdditionalCost(gameData, player, card, additionalCosts, costSelection);
            boolean waterbendCostPaid = isWaterbendCostPaid(additionalCosts, waterbendPaid);
              additionalSpellCostService.validateAll(
                    gameData, player, card, additionalCosts, costSelection,
                    additionalCosts.tieredManaCost() != null ? modeEncoding : effectiveXValue,
                    waterbendCostPaid, collectEvidenceMinimumManaValue);
            int maximumDelveReduction = castingCostService.maximumDelveReduction(
                    gameData, playerId, card, resolvedXValue + perTargetCost,
                    castingCostService.getCastCostModifier(
                            gameData, playerId, card, resolvedXValue, null, collectEvidenceCostPaid)
                            - targetSubtypeCostReduction + targetingTax);
            if (additionalCosts.delveCost() != null) {
                additionalSpellCostService.validateDelveCost(gameData, player, card, additionalCosts.delveCost(),
                        costSelection.exileGraveyardCardIndices(), maximumDelveReduction);
            }
            int delveReduction = additionalSpellCostService.delveReduction(additionalCosts,
                    costSelection.exileGraveyardCardIndices());
            validateSpliceCosts(gameData, player, card, pendingSpliceCosts, spliceCostPermanentIds);
            if (additionalCosts.payXLife()) {
                additionalSpellCostService.validatePayXLifeCost(gameData, player, card, resolvedXValue);
            }
            if (additionalCosts.discardXCardsCost() != null) {
                additionalSpellCostService.validateDiscardXCardsCost(gameData, player, card,
                        additionalCosts.discardXCardsCost(), resolvedXValue, discardHandCardIndices, cardIndex);
            }
            validateImposedSacrificeTax(gameData, player, card, imposedSacrificePermanentIds);
            if (kicked && kickerEffect != null && kickerEffect.hasLifeCost()) {
                additionalSpellCostService.validatePayLifeCost(gameData, player, card, kickerEffect.lifeCost());
            }
            if (kicked && kickerEffect != null && kickerEffect.hasSacrificeCost()) {
                validateKickerSacrificeCost(gameData, player, card, kickerEffect, sacrificePermanentId,
                        additionalCostSacrificePermanentIds);
            }
            if (kicked && kickerEffect != null && kickerEffect.hasTapCost()) {
                additionalSpellCostService.validateSingleTapCost(gameData, player, card,
                        kickerEffect.tapPredicate(), sacrificePermanentId);
            }
            if (kicked && kickerEffect != null && kickerEffect.hasReturnCost()) {
                additionalSpellCostService.validateReturnPermanentToHandCost(gameData, player, card,
                        new ReturnPermanentToHandCost(kickerEffect.returnPredicate()), sacrificePermanentId);
            }
            if (kicked && kickerEffect != null && kickerEffect.hasDiscardCost()) {
                additionalSpellCostService.validateDiscardCost(gameData, player, card,
                        new DiscardCardTypeCost(kickerEffect.discardPredicate(), kickerEffect.discardDescription()),
                        discardHandCardIndex, cardIndex);
            }
            if (buyback && buybackEffect != null && buybackEffect.hasSacrificeCost()) {
                validateBuybackSacrificeCost(gameData, player, card, buybackEffect, sacrificePermanentId,
                        additionalCostSacrificePermanentIds);
            }

            int waterbendAdditionalGenericCost = waterbendCostPaid
                    ? additionalCosts.waterbendCost().effectiveAmount(resolvedXValue) : 0;
            convokeContributions = appendManaContributions(convokeContributions,
                    applyWaterbendCost(gameData, player, card, additionalCosts.waterbendCost(),
                            paymentCostSelection.sacrificePermanentIds(), resolvedXValue, waterbendCostPaid));

            int sacrificeCostReduction = 0;
            if (hasSacrificeForCostReduction) {
                sacrificeCostReduction = paySacrificeCreaturesForCostReduction(
                        gameData, player, card, costReductionSacrificeIds);
            }
            ManaPool preManaPaymentPool = (kicked && kickerEffect != null && kickerEffect.hasManaCost())
                    || (buyback && buybackEffect != null)
                    || !pendingSpliceCosts.isEmpty()
                    || (additionalCosts.payLifeOrPayManaCost() != null
                    && !Boolean.TRUE.equals(payLifeForAdditionalCost))
                    || (additionalCosts.sacrificePermanentOrPayManaCost() != null && sacrificePermanentId == null)
                    || (additionalCosts.forageOrPayManaCost() != null && sacrificePermanentId == null
                    && (exileGraveyardCardIndices == null || exileGraveyardCardIndices.isEmpty()))
                    || (additionalCosts.putCountersOrPayManaCost() != null && sacrificePermanentId == null)
                    || (additionalCosts.discardCardOrPayManaCost() != null && discardHandCardIndex == null)
                    ? new ManaPool(gameData.playerManaPools.get(playerId)) : null;
            if (usingAlternateCost) {
                if (usingWebSlingingCost) {
                    webSlingingReturnedCreatureManaValue = payWebSlingingCost(gameData, player, card, webSlingingEffect,
                            alternateCostSacrificePermanentIds.getFirst(), targetingTax,
                            hasXCost ? 0 : perTargetCost, perTargetManaCost + escalateManaSuffix);
                } else if (usingBestowCost) {
                    payBestowCastingCost(gameData, player, card, targetingTax);
                } else if (usingSharedColorDiscardAlternativeCost) {
                    paySharedColorDiscardAlternativeCost(gameData, player, card,
                    sharedColorDiscardHandCardIndex, cardIndex);
                } else if (usingCollectEvidenceAlternativeCost) {
                    payCollectEvidenceCost(gameData, player, card, collectEvidenceAlternativeCost,
                            paymentCostSelection.exileGraveyardCardIndices());
                } else {
                    webSlingingReturnedCreatureManaValue = payAlternateCastingCost(gameData, player, card, alternateCostSacrificePermanentIds,
                            hasDiscardHandAlternateCost ? alternateDiscardHandCardIndex : discardHandCardIndex,
                            hasDiscardHandAlternateCost ? alternateDiscardHandCardIndices : discardHandCardIndices,
                            exileGraveyardCardIndex, cardIndex, resolvedXValue);
                }
                if (!usingWebSlingingCost) {
                    payEscalateManaOnly(gameData, playerId, card, escalateManaSuffix, targetingTax);
                }
            } else {
                paySpellManaCost(gameData, playerId, castCharacteristics,
                        resolvedXValue + (hasXCost ? perTargetCost : 0), convokeContributions,
                        phyrexianLifeCount, kicked,
                        targetSubtypeCostReduction + sacrificeCostReduction + delveReduction, targetingTax,
                        (hasXCost ? 0 : perTargetCost) + waterbendAdditionalGenericCost,
                        perTargetManaCost, escalateManaSuffix, collectEvidenceCostPaid);
            }
            if (kicked && kickerEffect != null) {
                payKickerCost(gameData, player, card, kickerEffect, sacrificePermanentId, discardHandCardIndex,
                        additionalCostSacrificePermanentIds, cardIndex, preManaPaymentPool, resolvedXValue);
            }
            if (buyback && buybackEffect != null) {
                payBuybackCost(gameData, player, card, buybackEffect, sacrificePermanentId,
                        additionalCostSacrificePermanentIds, discardHandCardIndices, cardIndex, preManaPaymentPool);
            }
            paySpliceCosts(gameData, player, card, pendingSpliceCosts, spliceCostPermanentIds, preManaPaymentPool);
            AdditionalCostPayment additionalCostPayment = payAdditionalCosts(
                    gameData, player, card, additionalCosts, paymentCostSelection,
                    resolvedXValue, preManaPaymentPool, resolvedXValue,
                    collectEvidenceMinimumManaValue);
            resolvedXValue = additionalCostPayment.resolvedXValue();
            payPerTargetLifeCost(gameData, player, card, perTargetLifeCost);
            payImposedSacrificeTax(gameData, player, card, imposedSacrificePermanentIds);
            payTargetingLifeCost(gameData, player, card, targetingLifeTax);
            if (convergeSnapshot != null) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int converge = ManaPool.countDistinctColoredManaSpent(
                        convergeSnapshot, pool.getColoredManaTotals(), convokeContributions);
                gameData.setSpellCastConvergeValue(card.getId(), converge);
                resolvedXValue = converge;
            }
            if (colorsSpentSnapshot != null) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                gameData.setSpellCastColorsSpent(card.getId(), ManaPool.coloredManaColorsSpent(
                        colorsSpentSnapshot, pool.getColoredManaTotals(), convokeContributions));
                gameData.setSpellCastManaSpentByColor(card.getId(), ManaPool.coloredManaSpent(
                        colorsSpentSnapshot, pool.getColoredManaTotals(), convokeContributions));
            }
            if (EffectResolution.hasManaSpentToCastDamageEffect(card)) {
                resolvedXValue = gameData.getSpellCastManaSpent(card.getId());
            }
            deferSpellCastCostTriggers(gameData, stackBeforeCastingCosts);

            // Check for "up to N target cards from all graveyards" pile separation spells (e.g. Boneyard Parley)
            ExileTargetGraveyardCardsAndSeparateIntoPilesEffect pileSeparationEffect =
                    (ExileTargetGraveyardCardsAndSeparateIntoPilesEffect) filteredSpellEffects.stream()
                            .filter(ExileTargetGraveyardCardsAndSeparateIntoPilesEffect.class::isInstance)
                            .findFirst().orElse(null);

            ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect shuffleCardsIntoOwnersLibrariesEffect =
                    (ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect) filteredSpellEffects.stream()
                            .filter(ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect.class::isInstance)
                            .findFirst().orElse(null);

            // Check for "up to N target cards from graveyard" spells (e.g. Morbid Plunder)
            ReturnTargetCardsFromGraveyardToHandEffect graveyardToHandEffect =
                    (ReturnTargetCardsFromGraveyardToHandEffect) filteredSpellEffects.stream()
                            .filter(ReturnTargetCardsFromGraveyardToHandEffect.class::isInstance)
                            .findFirst().orElse(null);

            ReturnUpToOneOfEachFilterFromGraveyardToHandEffect oneOfEachFilterGraveyardEffect =
                    (ReturnUpToOneOfEachFilterFromGraveyardToHandEffect) filteredSpellEffects.stream()
                            .filter(ReturnUpToOneOfEachFilterFromGraveyardToHandEffect.class::isInstance)
                            .findFirst().orElse(null);

            IndependentlyTargetedGraveyardCardsEffect independentGraveyardTargets =
                    (IndependentlyTargetedGraveyardCardsEffect) EffectResolution
                            .expandConditionalTargetingEffects(filteredSpellEffects).stream()
                            .filter(IndependentlyTargetedGraveyardCardsEffect.class::isInstance)
                            .findFirst().orElse(null);

            // Check for "any number of target cards from graveyard" spells (e.g. Frantic Salvage)
            PutTargetCardsFromGraveyardOnTopOfLibraryEffect graveyardToTopEffect =
                    (PutTargetCardsFromGraveyardOnTopOfLibraryEffect) card.getEffects(EffectSlot.SPELL).stream()
                            .filter(PutTargetCardsFromGraveyardOnTopOfLibraryEffect.class::isInstance)
                            .findFirst().orElse(null);

            // Check for "target player shuffles up to N cards from their graveyard" spells (e.g. Memory's Journey)
            ShuffleTargetCardsFromGraveyardIntoLibraryEffect shuffleGraveyardCardsEffect =
                    (ShuffleTargetCardsFromGraveyardIntoLibraryEffect) filteredSpellEffects.stream()
                            .filter(ShuffleTargetCardsFromGraveyardIntoLibraryEffect.class::isInstance)
                            .findFirst().orElse(null);

            // Check for "any number of target creature cards from your graveyard" spells (e.g. Piper's Melody)
            ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect shuffleOwnGraveyardCardsEffect =
                    (ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect) filteredSpellEffects.stream()
                            .filter(ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect.class::isInstance)
                            .findFirst().orElse(null);

            // Check for "exile up to N target cards from a single graveyard" spells (e.g. Scarab Feast)
            ExileCardsFromGraveyardEffect exileFromGraveyardEffect =
                    (ExileCardsFromGraveyardEffect) filteredSpellEffects.stream()
                            .filter(ExileCardsFromGraveyardEffect.class::isInstance)
                            .findFirst().orElse(null);

            TargetPlayerGraveyardExileEffect targetPlayerGraveyardExileEffect = filteredSpellEffects.stream()
                    .filter(TargetPlayerGraveyardExileEffect.class::isInstance)
                    .map(TargetPlayerGraveyardExileEffect.class::cast)
                    .findFirst().orElse(null);

            if (graveyardToHandEffect != null && shuffleGraveyardCardsEffect != null) {
                gameData.graveyardTargetOperation.card = card;
                gameData.graveyardTargetOperation.controllerId = playerId;
                gameData.graveyardTargetOperation.effects = new ArrayList<>(filteredSpellEffects);
                gameData.graveyardTargetOperation.entryType = entryType;
                gameData.graveyardTargetOperation.xValue = resolvedXValue;
                gameData.graveyardTargetOperation.targetPlayerId = targetId;
                gameData.graveyardTargetOperation.pendingSpellGraveyardChoiceEffects =
                        new ArrayList<>(List.of(graveyardToHandEffect, shuffleGraveyardCardsEffect));
                gameData.graveyardTargetOperation.activeSpellGraveyardChoiceEffect = null;
                gameData.graveyardTargetOperation.spellGraveyardCardIdsByEffect.clear();
                if (graveyardTargetingService.beginNextSpellGraveyardChoice(gameData)) {
                    return;
                }
                gameData.graveyardTargetOperation.pendingSpellGraveyardChoiceEffects = List.of();
                gameData.graveyardTargetOperation.activeSpellGraveyardChoiceEffect = null;
                gameData.graveyardTargetOperation.spellGraveyardCardIdsByEffect.clear();
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, targetId,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (pileSeparationEffect != null) {
                // Target up to N creature cards from ALL graveyards
                long matchingCount = 0;
                for (UUID pid : gameData.orderedPlayerIds) {
                    matchingCount += gameData.playerGraveyards.getOrDefault(pid, List.of()).stream()
                            .filter(c -> !gameQueryService.isLandCardTargetRestricted(gameData, c, playerId))
                            .filter(c -> predicateEvaluationService.matchesCardPredicate(c, pileSeparationEffect.filter(), card.getId()))
                            .count();
                }
                if (matchingCount > 0) {
                    graveyardTargetingService.handleUpToNAllGraveyardsSpellTargeting(gameData, playerId, card,
                            entryType, pileSeparationEffect.filter(),
                            pileSeparationEffect.maxTargets(), filteredSpellEffects);
                    return; // finishSpellCast handled in handleMultipleCardsChosen
                }
                // No matching cards in any graveyard — put spell on stack with 0 targets
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (shuffleCardsIntoOwnersLibrariesEffect != null) {
                long matchingCount = gameQueryService.canGraveyardCardsBeTargeted(gameData)
                        ? gameData.orderedPlayerIds.stream()
                        .flatMap(pid -> gameData.playerGraveyards.getOrDefault(pid, List.of()).stream())
                        .filter(graveyardCard -> predicateEvaluationService.matchesCardPredicate(
                                graveyardCard, shuffleCardsIntoOwnersLibrariesEffect.filter(), card.getId()))
                        .count()
                        : 0;
                if (matchingCount > 0) {
                    graveyardTargetingService.handleUpToNAllGraveyardsSpellTargeting(gameData, playerId, card,
                            entryType, shuffleCardsIntoOwnersLibrariesEffect.filter(),
                            shuffleCardsIntoOwnersLibrariesEffect.maxTargets(), filteredSpellEffects);
                    return;
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (filteredSpellEffects.stream().anyMatch(DeliverUntoEvilEffect.class::isInstance)) {
                long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                        .filter(c -> !gameQueryService.isLandCardTargetRestricted(gameData, c, playerId))
                        .count();
                if (matchingCount > 0) {
                    graveyardTargetingService.handleUpToNGraveyardSpellTargeting(gameData, playerId, card,
                            entryType, (CardPredicate) null, 4, filteredSpellEffects);
                    return; // finishSpellCast handled in handleMultipleCardsChosen
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (shuffleOwnGraveyardCardsEffect != null) {
                UUID spellCounterTargetId = (unwrappedNeedsSpellTarget && targetingSpellOnStack) ? targetId : null;
                long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                        .filter(c -> predicateEvaluationService.matchesCardPredicate(
                                c, shuffleOwnGraveyardCardsEffect.filter(), card.getId()))
                        .count();
                if (matchingCount > 0) {
                    gameData.graveyardTargetOperation.spellCounterTargetId = spellCounterTargetId;
                    gameData.graveyardTargetOperation.permanentTargetIds = new ArrayList<>(targetIds);
                    if (shuffleOwnGraveyardCardsEffect.maxTargets()
                            == ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect.ANY_NUMBER) {
                        graveyardTargetingService.handleAnyNumberGraveyardSpellTargeting(gameData, playerId, card,
                                entryType, shuffleOwnGraveyardCardsEffect.filter(), filteredSpellEffects);
                    } else {
                        graveyardTargetingService.handleUpToNGraveyardSpellTargeting(gameData, playerId, card,
                                entryType, shuffleOwnGraveyardCardsEffect.filter(),
                                shuffleOwnGraveyardCardsEffect.maxTargets(), filteredSpellEffects);
                    }
                    return; // finishSpellCast handled in handleMultipleCardsChosen
                }
                // No matching cards in your graveyard — put the spell on the stack with 0 targets
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, spellCounterTargetId,
                        null, Map.of(), spellCounterTargetId != null ? Zone.STACK : null, List.of(), targetIds
                ));
            } else if (shuffleGraveyardCardsEffect != null) {
                // A modal spell can pair this effect with a spell-countering mode. In that case
                // targetId carries the spell on the stack and the target player is one of the
                // chosen modal target groups in targetIds (Quandrix Command).
                UUID spellCounterTargetId = (unwrappedNeedsSpellTarget && targetingSpellOnStack)
                        ? targetId : null;
                UUID targetGraveyardOwner = targetIds.stream()
                        .filter(gameData.playerIds::contains)
                        .findFirst()
                        .orElse(targetId);
                if (targetGraveyardOwner == null) {
                    throw new IllegalStateException("Must target a player");
                }
                long matchingCount = gameData.playerGraveyards.getOrDefault(targetGraveyardOwner, List.of()).stream()
                        .filter(c -> !gameQueryService.isLandCardTargetRestricted(gameData, c, playerId))
                        .filter(c -> predicateEvaluationService.matchesCardPredicate(c, shuffleGraveyardCardsEffect.filter(), card.getId()))
                        .count();
                if (matchingCount > 0) {
                    gameData.graveyardTargetOperation.spellCounterTargetId = spellCounterTargetId;
                    gameData.graveyardTargetOperation.permanentTargetIds = spellCounterTargetId == null
                            ? null : new ArrayList<>(targetIds);
                    graveyardTargetingService.handleUpToNTargetPlayerGraveyardSpellTargeting(gameData, playerId,
                            targetGraveyardOwner, card, entryType, shuffleGraveyardCardsEffect.filter(),
                            shuffleGraveyardCardsEffect.maxTargets(), filteredSpellEffects);
                    return; // finishSpellCast handled in handleMultipleCardsChosen
                }
                // No matching cards in target player's graveyard — put spell on stack with 0 targets
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0,
                        spellCounterTargetId != null ? spellCounterTargetId : targetGraveyardOwner,
                        null, Map.of(), spellCounterTargetId != null ? Zone.STACK : null, List.of(),
                        spellCounterTargetId != null ? targetIds : List.of()
                ));
            } else if (targetPlayerGraveyardExileEffect != null) {
                UUID targetGraveyardOwner = targetId;
                if (targetGraveyardOwner == null) {
                    throw new IllegalStateException("Must target a player");
                }
                if (resolvedXValue > 0) {
                    long matchingCount = gameQueryService.canGraveyardCardsBeTargeted(gameData)
                            ? gameData.playerGraveyards.getOrDefault(targetGraveyardOwner, List.of()).stream()
                            .filter(c -> !gameQueryService.isLandCardTargetRestricted(gameData, c, playerId))
                            .filter(c -> predicateEvaluationService.matchesCardPredicate(
                                    c, targetPlayerGraveyardExileEffect.filter(), card.getId()))
                            .count()
                            : 0;
                    if (matchingCount < resolvedXValue) {
                        throw new IllegalStateException("Not enough cards in target player's graveyard");
                    }
                    graveyardTargetingService.handleExactNTargetPlayerGraveyardSpellTargeting(
                            gameData, playerId, targetGraveyardOwner, card, entryType, resolvedXValue,
                            targetPlayerGraveyardExileEffect.filter(), filteredSpellEffects);
                    return; // finishSpellCast handled in handleMultipleCardsChosen
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, targetGraveyardOwner,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (oneOfEachFilterGraveyardEffect != null) {
                long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                        .filter(c -> oneOfEachFilterGraveyardEffect.filters().stream().anyMatch(filter ->
                                predicateEvaluationService.matchesCardPredicate(c, filter, card.getId())))
                        .count();
                if (matchingCount > 0) {
                    graveyardTargetingService.handleUpToOneOfEachFilterGraveyardSpellTargeting(
                            gameData, playerId, card, entryType, oneOfEachFilterGraveyardEffect, filteredSpellEffects);
                    return;
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (independentGraveyardTargets != null) {
                gameData.graveyardTargetOperation.card = card;
                gameData.graveyardTargetOperation.controllerId = playerId;
                gameData.graveyardTargetOperation.effects = new ArrayList<>(filteredSpellEffects);
                gameData.graveyardTargetOperation.entryType = entryType;
                gameData.graveyardTargetOperation.xValue = resolvedXValue;
                gameData.graveyardTargetOperation.anyNumber = true;
                gameData.graveyardTargetOperation.independentTargetGroupIndex = 0;
                gameData.graveyardTargetOperation.independentTargetCardIds.clear();
                gameData.graveyardTargetOperation.independentTargetGroupSizes.clear();
                if (graveyardTargetingService.beginIndependentGraveyardSpellTargeting(
                        gameData, playerId, independentGraveyardTargets)) {
                    return;
                }
                StackEntry spellEntry = new StackEntry(
                        entryType, card, playerId, card.getName(), filteredSpellEffects, resolvedXValue,
                        null, null, Map.of(), null, List.of(), List.of());
                spellEntry.setTargetCardGroupSizes(List.copyOf(
                        gameData.graveyardTargetOperation.independentTargetGroupSizes));
                gameData.graveyardTargetOperation.card = null;
                gameData.graveyardTargetOperation.controllerId = null;
                gameData.graveyardTargetOperation.effects = null;
                gameData.graveyardTargetOperation.entryType = null;
                gameData.graveyardTargetOperation.xValue = 0;
                gameData.graveyardTargetOperation.anyNumber = false;
                gameData.graveyardTargetOperation.independentTargetGroupIndex = -1;
                gameData.graveyardTargetOperation.independentTargetCardIds.clear();
                gameData.graveyardTargetOperation.independentTargetGroupSizes.clear();
                gameData.stack.add(spellEntry);
            } else if (graveyardToHandEffect != null && graveyardToHandEffect.xScaled()) {
                // "Return X target creature cards from your graveyard to your hand" (Shattered
                // Crypt): exactly X targets, chosen before the spell goes on the stack. X rides on
                // the resulting stack entry so riders such as "you lose X life" read the same X.
                if (resolvedXValue > 0) {
                    graveyardTargetingService.handleExactNGraveyardSpellTargeting(
                            gameData, playerId, card, entryType, resolvedXValue,
                            graveyardToHandEffect.filter(), "to your hand");
                    return; // finishSpellCast handled in handleMultipleCardsChosen
                }
                // X=0: no targets, but the spell still resolves (losing 0 life)
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (graveyardToHandEffect != null && graveyardToHandEffect.exactTargets()) {
                graveyardTargetingService.handleExactNGraveyardSpellTargeting(
                        gameData, playerId, card, entryType, graveyardToHandEffect.maxTargets(),
                        graveyardToHandEffect.filter(), "to your hand");
                return; // finishSpellCast handled in graveyard targeting callback
            } else if (graveyardToHandEffect != null) {
                // A modal "both" mode may pair the graveyard return with a spell-on-stack counter
                // (Soul Manipulation): carry the counter's spell target through the interactive
                // graveyard choice so it survives onto the resulting stack entry's targetId.
                UUID spellCounterTargetId = (unwrappedNeedsSpellTarget && targetingSpellOnStack) ? targetId : null;
                long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                        .filter(c -> predicateEvaluationService.matchesCardPredicate(c, graveyardToHandEffect.filter(), card.getId()))
                        .count();
                // "Up to X target cards … where X is [something about target opponent] as you cast
                // this spell" (Reap): X is locked in here, once the player target is known.
                int graveyardMaxTargets = graveyardToHandEffect.dynamicMaxTargets() == null
                        ? graveyardToHandEffect.maxTargets()
                        : amountEvaluationService.evaluate(gameData, graveyardToHandEffect.dynamicMaxTargets(),
                                new com.github.laxika.magicalvibes.service.effect.AmountContext(
                                        playerId, null, targetId, 0, 0));
                int graveyardMinTargets = graveyardToHandEffect.minTargets();
                int targetGroupIndex = card.getEffectTargetIndex(graveyardToHandEffect);
                if (targetGroupIndex >= 0 && targetGroupIndex < card.getSpellTargets().size()) {
                    SpellTarget targetGroup = card.getSpellTargets().get(targetGroupIndex);
                    if (targetGroup.getFilter() instanceof GraveyardCardPredicateTargetFilter) {
                        graveyardMinTargets = Math.max(graveyardMinTargets,
                                giftPromised
                                        ? targetGroup.getGiftPromisedMinTargets()
                                        : targetGroup.getMinTargets());
                        graveyardMaxTargets = Math.min(graveyardMaxTargets,
                                targetLegalityService.getEffectiveMaxTargetsForGroup(
                                        gameData, card, playerId, null, targetGroup, giftPromised));
                    }
                }
                if (matchingCount > 0 && graveyardMaxTargets > 0) {
                    gameData.graveyardTargetOperation.spellCounterTargetId = spellCounterTargetId;
                    gameData.graveyardTargetOperation.permanentTargetIds = new ArrayList<>(targetIds);
                    gameData.graveyardTargetOperation.physicalCard = adventure ? physicalHandCard : null;
                    gameData.graveyardTargetOperation.castWithAdventure = adventure;
                    gameData.graveyardTargetOperation.giftPromised = giftPromised;
                    graveyardTargetingService.handleUpToNGraveyardSpellTargeting(gameData, playerId, card,
                            entryType, graveyardToHandEffect,
                            graveyardMaxTargets, filteredSpellEffects, graveyardMinTargets);
                    return; // finishSpellCast handled in handleMultipleCardsChosen
                }
                // No matching cards — put spell on stack with 0 graveyard targets (the return fizzles),
                // preserving any spell-on-stack counter target (Zone.STACK) so the counter still resolves.
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, spellCounterTargetId,
                        null, Map.of(), spellCounterTargetId != null ? Zone.STACK : null, List.of(), targetIds
                ));
            } else if (graveyardToTopEffect != null) {
                long matchingCount = 0;
                if (graveyardToTopEffect.fromOtherGraveyards()) {
                    for (UUID pid : gameData.orderedPlayerIds) {
                        if (pid.equals(playerId)) {
                            continue;
                        }
                        matchingCount += gameData.playerGraveyards.getOrDefault(pid, List.of()).stream()
                                .filter(c -> !gameQueryService.isLandCardTargetRestricted(gameData, c, playerId))
                                .filter(c -> predicateEvaluationService.matchesCardPredicate(c, graveyardToTopEffect.filter(), card.getId()))
                                .count();
                    }
                } else {
                    matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                            .filter(c -> predicateEvaluationService.matchesCardPredicate(c, graveyardToTopEffect.filter(), card.getId()))
                            .count();
                }
                if (matchingCount > 0) {
                    if (graveyardToTopEffect.fromOtherGraveyards()) {
                        graveyardTargetingService.handleUpToNOpponentGraveyardSpellTargeting(gameData, playerId, card,
                                entryType, graveyardToTopEffect.filter(), graveyardToTopEffect.maxTargets(),
                                filteredSpellEffects);
                    } else if (graveyardToTopEffect.maxTargets() == PutTargetCardsFromGraveyardOnTopOfLibraryEffect.ANY_NUMBER) {
                        graveyardTargetingService.handleAnyNumberGraveyardSpellTargeting(gameData, playerId, card,
                                entryType, graveyardToTopEffect.filter());
                    } else {
                        graveyardTargetingService.handleUpToNGraveyardSpellTargeting(gameData, playerId, card,
                                entryType, graveyardToTopEffect.filter(), graveyardToTopEffect.maxTargets(),
                                filteredSpellEffects);
                    }
                    return; // finishSpellCast handled in handleMultipleCardsChosen
                }
                // No matching cards — put spell on stack with 0 targets (still draws, etc.)
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (exileFromGraveyardEffect != null) {
                long matchingCount = 0;
                for (UUID pid : gameData.orderedPlayerIds) {
                    matchingCount += gameData.playerGraveyards.getOrDefault(pid, List.of()).stream()
                            .filter(candidate -> exileFromGraveyardEffect.filter() == null
                                    || predicateEvaluationService.matchesCardPredicate(
                                    candidate, exileFromGraveyardEffect.filter(), card.getId()))
                            .filter(candidate -> !gameQueryService.isLandCardTargetRestricted(
                                    gameData, candidate, playerId))
                            .count();
                }
                int graveyardMaxTargets = exileFromGraveyardEffect.xScaled()
                        ? exileFromGraveyardEffect.maxTargetsForX(resolvedXValue)
                        : exileFromGraveyardEffect.maxTargets();
                if (exileFromGraveyardEffect.exactTargets() && graveyardMaxTargets > 0 && matchingCount == 0) {
                    throw new IllegalStateException("No legal graveyard cards to target");
                }
                if (matchingCount > 0 && graveyardMaxTargets > 0) {
                    gameData.graveyardTargetOperation.permanentTargetIds = new ArrayList<>(targetIds);
                    gameData.graveyardTargetOperation.targetPlayerId = targetId;
                    if (exileFromGraveyardEffect.allGraveyardsForSpell()
                            && !exileFromGraveyardEffect.exactTargets()) {
                        graveyardTargetingService.handleUpToNAllGraveyardsSpellTargeting(gameData, playerId, card,
                                entryType, exileFromGraveyardEffect.filter(), graveyardMaxTargets, filteredSpellEffects);
                    } else {
                        graveyardTargetingService.handleUpToNSingleGraveyardSpellTargeting(gameData, playerId, card,
                                entryType, graveyardMaxTargets, exileFromGraveyardEffect.filter(), filteredSpellEffects,
                                exileFromGraveyardEffect.exactTargets());
                    }
                    return; // finishSpellCast handled in handleMultipleCardsChosen
                }
                // No cards in any graveyard — put spell on stack with 0 targets (resolves doing nothing)
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), targetIds
                ));
            } else if (needsGraveyardCreatureTargeting && resolvedXValue > 0) {
                // Prompt player to choose graveyard targets before putting spell on stack
                graveyardTargetingService.handleGraveyardSpellTargeting(gameData, playerId, card,
                        entryType, resolvedXValue, filteredSpellEffects,
                        graveyardCreatureTargetingEffect.graveyardScope());
                return; // finishSpellCast handled in graveyard targeting callback
            } else if (needsGraveyardCreatureTargeting) {
                // X=0: no targets needed, put spell on stack directly (resolves doing nothing)
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, null, null, List.of(), List.of()
                ));
            } else if (returnToBattlefieldEffect != null && !returnToBattlefieldEffect.xScaled()) {
                int targetXValue = resolvedXValue;
                List<UUID> graveyardOwners = switch (returnToBattlefieldEffect.source()) {
                    case CONTROLLERS_GRAVEYARD -> List.of(playerId);
                    case OPPONENT_GRAVEYARD -> gameData.orderedPlayerIds.stream()
                            .filter(ownerId -> !ownerId.equals(playerId))
                            .toList();
                    case ALL_GRAVEYARDS -> gameData.orderedPlayerIds;
                };
                int maxTargets = returnToBattlefieldEffect.dynamicMaxTargets() == null
                        ? returnToBattlefieldEffect.maxTargets()
                        : Math.max(0, amountEvaluationService.evaluate(gameData,
                        returnToBattlefieldEffect.dynamicMaxTargets(),
                        new com.github.laxika.magicalvibes.service.effect.AmountContext(
                                playerId, null, null, resolvedXValue, 0)));
                long matchingCount = graveyardOwners.stream()
                        .flatMap(ownerId -> gameData.playerGraveyards.getOrDefault(ownerId, List.of()).stream()
                                .filter(c -> !returnToBattlefieldEffect.fromBattlefieldThisTurn()
                                        || gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn
                                        .getOrDefault(ownerId, Set.of()).contains(c.getId())))
                        .filter(c -> predicateEvaluationService.matchesCardPredicate(
                                c, returnToBattlefieldEffect.filter(), card.getId(), gameData,
                                playerId, null, null, targetXValue))
                        .count();
                if (matchingCount > 0 && maxTargets > 0) {
                    graveyardTargetingService.handleUpToNGraveyardSpellTargeting(
                            gameData, playerId, card, entryType, returnToBattlefieldEffect,
                            maxTargets, targetXValue, filteredSpellEffects);
                    return;
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, null, null, List.of(), List.of()
                ));
            } else if (sacrificeAndReturnEffect != null) {
                graveyardTargetingService.handleExactNGraveyardSpellTargeting(
                        gameData, playerId, card, entryType, sacrificeAndReturnEffect.targetCount(),
                        sacrificeAndReturnEffect.returnFilter(), "to the battlefield");
                return;
            } else if (returnToBattlefieldEffect != null && resolvedXValue > 0) {
                graveyardTargetingService.handleExactNGraveyardSpellTargeting(
                        gameData, playerId, card, entryType, resolvedXValue,
                        returnToBattlefieldEffect.filter(), "to the battlefield");
                return;
            } else if (returnToBattlefieldEffect != null) {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, null, null, List.of(), List.of()
                ));
            } else if (kicked && damageAssignments != null && !damageAssignments.isEmpty()
                    && findKickedDividedDamageEffect(filteredSpellEffects) != null) {
                // Kicked spell with divided damage among any targets (e.g. Fight with Fire)
                DealDividedDamageEffect dividedEffect = findKickedDividedDamageEffect(filteredSpellEffects);
                int expectedTotal = ((Fixed) dividedEffect.totalDamage()).value();
                int totalDamage = damageAssignments.values().stream().mapToInt(Integer::intValue).sum();
                if (totalDamage != expectedTotal) {
                    throw new IllegalStateException("Damage assignments must sum to " + expectedTotal);
                }
                // Each announced assignment target (CR 601.2d) has to be legal for the effect. The
                // restriction is evaluated from what the effect declares rather than re-implemented,
                // so this cast-time gate cannot drift from enumeration, and it is layer-aware
                // (CR 613.1d): a planeswalker a type-replacing effect turned into a land is no
                // longer an any target (CR 115.4).
                PermanentPredicate assignmentRestriction =
                        dividedEffect.targetSpec().targetPredicate().permanentRestriction().orElseThrow();
                FilterContext assignmentContext = new FilterContext(gameData, card.getId(), playerId, null, null);
                for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                    UUID target = assignment.getKey();
                    boolean isPlayer = gameData.playerIds.contains(target);
                    if (!isPlayer) {
                        Permanent perm = gameQueryService.findPermanentById(gameData, target);
                        if (perm == null) {
                            throw new IllegalStateException("Invalid target");
                        }
                        if (!predicateEvaluationService.matchesPermanentPredicate(
                                perm, assignmentRestriction, assignmentContext)) {
                            throw new IllegalStateException(
                                    "Target must be a creature, planeswalker, battle, or player");
                        }
                    }
                    if (assignment.getValue() <= 0) {
                        throw new IllegalStateException("Each damage assignment must be positive");
                    }
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, null, damageAssignments
                ));
            } else if (findChosenCounterDistribution(filteredSpellEffects) != null) {
                // "Distribute X counters among any number of target creatures" (Spoils of War).
                // X is evaluated from game state as the spell is cast; each target needs at least one
                // counter, so X is also the effective cap on the number of targets. X = 0 means no
                // legal division exists, so the spell is cast with no targets and resolves doing
                // nothing. Per-target amounts ride on damageAssignments, like divided damage.
                DistributeCountersAmongTargetsEffect distributeCounters =
                        findChosenCounterDistribution(filteredSpellEffects);
                int expectedTotal = amountEvaluationService.evaluate(gameData, distributeCounters.total(),
                        com.github.laxika.magicalvibes.service.effect.AmountContext.forCasting(playerId));
                Map<UUID, Integer> counterAssignments =
                        damageAssignments == null ? Map.of() : damageAssignments;
                int assignedTotal = counterAssignments.values().stream().mapToInt(Integer::intValue).sum();
                if (!counterAssignments.isEmpty() && assignedTotal != expectedTotal) {
                    throw new IllegalStateException("Counter assignments must sum to " + expectedTotal);
                }
                FilterContext assignmentContext = FilterContext.of(gameData)
                        .withSourceCardId(card.getId())
                        .withSourceControllerId(playerId);
                for (Map.Entry<UUID, Integer> assignment : counterAssignments.entrySet()) {
                    Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
                    if (target == null || !gameQueryService.isCreature(gameData, target)) {
                        throw new IllegalStateException("All targets must be creatures");
                    }
                    if (card.getTargetFilter() != null) {
                        predicateEvaluationService.validateTargetFilter(
                                card.getTargetFilter(), target, assignmentContext);
                    }
                    if (distributeCounters.targetRestriction() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(
                            target, distributeCounters.targetRestriction(), assignmentContext)) {
                        throw new IllegalStateException("Target is not a legal target");
                    }
                    if (assignment.getValue() <= 0) {
                        throw new IllegalStateException("Each counter assignment must be positive");
                    }
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, null, counterAssignments
                ));
            } else if (EffectResolution.needsDamageDistribution(targetingSpellEffects)) {
                // Validate damage assignments for damage distribution spells. Empty assignments are
                // legal only when the total damage/prevention is 0 (X=0) — sum checks below reject
                // empty when the total is positive.
                if (damageAssignments == null) {
                    damageAssignments = Map.of();
                }

                PreventDividedDamageEffect preventDivided = targetingSpellEffects.stream()
                        .filter(PreventDividedDamageEffect.class::isInstance)
                        .map(PreventDividedDamageEffect.class::cast)
                        .findFirst().orElse(null);
                if (preventDivided != null) {
                    // "Prevent the next N damage ... to any number of targets, divided as you choose"
                    // (Remedy). Per-target shield amounts ride on damageAssignments; each target needs
                    // at least 1, so N is the effective cap on the number of targets.
                    int expectedPrevention = amountEvaluationService.evaluate(gameData,
                            preventDivided.amount(),
                            com.github.laxika.magicalvibes.service.effect.AmountContext.forCasting(playerId));
                    int totalPrevention = damageAssignments.values().stream().mapToInt(Integer::intValue).sum();
                    if (totalPrevention != expectedPrevention) {
                        throw new IllegalStateException("Prevention assignments must sum to " + expectedPrevention);
                    }
                    if (damageAssignments.size() > expectedPrevention) {
                        throw new IllegalStateException("Too many targets");
                    }
                    PermanentPredicate assignmentRestriction = preventDivided.targetSpec().targetPredicate()
                            .permanentRestriction().orElseThrow();
                    FilterContext assignmentContext = new FilterContext(gameData, card.getId(), playerId, null, null);
                    for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                        if (!gameData.playerIds.contains(assignment.getKey())) {
                            Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
                            if (target == null) {
                                throw new IllegalStateException("Invalid target");
                            }
                            if (!predicateEvaluationService.matchesPermanentPredicate(
                                    target, assignmentRestriction, assignmentContext)) {
                                throw new IllegalStateException("Target must be a creature, planeswalker, battle, or player");
                            }
                        }
                        if (assignment.getValue() <= 0) {
                            throw new IllegalStateException("Each prevention assignment must be positive");
                        }
                    }
                } else {
                    DealDividedDamageEffect dividedEffect =
                            findChosenDividedDamageEffect(filteredSpellEffects);

                    int totalDamage = damageAssignments.values().stream().mapToInt(Integer::intValue).sum();

                    if (dividedEffect != null && dividedEffect.totalDamage() instanceof Fixed fixedTotal) {
                        // Fixed-damage divided damage spell (e.g. Ignite Disorder, Pyrotechnics)
                        if (totalDamage != fixedTotal.value()) {
                            throw new IllegalStateException("Damage assignments must sum to " + fixedTotal.value());
                        }
                        boolean canTargetPlayers = dividedEffect.canTargetPlayers();
                        // Unbounded (maxTargets 0) among any number of targets: each target needs at
                        // least 1 damage, so the total damage is the effective cap (Pyrotechnics).
                        // A player target group belongs to a separate effect (Fiery Justice's
                        // "target opponent gains 5 life"), so it must not cap the damage targets.
                        boolean cardTargetGroupIsDamage = card.getMaxTargets() > 0
                                && !(card.getTargetFilter() instanceof PlayerPredicateTargetFilter);
                        int maxTargets = dividedEffect.maxTargets() > 0 ? dividedEffect.maxTargets()
                                : (cardTargetGroupIsDamage ? card.getMaxTargets() : fixedTotal.value());
                        if (damageAssignments.size() > maxTargets) {
                            throw new IllegalStateException("Too many targets");
                        }
                        for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                            boolean isPlayer = gameData.playerIds.contains(assignment.getKey());
                            if (isPlayer) {
                                if (!canTargetPlayers) {
                                    throw new IllegalStateException("All targets must be creatures");
                                }
                            } else {
                                Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
                                if (target == null || !gameQueryService.isCreature(gameData, target)) {
                                    throw new IllegalStateException("All targets must be creatures");
                                }
                                if (card.getTargetFilter() != null
                                        && !(card.getTargetFilter() instanceof PlayerPredicateTargetFilter)) {
                                    predicateEvaluationService.validateTargetFilter(gameData, card.getTargetFilter(), target);
                                }
                            }
                            if (assignment.getValue() <= 0) {
                                throw new IllegalStateException("Each damage assignment must be positive");
                            }
                        }
                    } else if (dividedEffect != null
                            && !(dividedEffect.totalDamage() instanceof Fixed)) {
                        // Dynamic total divided as you choose among any number of targets.
                        int expectedTotal = amountEvaluationService.evaluate(gameData,
                                dividedEffect.totalDamage(),
                                com.github.laxika.magicalvibes.service.effect.AmountContext.forCasting(playerId, resolvedXValue));
                        if (totalDamage != expectedTotal) {
                            throw new IllegalStateException("Damage assignments must sum to " + expectedTotal);
                        }
                        for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                            UUID assignedTargetId = assignment.getKey();
                            boolean isPlayer = gameData.playerIds.contains(assignedTargetId);
                            if (isPlayer) {
                                if (!dividedEffect.canTargetPlayers()) {
                                    throw new IllegalStateException("All targets must be creatures");
                                }
                            } else {
                                Permanent target = gameQueryService.findPermanentById(gameData, assignedTargetId);
                                if (target == null) {
                                    throw new IllegalStateException("Invalid target");
                                }
                                if (!dividedEffect.canTargetPlayers()
                                        && !gameQueryService.isCreature(gameData, target)) {
                                    throw new IllegalStateException("All targets must be creatures");
                                }
                                if (dividedEffect.targetRestriction() != null
                                        && !predicateEvaluationService.matchesPermanentPredicate(
                                        gameData, target, dividedEffect.targetRestriction())) {
                                    throw new IllegalStateException("Illegal target for divided damage");
                                }
                            }
                            if (assignment.getValue() <= 0) {
                                throw new IllegalStateException("Each damage assignment must be positive");
                            }
                        }
                    } else {
                        // Damage divided among target creatures — restriction comes from
                        // DealDividedDamageEffect.targetRestriction (Hail of Arrows: attacking;
                        // Fire Covenant: any creature; Rock Slide: attacking/blocking without flying).
                        // Dynamic totals such as Volcanic Wind's battlefield creature count are
                        // evaluated in the same cast-time context as X-based totals.
                        int expectedTotal = dividedEffect == null
                                ? resolvedXValue
                                : amountEvaluationService.evaluate(gameData,
                                        dividedEffect.totalDamage(),
                                        com.github.laxika.magicalvibes.service.effect.AmountContext
                                                .forCasting(playerId, resolvedXValue));
                        if (totalDamage != expectedTotal) {
                            throw new IllegalStateException("Damage assignments must sum to " + expectedTotal);
                        }
                        for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                            Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
                            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                                throw new IllegalStateException("All targets must be creatures");
                            }
                            if (dividedEffect != null && dividedEffect.targetRestriction() != null
                                    && !predicateEvaluationService.matchesPermanentPredicate(
                                            gameData, target, dividedEffect.targetRestriction())) {
                                throw new IllegalStateException("Illegal target for divided damage");
                            }
                            if (assignment.getValue() <= 0) {
                                throw new IllegalStateException("Each damage assignment must be positive");
                            }
                        }
                    }
                }
                // A divided-damage spell may also target a player with a separate effect ("Target
                // opponent gains 5 life" — Fiery Justice). That target rides on targetId, is
                // validated against the card's player filter here, and is carried on the stack
                // entry alongside the damage assignments.
                if (card.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter) {
                    targetLegalityService.validateSpellPlayerTarget(gameData, targetId, playerId, card, playerFilter);
                }
                if (dividedDamageTargetGroupSizes.isEmpty()) {
                    gameData.stack.add(new StackEntry(
                            entryType, card, playerId, card.getName(),
                            filteredSpellEffects, resolvedXValue, targetId, damageAssignments
                    ));
                } else {
                    StackEntry dividedEntry = new StackEntry(
                            entryType, card, playerId, card.getName(), filteredSpellEffects,
                            resolvedXValue, targetId, null, damageAssignments, null, List.of(), targetIds);
                    dividedEntry.setTargetGroupSizes(dividedDamageTargetGroupSizes);
                    gameData.stack.add(dividedEntry);
                }
            } else if (unwrappedNeedsSpellTarget && targetingSpellOnStack) {
                if (multipleSpellTargets) {
                    // Spell targets multiple distinct spells on the stack (e.g. Choreographed Sparks'
                    // "both" mode). Each spell target is resolved by its mapped effect via targetIds.
                    gameData.stack.add(new StackEntry(
                            entryType, card, playerId, card.getName(),
                            filteredSpellEffects, resolvedXValue, null,
                            null, Map.of(), Zone.STACK, List.of(), targetIds
                    ));
                } else if (unwrappedNeedsTarget && !targetIds.isEmpty()) {
                    // Spell targets both a spell on the stack and permanent(s) (e.g. Lost in the Mist)
                    StackEntry entry = new StackEntry(
                            entryType, card, playerId, card.getName(),
                            filteredSpellEffects, resolvedXValue, targetId,
                            null, Map.of(), Zone.STACK, List.of(), targetIds
                    );
                    entry.setPrimaryTargetStoredSeparately(
                            firstDeclaredGroupTargetsSpell || allSpellTargetsAlsoAllowPermanents);
                    gameData.stack.add(entry);
                } else {
                    gameData.stack.add(new StackEntry(
                            entryType, card, playerId, card.getName(),
                            filteredSpellEffects, resolvedXValue, targetId,
                            null, Map.of(), Zone.STACK, List.of(), List.of()
                    ));
                }
            } else if (kicked && targetId != null && !targetIds.isEmpty()) {
                // Kicked spell with primary target + additional kicked target(s)
                // (e.g. Goblin Barrage: primary = creature, kicked = player)
                for (UUID kickerTargetId : targetIds) {
                    if (!gameData.playerIds.contains(kickerTargetId)) {
                        Permanent kickerTarget = gameQueryService.findPermanentById(gameData, kickerTargetId);
                        if (kickerTarget == null) {
                            throw new IllegalStateException("Invalid kicker target");
                        }
                    }
                }
                List<UUID> kickedTargetIds = targetIds;
                // The primary target belongs in the flat list only when the first declared group
                // actually consumes it. A zero-sized first group is an indexing placeholder for a
                // primary target stored on the entry itself (for example, Orim's Thunder).
                if (card.getSpellTargets().size() > 1
                        && card.getSpellTargets().getFirst().getKickedMaxTargets() > 0) {
                    kickedTargetIds = new ArrayList<>();
                    kickedTargetIds.add(targetId);
                    kickedTargetIds.addAll(targetIds);
                }
                StackEntry kickedEntry = new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId,
                        null, Map.of(), null, List.of(), kickedTargetIds
                );
                kickedEntry.setPrimaryTargetStoredSeparately(false);
                gameData.stack.add(kickedEntry);
            } else if (!targetIds.isEmpty()
                    && card.getMultiTargetConstraint() == MultiTargetConstraint.AT_MOST_ONE_PER_COLOR
                    && needsGraveyardEffectTargeting && targetId == null) {
                var assignment = targetGroupAssignmentService.assignDistinctColors(gameData, targetIds)
                        .orElseThrow(() -> new IllegalStateException("Must choose at most one card for each color"));
                StackEntry entry = new StackEntry(
                        entryType, card, playerId, card.getName(), filteredSpellEffects, resolvedXValue,
                        null, null, Map.of(), Zone.GRAVEYARD, List.of(), assignment.orderedTargetIds());
                entry.setTargetGroupSizes(assignment.groupSizes());
                gameData.stack.add(entry);
            } else if (!targetIds.isEmpty() && (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting) && targetId != null) {
                // Combined graveyard + permanent targeting (e.g. Yawgmoth's Vile Offering)
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId,
                        null, Map.of(), Zone.GRAVEYARD, List.of(), targetIds
                ));
            } else if (targetId != null && !targetIds.isEmpty() && !additionalCosts.sacrificeAllCreatures()) {
                // Preserve a separately transported target alongside modal target groups (e.g.
                // Grab the Reins entwined: targetIds contains group 0's control target, while
                // targetId contains group 1's damage target).
                StackEntry entry = new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId,
                        null, Map.of(), targetingSpellOnStack ? Zone.STACK : null,
                        List.of(), targetIds
                );
                entry.setPrimaryTargetStoredSeparately(
                        firstDeclaredGroupTargetsSpell || allSpellTargetsAlsoAllowPermanents);
                gameData.stack.add(entry);
            } else if (!targetIds.isEmpty() && !additionalCosts.sacrificeAllCreatures()) {
                // Multi-target spell (e.g. "one or two target creatures each get +2/+1")
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetIds
                ));
            } else if (needsGraveyardOrExileTargeting) {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId, null,
                        Map.of(), graveyardOrExileTargetZone, List.of(), List.of()
                ));
            } else if (needsExileTargeting && targetIsExiledCard) {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId, null,
                        Map.of(), Zone.EXILE, List.of(), List.of()
                ));
            } else if (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting) {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId, null,
                        Map.of(), Zone.GRAVEYARD, List.of(), List.of()
                ));
            } else {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId, null
                ));
            }
            if (adventure && !gameData.stack.isEmpty()) {
                gameData.stack.getLast().setPhysicalCard(physicalHandCard);
                gameData.stack.getLast().setCastWithAdventure(true);
            }
            if (kicked && kickerEffect != null && !gameData.stack.isEmpty()) {
                gameData.stack.getLast().setKicked(true);
            }
            if (buyback && buybackEffect != null && !gameData.stack.isEmpty()) {
                gameData.stack.getLast().setBuyback(true);
            }
            if (!gameData.stack.isEmpty()) {
                stampSacrificedCostSnapshot(gameData.stack.getLast(), additionalCostPayment);
            }
            if (additionalCostPayment.sacrificedCardSnapshot() != null && !gameData.stack.isEmpty()) {
                gameData.stack.getLast().setSacrificedCard(additionalCostPayment.sacrificedCardSnapshot());
            }
            if (additionalCostPayment.exiledCostCardId() != null && !gameData.stack.isEmpty()) {
                gameData.stack.getLast().setExiledCostCardId(additionalCostPayment.exiledCostCardId());
                gameData.stack.getLast().setExiledCostCardSnapshot(additionalCostPayment.exiledCostCardSnapshot());
            }
            if (!gameData.stack.isEmpty()) {
                gameData.stack.getLast().setPutCounterCostPaid(
                        isPutCounterCostPaid(additionalCosts, paymentCostSelection));
                gameData.stack.getLast().setCollectEvidenceCostPaid(
                        usingCollectEvidenceAlternativeCost
                                || isCollectEvidenceCostPaid(additionalCosts, paymentCostSelection));
                gameData.stack.getLast().setBeholdCostPaid(
                        isBeholdCostPaid(additionalCosts, paymentCostSelection));
                gameData.stack.getLast().setWaterbendCostPaid(waterbendCostPaid);
            }
            if (!repeatedAdditionalCosts.isEmpty() && !gameData.stack.isEmpty()) {
                gameData.stack.getLast().setRepeatedAdditionalCosts(List.copyOf(repeatedAdditionalCosts));
            }
            // Prowl (CR 702.75): flag the sorcery/instant entry so a "if this spell's prowl cost was
            // paid" SPELL effect can gate on it (e.g. Notorious Throng's extra turn).
            if (usingAlternateCost && !gameData.stack.isEmpty()) {
                gameData.stack.getLast().setAlternateCost(true);
                AlternateHandCast altHandCast = card.getCastingOption(AlternateHandCast.class).orElse(null);
                if (altHandCast != null && !altHandCast.prowlDamageSubtypes().isEmpty()) {
                    gameData.stack.getLast().setProwl(true);
                }
                if (altHandCast != null && altHandCast.spectacle()) {
                    gameData.stack.getLast().setSpectacle(true);
                }
            }
            // Overload (CR 702.96a): flag the entry so the "target"→"each" text change is applied
            // again when the spell resolves.
            if (overloaded && !gameData.stack.isEmpty()) {
                gameData.stack.getLast().setOverloaded(true);
            }
            if (!gameData.stack.isEmpty()) {
                gameData.stack.getLast().setBeholdChosenSubtype(beholdChosenSubtype);
                gameData.stack.getLast().setChosenCreatureType(chosenCreatureType);
                gameData.stack.getLast().setSourceZone(Zone.HAND);
                if (wasModal) {
                    gameData.stack.getLast().setModalModeCount(modalModeCount);
                }
            }
            gameData.stack.stream()
                    .filter(stackEntry -> stackEntry.getCard().getId().equals(card.getId()))
                    .findFirst()
                    .ifPresent(stackEntry -> stackEntry.setGiftPromised(giftPromised));
            finishSpellCast(gameData, playerId, player, hand, card);
        }
    }

    private void validateCastTimeCreatureTypeChoice(GameData gameData, UUID playerId, UUID targetId,
                                                    List<UUID> targetIds, List<CardEffect> effects,
                                                    CardSubtype chosenCreatureType) {
        boolean requiresChoice = effects.stream()
                .filter(CastTimeCreatureTypeChoiceEffect.class::isInstance)
                .map(CastTimeCreatureTypeChoiceEffect.class::cast)
                .anyMatch(CastTimeCreatureTypeChoiceEffect::requiresCastTimeCreatureTypeChoice);
        if (!requiresChoice) {
            return;
        }
        if (chosenCreatureType == null || !gameQueryService.isCreatureSubtype(chosenCreatureType)) {
            throw new IllegalStateException("Must choose a creature type");
        }

        PermanentPredicate chosenTypeFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(chosenCreatureType)));
        List<UUID> announcedTargets = !targetIds.isEmpty()
                ? targetIds
                : targetId != null ? List.of(targetId) : List.of();
        for (UUID announcedTarget : announcedTargets) {
            Permanent target = gameQueryService.findPermanentById(gameData, announcedTarget);
            if (target == null || !predicateEvaluationService.matchesPermanentPredicate(
                    gameData, target, chosenTypeFilter)) {
                throw new IllegalStateException("Targets must have the chosen creature type");
            }
        }
    }

    private static List<CardEffect> effectsForTargetPosition(Card card, List<CardEffect> effects,
                                                             int targetPosition) {
        List<CardEffect> positionEffects = effects.stream()
                .filter(effect -> card.getEffectTargetIndex(effect) == targetPosition
                        || card.getEffectTargetIndex(effect) < 0)
                .toList();
        return positionEffects.isEmpty() ? effects : positionEffects;
    }

    // --- Additional cast-cost payment (validated up front by AdditionalSpellCostService) ---

    private void deferSpellCastCostTriggers(GameData gameData, int stackBeforeCastingCosts) {
        if (gameData.stack.size() <= stackBeforeCastingCosts) return;

        gameData.pendingSpellCastCostTriggers.addAll(
                new ArrayList<>(gameData.stack.subList(stackBeforeCastingCosts, gameData.stack.size())));
        gameData.stack.subList(stackBeforeCastingCosts, gameData.stack.size()).clear();
    }

    private void validateSharedColorDiscardDoesNotOverlapAdditionalCosts(
            AdditionalSpellCostService.ExtractedCosts costs,
            AdditionalSpellCostService.CostSelection selection,
            Integer sharedColorDiscardHandCardIndex, Card card) {
        if (sharedColorDiscardHandCardIndex == null) {
            return;
        }
        if ((costs.discardCardOrPayManaCost() != null || costs.discardCost() != null)
                && sharedColorDiscardHandCardIndex.equals(selection.discardHandCardIndex())) {
            throw new IllegalStateException("The same card cannot pay Dream Halls and an additional discard cost for "
                    + card.getName());
        }
        if (costs.discardXCardsCost() != null || costs.escalateDiscardCost() != null) {
            if (selection.discardHandCardIndices() != null
                    && selection.discardHandCardIndices().contains(sharedColorDiscardHandCardIndex)) {
                throw new IllegalStateException("The same card cannot pay Dream Halls and an additional discard cost for "
                        + card.getName());
            }
        }
    }

    private AdditionalSpellCostService.CostSelection adjustCostSelectionAfterSharedColorDiscard(
            AdditionalSpellCostService.CostSelection selection, Integer sharedColorDiscardHandCardIndex) {
        if (sharedColorDiscardHandCardIndex == null) {
            return selection;
        }
        Integer discardHandCardIndex = adjustAfterHandRemoval(
                selection.discardHandCardIndex(), sharedColorDiscardHandCardIndex);
        List<Integer> discardHandCardIndices = selection.discardHandCardIndices() == null
                ? null
                : selection.discardHandCardIndices().stream()
                        .map(index -> adjustAfterHandRemoval(index, sharedColorDiscardHandCardIndex))
                        .toList();
        return new AdditionalSpellCostService.CostSelection(
                selection.sacrificePermanentId(), selection.exileGraveyardCardIndex(),
                selection.exileGraveyardCardIndices(), discardHandCardIndex, discardHandCardIndices,
                selection.escalateModeCount(), selection.spellCardIndex(), selection.sacrificePermanentIds(),
                selection.beholdPermanentId(), selection.beholdHandCardIndex(), selection.beholdPermanentIds(),
                selection.beholdHandCardIndices(), selection.beholdChosenSubtype(),
                selection.payLifeForAdditionalCost());
    }

    private Integer adjustAfterHandRemoval(Integer handCardIndex, int removedHandCardIndex) {
        if (handCardIndex == null) {
            return null;
        }
        return handCardIndex > removedHandCardIndex ? handCardIndex - 1 : handCardIndex;
    }

    /**
     * Pays every additional cast cost extracted from the spell, in the canonical order matching
     * {@code AdditionalSpellCostService.validateAll}. Only called after that validation passed,
     * so no throw in here can strand a partially paid cast.
     */
    private boolean isPutCounterCostPaid(AdditionalSpellCostService.ExtractedCosts costs,
                                         AdditionalSpellCostService.CostSelection selection) {
        return costs.putCounterCost() != null && selection.sacrificePermanentId() != null;
    }

    private boolean isCollectEvidenceCostPaid(AdditionalSpellCostService.ExtractedCosts costs,
                                              AdditionalSpellCostService.CostSelection selection) {
        return costs.collectEvidenceCost() != null
                && selection.exileGraveyardCardIndices() != null
                && !selection.exileGraveyardCardIndices().isEmpty();
    }

    private boolean isBeholdCostPaid(AdditionalSpellCostService.ExtractedCosts costs,
                                     AdditionalSpellCostService.CostSelection selection) {
        if (costs.beholdSelectionCost() == null) {
            return false;
        }
        return (selection.beholdPermanentIds() != null && !selection.beholdPermanentIds().isEmpty())
                || (selection.beholdHandCardIndices() != null && !selection.beholdHandCardIndices().isEmpty());
    }

    private boolean isWaterbendCostPaid(AdditionalSpellCostService.ExtractedCosts costs,
                                        boolean waterbendPaid) {
        return costs.waterbendCost() != null
                && (!costs.waterbendCost().optional() || waterbendPaid);
    }

    private void validateCardFlashAdditionalCost(GameData gameData, Player player, Card card,
                                                 AdditionalSpellCostService.ExtractedCosts costs,
                                                 AdditionalSpellCostService.CostSelection selection) {
        BeholdCost beholdCost = costs.beholdSelectionCost();
        if (beholdCost == null
                || !beholdCost.optional()
                || !castingPermissionService.isUsingCardFlashPermission(gameData, player.getId(), card)) {
            return;
        }
        if (!isBeholdCostPaid(costs, selection)) {
            throw new IllegalStateException("Must behold a " + beholdCost.subtype().name().toLowerCase()
                    + " to cast " + card.getName() + " at instant speed");
        }
        additionalSpellCostService.validateBeholdCost(gameData, player, card, beholdCost, selection);
    }

    private AdditionalCostPayment payAdditionalCosts(GameData gameData, Player player, Card card,
                                                     AdditionalSpellCostService.ExtractedCosts costs,
                                                     AdditionalSpellCostService.CostSelection selection,
                                                     int resolvedXValue) {
        return payAdditionalCosts(gameData, player, card, costs, selection, resolvedXValue, null,
                resolvedXValue);
    }

    private AdditionalCostPayment payAdditionalCosts(GameData gameData, Player player, Card card,
                                                     AdditionalSpellCostService.ExtractedCosts costs,
                                                     AdditionalSpellCostService.CostSelection selection,
                                                     int resolvedXValue, ManaPool preManaPaymentPool) {
        return payAdditionalCosts(gameData, player, card, costs, selection, resolvedXValue,
                preManaPaymentPool, resolvedXValue);
    }

    private AdditionalCostPayment payAdditionalCosts(GameData gameData, Player player, Card card,
                                                     AdditionalSpellCostService.ExtractedCosts costs,
                                                     AdditionalSpellCostService.CostSelection selection,
                                                     int resolvedXValue, ManaPool preManaPaymentPool,
                                                     int announcedXValue) {
        return payAdditionalCosts(gameData, player, card, costs, selection, resolvedXValue,
                preManaPaymentPool, announcedXValue, null);
    }

    private AdditionalCostPayment payAdditionalCosts(GameData gameData, Player player, Card card,
                                                     AdditionalSpellCostService.ExtractedCosts costs,
                                                     AdditionalSpellCostService.CostSelection selection,
                                                     int resolvedXValue, ManaPool preManaPaymentPool,
                                                     int announcedXValue,
                                                     Integer collectEvidenceMinimumManaValue) {
        if (costs.payXLife()) {
            payXLifeCost(gameData, player, card, resolvedXValue);
        }
        if (costs.payLifeCost() != null) {
            payLifeCost(gameData, player, card, costs.payLifeCost());
        }
        payLifeOrPayManaCost(gameData, player, card, costs.payLifeOrPayManaCost(),
                selection.payLifeForAdditionalCost(), preManaPaymentPool);
        if (costs.payLifeOrSacrificePermanentCost() != null) {
            PayLifeOrSacrificePermanentCost cost = costs.payLifeOrSacrificePermanentCost();
            if (selection.sacrificePermanentId() == null) {
                payLifeCost(gameData, player, card, new PayLifeCost(cost.lifeAmount()));
            } else {
                paySingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                        "a creature or enchantment",
                        p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
            }
        }
        SacrificeCostPayment sacrificeCostPayment = payAllSacrificeCosts(
                gameData, player, card, selection.sacrificePermanentId(), costs, resolvedXValue);
        resolvedXValue = sacrificeCostPayment.resolvedXValue();
        paySacrificePermanentOrDiscardCardCost(gameData, player, card,
                costs.sacrificePermanentOrDiscardCardCost(), selection.sacrificePermanentId(),
                selection.discardHandCardIndex(), selection.spellCardIndex());
        SacrificeCostPayment discardOrSacrificeCostPayment = payDiscardCardOrSacrificePermanentCost(
                gameData, player, card, costs.discardCardOrSacrificePermanentCost(),
                selection.sacrificePermanentId(), selection.discardHandCardIndex(),
                selection.spellCardIndex());
        ExiledCostPayment exiledCostPayment = payExileCreatureCost(
                gameData, player, card, costs.exileCreatureCost(), selection.sacrificePermanentId());
        payMultipleSacrificeCost(gameData, player, card, costs.sacrificeMultiplePermanentsCost(),
                selection.sacrificePermanentIds());
        payEscalateSacrificeCost(gameData, player, card, costs.escalateSacrificeCost(),
                selection.escalateModeCount(), selection.sacrificePermanentIds());
        payEscalateTapCost(gameData, player, card, costs.escalateTapCost(),
                selection.escalateModeCount(), selection.sacrificePermanentIds());
        if (costs.sacrificeAnyNumberCost() != null) {
            resolvedXValue = paySacrificeAnyNumberOfPermanentsCost(gameData, player, card,
                    costs.sacrificeAnyNumberCost(), selection.sacrificePermanentIds());
        }
        if (costs.tapAnyNumberCost() != null) {
            resolvedXValue = payTapAnyNumberOfPermanentsCost(gameData, player, card, costs.tapAnyNumberCost(),
                    selection.sacrificePermanentIds());
        }
        if (costs.tapMultipleCost() != null) {
            payTapMultiplePermanentsCost(gameData, player, card, costs.tapMultipleCost(),
                    selection.sacrificePermanentIds(), announcedXValue);
        }
        if (costs.returnAnyNumberCost() != null) {
            resolvedXValue = payReturnAnyNumberOfPermanentsToHandCost(gameData, player, card,
                    costs.returnAnyNumberCost(), selection.sacrificePermanentIds());
        }
        if (costs.returnPermanentToHand() != null) {
            payReturnPermanentToHandCost(gameData, player, card, costs.returnPermanentToHand(),
                    selection.sacrificePermanentId());
        }
        paySacrificePermanentOrPayManaCost(gameData, player, card, costs.sacrificePermanentOrPayManaCost(),
                selection.sacrificePermanentId(), preManaPaymentPool);
        payForageOrPayManaCost(gameData, player, card, costs.forageOrPayManaCost(), selection,
                preManaPaymentPool);
        if (costs.returnCreatureToHand()) {
            payReturnCreatureToHandCost(gameData, player, card, selection.sacrificePermanentId());
        }
        payBlightCost(gameData, player, card, costs.blightCost(), selection.sacrificePermanentId(), announcedXValue);
        payPutCounterOnControlledCreatureCost(gameData, player, card, costs.putCounterCost(), selection.sacrificePermanentId());
        payPutCountersOnControlledCreatureOrPayManaCost(gameData, player, card,
                costs.putCountersOrPayManaCost(), selection.sacrificePermanentId(), preManaPaymentPool);
        resolvedXValue = payExileGraveyardCost(gameData, player, card, costs.exileGraveyardCost(), selection.exileGraveyardCardIndex(), resolvedXValue);
        resolvedXValue = payExileXCardsFromGraveyardCost(gameData, player, card, costs.exileXCardsCost(), selection.exileGraveyardCardIndices(), resolvedXValue);
        payCollectEvidenceCost(gameData, player, card, costs.collectEvidenceCost(),
                selection.exileGraveyardCardIndices(), collectEvidenceMinimumManaValue);
        payExileNCardsFromGraveyardCost(gameData, player, card, costs.exileNCardsCost(), selection.exileGraveyardCardIndices());
        payDelveCost(gameData, player, card, costs.delveCost(), selection.exileGraveyardCardIndices());
        payDiscardCardOrPayManaCost(gameData, player, card, costs.discardCardOrPayManaCost(),
                selection.discardHandCardIndex(), selection.spellCardIndex(), preManaPaymentPool);
        payDiscardCardOrPayLifeCost(gameData, player, card, costs.discardCardOrPayLifeCost(),
                selection.discardHandCardIndex(), selection.spellCardIndex());
        resolvedXValue = payRevealCardFromHandCost(gameData, player, card, costs.revealCardCost(),
                selection.discardHandCardIndex(), selection.spellCardIndex(), resolvedXValue);
        if (costs.discardCost() != null && costs.discardCost().count() == 1) {
            payDiscardCost(gameData, player, card, costs.discardCost(), selection.discardHandCardIndex(), selection.spellCardIndex());
        } else if (costs.discardCost() != null) {
            payDiscardCardsCost(gameData, player, card, costs.discardCost(),
                    selection.discardHandCardIndices(), selection.spellCardIndex());
        }
        payRandomDiscardCost(gameData, player, card, costs.discardRandomCost());
        if (costs.discardHand()) {
            payDiscardHandCost(gameData, player, card);
        }
        if (costs.discardXCardsCost() != null) {
            int discardedManaValue = payDiscardXCardsCost(gameData, player, card, costs.discardXCardsCost(), resolvedXValue,
                    selection.discardHandCardIndices(), selection.spellCardIndex());
            if (costs.discardXCardsCost().trackManaValue()) {
                resolvedXValue = discardedManaValue;
            }
        }
        payEscalateDiscardCost(gameData, player, card, costs.escalateDiscardCost(),
                selection.escalateModeCount(), selection.discardHandCardIndices(), selection.spellCardIndex());
        SacrificeCostPayment effectiveSacrificePayment = sacrificeCostPayment.sacrificedCardId() != null
                ? sacrificeCostPayment : discardOrSacrificeCostPayment;
        return new AdditionalCostPayment(resolvedXValue, effectiveSacrificePayment.sacrificedCardId(),
                effectiveSacrificePayment.sacrificedCardSnapshot(),
                effectiveSacrificePayment.sacrificedPower(), effectiveSacrificePayment.sacrificedToughness(),
                exiledCostPayment.cardId(),
                exiledCostPayment.cardSnapshot());
    }

    private ExiledCostPayment payExileCreatureCost(GameData gameData, Player player, Card card,
                                                    ExileCreatureCost cost, UUID permanentId) {
        if (cost == null) {
            return new ExiledCostPayment(null, null);
        }
        Permanent toExile = additionalSpellCostService.validateSingleSacrificeCost(gameData, player, card,
                permanentId, "a creature", p -> gameQueryService.isCreature(gameData, p));
        Card exiledCard = toExile.getCard();
        if (!permanentRemovalService.removePermanentToExile(gameData, toExile)) {
            throw new IllegalStateException("Creature is no longer on the battlefield");
        }
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " exiles ")
                .card(exiledCard)
                .text(" to cast ")
                .card(card)
                .text(".")
                .build());
        return new ExiledCostPayment(exiledCard.getId(), exiledCard);
    }

    private BeheldCardPayment payBeholdCost(GameData gameData, Player player, Card card,
                                            BeholdAndExileCost cost,
                                            AdditionalSpellCostService.CostSelection selection) {
        if (cost == null) {
            return null;
        }
        Card beheldCard = additionalSpellCostService.validateBeholdCost(gameData, player, card, cost, selection);
        UUID ownerId = player.getId();
        if (selection.beholdPermanentId() != null) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, selection.beholdPermanentId());
            ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), player.getId());
            if (!permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                throw new IllegalStateException("Beheld permanent is no longer on the battlefield");
            }
        } else {
            List<Card> hand = gameData.playerHands.get(player.getId());
            int selectedIndex = selection.beholdHandCardIndex();
            int effectiveIndex = selection.spellCardIndex() >= 0 && selectedIndex > selection.spellCardIndex()
                    ? selectedIndex - 1 : selectedIndex;
            Card removed = hand.remove(effectiveIndex);
            if (!removed.getId().equals(beheldCard.getId())) {
                throw new IllegalStateException("Beheld hand card changed before payment");
            }
            exileService.exileCard(gameData, ownerId, removed);
        }
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " exiles ")
                .card(beheldCard)
                .text(" to behold it for ")
                .card(card)
                .text(".")
                .build());
        return new BeheldCardPayment(beheldCard, ownerId);
    }

    /**
     * Pays the "pay X life" additional cast cost (Fire Covenant) for the announced X. Legality
     * (life total at least X, CR 119.4) is checked by
     * {@code AdditionalSpellCostService.validatePayXLifeCost} before any cost is consumed.
     */
    private void payXLifeCost(GameData gameData, Player player, Card card, int announcedX) {
        if (announcedX <= 0) {
            return;
        }
        UUID playerId = player.getId();
        int lifeLoss = announcedX * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
        gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - lifeLoss);
        gameData.lifeLostThisTurn.merge(playerId, lifeLoss, Integer::sum);
        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " pays " + lifeLoss + " life to cast " + card.getName() + "."));
    }

    /**
     * Pays a fixed "pay N life" additional cast cost (Fumarole). Legality (life total at least N,
     * CR 119.4) is checked by {@code AdditionalSpellCostService.validatePayLifeCost} before any
     * cost is consumed.
     */
    private void payLifeCost(GameData gameData, Player player, Card card, PayLifeCost cost) {
        UUID playerId = player.getId();
        int amount = cost.effectiveAmount(gameData.getLife(playerId));
        if (amount <= 0) {
            return;
        }
        int lifeLoss = amount * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
        gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - lifeLoss);
        gameData.lifeLostThisTurn.merge(playerId, lifeLoss, Integer::sum);
        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " pays " + lifeLoss + " life to cast " + card.getName() + "."));
    }

    private void payLifeOrPayManaCost(GameData gameData, Player player, Card card,
                                      PayLifeOrPayManaCost cost, Boolean payLife,
                                      ManaPool preManaPaymentPool) {
        if (cost == null) {
            return;
        }
        if (Boolean.TRUE.equals(payLife)) {
            payLifeCost(gameData, player, card, new PayLifeCost(cost.lifeAmount()));
            return;
        }
        try {
            ManaCost extra = new ManaCost(cost.manaCost());
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            int before = pool.getTotalAllMana();
            if (!extra.canPay(pool)) {
                throw new IllegalStateException("Not enough mana to pay " + cost.manaCost() + " for " + card.getName());
            }
            extra.pay(pool);
            gameData.addSpellCastManaSpent(card.getId(), before - pool.getTotalAllMana());
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " pays " + cost.manaCost() + " for ")
                    .card(card)
                    .text(".")
                    .build());
        } catch (IllegalStateException e) {
            if (preManaPaymentPool != null) {
                gameData.playerManaPools.put(player.getId(), preManaPaymentPool);
            }
            throw e;
        }
    }

    /**
     * Pays the "this spell costs N life more to cast for each target" cost increase (Phyrexian
     * Purge). Affordability is checked before any cost is consumed, in the cast branch that
     * computes the amount from the chosen target count.
     */
    private void payPerTargetLifeCost(GameData gameData, Player player, Card card, int amount) {
        if (amount <= 0) {
            return;
        }
        UUID playerId = player.getId();
        int lifeLoss = amount * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
        gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - lifeLoss);
        gameData.lifeLostThisTurn.merge(playerId, lifeLoss, Integer::sum);
        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " pays " + lifeLoss + " life to cast " + card.getName() + "."));
    }

    private void payTargetingLifeCost(GameData gameData, Player player, Card card, int amount) {
        if (amount <= 0) return;
        UUID playerId = player.getId();
        int currentLife = gameData.getLife(playerId);
        if (currentLife < amount) {
            throw new IllegalStateException("Not enough life to pay the targeting life cost");
        }
        int lifeLoss = amount * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
        gameData.playerLifeTotals.put(playerId, currentLife - lifeLoss);
        gameData.lifeLostThisTurn.merge(playerId, lifeLoss, Integer::sum);
        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " pays " + lifeLoss + " life to cast " + card.getName() + "."));
    }

    /**
     * Validates a battlefield-imposed per-mana-symbol sacrifice tax (Drought) before any cost is
     * paid. Requires exactly {@code requirement.count()} distinct matching permanents.
     */
    private void validateImposedSacrificeTax(GameData gameData, Player player, Card card,
                                             List<UUID> imposedSacrificePermanentIds) {
        CastingCostService.ImposedSacrificeRequirement req =
                castingCostService.getImposedSacrificeRequirementForSpell(gameData, card);
        if (req.isEmpty()) {
            return;
        }
        List<UUID> ids = imposedSacrificePermanentIds != null ? imposedSacrificePermanentIds : List.of();
        if (ids.size() != req.count()) {
            throw new IllegalStateException("Must sacrifice " + req.count()
                    + " permanent(s) (" + req.description() + ") to cast " + card.getName());
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate sacrifice targets for imposed tax");
        }
        for (UUID id : ids) {
            additionalSpellCostService.validateSingleSacrificeCost(gameData, player, card, id,
                    req.description(),
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, req.filter()));
        }
    }

    private void payImposedSacrificeTax(GameData gameData, Player player, Card card,
                                        List<UUID> imposedSacrificePermanentIds) {
        CastingCostService.ImposedSacrificeRequirement req =
                castingCostService.getImposedSacrificeRequirementForSpell(gameData, card);
        if (req.isEmpty()) return;
        for (UUID id : imposedSacrificePermanentIds) {
            paySingleSacrificeCost(gameData, player, card, id, req.description(),
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, req.filter()));
        }
    }

    private SacrificeCostPayment payAllSacrificeCosts(GameData gameData, Player player, Card card,
                                                      UUID sacrificePermanentId,
                                                      AdditionalSpellCostService.ExtractedCosts costs,
        int resolvedXValue) {
        UUID sacrificedCardId = null;
        Card sacrificedCardSnapshot = null;
        int sacrificedPower = 0;
        int sacrificedToughness = 0;
        if (costs.sacrificeCreature()) {
            SacrificedCreatureStats stats = paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    "a creature", p -> gameQueryService.isCreature(gameData, p));
            sacrificedCardId = stats.cardId();
            sacrificedCardSnapshot = stats.card();
            SacrificeCreatureCost sacCreatureCost = (SacrificeCreatureCost) card.getEffects(EffectSlot.SPELL).stream()
                    .filter(SacrificeCreatureCost.class::isInstance)
                    .findFirst().orElseThrow();
            if (sacCreatureCost.trackSacrificedManaValue()) {
                resolvedXValue = stats.manaValue();
            }
            if (sacCreatureCost.trackSacrificedPower()) {
                resolvedXValue = stats.power();
                sacrificedPower = stats.power();
            }
            if (sacCreatureCost.trackSacrificedToughness()) {
                resolvedXValue = stats.toughness();
                sacrificedToughness = stats.toughness();
            }
        }
        if (costs.sacrificePermanentCost() != null) {
            SacrificePermanentCost sacPermCost = costs.sacrificePermanentCost();
            SacrificedCreatureStats stats = paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    sacPermCost.description(),
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacPermCost.filter()));
            if (sacrificedCardId == null) {
                sacrificedCardId = stats.cardId();
                sacrificedCardSnapshot = stats.card();
            }
            if (sacPermCost.trackSacrificedManaValue()) {
                resolvedXValue = stats.manaValue();
            }
            if (sacPermCost.trackSacrificedPower()) {
                resolvedXValue = stats.power();
                sacrificedPower = stats.power();
            }
            if (sacPermCost.trackSacrificedToughness()) {
                resolvedXValue = stats.toughness();
                sacrificedToughness = stats.toughness();
            }
        }
        if (costs.sacrificeAllCreatures()) {
            resolvedXValue = paySacrificeAllCreaturesYouControlCost(gameData, player, card);
        }
        if (costs.sacrificeAllPermanents()) {
            paySacrificeAllPermanentsYouControlCost(gameData, player, card);
        }
        return new SacrificeCostPayment(resolvedXValue, sacrificedCardId, sacrificedCardSnapshot,
                sacrificedPower, sacrificedToughness);
    }

    private SacrificeCostPayment payDiscardCardOrSacrificePermanentCost(
            GameData gameData, Player player, Card card, DiscardCardOrSacrificePermanentCost cost,
            UUID sacrificePermanentId, Integer discardHandCardIndex, int spellCardIndex) {
        if (cost == null) {
            return new SacrificeCostPayment(0, null, null, 0, 0);
        }
        if (sacrificePermanentId != null) {
            SacrificedCreatureStats stats = paySingleSacrificeCost(gameData, player, card,
                    sacrificePermanentId, cost.description(),
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
            return new SacrificeCostPayment(0, stats.cardId(), stats.card(), 0, 0);
        }
        payDiscardCost(gameData, player, card, new DiscardCardTypeCost(null, null),
                discardHandCardIndex, spellCardIndex);
        return new SacrificeCostPayment(0, null, null, 0, 0);
    }

    /**
     * Pays a multi-permanent sacrifice additional cast cost (Phyrexian Tribute). Legality is
     * checked by {@code AdditionalSpellCostService.validateMultipleSacrificeCost} before any cost
     * is consumed, so each sacrifice here is already known to be legal.
     */
    private void payMultipleSacrificeCost(GameData gameData, Player player, Card card,
                                          SacrificeMultiplePermanentsCost cost, List<UUID> sacrificePermanentIds) {
        if (cost == null) {
            return;
        }
        for (UUID id : sacrificePermanentIds) {
            paySingleSacrificeCost(gameData, player, card, id, "a matching permanent",
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
        }
    }

    /** Pays the permanent cost for each mode chosen beyond the first. */
    private void payEscalateSacrificeCost(GameData gameData, Player player, Card card,
                                          EscalateSacrificeCost cost, int modesChosen,
                                          List<UUID> sacrificePermanentIds) {
        if (cost == null) {
            return;
        }
        int required = cost.count() * Math.max(0, modesChosen - 1);
        if (required == 0) {
            return;
        }
        for (UUID id : sacrificePermanentIds) {
            paySingleSacrificeCost(gameData, player, card, id, "a matching permanent",
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
        }
    }

    private void payEscalateTapCost(GameData gameData, Player player, Card card,
                                    EscalateTapCost cost, int modesChosen, List<UUID> tapPermanentIds) {
        if (cost == null) {
            return;
        }
        List<Permanent> toTap = additionalSpellCostService.validateEscalateTapCost(
                gameData, player, card, cost, modesChosen, tapPermanentIds);
        triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
        try {
            for (Permanent permanent : toTap) {
                permanent.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " taps ")
                        .card(permanent.getCard())
                        .text("to escalate ")
                        .card(card)
                        .text(".")
                        .build());
            }
        } finally {
            triggerCollectionService.endPermanentTapTriggerBatch(gameData);
        }
    }

    /**
     * Pays the "sacrifice any number of permanents you control" additional cast cost and returns
     * either the number sacrificed or their total effective power, depending on the cost. Power
     * is snapshotted before any selected permanent is removed. Legality is checked by
     * {@code AdditionalSpellCostService.validateSacrificeAnyNumberOfPermanentsCost} before any cost
     * is consumed.
     */
    private int paySacrificeAnyNumberOfPermanentsCost(GameData gameData, Player player, Card card,
                                                      SacrificeAnyNumberOfPermanentsCost cost,
                                                      List<UUID> sacrificePermanentIds) {
        List<UUID> ids = sacrificePermanentIds != null ? sacrificePermanentIds : List.of();
        List<Permanent> toSacrifice = additionalSpellCostService.validateSacrificeAnyNumberOfPermanentsCost(
                gameData, player, card, cost, ids);
        int resolvedXValue = cost.trackSacrificedPower()
                ? toSacrifice.stream()
                        .mapToInt(permanent -> gameQueryService.getEffectivePower(gameData, permanent))
                        .sum()
                : toSacrifice.size();
        for (Permanent permanent : toSacrifice) {
            paySingleSacrificeCost(gameData, player, card, permanent.getId(), "a matching permanent",
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
        }
        return Math.max(0, resolvedXValue);
    }

    /**
     * Pays the "tap any number of untapped permanents you control" additional cast cost (Burn at
     * the Stake) and returns the number tapped, which becomes the spell's X value so a companion
     * effect can scale with it. Legality is checked by
     * {@code AdditionalSpellCostService.validateTapAnyNumberOfPermanentsCost} before any cost is
     * consumed.
     */
    private int payTapAnyNumberOfPermanentsCost(GameData gameData, Player player, Card card,
                                                TapAnyNumberOfPermanentsCost cost, List<UUID> tapPermanentIds) {
        List<Permanent> toTap = additionalSpellCostService.validateTapAnyNumberOfPermanentsCost(
                gameData, player, card, cost, tapPermanentIds);
        triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
        try {
            for (Permanent permanent : toTap) {
                permanent.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " taps ")
                        .card(permanent.getCard())
                        .text(" to cast ")
                        .card(card)
                        .text(".")
                        .build());
            }
        } finally {
            triggerCollectionService.endPermanentTapTriggerBatch(gameData);
        }
        return toTap.size();
    }

    /** Pays an exact-count untapped-permanent tap cost after cast-time validation. */
    private void payTapMultiplePermanentsCost(GameData gameData, Player player, Card card,
                                              TapMultiplePermanentsCost cost, List<UUID> tapPermanentIds,
                                              int announcedXValue) {
        List<Permanent> toTap = additionalSpellCostService.validateTapMultiplePermanentsCost(
                gameData, player, card, cost, tapPermanentIds, announcedXValue);
        triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
        try {
            for (Permanent permanent : toTap) {
                permanent.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " taps ")
                        .card(permanent.getCard())
                        .text(" to cast ")
                        .card(card)
                        .text(".")
                        .build());
            }
        } finally {
            triggerCollectionService.endPermanentTapTriggerBatch(gameData);
        }
    }

    /** Applies the selected artifact/creature contributions to a spell's Waterbend cost. */
    private List<ManaColor> applyWaterbendCost(GameData gameData, Player player, Card card,
                                                WaterbendCost cost, List<UUID> waterbendPermanentIds,
                                                int announcedXValue, boolean waterbendPaid) {
        if (cost == null || !waterbendPaid) {
            return List.of();
        }
        List<Permanent> toTap = additionalSpellCostService.validateWaterbendCost(
                gameData, player, card, cost, waterbendPermanentIds, announcedXValue);
        for (Permanent permanent : toTap) {
            permanent.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " taps ")
                    .card(permanent.getCard())
                    .text(" as a waterbend cost for ")
                    .card(card)
                    .text(".")
                    .build());
        }
        triggerCollectionService.checkBendingTriggers(gameData, player.getId(), BendingType.WATERBEND);
        return java.util.Collections.nCopies(toTap.size(), null);
    }

    private List<ManaColor> appendManaContributions(List<ManaColor> existing, List<ManaColor> additions) {
        if (additions.isEmpty()) {
            return existing;
        }
        List<ManaColor> combined = new ArrayList<>(existing);
        combined.addAll(additions);
        return java.util.Collections.unmodifiableList(combined);
    }

    /**
     * Pays the "return any number of permanents you control to their owner's hand" additional cast
     * cost (Infernal Harvest) and returns the number returned, which becomes the spell's X value so
     * a companion effect can scale with it. Legality is checked by
     * {@code AdditionalSpellCostService.validateReturnAnyNumberOfPermanentsToHandCost} before any
     * cost is consumed.
     */
    private int payReturnAnyNumberOfPermanentsToHandCost(GameData gameData, Player player, Card card,
                                                         ReturnAnyNumberOfPermanentsToHandCost cost,
                                                         List<UUID> returnPermanentIds) {
        List<Permanent> toReturn = additionalSpellCostService.validateReturnAnyNumberOfPermanentsToHandCost(
                gameData, player, card, cost, returnPermanentIds);
        for (Permanent permanent : toReturn) {
            permanentRemovalService.removePermanentToHand(gameData, permanent);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " returns ")
                    .card(permanent.getCard())
                    .text(" to hand to cast ")
                    .card(card)
                    .text(".")
                    .build());
        }
        return toReturn.size();
    }

    /** Pays the "return a permanent you control to its owner's hand" additional cast cost. */
    private void payReturnPermanentToHandCost(GameData gameData, Player player, Card card,
                                               ReturnPermanentToHandCost cost, UUID returnPermanentId) {
        Permanent toReturn = additionalSpellCostService.validateReturnPermanentToHandCost(
                gameData, player, card, cost, returnPermanentId);
        permanentRemovalService.removePermanentToHand(gameData, toReturn);
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " returns ")
                .card(toReturn.getCard())
                .text(" to hand to cast ")
                .card(card)
                .text(".")
                .build());
    }

    private record AdditionalCostPayment(int resolvedXValue, UUID sacrificedCardId,
                                         Card sacrificedCardSnapshot, int sacrificedPower,
                                         int sacrificedToughness, UUID exiledCostCardId,
                                         Card exiledCostCardSnapshot) {}

    private record SacrificeCostPayment(int resolvedXValue, UUID sacrificedCardId,
                                        Card sacrificedCardSnapshot, int sacrificedPower,
                                        int sacrificedToughness) {}

    private void stampSacrificedCostSnapshot(StackEntry entry, AdditionalCostPayment payment) {
        if (payment.sacrificedCardId() == null) return;
        entry.setSacrificedCardId(payment.sacrificedCardId());
        entry.setSacrificedCardSnapshot(payment.sacrificedCardSnapshot());
        entry.setSacrificedPower(payment.sacrificedPower());
        entry.setSacrificedToughness(payment.sacrificedToughness());
    }

    private record ExiledCostPayment(UUID cardId, Card cardSnapshot) {}

    private record SacrificedCreatureStats(UUID cardId, Card card, int manaValue, int power, int toughness) {}

    private SacrificedCreatureStats paySingleSacrificeCost(GameData gameData, Player player, Card sourceCard,
                                       UUID sacrificePermanentId, String typeDescription,
                                       Predicate<Permanent> typeCheck) {
        Permanent toSacrifice = additionalSpellCostService.validateSingleSacrificeCost(gameData, player, sourceCard,
                sacrificePermanentId, typeDescription, typeCheck);
        int manaValue = toSacrifice.getCard().getManaValue();
        int power = gameQueryService.getEffectivePower(gameData, toSacrifice);
        int toughness = gameQueryService.getEffectiveToughness(gameData, toSacrifice);
        if (permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice)) {
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " sacrifices ")
                    .card(toSacrifice.getCard())
                    .text(" for ")
                    .card(sourceCard)
                    .text(".")
                    .build());
            triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, player.getId(), toSacrifice.getCard(), sourceCard);
        }
        return new SacrificedCreatureStats(toSacrifice.getCard().getId(), toSacrifice.getCard(), manaValue, power, toughness);
    }

    private int paySacrificeAllCreaturesYouControlCost(GameData gameData, Player player, Card sourceCard) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Permanent> creaturesToSacrifice = battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .toList();

        // Snapshot total power first, because all chosen creatures are sacrificed together.
        int totalPower = 0;
        for (Permanent creature : creaturesToSacrifice) {
            totalPower += gameQueryService.getEffectivePower(gameData, creature);
        }
        for (Permanent creature : creaturesToSacrifice) {
            if (permanentRemovalService.removePermanentToGraveyard(gameData, creature)) {
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " sacrifices ")
                        .card(creature.getCard())
                        .text(" for ")
                        .card(sourceCard)
                        .text(".")
                        .build());
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, player.getId(), creature.getCard(), sourceCard);
            }
        }
        return Math.max(0, totalPower);
    }

    private void paySacrificeAllPermanentsYouControlCost(GameData gameData, Player player, Card sourceCard) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Permanent> toSacrifice = List.copyOf(battlefield);
        for (Permanent permanent : toSacrifice) {
            if (permanentRemovalService.removePermanentToGraveyard(gameData, permanent)) {
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " sacrifices ")
                        .card(permanent.getCard())
                        .text(" for ")
                        .card(sourceCard)
                        .text(".")
                        .build());
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, permanent.getCard(), sourceCard);
            }
        }
    }

    private void payDiscardHandCost(GameData gameData, Player player, Card sourceCard) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }
        List<Card> discarded = new ArrayList<>(hand);
        hand.clear();
        gameData.discardCausedByOpponent = false;
        triggerCollectionService.beginDiscardEvent(gameData, playerId);
        for (Card discardedCard : discarded) {
            graveyardService.addCardToGraveyard(gameData, playerId, discardedCard);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, discardedCard);
        }
        triggerCollectionService.finishDiscardEvent(gameData);
        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " discards their hand (" + discarded.size()
                        + " card" + (discarded.size() != 1 ? "s" : "") + ") to cast "
                        + sourceCard.getName() + "."));
    }

    /**
     * Pays "sacrifice a permanent or pay {mana}". Sacrifice when an id is supplied; otherwise
     * pays the alternate mana from the remaining pool (base mana already paid). Restores
     * {@code preManaPaymentPool} if the alternate mana cannot be paid (CR 601.2h).
     */
    private void paySacrificePermanentOrPayManaCost(GameData gameData, Player player, Card card,
                                                   SacrificePermanentOrPayManaCost cost, UUID sacrificePermanentId,
                                                   ManaPool preManaPaymentPool) {
        if (cost == null) {
            return;
        }
        if (sacrificePermanentId != null) {
            paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    cost.description(), p -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, p, cost.filter()));
            return;
        }
        try {
            ManaCost extra = new ManaCost(cost.manaCost());
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            int before = pool.getTotalAllMana();
            if (!extra.canPay(pool)) {
                throw new IllegalStateException("Not enough mana to pay " + cost.manaCost() + " for " + card.getName());
            }
            extra.pay(pool);
            gameData.addSpellCastManaSpent(card.getId(), before - pool.getTotalAllMana());
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " pays " + cost.manaCost() + " for ")
                    .card(card)
                    .text(".")
                    .build());
        } catch (IllegalStateException e) {
            if (preManaPaymentPool != null) {
                gameData.playerManaPools.put(player.getId(), preManaPaymentPool);
            }
            throw e;
        }
    }

    private void payForageOrPayManaCost(GameData gameData, Player player, Card card,
                                         ForageOrPayManaCost cost,
                                         AdditionalSpellCostService.CostSelection selection,
                                         ManaPool preManaPaymentPool) {
        if (cost == null) {
            return;
        }
        if (selection.sacrificePermanentId() != null) {
            paySingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                    "a Food", permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, cost.consumedPermanentFilter()));
            triggerCollectionService.checkForageTriggers(gameData, player.getId());
            return;
        }
        List<Integer> graveyardIndices = selection.exileGraveyardCardIndices();
        if (graveyardIndices != null && !graveyardIndices.isEmpty()) {
            payExileNCardsFromGraveyardCost(gameData, player, card,
                    new ExileNCardsFromGraveyardCost(cost.consumedGraveyardCardCount(), null),
                    graveyardIndices);
            triggerCollectionService.checkForageTriggers(gameData, player.getId());
            return;
        }
        if (cost.manaCost() == null) {
            throw new IllegalStateException("Must forage to cast " + card.getName());
        }
        try {
            ManaCost extra = new ManaCost(cost.manaCost());
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            int before = pool.getTotalAllMana();
            if (!extra.canPay(pool)) {
                throw new IllegalStateException(
                        "Not enough mana to pay " + cost.manaCost() + " for " + card.getName());
            }
            extra.pay(pool);
            gameData.addSpellCastManaSpent(card.getId(), before - pool.getTotalAllMana());
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " pays " + cost.manaCost() + " for ")
                    .card(card)
                    .text(".")
                    .build());
        } catch (IllegalStateException e) {
            if (preManaPaymentPool != null) {
                gameData.playerManaPools.put(player.getId(), preManaPaymentPool);
            }
            throw e;
        }
    }

    /**
     * Pays "discard a card or pay {mana}". Discards when a hand index is supplied; otherwise pays
     * the alternate mana from the remaining pool (base mana already paid). Restores
     * {@code preManaPaymentPool} if the alternate mana cannot be paid (CR 601.2h).
     */
    private void payDiscardCardOrPayManaCost(GameData gameData, Player player, Card card,
                                             DiscardCardOrPayManaCost cost, Integer discardHandCardIndex,
                                             int spellCardIndex, ManaPool preManaPaymentPool) {
        if (cost == null) {
            return;
        }
        if (discardHandCardIndex != null) {
            payDiscardCost(gameData, player, card, new DiscardCardTypeCost(null, null),
                    discardHandCardIndex, spellCardIndex);
            return;
        }
        try {
            ManaCost extra = new ManaCost(cost.manaCost());
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            int before = pool.getTotalAllMana();
            if (!extra.canPay(pool)) {
                throw new IllegalStateException("Not enough mana to pay " + cost.manaCost() + " for " + card.getName());
            }
            extra.pay(pool);
            gameData.addSpellCastManaSpent(card.getId(), before - pool.getTotalAllMana());
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " pays " + cost.manaCost() + " for ")
                    .card(card)
                    .text(".")
                    .build());
        } catch (IllegalStateException e) {
            if (preManaPaymentPool != null) {
                gameData.playerManaPools.put(player.getId(), preManaPaymentPool);
            }
            throw e;
        }
    }

    private void paySacrificePermanentOrDiscardCardCost(GameData gameData, Player player, Card card,
                                                        SacrificePermanentOrDiscardCardCost cost,
                                                        UUID sacrificePermanentId,
                                                        Integer discardHandCardIndex,
                                                        int spellCardIndex) {
        if (cost == null) {
            return;
        }
        if (sacrificePermanentId != null) {
            paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    cost.description(), p -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, p, cost.filter()));
        } else {
            payDiscardCost(gameData, player, card, new DiscardCardTypeCost(null, null),
                    discardHandCardIndex, spellCardIndex);
        }
    }

    /** Pays "discard a card or pay N life" as an additional cast cost. */
    private void payDiscardCardOrPayLifeCost(GameData gameData, Player player, Card card,
                                             DiscardCardOrPayLifeCost cost, Integer discardHandCardIndex,
                                             int spellCardIndex) {
        if (cost == null) {
            return;
        }
        if (discardHandCardIndex != null) {
            payDiscardCost(gameData, player, card, new DiscardCardTypeCost(null, null),
                    discardHandCardIndex, spellCardIndex);
            return;
        }
        payLifeCost(gameData, player, card, new PayLifeCost(cost.lifeAmount()));
    }

    private int paySacrificeCreaturesForCostReduction(GameData gameData, Player player, Card card, List<UUID> sacrificeIds) {
        SacrificeCreaturesForCostReductionEffect effect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(SacrificeCreaturesForCostReductionEffect.class::isInstance)
                .map(SacrificeCreaturesForCostReductionEffect.class::cast)
                .findFirst().orElseThrow();

        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        int sacrificedCount = 0;

        for (UUID sacId : sacrificeIds) {
            Permanent toSacrifice = battlefield.stream()
                    .filter(p -> p.getId().equals(sacId))
                    .findFirst().orElse(null);
            if (toSacrifice == null) {
                throw new IllegalStateException("Sacrifice target not found on battlefield");
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, sacId);
            if (!playerId.equals(controllerId)) {
                throw new IllegalStateException("Can only sacrifice permanents you control");
            }
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, toSacrifice, effect.filter())) {
                throw new IllegalStateException("Permanent is not eligible to reduce this spell's cost");
            }
            if (permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice)) {
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " sacrifices ")
                        .card(toSacrifice.getCard())
                        .text(" to reduce the cost of ")
                        .card(card)
                        .text(".")
                        .build());
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, player.getId(), toSacrifice.getCard(), card);
                sacrificedCount++;
            }
        }

        return sacrificedCount * effect.reductionPerCreature();
    }

    private int payExileGraveyardCost(GameData gameData, Player player, Card card,
                                       ExileCardFromGraveyardCost cost, Integer exileGraveyardCardIndex, int resolvedXValue) {
        if (cost == null) return resolvedXValue;
        Card exiledCard = additionalSpellCostService.validateExileGraveyardCost(gameData, player, card, cost, exileGraveyardCardIndex);
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        int exiledPower = exiledCard.getPower() != null ? exiledCard.getPower() : 0;
        int exiledManaValue = exiledCard.getManaValue();
        graveyard.remove((int) exileGraveyardCardIndex);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiledCard);
        gameData.addToExile(playerId, exiledCard);
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " exiles ")
                .card(exiledCard)
                .text(" from graveyard for ")
                .card(card)
                .text(".")
                .build());
        if (cost.trackExiledPower()) {
            resolvedXValue = exiledPower;
        } else if (cost.trackExiledManaValue()) {
            resolvedXValue = exiledManaValue;
        }
        return resolvedXValue;
    }

    private int payExileXCardsFromGraveyardCost(GameData gameData, Player player, Card card,
                                                 ExileXCardsFromGraveyardCost cost, List<Integer> exileGraveyardCardIndices, int resolvedXValue) {
        if (cost == null) return resolvedXValue;
        additionalSpellCostService.validateExileXCardsFromGraveyardCost(gameData, player, card, cost, exileGraveyardCardIndices);
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        // Remove in descending index order so earlier indices remain valid
        List<Integer> sortedDescending = exileGraveyardCardIndices.stream().sorted(java.util.Comparator.reverseOrder()).toList();
        List<Card> exiledCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (int idx : sortedDescending) {
                Card exiledCard = graveyard.remove(idx);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiledCard);
                exiledCards.add(exiledCard);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        for (Card exiledCard : exiledCards) {
            gameData.addToExile(playerId, exiledCard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " exiles ")
                    .card(exiledCard)
                    .text(" from graveyard for ")
                    .card(card)
                    .text(".")
                    .build());
        }
        return exiledCards.size();
    }

    private void payCollectEvidenceCost(GameData gameData, Player player, Card card,
                                         CollectEvidenceCost cost,
                                         List<Integer> exileGraveyardCardIndices) {
        payCollectEvidenceCost(gameData, player, card, cost, exileGraveyardCardIndices, null);
    }

    private void payCollectEvidenceCost(GameData gameData, Player player, Card card,
                                         CollectEvidenceCost cost,
                                         List<Integer> exileGraveyardCardIndices,
                                         Integer collectEvidenceMinimumManaValue) {
        if (cost == null || exileGraveyardCardIndices == null || exileGraveyardCardIndices.isEmpty()) {
            return;
        }
        int minimumManaValue = cost.usesTargetManaValue()
                ? requireResolvedCollectEvidenceMinimum(collectEvidenceMinimumManaValue, card)
                : cost.minimumManaValue();
        additionalSpellCostService.validateCollectEvidenceCost(
                gameData, player, card, cost, exileGraveyardCardIndices, minimumManaValue);
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> sortedDescending = exileGraveyardCardIndices.stream()
                .sorted(java.util.Comparator.reverseOrder()).toList();
        List<Card> exiledCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (int idx : sortedDescending) {
                Card exiledCard = graveyard.remove(idx);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiledCard);
                exiledCards.add(exiledCard);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        for (Card exiledCard : exiledCards) {
            gameData.addToExile(playerId, exiledCard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " exiles ")
                    .card(exiledCard)
                    .text(" from graveyard for ")
                    .card(card)
                    .text(".")
                    .build());
        }
        triggerCollectionService.checkCollectEvidenceTriggers(gameData, playerId);
    }

    private int requireResolvedCollectEvidenceMinimum(Integer resolvedMinimum, Card card) {
        if (resolvedMinimum == null) {
            throw new IllegalStateException("Target-based evidence threshold was not resolved for " + card.getName());
        }
        return resolvedMinimum;
    }

    private void payExileNCardsFromGraveyardCost(GameData gameData, Player player, Card card,
                                                  ExileNCardsFromGraveyardCost cost, List<Integer> exileGraveyardCardIndices) {
        if (cost == null) return;
        additionalSpellCostService.validateExileNCardsFromGraveyardCost(gameData, player, card, cost, exileGraveyardCardIndices, -1);
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        // Remove in descending index order so earlier indices remain valid
        List<Integer> sortedDescending = exileGraveyardCardIndices.stream().sorted(java.util.Comparator.reverseOrder()).toList();
        List<Card> exiledCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (int idx : sortedDescending) {
                Card exiledCard = graveyard.remove(idx);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiledCard);
                exiledCards.add(exiledCard);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        for (Card exiledCard : exiledCards) {
            gameData.addToExile(playerId, exiledCard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " exiles ")
                    .card(exiledCard)
                    .text(" from graveyard for ")
                    .card(card)
                    .text(".")
                    .build());
        }
    }

    private List<Integer> validateEscapeExileCost(GameData gameData, Player player, Card card,
                                                   List<Card> sourceGraveyard, int sourceGraveyardIndex,
                                                   int escapeExileCount, ExileNCardsFromGraveyardCost additionalCost,
                                                   List<Integer> exileGraveyardCardIndices) {
        List<Integer> indices = exileGraveyardCardIndices == null ? List.of() : exileGraveyardCardIndices;
        int additionalCostCount = additionalCost == null ? 0 : additionalCost.count();
        int requiredCount = escapeExileCount + additionalCostCount;
        if (indices.size() != requiredCount) {
            throw new IllegalStateException("Must choose exactly " + requiredCount
                    + " graveyard cards to pay the escape cost for " + card.getName());
        }
        if (indices.stream().distinct().count() != indices.size()) {
            throw new IllegalStateException("Duplicate graveyard card indices");
        }

        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        if (graveyard == null) {
            throw new IllegalStateException("Your graveyard is unavailable");
        }
        int excludedGraveyardIndex = sourceGraveyard == graveyard ? sourceGraveyardIndex : -1;
        int effectiveSize = graveyard.size() - (excludedGraveyardIndex >= 0 ? 1 : 0);
        if (effectiveSize < requiredCount) {
            throw new IllegalStateException("Not enough other cards in graveyard to pay the escape cost");
        }

        int matchingAdditionalCostCards = 0;
        for (int index : indices) {
            if (index < 0 || index >= effectiveSize) {
                throw new IllegalStateException("Invalid graveyard card chosen for the escape cost of "
                        + card.getName());
            }
            int actualIndex = excludedGraveyardIndex >= 0 && index >= excludedGraveyardIndex
                    ? index + 1 : index;
            Card selected = graveyard.get(actualIndex);
            if (selected.getId().equals(card.getId())) {
                throw new IllegalStateException("A spell cannot exile itself for its escape cost");
            }
            if (additionalCost != null
                    && (additionalCost.requiredType() == null || selected.hasType(additionalCost.requiredType()))
                    && (additionalCost.predicate() == null
                    || predicateEvaluationService.matchesCardPredicate(selected, additionalCost.predicate(), null))) {
                matchingAdditionalCostCards++;
            }
        }
        if (matchingAdditionalCostCards < additionalCostCount) {
            throw new IllegalStateException("The selected cards do not satisfy the additional graveyard cost of "
                    + card.getName());
        }
        return List.copyOf(indices);
    }

    private void validateFlashbackGraveyardExileCost(GameData gameData, Player player, Card card,
                                                      List<Card> sourceGraveyard, int sourceGraveyardIndex,
                                                      Optional<FlashbackCast> flashbackOpt, int xValue,
                                                      List<Integer> exileGraveyardCardIndices) {
        var cost = flashbackOpt.flatMap(flashback ->
                flashback.getCost(ExileXCardsFromGraveyardCastingCost.class)).orElse(null);
        if (cost == null) return;

        List<Integer> indices = exileGraveyardCardIndices == null ? List.of() : exileGraveyardCardIndices;
        if (xValue < 0) {
            throw new IllegalStateException("X must not be negative");
        }
        if (indices.size() != xValue) {
            throw new IllegalStateException("Must exile exactly " + xValue + " cards from your graveyard to cast "
                    + card.getName() + " with flashback");
        }
        if (indices.stream().distinct().count() != indices.size()) {
            throw new IllegalStateException("Duplicate cards chosen for the flashback cost of " + card.getName());
        }

        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            throw new IllegalStateException("Your graveyard is unavailable");
        }
        boolean sourceIsCasterGraveyard = sourceGraveyard == graveyard;
        for (int index : indices) {
            if (index < 0 || index >= graveyard.size()) {
                throw new IllegalStateException("Invalid graveyard card chosen for the flashback cost of "
                        + card.getName());
            }
            if ((sourceIsCasterGraveyard && index == sourceGraveyardIndex)
                    || graveyard.get(index).getId().equals(card.getId())) {
                throw new IllegalStateException("A spell cannot exile itself for its flashback cost");
            }
            Card selected = graveyard.get(index);
            if (cost.predicate() != null
                    && !predicateEvaluationService.matchesCardPredicate(selected, cost.predicate(), selected.getId())) {
                String label = cost.label() == null ? "a matching" : "a " + cost.label();
                throw new IllegalStateException("Each card exiled for the flashback cost must be " + label);
            }
        }
    }

    private void validateFlashbackPermanentSacrificeCosts(
            GameData gameData, Player player, Card card, Optional<FlashbackCast> flashbackOpt,
            int xValue, UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds) {
        if (flashbackOpt.isEmpty()) {
            return;
        }
        List<SacrificePermanentsCost> sacrificeCosts = flashbackOpt.get().getCosts(SacrificePermanentsCost.class);
        List<SacrificeXPermanentsCastingCost> xSacrificeCosts =
                flashbackOpt.get().getCosts(SacrificeXPermanentsCastingCost.class);
        if (sacrificeCosts.isEmpty() && xSacrificeCosts.isEmpty()) {
            return;
        }
        if (xValue < 0) {
            throw new IllegalStateException("X must not be negative");
        }

        List<UUID> selectedIds = new ArrayList<>();
        if (sacrificePermanentId != null) {
            selectedIds.add(sacrificePermanentId);
        }
        if (additionalCostSacrificePermanentIds != null) {
            selectedIds.addAll(additionalCostSacrificePermanentIds);
        }
        int requiredCount = sacrificeCosts.stream().mapToInt(SacrificePermanentsCost::count).sum()
                + xSacrificeCosts.size() * xValue;
        if (selectedIds.size() != requiredCount) {
            throw new IllegalStateException("Must sacrifice exactly " + requiredCount
                    + " permanents for the flashback cost");
        }
        if (new HashSet<>(selectedIds).size() != selectedIds.size()) {
            throw new IllegalStateException("Duplicate permanents cannot be sacrificed");
        }

        int selectedIndex = 0;
        UUID playerId = player.getId();
        for (CastingCost castingCost : flashbackOpt.get().costs()) {
            int count;
            PermanentPredicate filter;
            if (castingCost instanceof SacrificePermanentsCost sacrificeCost) {
                count = sacrificeCost.count();
                filter = sacrificeCost.filter();
            } else if (castingCost instanceof SacrificeXPermanentsCastingCost xSacrificeCost) {
                count = xValue;
                filter = xSacrificeCost.filter();
            } else {
                continue;
            }
            for (int i = 0; i < count; i++) {
                UUID permanentId = selectedIds.get(selectedIndex++);
                Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
                if (permanent == null || !playerId.equals(gameQueryService.findPermanentController(gameData, permanentId))) {
                    throw new IllegalStateException("Sacrifice target is not on your battlefield");
                }
                if (!predicateEvaluationService.matchesPermanentPredicate(permanent,
                        filter, FilterContext.of(gameData).withSourceControllerId(playerId))) {
                    throw new IllegalStateException("Sacrifice target does not match the required filter");
                }
            }
        }
    }

    private void payFlashbackPermanentSacrificeCosts(
            GameData gameData, Player player, Card card, FlashbackCast flashback,
            int xValue, List<UUID> sacrificePermanentIds) {
        int selectedIndex = 0;
        for (CastingCost castingCost : flashback.costs()) {
            int count;
            if (castingCost instanceof SacrificePermanentsCost sacrificeCost) {
                count = sacrificeCost.count();
            } else if (castingCost instanceof SacrificeXPermanentsCastingCost) {
                count = xValue;
            } else {
                continue;
            }
            for (int i = 0; i < count; i++) {
                Permanent permanent = gameQueryService.findPermanentById(
                        gameData, sacrificePermanentIds.get(selectedIndex++));
                if (permanentRemovalService.removePermanentToGraveyard(gameData, permanent)) {
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " sacrifices ")
                            .card(permanent.getCard())
                            .text(" for ")
                            .card(card)
                            .text(".")
                            .build());
                    triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                            gameData, player.getId(), permanent.getCard());
                }
            }
        }
    }

    private void payFlashbackGraveyardExileCost(GameData gameData, Player player, Card card,
                                                 List<Card> sourceGraveyard, int sourceGraveyardIndex,
                                                 Optional<FlashbackCast> flashbackOpt, int xValue,
                                                 List<Integer> exileGraveyardCardIndices) {
        var cost = flashbackOpt.flatMap(flashback ->
                flashback.getCost(ExileXCardsFromGraveyardCastingCost.class)).orElse(null);
        if (cost == null || xValue == 0) return;

        validateFlashbackGraveyardExileCost(gameData, player, card, sourceGraveyard, sourceGraveyardIndex,
                flashbackOpt, xValue, exileGraveyardCardIndices);
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> sortedDescending = exileGraveyardCardIndices.stream()
                .sorted(java.util.Comparator.reverseOrder()).toList();
        List<Card> exiledCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (int index : sortedDescending) {
                Card exiledCard = graveyard.remove(index);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiledCard);
                exiledCards.add(exiledCard);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        for (Card exiledCard : exiledCards) {
            gameData.addToExile(playerId, exiledCard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " exiles ")
                    .card(exiledCard)
                    .text(" from graveyard for the flashback cost of ")
                    .card(card)
                    .text(".")
                    .build());
        }
    }

    private void payDelveCost(GameData gameData, Player player, Card card, DelveCost cost,
                               List<Integer> exileGraveyardCardIndices) {
        if (cost == null) return;
        additionalSpellCostService.validateDelveCost(gameData, player, card, cost, exileGraveyardCardIndices);
        List<Integer> indices = exileGraveyardCardIndices == null ? List.of() : exileGraveyardCardIndices;
        if (indices.isEmpty()) return;
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> sortedDescending = indices.stream().sorted(java.util.Comparator.reverseOrder()).toList();
        List<Card> exiledCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (int idx : sortedDescending) {
                Card exiledCard = graveyard.remove(idx);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiledCard);
                exiledCards.add(exiledCard);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        for (Card exiledCard : exiledCards) {
            gameData.addToExile(playerId, exiledCard, card.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " exiles ")
                    .card(exiledCard)
                    .text(" from graveyard for delve on ")
                    .card(card)
                    .text(".")
                    .build());
        }
    }

    // --- Play with flashback from graveyard ---

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetId) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, List.of(), null, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetId, List<UUID> targetIds) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds, null, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds, exileGraveyardCardIndices, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, List.of());
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds, retraceDiscardHandCardIndex, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, Map<UUID, Integer> damageAssignments) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, List.of(), damageAssignments);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                    Map<UUID, Integer> damageAssignments) {
        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        playFlashbackSpellFromLocation(gameData, player, graveyard, graveyardCardIndex, xValue, targetId,
                targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                damageAssignments, List.of(), List.of());
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                   UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, List<UUID> beholdPermanentIds,
                                    List<Integer> beholdHandCardIndices) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, List.of(), beholdPermanentIds,
                beholdHandCardIndices);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                    List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices) {
        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        playFlashbackSpellFromLocation(gameData, player, graveyard, graveyardCardIndex, xValue, targetId,
                targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds, null,
                beholdPermanentIds, beholdHandCardIndices);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                    Map<UUID, Integer> damageAssignments,
                                    List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                                    List<Integer> discardHandCardIndices) {
        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        playFlashbackSpellFromLocation(gameData, player, graveyard, graveyardCardIndex, xValue, targetId,
                targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                damageAssignments, beholdPermanentIds, beholdHandCardIndices, discardHandCardIndices);
    }

    public void playFlashbackSpell(GameData gameData, Player player, UUID graveyardCardId, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType) {
        playFlashbackSpell(gameData, player, graveyardCardId, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, List.of());
    }

    public void playFlashbackSpell(GameData gameData, Player player, UUID graveyardCardId, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds) {
        GraveyardCardLocation location = findGraveyardCardLocation(gameData, graveyardCardId);
        if (location == null) {
            throw new IllegalArgumentException("Invalid graveyard card id");
        }
        playFlashbackSpellFromLocation(gameData, player, location.graveyard(), location.index(), xValue, targetId,
                targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds, null, null, List.of(),
                null, List.of(), List.of());
    }

    private void playFlashbackSpellFromLocation(GameData gameData, Player player, List<Card> graveyard,
                                    int graveyardCardIndex, Integer xValue, UUID targetId, List<UUID> targetIds,
                                     List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                     List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                     UUID sacrificePermanentId, Map<UUID, Integer> damageAssignments) {
        playFlashbackSpellFromLocation(gameData, player, graveyard, graveyardCardIndex, xValue, targetId,
                targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, List.of(), damageAssignments, List.of(), List.of());
    }

    private void playFlashbackSpellFromLocation(GameData gameData, Player player, List<Card> graveyard,
                                     int graveyardCardIndex, Integer xValue, UUID targetId, List<UUID> targetIds,
                                     List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                     List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                     UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                     Map<UUID, Integer> damageAssignments,
                                     List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices) {
        playFlashbackSpellFromLocation(gameData, player, graveyard, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds, retraceDiscardHandCardIndex,
                sacrificePermanentId, additionalCostSacrificePermanentIds, damageAssignments, beholdPermanentIds,
                beholdHandCardIndices, List.of());
    }

    private void playFlashbackSpellFromLocation(GameData gameData, Player player, List<Card> graveyard,
                                     int graveyardCardIndex, Integer xValue, UUID targetId, List<UUID> targetIds,
                                     List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                     List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                     UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                     Map<UUID, Integer> damageAssignments,
                                     List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                                     List<Integer> discardHandCardIndices) {
        if (additionalCostSacrificePermanentIds == null) {
            additionalCostSacrificePermanentIds = List.of();
        }
        if (tapPermanentIds == null) {
            tapPermanentIds = List.of();
        }
        if (beholdPermanentIds == null) beholdPermanentIds = List.of();
        if (beholdHandCardIndices == null) beholdHandCardIndices = List.of();
        if (discardHandCardIndices == null) discardHandCardIndices = List.of();
        int effectiveXValue = xValue != null ? xValue : 0;
        if (targetIds == null) targetIds = List.of();
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();
        // Ashes of the Abhorrent and temporary graveyard restrictions stop this player's cast.
        if (!gameQueryService.canPlayerCastSpellsFromZone(gameData, playerId, Zone.GRAVEYARD)) {
            throw new IllegalStateException("Spells can't be cast from graveyards");
        }
        if (graveyard == null || graveyardCardIndex < 0 || graveyardCardIndex >= graveyard.size()) {
            throw new IllegalArgumentException("Invalid graveyard card index");
        }

        Card card = graveyard.get(graveyardCardIndex);
        UUID graveyardOwnerId = resolveGraveyardOwner(gameData, graveyard, card.getId());
        boolean graveyardAbilitiesSuppressed = gameQueryService.graveyardCardsHaveLostAllAbilities(gameData);
        if (!card.hasType(CardType.LAND)
                && !gameQueryService.canCastSpellFromZone(gameData, card, Zone.GRAVEYARD, playerId)) {
            throw new IllegalStateException("Card can't be cast from the graveyard");
        }
        effectiveXValue = resolveCastTimeXValue(gameData, card, playerId, effectiveXValue);
        validateXValueCap(gameData, card, playerId, effectiveXValue);
        if (castingPermissionService.isOpponentsChosenColorSpellCastRestricted(gameData, playerId, card)
                || castingPermissionService.isOpponentsSpellMatchingPredicateRestricted(gameData, playerId, card)
                || castingPermissionService.isSpellTypeRestricted(gameData, playerId, card)
                || castingPermissionService.isSpellCastingRestrictedByMostRecentSpell(gameData, card)
                || castingPermissionService.isOpponentsManaValueSpellCastRestricted(gameData, playerId, card, effectiveXValue)) {
            throw new IllegalStateException("Card is not playable");
        }
        // Aftermath splits: FlashbackCast lives on the back face; effects/type come from that half,
        // but the physical parent card stays on the stack so exile disposition moves the whole card.
        var flashbackOpt = graveyardAbilitiesSuppressed
                ? Optional.<FlashbackCast>empty()
                : card.effectiveFlashbackCast();
        if (flashbackOpt.isPresent()
                && !castingPermissionService.canUseFlashback(gameData, playerId, flashbackOpt.get())) {
            flashbackOpt = Optional.empty();
        }
        Card castHalf = flashbackOpt.isPresent() ? card.graveyardCastHalf() : card;
        var disturbOpt = graveyardAbilitiesSuppressed
                ? Optional.<DisturbCast>empty()
                : card.getCastingOption(DisturbCast.class);
        var graveyardCastOpt = card.getCastingOption(GraveyardCast.class);
        if (graveyardAbilitiesSuppressed) {
            graveyardCastOpt = Optional.empty();
        } else if (graveyardCastOpt.isEmpty()) {
            graveyardCastOpt = castingPermissionService.findMayhemCastOption(gameData, playerId, card);
        }
        var harmonizeOpt = graveyardAbilitiesSuppressed
                ? Optional.<HarmonizeCast>empty()
                : card.getCastingOption(HarmonizeCast.class);
        boolean isDisturb = disturbOpt.isPresent() && flashbackOpt.isEmpty();
        Optional<FlashbackCast> grantedFlashbackOption = flashbackOpt.isEmpty() && !isDisturb
                ? castingPermissionService.findGrantedFlashback(gameData, playerId, card)
                : Optional.empty();
        boolean grantedHarmonize = !graveyardAbilitiesSuppressed
                && harmonizeOpt.isEmpty() && flashbackOpt.isEmpty() && !isDisturb
                && gameData.cardsGrantedHarmonizeUntilEndOfTurn.contains(card.getId());
        boolean isHarmonize = (harmonizeOpt.isPresent() && flashbackOpt.isEmpty() && !isDisturb) || grantedHarmonize;
        boolean isJumpStart = !graveyardAbilitiesSuppressed
                && (card.getCastingOption(JumpStartCast.class).isPresent()
                || hasSpellCastingAbilityGrantForCard(gameData, playerId, card, Keyword.JUMP_START, Zone.GRAVEYARD))
                && flashbackOpt.isEmpty() && !isDisturb && !isHarmonize;
        boolean isRetrace = !graveyardAbilitiesSuppressed
                && card.getCastingOption(Retrace.class).isPresent()
                && flashbackOpt.isEmpty() && !isDisturb && !isHarmonize && !isJumpStart;
        boolean grantedFlashback = !graveyardAbilitiesSuppressed
                && flashbackOpt.isEmpty()
                && !isDisturb
                && !isHarmonize
                && gameData.cardsGrantedFlashbackUntilEndOfTurn.contains(card.getId());
        boolean emblemFlashback = !graveyardAbilitiesSuppressed
                && flashbackOpt.isEmpty() && !isDisturb && !grantedFlashback
                && !isHarmonize
                && grantedFlashbackOption.isPresent();
        boolean grantedGraveyardCardCast = flashbackOpt.isEmpty()
                && !isDisturb
                && !isHarmonize
                && !grantedFlashback
                && !emblemFlashback
                && castingPermissionService.hasGrantedGraveyardCardCastPermission(gameData, card, playerId);
        boolean isGrantedGraveyardPlay = flashbackOpt.isEmpty()
                && !isDisturb
                && !isHarmonize
                && !grantedFlashback
                && !emblemFlashback
                && !grantedGraveyardCardCast
                && castingPermissionService.hasGraveyardPlayPermission(gameData, card, playerId);
        Optional<GameData.GraveyardCastFilterPermission> grantedFilterPermission =
                isGrantedGraveyardPlay
                        && !playerId.equals(gameData.graveyardPlayPermissions.get(card.getId()))
                        ? castingPermissionService.findGraveyardCastFilterPermission(gameData, card, playerId)
                        : Optional.empty();
        boolean isGraveyardCast = graveyardCastOpt.isPresent() && flashbackOpt.isEmpty()
                && !isDisturb
                && !isHarmonize
                && !grantedFlashback && !emblemFlashback && !grantedGraveyardCardCast
                && !isGrantedGraveyardPlay
                && castingPermissionService.isGraveyardCastAvailable(gameData, playerId, card, graveyardCastOpt.get());
        RemoveCountersFromControlledCreaturesCastingCost graveyardCounterCost = isGraveyardCast
                ? graveyardCastOpt.get().getCost(RemoveCountersFromControlledCreaturesCastingCost.class).orElse(null)
                : null;
        RemoveXCountersFromControlledPermanentsCastingCost flashbackCounterCost = flashbackOpt.isPresent()
                ? flashbackOpt.get().getCost(RemoveXCountersFromControlledPermanentsCastingCost.class).orElse(null)
                : null;

        // Check if this card is castable via a Muldrotha-style static graveyard permanent cast effect
        boolean isGrantedGraveyardCast = false;
        Optional<UUID> graveyardCastSourceId = Optional.empty();
        if (flashbackOpt.isEmpty() && !isDisturb && !isHarmonize && !grantedFlashback && !emblemFlashback && !grantedGraveyardCardCast
                && !isGrantedGraveyardPlay && !isGraveyardCast) {
            graveyardCastSourceId = castingPermissionService.findGraveyardCastSourcePermanentId(gameData, playerId);
            if (graveyardCastSourceId.isPresent()) {
                Set<CardType> typesCastFromGraveyard = gameData.permanentTypesCastFromGraveyardThisTurn
                        .getOrDefault(graveyardCastSourceId.get(), Set.of());
                isGrantedGraveyardCast = CastingPermissionService.hasUnusedPermanentTypeSlot(card, typesCastFromGraveyard);
            }
        }

        // Abandoned Sarcophagus: cast spells with cycling from graveyard (any number, normal cost)
        Optional<CastingPermissionService.FilteredGraveyardPermission> filteredGraveyardPermission =
                flashbackOpt.isEmpty() && !isDisturb && !isHarmonize
                        && !grantedFlashback && !emblemFlashback && !grantedGraveyardCardCast
                        && !isGrantedGraveyardPlay && !isGraveyardCast && !isGrantedGraveyardCast
                        ? castingPermissionService.findFilteredGraveyardPermission(gameData, playerId, card)
                        : Optional.empty();
        Optional<UUID> filteredGraveyardPermissionSourceId =
                filteredGraveyardPermission.map(CastingPermissionService.FilteredGraveyardPermission::sourcePermanentId);
        List<CastingCost> filteredGraveyardAdditionalCosts = filteredGraveyardPermission
                .map(permission -> permission.permission().additionalCosts())
                .orElse(List.of());
        boolean isGrantedCyclingGraveyardCast = filteredGraveyardPermission.isPresent();

        // Bösium Strip: cast the top instant/sorcery of your graveyard until end of turn
        boolean isMayCastTopInstantOrSorcery = flashbackOpt.isEmpty() && !isDisturb && !isHarmonize
                && !grantedFlashback && !emblemFlashback && !grantedGraveyardCardCast
                && !isGrantedGraveyardPlay && !isGraveyardCast && !isGrantedGraveyardCast
                && !isGrantedCyclingGraveyardCast && !isJumpStart && !isRetrace
                && castingPermissionService.canCastTopInstantOrSorceryFromGraveyard(gameData, playerId, card);

        if (flashbackOpt.isEmpty() && !isDisturb && !isHarmonize && !grantedFlashback && !emblemFlashback && !grantedGraveyardCardCast
                && !isGraveyardCast && !isGrantedGraveyardCast && !isGrantedGraveyardPlay && !isRetrace
                && !isJumpStart && !isGrantedCyclingGraveyardCast && !isMayCastTopInstantOrSorcery) {
            throw new IllegalStateException("Card cannot be cast from graveyard");
        }
        if (!castingPermissionService.isSpellCastingAllowed(gameData, playerId, card)) {
            throw new IllegalStateException("Card is not playable");
        }

        // Validate timing (aftermath / flashback half may differ in type from the parent split card)
        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        if (!castingPermissionService.canCastWithTiming(gameData, playerId, castHalf,
                isActivePlayer, isMainPhase, stackEmpty)) {
            throw new IllegalStateException("Cannot cast sorcery-speed spell from graveyard now");
        }

        // Additional cast costs on a graveyard cast. Exile-N, sacrifice-a-creature, and a selected
        // discard cost have selection wires on this path; any other additional cost is rejected up
        // front rather than silently skipped. Selections are validated BEFORE
        // Additional cast costs (CR 601.2b) on a graveyard cast. Exile-N (e.g. Skaab Ruinator),
        // sacrifice-a-creature (e.g. Finish), and single-permanent sacrifice costs have selection
        // wires on this path; any other additional cost is rejected up front rather than silently skipped. Selections are validated BEFORE
        // the mana payment below — a rejected cast must leak neither the mana nor the spell's
        // graveyard position (CR 601.2h). The spell itself still sits in this graveyard here
        // (removed further down), so the caller's post-removal indices are checked with the
        // spell's own slot excluded.
        List<CardEffect> spellEffects = new ArrayList<>(castHalf.getEffects(EffectSlot.SPELL));
        AdditionalSpellCostService.ExtractedCosts additionalCosts =
                additionalSpellCostService.extractAndRemove(gameData, playerId, castHalf, spellEffects);
        ForageOrPayManaCost effectiveForageCost = additionalCosts.forageOrPayManaCost() != null
                ? additionalCosts.forageOrPayManaCost()
                : grantedFilterPermission.map(GameData.GraveyardCastFilterPermission::additionalCost)
                        .orElse(null);
        DealDividedDamageEffect dividedDamageEffect = findChosenDividedDamageEffect(spellEffects);
        if (dividedDamageEffect != null) {
            damageAssignments = damageAssignments == null ? Map.of() : damageAssignments;
            validateDividedDamageAssignments(gameData, card, playerId, effectiveXValue, targetId,
                    damageAssignments, dividedDamageEffect);
        }
        ExileNCardsFromGraveyardCost exileNCost = additionalCosts.exileNCardsCost();
        List<Integer> escapeExileIndices = null;
        if (isGrantedCyclingGraveyardCast) {
            int escapeExileCount = filteredGraveyardPermission.get().permission().additionalGraveyardExileCount();
            if (escapeExileCount > 0) {
                escapeExileIndices = validateEscapeExileCost(gameData, player, card, graveyard,
                        graveyardCardIndex, escapeExileCount, exileNCost, exileGraveyardCardIndices);
            }
        }
        // Exile-N, fixed tap-multiple, and sacrifice-a-creature costs are handled below; reject
        // unsupported costs rather than silently skipping them.
        boolean hasUnsupportedAdditionalCost = additionalCosts.sacrificeAllCreatures()
                || additionalCosts.sacrificeAllPermanents()
                || additionalCosts.sacrificePermanentOrPayManaCost() != null
                || additionalCosts.sacrificePermanentOrDiscardCardCost() != null
                || additionalCosts.discardCardOrSacrificePermanentCost() != null
                || additionalCosts.discardCardOrPayLifeCost() != null
                || additionalCosts.returnPermanentToHand() != null
                || additionalCosts.returnCreatureToHand()
                || additionalCosts.blightCost() != null || additionalCosts.putCounterCost() != null
                || additionalCosts.putCountersOrPayManaCost() != null
                || additionalCosts.exileGraveyardCost() != null
                || additionalCosts.exileXCardsCost() != null
                || additionalCosts.discardRandomCost() != null
                || additionalCosts.discardCardOrPayManaCost() != null || additionalCosts.discardHand()
                || additionalCosts.discardXCardsCost() != null
                || additionalCosts.escalateDiscardCost() != null
                || additionalCosts.escalateManaCost() != null
                || additionalCosts.escalateSacrificeCost() != null
                || additionalCosts.tieredManaCost() != null
                || additionalCosts.delveCost() != null
                || additionalCosts.chooseCreatureTypeCost() != null
                || additionalCosts.waterbendCost() != null;
        if (hasUnsupportedAdditionalCost) {
            throw new IllegalStateException("Cannot cast " + castHalf.getName()
                    + " from the graveyard — paying its additional cast cost is not supported from this zone");
        }
        if (additionalCosts.chooseXValueCost() != null) {
            additionalSpellCostService.validateChooseXValueCost(
                    castHalf, additionalCosts.chooseXValueCost(), effectiveXValue);
        }
        if (exileNCost != null && escapeExileIndices == null) {
            int excludedGraveyardIndex = graveyard == gameData.playerGraveyards.get(playerId)
                    ? graveyardCardIndex : -1;
            additionalSpellCostService.validateExileNCardsFromGraveyardCost(gameData, player, card, exileNCost,
                    exileGraveyardCardIndices, excludedGraveyardIndex);
        }
        if (additionalCosts.tapMultipleCost() != null) {
            additionalSpellCostService.validateTapMultiplePermanentsCost(
                    gameData, player, castHalf, additionalCosts.tapMultipleCost(),
                    additionalCostSacrificePermanentIds, effectiveXValue);
        }
        if (additionalCosts.sacrificeAnyNumberCost() != null) {
            additionalSpellCostService.validateSacrificeAnyNumberOfPermanentsCost(
                    gameData, player, castHalf, additionalCosts.sacrificeAnyNumberCost(),
                    additionalCostSacrificePermanentIds);
        }
        if (additionalCosts.discardCost() != null) {
            if (additionalCosts.discardCost().count() == 1) {
                Integer discardIndex = discardHandCardIndices.size() == 1
                        ? discardHandCardIndices.getFirst() : null;
                additionalSpellCostService.validateDiscardCost(
                        gameData, player, castHalf, additionalCosts.discardCost(), discardIndex, -1);
            } else {
                additionalSpellCostService.validateDiscardCardsCost(
                        gameData, player, castHalf, additionalCosts.discardCost(),
                        discardHandCardIndices, -1);
            }
        }
        validateFlashbackGraveyardExileCost(gameData, player, card, graveyard, graveyardCardIndex,
                flashbackOpt, effectiveXValue, exileGraveyardCardIndices);
        validateFlashbackPermanentSacrificeCosts(gameData, player, card, flashbackOpt, effectiveXValue,
                sacrificePermanentId, additionalCostSacrificePermanentIds);
        AdditionalSpellCostService.CostSelection graveyardCostSelection = new AdditionalSpellCostService.CostSelection(
                sacrificePermanentId, null, exileGraveyardCardIndices, retraceDiscardHandCardIndex, null, 0, -1,
                List.of(), null, null,
                beholdPermanentIds, beholdHandCardIndices);
        if (effectiveForageCost != null) {
            int excludedGraveyardIndex = graveyard == gameData.playerGraveyards.get(playerId)
                    ? graveyardCardIndex : -1;
            additionalSpellCostService.validateForageOrPayManaCost(
                    gameData, player, card, effectiveForageCost,
                    graveyardCostSelection, excludedGraveyardIndex);
        }
        if (additionalCosts.beholdSelectionCost() != null) {
            additionalSpellCostService.validateBeholdCost(gameData, player, castHalf,
                    additionalCosts.beholdSelectionCost(), graveyardCostSelection);
        }
        if (additionalCosts.sacrificeCreature()) {
            AdditionalSpellCostService.ExtractedCosts sacrificeOnly =
                    additionalSpellCostService.extractAndRemove(
                            new ArrayList<>(List.of(new SacrificeCreatureCost())));
            AdditionalSpellCostService.CostSelection sacrificeSelection =
                    new AdditionalSpellCostService.CostSelection(
                            sacrificePermanentId, null, null, null, null, 0, -1, null);
            additionalSpellCostService.validateAll(
                    gameData, player, castHalf, sacrificeOnly, sacrificeSelection, effectiveXValue);
        }
        if (additionalCosts.sacrificePermanentCost() != null) {
            additionalSpellCostService.validateSingleSacrificeCost(
                    gameData, player, castHalf, sacrificePermanentId,
                    additionalCosts.sacrificePermanentCost().description(),
                    p -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, p, additionalCosts.sacrificePermanentCost().filter()));
        }
        additionalSpellCostService.validateRemoveCountersFromControlledCreaturesCost(
                gameData, player, card, graveyardCounterCost, additionalCostSacrificePermanentIds);
        additionalSpellCostService.validateRemoveXCountersFromControlledPermanentsCost(
                gameData, player, card, flashbackCounterCost, effectiveXValue, additionalCostSacrificePermanentIds);
        if (isJumpStart) {
            validateJumpStartDiscardCost(gameData, player, card, retraceDiscardHandCardIndex);
        }
        if (isGraveyardCast) {
            validateGraveyardCastPermanentSacrificeCosts(gameData, player, card,
                    graveyardCastOpt.orElseThrow(), tapPermanentIds);
        }
        if (isHarmonize) {
            validateHarmonizeTapCost(gameData, player, tapPermanentIds);
        }
        if (flashbackOpt.isPresent()) {
            validateFlashbackDiscardCost(gameData, player, card, flashbackOpt.get(), effectiveXValue,
                    discardHandCardIndices);
        }

        // Validate and pay flashback / disturb / graveyard cast cost
        boolean paysFlashbackCost = flashbackOpt.isPresent() || grantedFlashback || emblemFlashback;
        int additionalCost = castingCostService.getCastCostModifier(
                gameData, playerId, card, paysFlashbackCost, effectiveXValue, Zone.GRAVEYARD);
        additionalCost += castingCostService.getTargetingSpellCostModifier(gameData, playerId, card, targetId, targetIds)
                + castingCostService.getTargetingStackEntryTax(gameData, targetId, targetIds);
        if (grantedGraveyardCardCast) {
            GameData.GraveyardCardCastPermission permission =
                    gameData.graveyardCardCastPermissionsUntilEndOfTurn.get(card.getId());
            if (permission != null && !targetsCreatureControlledBy(
                    gameData, playerId, targetId, targetIds)) {
                additionalCost += permission.additionalGenericCost();
            }
        }
        int targetingLifeTax = castingCostService.getTargetingLifeTax(gameData, playerId, targetId, targetIds);
        if (targetingLifeTax > 0 && targetingLifeTax > gameData.getLife(playerId)) {
            throw new IllegalStateException("Not enough life to pay the targeting life cost");
        }
        if (flashbackOpt.isPresent()
                && flashbackOpt.get().getCost(LifeCastingCost.class).isPresent()
                && !castingCostService.canPayFlashbackLifeCost(gameData, playerId, flashbackOpt.get())) {
            throw new IllegalStateException("Cannot pay flashback life cost");
        }
        if (isGraveyardCast) {
            validateGraveyardCastAdditionalCosts(gameData, playerId, card, graveyardCastOpt.orElseThrow(),
                    retraceDiscardHandCardIndex, exileGraveyardCardIndices);
        }
        if (isGrantedCyclingGraveyardCast) {
            validateGraveyardCastAdditionalCosts(gameData, playerId, filteredGraveyardAdditionalCosts,
                    retraceDiscardHandCardIndex);
        }
        effectiveXValue = payFlashbackOrGraveyardCastCost(gameData, player, card, flashbackOpt,
                grantedFlashbackOption, harmonizeOpt,
                disturbOpt, graveyardCastOpt,
                grantedFlashback, emblemFlashback, grantedGraveyardCardCast, isGrantedGraveyardCast, isGrantedGraveyardPlay,
                isGraveyardCast, isHarmonize, isRetrace, isJumpStart, isDisturb, isGrantedCyclingGraveyardCast, isMayCastTopInstantOrSorcery,
                gameData.cardsGrantedFlashbackWithoutPayingManaCostUntilEndOfTurn.contains(card.getId())
                        || grantedGraveyardCardCast
                        && gameData.graveyardCardCastPermissionsUntilEndOfTurn.get(card.getId())
                        .withoutPayingManaCost(),
                effectiveXValue, additionalCost, tapPermanentIds, retraceDiscardHandCardIndex,
                sacrificePermanentId, additionalCostSacrificePermanentIds, discardHandCardIndices);
        if (additionalCosts.tapMultipleCost() != null) {
            payTapMultiplePermanentsCost(gameData, player, castHalf, additionalCosts.tapMultipleCost(),
                    additionalCostSacrificePermanentIds, effectiveXValue);
        }
        payFlashbackGraveyardExileCost(gameData, player, card, graveyard, graveyardCardIndex,
                flashbackOpt, effectiveXValue, exileGraveyardCardIndices);
        if (isGraveyardCast) {
            payGraveyardCastAdditionalCosts(gameData, player, card, graveyardCastOpt.orElseThrow(),
                    retraceDiscardHandCardIndex, exileGraveyardCardIndices);
        }
        if (isGrantedCyclingGraveyardCast) {
            payGraveyardCastAdditionalCosts(gameData, player, card, filteredGraveyardAdditionalCosts,
                    retraceDiscardHandCardIndex);
        }
        payTargetingLifeCost(gameData, player, card, targetingLifeTax);
        payRemoveCountersFromControlledCreaturesCost(
                gameData, player, card, graveyardCounterCost, additionalCostSacrificePermanentIds);
        payRemoveXCountersFromControlledPermanentsCost(
                gameData, player, card, flashbackCounterCost, effectiveXValue, additionalCostSacrificePermanentIds);
        if (additionalCosts.discardCost() != null) {
            if (additionalCosts.discardCost().count() == 1) {
                payDiscardCost(gameData, player, castHalf, additionalCosts.discardCost(),
                        discardHandCardIndices.getFirst(), -1);
            } else {
                payDiscardCardsCost(gameData, player, castHalf, additionalCosts.discardCost(),
                        discardHandCardIndices, -1);
            }
        }
        if (EffectResolution.hasManaSpentToCastDamageEffect(castHalf)) {
            effectiveXValue = gameData.getSpellCastManaSpent(card.getId());
        }

        // Remove card from graveyard
        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        filteredGraveyardPermissionSourceId.ifPresent(sourceId ->
                castingPermissionService.markFilteredGraveyardPermissionUsed(gameData, playerId, sourceId));
        gameData.graveyardPlayPermissions.remove(card.getId());
        gameData.graveyardPlayPermissionsExpireEndOfTurn.remove(card.getId());

        // Pay exile-N-cards-from-graveyard cost if present (validated above; the spell has left
        // the graveyard now, so the caller's indices apply directly)
        if (escapeExileIndices != null) {
            payExileNCardsFromGraveyardCost(gameData, player, card,
                    new ExileNCardsFromGraveyardCost(escapeExileIndices.size(), null), escapeExileIndices);
        } else {
            payExileNCardsFromGraveyardCost(gameData, player, card, exileNCost, exileGraveyardCardIndices);
        }
        payForageOrPayManaCost(gameData, player, card, effectiveForageCost,
                graveyardCostSelection, null);
        // Pay sacrifice-a-creature additional cast cost (Finish / aftermath half). Use castHalf so
        // tracking flags on the back-face cost are found (parent split has no SPELL-slot costs).
        if (additionalCosts.sacrificeCreature() || additionalCosts.sacrificePermanentCost() != null) {
            payAllSacrificeCosts(gameData, player, castHalf, sacrificePermanentId, additionalCosts, effectiveXValue);
        }
        if (additionalCosts.sacrificeAnyNumberCost() != null) {
            effectiveXValue = paySacrificeAnyNumberOfPermanentsCost(
                    gameData, player, castHalf, additionalCosts.sacrificeAnyNumberCost(),
                    additionalCostSacrificePermanentIds);
        }

        boolean grantedPermanentCast = grantedGraveyardCardCast
                && castHalf.getType().isPermanentType() && castHalf.getType() != CardType.LAND;
        boolean graveyardCastPermanent = isGraveyardCast
                && castHalf.getType().isPermanentType() && castHalf.getType() != CardType.LAND;
        if (graveyardCastPermanent || grantedPermanentCast || isGrantedGraveyardCast
                || (isGrantedCyclingGraveyardCast && castHalf.getType().isPermanentType() && castHalf.getType() != CardType.LAND)
                || (isGrantedGraveyardPlay && card.getType().isPermanentType() && card.getType() != CardType.LAND)) {
            // GraveyardCast / granted graveyard cast: permanent spell — enters battlefield on resolution, no exile
            if (isGrantedGraveyardCast && graveyardCastSourceId.isPresent()) {
                // Track which permanent type slot was used, keyed by the granting permanent's UUID
                Set<CardType> typesCastFromGraveyard = gameData.permanentTypesCastFromGraveyardThisTurn
                        .computeIfAbsent(graveyardCastSourceId.get(), k -> ConcurrentHashMap.newKeySet());
                if (chosenGraveyardType != null) {
                    // Player chose which type slot to use (for multi-type cards)
                    if (!card.hasType(chosenGraveyardType) || !chosenGraveyardType.isPermanentType()
                            || chosenGraveyardType == CardType.LAND || typesCastFromGraveyard.contains(chosenGraveyardType)) {
                        throw new IllegalStateException("Invalid chosen graveyard type: " + chosenGraveyardType);
                    }
                    typesCastFromGraveyard.add(chosenGraveyardType);
                } else {
                    // Auto-pick the first available type (for single-type cards or when no choice provided)
                    CardType primary = card.getType();
                    if (primary.isPermanentType() && primary != CardType.LAND && !typesCastFromGraveyard.contains(primary)) {
                        typesCastFromGraveyard.add(primary);
                    } else {
                        card.getAdditionalTypes().stream()
                                .filter(t -> t.isPermanentType() && t != CardType.LAND && !typesCastFromGraveyard.contains(t))
                                .findFirst()
                                .ifPresent(typesCastFromGraveyard::add);
                    }
                }
            }
            StackEntryType entryType = cardTypeToStackEntryType(card.getType());
            if (isGrantedGraveyardPlay) {
                GameData.GraveyardCastFilterPermission permission = grantedFilterPermission.orElse(null);
                if (permission != null && permission.singleUse()) {
                    if (permission.entryCounterType() != null) {
                        gameData.spellEntryCounters
                                .computeIfAbsent(card.getId(), ignored -> new ConcurrentHashMap<>())
                                .merge(permission.entryCounterType(), 1, Integer::sum);
                    }
                    if (permission.grantedSubtype() != null) {
                        gameData.spellGrantedSubtypesOnEntry
                                .computeIfAbsent(card.getId(), ignored -> ConcurrentHashMap.newKeySet())
                                .add(permission.grantedSubtype());
                    }
                    castingPermissionService.consumeGraveyardCastFilterPermission(gameData, permission);
                }
            }
            StackEntry stackEntry = new StackEntry(
                    entryType, card, playerId, card.getName(),
                    List.of(), 0, targetId, null
            );
            stackEntry.setSourceZone(Zone.GRAVEYARD);
            grantedFilterPermission.ifPresent(permission -> {
                if (permission.enterWithCounter() != null && permission.enterWithCounterCount() > 0) {
                    stackEntry.setEnteringCounterCount(
                            permission.enterWithCounter(), permission.enterWithCounterCount());
                }
            });
            if ((isGraveyardCast && graveyardCastOpt.orElseThrow().escape())
                            || (isGrantedCyclingGraveyardCast
                            && filteredGraveyardPermission.orElseThrow().permission().escape())) {
                stackEntry.setCastWithEscape(true);
            }
            if (isGraveyardCast && graveyardCastOpt.orElseThrow().exileAfterResolution()) {
                stackEntry.setExileInsteadOfGraveyard(true);
            }
            if (isGrantedCyclingGraveyardCast && filteredGraveyardPermission.isPresent()) {
                var permission = filteredGraveyardPermission.get().permission();
                if (permission.enterWithCounter() != null) {
                    stackEntry.setEnteringCounterCount(permission.enterWithCounter(),
                            permission.enterWithCounterCount());
                }
            }
            stackEntry.setEntersTapped(gameData.graveyardCardsEnterTapped.remove(card.getId()));
            preserveGraveyardOwner(stackEntry, playerId, graveyardOwnerId);
            gameData.stack.add(stackEntry);
            if (grantedGraveyardCardCast) {
                consumeGraveyardCardCastPermission(gameData, playerId, card);
            }
            finishSpellCast(gameData, playerId, player, graveyard, card, false);
            return;
        }

        if (isDisturb) {
            // CR 702.146: cast transformed from graveyard — permanent spell enters back-face up.
            if (card.getBackFaceCard() == null) {
                throw new IllegalStateException("Disturb requires a double-faced card");
            }
            StackEntryType entryType = cardTypeToStackEntryType(card.getType());
            // Aura disturb backs are possible; use back-face type when it differs.
            Card backFace = card.getBackFaceCard();
            if (backFace.getType().isPermanentType()) {
                entryType = cardTypeToStackEntryType(backFace.getType());
            }
            // Target using back-face characteristics (e.g. Disturb Aura "enchant creature").
            if (EffectResolution.needsTarget(backFace)) {
                if (targetId == null) {
                    throw new IllegalStateException("Spell requires a target");
                }
                targetLegalityService.validateSpellTargeting(gameData, backFace, targetId, null, playerId, true);
            }
            StackEntry stackEntry = new StackEntry(
                    entryType, card, playerId, backFace.getName(),
                    List.of(), 0, targetId, null
            );
            stackEntry.setCastWithDisturb(true);
            stackEntry.setSourceZone(Zone.GRAVEYARD);
            preserveGraveyardOwner(stackEntry, playerId, graveyardOwnerId);
            gameData.stack.add(stackEntry);
            finishSpellCast(gameData, playerId, player, graveyard, card, false);
            return;
        }

        // A one-shot permission (Toshiro Umezawa) on an instant or sorcery card lands here rather than
        // in the permanent-spell branch above.
        boolean grantedInstantOrSorceryCast = grantedGraveyardCardCast && !grantedPermanentCast;
        if (isGraveyardCast || isGrantedGraveyardPlay || isGrantedCyclingGraveyardCast
                || grantedInstantOrSorceryCast) {
            StackEntryType entryType = card.hasType(CardType.INSTANT)
                    ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;
            StackEntry stackEntry;
            if (targetId != null) {
                boolean targetsGraveyard = spellEffects.stream()
                        .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
                if (targetsGraveyard) {
                    targetLegalityService.validateEffectTargetInZone(
                            gameData, card, spellEffects, targetId, Zone.GRAVEYARD, effectiveXValue, playerId);
                    stackEntry = new StackEntry(
                            entryType, card, playerId, card.getName(),
                            spellEffects, effectiveXValue, targetId, null,
                            Map.of(), Zone.GRAVEYARD, List.of(targetId), List.of());
                } else {
                    targetLegalityService.validateSpellTargeting(gameData, card, targetId, null, playerId, true);
                    stackEntry = new StackEntry(
                            entryType, card, playerId, card.getName(),
                            spellEffects, effectiveXValue, targetId, Map.of()
                    );
                }
            } else {
                stackEntry = new StackEntry(
                        entryType, card, playerId, card.getName(),
                        spellEffects, effectiveXValue
                );
            }
            stackEntry.setSourceZone(Zone.GRAVEYARD);
                    if ((isGraveyardCast && graveyardCastOpt.orElseThrow().escape())
                            || (isGrantedCyclingGraveyardCast
                            && filteredGraveyardPermission.orElseThrow().permission().escape())) {
                stackEntry.setCastWithEscape(true);
            }
            if (isGraveyardCast && graveyardCastOpt.orElseThrow().alternateManaCost() != null) {
                stackEntry.setAlternateCost(true);
            }
            if (isGraveyardCast && graveyardCastOpt.orElseThrow().exileAfterResolution()) {
                stackEntry.setExileInsteadOfGraveyard(true);
            }
            if (isGrantedGraveyardPlay
                    && castingPermissionService.graveyardCastFilterPermissionExiles(
                    gameData, card, playerId)) {
                stackEntry.setExileInsteadOfGraveyard(true);
            }
            if (grantedInstantOrSorceryCast) {
                GameData.GraveyardCardCastPermission permission =
                        consumeGraveyardCardCastPermission(gameData, playerId, card);
                if (permission != null && permission.exileInsteadOfGraveyard()) {
                    stackEntry.setExileInsteadOfGraveyard(true);
                }
            }
            if (isGrantedGraveyardPlay) {
                castingPermissionService.consumeGraveyardCastFilterPermission(
                        gameData, grantedFilterPermission.orElse(null));
            }
            preserveGraveyardOwner(stackEntry, playerId, graveyardOwnerId);
            gameData.stack.add(stackEntry);
            finishSpellCast(gameData, playerId, player, graveyard, card, false);
            return;
        }

        StackEntryType entryType = castHalf.hasType(CardType.INSTANT) ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;
        String spellName = castHalf.getName() != null ? castHalf.getName() : card.getName();

        // Check for "target player shuffles up to N cards from their graveyard" flashback spells (e.g. Memory's Journey)
        ShuffleTargetCardsFromGraveyardIntoLibraryEffect shuffleGraveyardCardsEffect =
                (ShuffleTargetCardsFromGraveyardIntoLibraryEffect) spellEffects.stream()
                        .filter(ShuffleTargetCardsFromGraveyardIntoLibraryEffect.class::isInstance)
                        .findFirst().orElse(null);

        if (shuffleGraveyardCardsEffect != null) {
            UUID targetGraveyardOwner = targetId;
            if (targetGraveyardOwner == null) {
                throw new IllegalStateException("Must target a player");
            }
            long matchingCount = gameData.playerGraveyards.getOrDefault(targetGraveyardOwner, List.of()).stream()
                    .filter(c -> !gameQueryService.isLandCardTargetRestricted(gameData, c, playerId))
                    .filter(c -> predicateEvaluationService.matchesCardPredicate(c, shuffleGraveyardCardsEffect.filter(), card.getId()))
                    .count();
            if (matchingCount > 0) {
                graveyardTargetingService.handleUpToNTargetPlayerGraveyardSpellTargeting(gameData, playerId,
                        targetGraveyardOwner, card, entryType, shuffleGraveyardCardsEffect.filter(),
                        shuffleGraveyardCardsEffect.maxTargets(), spellEffects);
                gameData.graveyardTargetOperation.flashback = true;
                return; // finishSpellCast handled in handleMultipleCardsChosen
            }
            // No matching cards — put on stack with 0 targets
            StackEntry stackEntry = new StackEntry(
                    entryType, card, playerId, spellName,
                    spellEffects, 0, targetId,
                    null, Map.of(), null, List.of(), List.of()
            );
            stackEntry.setCastWithFlashback(true);
            stackEntry.setSourceZone(Zone.GRAVEYARD);
            preserveGraveyardOwner(stackEntry, playerId, graveyardOwnerId);
            gameData.stack.add(stackEntry);
            finishSpellCast(gameData, playerId, player, graveyard, card, false);
            return;
        }

        ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect shuffleCardsIntoOwnersLibrariesEffect =
                (ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect) spellEffects.stream()
                        .filter(ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect.class::isInstance)
                        .findFirst().orElse(null);
        if (shuffleCardsIntoOwnersLibrariesEffect != null) {
            long matchingCount = gameQueryService.canGraveyardCardsBeTargeted(gameData)
                    ? gameData.orderedPlayerIds.stream()
                    .flatMap(pid -> gameData.playerGraveyards.getOrDefault(pid, List.of()).stream())
                    .filter(graveyardCard -> predicateEvaluationService.matchesCardPredicate(
                            graveyardCard, shuffleCardsIntoOwnersLibrariesEffect.filter(), card.getId()))
                    .count()
                    : 0;
            if (matchingCount > 0) {
                graveyardTargetingService.handleUpToNAllGraveyardsSpellTargeting(gameData, playerId, card,
                        entryType, shuffleCardsIntoOwnersLibrariesEffect.filter(),
                        shuffleCardsIntoOwnersLibrariesEffect.maxTargets(), spellEffects);
                gameData.graveyardTargetOperation.flashback = true;
                return;
            }
            StackEntry noTargetStackEntry = new StackEntry(
                    entryType, card, playerId, spellName,
                    spellEffects, 0, null,
                    null, Map.of(), null, List.of(), List.of()
            );
            noTargetStackEntry.setCastWithFlashback(true);
            noTargetStackEntry.setSourceZone(Zone.GRAVEYARD);
            gameData.stack.add(noTargetStackEntry);
            finishSpellCast(gameData, playerId, player, graveyard, card, false);
            return;
        }

        ExileCardsFromGraveyardEffect exileFromGraveyardEffect = spellEffects.stream()
                .filter(ExileCardsFromGraveyardEffect.class::isInstance)
                .map(ExileCardsFromGraveyardEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (exileFromGraveyardEffect != null) {
            long matchingCount = gameData.orderedPlayerIds.stream()
                    .flatMap(playerIdToSearch -> gameData.playerGraveyards
                            .getOrDefault(playerIdToSearch, List.of()).stream())
                    .filter(candidate -> exileFromGraveyardEffect.filter() == null
                            || predicateEvaluationService.matchesCardPredicate(
                            candidate, exileFromGraveyardEffect.filter(), card.getId()))
                    .filter(candidate -> !gameQueryService.isLandCardTargetRestricted(
                            gameData, candidate, playerId))
                    .count();
            if (matchingCount > 0) {
                gameData.graveyardTargetOperation.permanentTargetIds = new ArrayList<>(targetIds);
                int graveyardMaxTargets = exileFromGraveyardEffect.maxTargetsForX(effectiveXValue);
                if (exileFromGraveyardEffect.allGraveyardsForSpell()
                        && !exileFromGraveyardEffect.exactTargets()) {
                    graveyardTargetingService.handleUpToNAllGraveyardsSpellTargeting(
                            gameData, playerId, card, entryType, exileFromGraveyardEffect.filter(),
                            graveyardMaxTargets, spellEffects);
                } else {
                    graveyardTargetingService.handleUpToNSingleGraveyardSpellTargeting(
                            gameData, playerId, card, entryType, graveyardMaxTargets,
                            exileFromGraveyardEffect.filter(), spellEffects, exileFromGraveyardEffect.exactTargets());
                }
                gameData.graveyardTargetOperation.flashback = true;
                return;
            }
            StackEntry noTargetStackEntry = new StackEntry(
                    entryType, card, playerId, spellName,
                    spellEffects, 0, null,
                    null, Map.of(), null, List.of(), targetIds
            );
            noTargetStackEntry.setCastWithFlashback(true);
            noTargetStackEntry.setSourceZone(Zone.GRAVEYARD);
            gameData.stack.add(noTargetStackEntry);
            finishSpellCast(gameData, playerId, player, graveyard, card, false);
            return;
        }

        StackEntry stackEntry;
        ReturnCardFromGraveyardEffect graveyardReturnEffect = spellEffects.stream()
                .map(SpellCastingService::unwrapConditional)
                .filter(e -> e instanceof ReturnCardFromGraveyardEffect returnEffect
                        && returnEffect.targetGraveyard())
                .map(e -> (ReturnCardFromGraveyardEffect) e)
                .findFirst()
                .orElse(null);
        boolean needsSingleGraveyardTargeting = graveyardReturnEffect != null;
        ReturnTargetCardFromGraveyardOrExileToHandEffect graveyardOrExileReturnEffect = spellEffects.stream()
                .filter(e -> e instanceof ReturnTargetCardFromGraveyardOrExileToHandEffect)
                .map(e -> (ReturnTargetCardFromGraveyardOrExileToHandEffect) e)
                .findFirst().orElse(null);
        boolean needsGraveyardOrExileTargeting = graveyardOrExileReturnEffect != null;
        boolean needsGraveyardEffectTargeting = !needsSingleGraveyardTargeting
                && !needsGraveyardOrExileTargeting
                && spellEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        boolean hasGraveyardTarget = needsSingleGraveyardTargeting || needsGraveyardEffectTargeting;
        // Multi-target flashback (incl. "up to N" with zero chosen: maxTargets > 0, minTargets == 0,
        // empty targetIds). Mirrors the hand-cast gate that allows empty targetIds when maxTargets > 0.
        boolean multiTargetFlashback = !targetIds.isEmpty()
                || (castHalf.getMaxTargets() > 0 && castHalf.getMinTargets() == 0 && targetId == null);

        if (!targetIds.isEmpty() && hasGraveyardTarget) {
            // Combined graveyard + permanent targeting (e.g. Crawl from the Cellar flashback)
            if (targetId == null) {
                if (needsSingleGraveyardTargeting && !graveyardReturnEffect.upTo()) {
                    String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                    throw new IllegalStateException("Must target a " + filterLabel + " in your graveyard");
                }
                if (needsGraveyardEffectTargeting && castHalf.getMaxTargets() == 0) {
                    throw new IllegalStateException("Must target a card in a graveyard");
                }
            } else if (needsSingleGraveyardTargeting
                    && graveyardReturnEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
                String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                boolean inControllersGraveyard = gameData.playerGraveyards
                        .getOrDefault(playerId, List.of())
                        .stream()
                        .anyMatch(c -> c.getId().equals(targetId));
                if (!inControllersGraveyard) {
                    throw new IllegalStateException("Target must be a " + filterLabel + " in your graveyard");
                }
                targetLegalityService.validateGraveyardEffectTargetOnly(gameData, castHalf, targetId);
            } else if (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting) {
                targetLegalityService.validateGraveyardEffectTargetOnly(gameData, castHalf, targetId);
            }
            if (castHalf.getMaxTargets() > 0) {
                targetLegalityService.validateMultiSpellTargets(gameData, castHalf, targetIds, playerId,
                        effectiveXValue, false);
            }
            stackEntry = new StackEntry(
                    entryType, card, playerId, spellName,
                    spellEffects, effectiveXValue, targetId,
                    null, Map.of(), Zone.GRAVEYARD,
                    targetId != null ? List.of(targetId) : List.of(), targetIds
            );
        } else if (dividedDamageEffect != null) {
            stackEntry = new StackEntry(
                    entryType, card, playerId, spellName,
                    spellEffects, effectiveXValue, targetId, damageAssignments
            );
        } else if (multiTargetFlashback) {
            // Multi-target flashback spell
            if (castHalf.getMaxTargets() > 0) {
                        targetLegalityService.validateMultiSpellTargets(gameData, castHalf, targetIds, playerId,
                                effectiveXValue, false);
            }
            stackEntry = new StackEntry(
                    entryType, card, playerId, spellName,
                    spellEffects, effectiveXValue, targetIds
            );
        } else if (EffectResolution.needsSpellTarget(castHalf)
                && (!EffectResolution.needsTarget(castHalf) || targetLegalityService.isSpellOnStack(gameData, targetId))) {
            // Spell that targets a spell on the stack (e.g. Increasing Vengeance flashback, or a
            // Glamerdye retrace aimed at a spell rather than a permanent).
            targetLegalityService.validateSpellTargetOnStack(gameData, targetId, castHalf.getTargetFilter(), playerId);
            stackEntry = new StackEntry(
                    entryType, card, playerId, spellName,
                    spellEffects, effectiveXValue, targetId,
                    null, Map.of(), Zone.STACK, List.of(), List.of()
            );
        } else {
            // Single-target or no-target flashback spell
            if (needsGraveyardOrExileTargeting) {
                Zone targetZone = findOwnedGraveyardOrExileTargetZone(gameData, playerId, targetId);
                if (targetZone == null) {
                    throw new IllegalStateException("Target must be an instant or sorcery in your graveyard or an exiled card with flashback you own");
                }
                targetLegalityService.validateEffectTargetInZone(gameData, castHalf, targetId, targetZone);
                stackEntry = new StackEntry(
                        entryType, card, playerId, spellName,
                        spellEffects, effectiveXValue, targetId, null,
                        Map.of(), targetZone, List.of(), List.of()
                );
            } else if (hasGraveyardTarget) {
                if (targetId != null) {
                    if (needsSingleGraveyardTargeting
                            && graveyardReturnEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
                        String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                        boolean inControllersGraveyard = gameData.playerGraveyards
                                .getOrDefault(playerId, List.of())
                                .stream()
                                .anyMatch(c -> c.getId().equals(targetId));
                        if (!inControllersGraveyard) {
                            throw new IllegalStateException("Target must be a " + filterLabel + " in your graveyard");
                        }
                    }
                    if (castHalf.getMaxTargets() > 0) {
                        // Mixed optional permanent groups: only validate graveyard effects against targetId
                        targetLegalityService.validateGraveyardEffectTargetOnly(gameData, castHalf, targetId);
                    } else {
                        targetLegalityService.validateEffectTargetInZone(gameData, castHalf, targetId, Zone.GRAVEYARD);
                    }
                } else if (needsSingleGraveyardTargeting && !graveyardReturnEffect.upTo()) {
                    String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                    throw new IllegalStateException("Must target a " + filterLabel + " in your graveyard");
                } else if (needsGraveyardEffectTargeting && castHalf.getMaxTargets() == 0) {
                    throw new IllegalStateException("Must target a card in a graveyard");
                }
                stackEntry = new StackEntry(
                        entryType, card, playerId, spellName,
                        spellEffects, effectiveXValue, targetId, null,
                        Map.of(), Zone.GRAVEYARD,
                        targetId != null ? List.of(targetId) : List.of(), List.of()
                );
            } else if (targetId != null && EffectResolution.needsTarget(castHalf)) {
                targetLegalityService.validateSpellTargeting(gameData, castHalf, targetId, null, playerId, true);
                stackEntry = new StackEntry(
                        entryType, card, playerId, spellName,
                        spellEffects, effectiveXValue, targetId, null
                );
            } else if (EffectResolution.needsTarget(castHalf) && targetId == null) {
                throw new IllegalStateException("Spell requires a target");
            } else {
                stackEntry = new StackEntry(
                        entryType, card, playerId, spellName,
                        spellEffects, effectiveXValue, targetId, null
                );
            }
        }
        // Retrace (CR 702.81) keeps the normal graveyard disposition — unlike flashback it is not
        // exiled after resolving, so it can be retraced again. Bösium Strip uses the exile-instead
        // replacement without granting the flashback keyword.
        if (isMayCastTopInstantOrSorcery
                || (isGraveyardCast && graveyardCastOpt.orElseThrow().exileAfterResolution())) {
            stackEntry.setExileInsteadOfGraveyard(true);
        } else if (flashbackOpt.isPresent() || grantedFlashback || emblemFlashback || isJumpStart) {
            stackEntry.setCastWithFlashback(true);
        } else {
            stackEntry.setCastWithFlashback(!isRetrace || isHarmonize);
        }
        stackEntry.setSourceZone(Zone.GRAVEYARD);
        preserveGraveyardOwner(stackEntry, playerId, graveyardOwnerId);
        gameData.stack.add(stackEntry);

        finishSpellCast(gameData, playerId, player, graveyard, card, false);
    }

    private void preserveGraveyardOwner(StackEntry stackEntry, UUID casterId, UUID graveyardOwnerId) {
        if (graveyardOwnerId != null && !graveyardOwnerId.equals(casterId)) {
            stackEntry.setOwnerIdOverride(graveyardOwnerId);
        }
    }

    public void playGraveyardLand(GameData gameData, Player player, UUID graveyardCardId, Integer xValue) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }
        UUID playerId = player.getId();
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, graveyardCardId);
        List<Card> graveyard = graveyardOwnerId == null
                ? null : gameData.playerGraveyards.get(graveyardOwnerId);
        int cardIndex = -1;
        if (graveyard != null) {
            for (int i = 0; i < graveyard.size(); i++) {
                if (graveyard.get(i).getId().equals(graveyardCardId)) {
                    cardIndex = i;
                    break;
                }
            }
        }
        if (cardIndex < 0) {
            throw new IllegalArgumentException("Invalid graveyard card id");
        }
        Card card = graveyard.get(cardIndex);
        if (!actionAvailabilityService.canPlayGraveyardLand(gameData, playerId, card, graveyardOwnerId)) {
            throw new IllegalStateException("Card is not playable from graveyard");
        }
        playGraveyardLandFromLocation(gameData, player, graveyard, cardIndex, xValue != null ? xValue : 0);
    }

    private void playGraveyardLandFromLocation(GameData gameData, Player player, List<Card> graveyard,
                                               int cardIndex, int xValue) {
        UUID playerId = player.getId();
        if (graveyard == null || cardIndex < 0 || cardIndex >= graveyard.size()) {
            throw new IllegalArgumentException("Invalid graveyard card index");
        }
        Card graveyardCard = graveyard.get(cardIndex);
        if (!graveyardCard.hasType(CardType.LAND)) {
            throw new IllegalStateException("Only lands can be played from graveyard");
        }
        Card landFace = selectedModalDoubleFacedLandFace(graveyardCard, xValue);
        boolean entersTapped = gameData.graveyardCardsEnterTapped.remove(graveyardCard.getId());
        UUID graveyardOwnerId = resolveGraveyardOwner(gameData, graveyard, graveyardCard.getId());
        permanentRemovalService.removeCardFromGraveyardById(gameData, graveyardCard.getId());
        gameData.graveyardPlayPermissions.remove(graveyardCard.getId());
        gameData.graveyardPlayPermissionsExpireEndOfTurn.remove(graveyardCard.getId());
        if (landCopyOnEnterService.prepare(gameData, playerId, graveyardCard, landFace,
                true, entersTapped, " from graveyard.", Zone.GRAVEYARD)) {
            return;
        }
        Permanent permanent = new Permanent(graveyardCard);
        permanent.setCard(landFace);
        permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
        if (entersTapped) {
            permanent.tap();
        }
        battlefieldEntryService.putLandOntoBattlefield(gameData, playerId, permanent, Zone.GRAVEYARD);
        gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);

        gameLogService.append(gameData,
                GameLog.playerPlays(player.getUsername(), landFace, " from graveyard."));

        log.info("Game {} - {} plays {} from graveyard", gameData.id, player.getUsername(), landFace.getName());

        if (!gameData.interaction.isAwaitingInput()) {
            battlefieldEntryService.processLandETBEffects(gameData, playerId, landFace);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            triggerCollectionService.checkControllerPlaysLandTriggers(
                    gameData, playerId, landFace, Zone.GRAVEYARD);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private UUID resolveGraveyardOwner(GameData gameData, List<Card> graveyard, UUID cardId) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (ownerId != null) {
            return ownerId;
        }
        return gameData.playerGraveyards.entrySet().stream()
                .filter(entry -> entry.getValue() == graveyard
                        || entry.getValue() != null && entry.getValue().stream()
                        .anyMatch(card -> card.getId().equals(cardId)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    // --- Play from exile ---

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue, UUID targetId) {
        playCardFromExileInternal(gameData, player, exileCardId, xValue, targetId,
                List.of(), List.of(), List.of(), false, List.of(), false, true, false, false);
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                  UUID targetId, List<UUID> exileCounterCostPermanentIds) {
        playCardFromExile(gameData, player, exileCardId, xValue, targetId,
                exileCounterCostPermanentIds, List.of());
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                  UUID targetId, List<UUID> exileCounterCostPermanentIds,
                                  List<UUID> convokeCreatureIds) {
        playCardFromExile(gameData, player, exileCardId, xValue, targetId,
                exileCounterCostPermanentIds, convokeCreatureIds, List.of(), false);
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                  UUID targetId, List<UUID> exileCounterCostPermanentIds,
                                  List<UUID> convokeCreatureIds, List<UUID> waterbendPermanentIds,
                                  boolean waterbendPaid) {
        playCardFromExileInternal(gameData, player, exileCardId, xValue, targetId,
                exileCounterCostPermanentIds, convokeCreatureIds, waterbendPermanentIds, waterbendPaid,
                List.of(), false, true, false, false);
    }

    public void playCardFromExileAsResolutionCast(GameData gameData, Player player, UUID exileCardId,
                                                  Integer xValue, UUID targetId) {
        playCardFromExileInternal(gameData, player, exileCardId, xValue, targetId,
                List.of(), List.of(), List.of(), false, List.of(), true, false, false, false);
    }

    public void playCardFromExileAsResolutionCast(GameData gameData, Player player, UUID exileCardId,
                                                  Integer xValue, UUID targetId, boolean copy,
                                                  boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard) {
        playCardFromExileInternal(gameData, player, exileCardId, xValue, targetId,
                List.of(), List.of(), List.of(), false, List.of(), true, false, copy,
                putOnBottomOfOwnersLibraryInsteadOfGraveyard);
    }

    public void playCardFromExileAsResolutionCast(GameData gameData, Player player, UUID exileCardId,
                                                  Integer xValue, UUID targetId, boolean copy) {
        playCardFromExileInternal(gameData, player, exileCardId, xValue, targetId,
                List.of(), List.of(), List.of(), false, List.of(), true, false, copy, false);
    }

    public void playCardFromExileAsResolutionCast(GameData gameData, Player player, UUID exileCardId,
                                                  Integer xValue, List<UUID> targetIds) {
        playCardFromExileInternal(gameData, player, exileCardId, xValue, null,
                List.of(), List.of(), List.of(), false, targetIds, true, false, false, false);
    }

    public void playCardFromExileAsResolutionCast(GameData gameData, Player player, UUID exileCardId,
                                                  Integer xValue, List<UUID> targetIds, boolean copy,
                                                  boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard) {
        playCardFromExileInternal(gameData, player, exileCardId, xValue, null,
                List.of(), List.of(), List.of(), false, targetIds, true, false, copy,
                putOnBottomOfOwnersLibraryInsteadOfGraveyard);
    }

    public void playCardFromExileAsResolutionCast(GameData gameData, Player player, UUID exileCardId,
                                                  Integer xValue, List<UUID> targetIds, boolean copy) {
        playCardFromExileInternal(gameData, player, exileCardId, xValue, null,
                List.of(), List.of(), List.of(), false, targetIds, true, false, copy, false);
    }

    private void playCardFromExileInternal(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                           UUID targetId, List<UUID> exileCounterCostPermanentIds,
                                           List<UUID> convokeCreatureIds, List<UUID> waterbendPermanentIds,
                                           boolean waterbendPaid, List<UUID> targetIds,
                                           boolean resolutionCast, boolean autoPass,
                                           boolean copy,
                                           boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (exileCounterCostPermanentIds == null) exileCounterCostPermanentIds = List.of();
        if (convokeCreatureIds == null) convokeCreatureIds = List.of();
        if (waterbendPermanentIds == null) waterbendPermanentIds = List.of();
        if (targetIds == null) targetIds = List.of();
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();

        // Find the card in exile — check player's own exile first, then all exile zones
        // (cards exiled by AllowCastFromCardsExiledWithSourceEffect may belong to other players)
        ExiledCardEntry exiledEntry = gameData.findExiledCard(exileCardId);
        if (exiledEntry == null) {
            throw new IllegalStateException("Card not found in exile");
        }
        Card card = exiledEntry.card();
        if (card.isCastOnlyFromGraveyard()) {
            throw new IllegalStateException("Card cannot be cast from exile");
        }
        if (!resolutionCast && !card.hasType(CardType.LAND)
                && !gameQueryService.canCastSpellFromZone(gameData, card, Zone.EXILE, playerId)) {
            throw new IllegalStateException("Card can't be cast from exile");
        }
        UUID permittedPlayer = gameData.exilePlayPermissions.get(exileCardId);
        OptionalInt sourceCounterCost = castingPermissionService.findAdditionalCounterCostFromSource(
                gameData, playerId, exileCardId);
        boolean sourceFreeCast = castingPermissionService.hasFreeCastFromExiledWithSource(
                gameData, playerId, exileCardId);
        boolean sourceCastPermission = castingPermissionService.hasCastFromExiledWithSourcePermission(
                gameData, playerId, exileCardId);
        boolean waterbendCast = !resolutionCast && waterbendPaid
                && castingPermissionService.hasWaterbendCastFromExiledWithSourcePermission(
                gameData, playerId, exileCardId);
        Optional<CounterType> sourceEntryCounterType = castingPermissionService
                .findEntryCounterTypeFromExiledWithSource(gameData, playerId, exileCardId);
        boolean castForForetell = gameData.foretoldCardIds.contains(exileCardId)
                && playerId.equals(exiledEntry.ownerId())
                && gameData.turnNumber > exiledEntry.exiledTurnNumber();
        if (!resolutionCast && gameData.plottedCardIds.contains(exileCardId)
                && gameData.turnNumber <= exiledEntry.exiledTurnNumber()) {
            throw new IllegalStateException("A plotted card cannot be cast on the turn it became plotted");
        }
        boolean hasPermission = resolutionCast
                || (permittedPlayer != null && permittedPlayer.equals(playerId))
                || sourceCounterCost.isPresent()
                || sourceCastPermission
                || waterbendCast
                || castForForetell;
        boolean anyManaType = castingPermissionService.hasAnyManaTypePermission(
                gameData, playerId, exileCardId);
        boolean snowManaAsAnyColor = castingPermissionService.hasSnowManaAsAnyColorPermission(
                gameData, playerId, exileCardId);
        int additionalCounterCost = permittedPlayer != null && permittedPlayer.equals(playerId)
                ? 0 : sourceCounterCost.orElse(0);
        effectiveXValue = resolveCastTimeXValue(gameData, card, playerId, effectiveXValue);
        validateXValueCap(gameData, card, playerId, effectiveXValue);
        if (castingPermissionService.isOpponentsChosenColorSpellCastRestricted(gameData, playerId, card)
                || castingPermissionService.isOpponentsSpellMatchingPredicateRestricted(gameData, playerId, card)
                || castingPermissionService.isSpellTypeRestricted(gameData, playerId, card)
                || castingPermissionService.isSpellCastingRestrictedByMostRecentSpell(gameData, card)
                || castingPermissionService.isOpponentsManaValueSpellCastRestricted(gameData, playerId, card, effectiveXValue)) {
            throw new IllegalStateException("Card is not playable");
        }

        // CR 601.2b: this path has no wire for additional-cost selections and pays no additional
        // costs — reject such casts up front (before any payment) instead of silently casting the
        // spell without its cost.
        AdditionalSpellCostService.ExtractedCosts additionalCosts =
                additionalSpellCostService.peek(gameData, playerId, card);
        if ((additionalCosts.any() && additionalCosts.chooseXValueCost() == null)
                || additionalCosts.delveCost() != null) {
            throw new IllegalStateException("Cannot cast " + card.getName()
                    + " from exile — paying its additional cast cost is not supported from this zone");
        }
        if (additionalCosts.chooseXValueCost() != null) {
            additionalSpellCostService.validateChooseXValueCost(card, additionalCosts.chooseXValueCost(), effectiveXValue);
        }

        boolean hasExileCast = card.getCastingOption(ExileCast.class).isPresent();
        if (!resolutionCast && !hasPermission && !hasExileCast) {
            throw new IllegalStateException("No permission to play this exiled card");
        }

        if (!resolutionCast && hasPermission && !hasExileCast && !card.hasType(CardType.LAND)
                && !castingPermissionService.canCastWithTiming(gameData, playerId, card,
                playerId.equals(gameData.activePlayerId),
                gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                        || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN,
                gameData.stack.isEmpty())) {
            throw new IllegalStateException("Cannot cast sorcery-speed spell from exile now");
        }

        validateExileCounterCost(gameData, player, card, additionalCounterCost,
                exileCounterCostPermanentIds);

        // Validate timing for ExileCast cards (creature/sorcery require sorcery-speed timing)
        if (!resolutionCast && hasExileCast && !card.hasType(CardType.LAND)) {
            boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
            boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                    || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
            boolean stackEmpty = gameData.stack.isEmpty();
            if (!castingPermissionService.canCastWithTiming(gameData, playerId, card,
                    isActivePlayer, isMainPhase, stackEmpty)) {
                throw new IllegalStateException("Cannot cast sorcery-speed spell from exile now");
            }
        }

        // Keep the card and its cast permissions in exile until the cast has passed every
        // validation and payment. A failed cast (for example, one that cannot pay its mana cost)
        // does not move the card out of exile.
        boolean playWithoutPaying = !waterbendCast
                && (gameData.exilePlayWithoutPayingManaCost.contains(exileCardId) || sourceFreeCast);
        boolean exileInsteadOfGraveyard = gameData.exileInsteadOfGraveyard.contains(exileCardId);
        boolean putOnBottomOfOwnersLibrary =
                castingPermissionService.putsExileCastOnBottomOfOwnersLibrary(
                        gameData, playerId, exileCardId);

        if (card.hasType(CardType.LAND)) {
            commitExileCast(gameData, playerId, exileCardId, sourceFreeCast, copy);
            Card landFace = selectedModalDoubleFacedLandFace(card, effectiveXValue);
            if (landCopyOnEnterService.prepare(gameData, playerId, card, landFace,
                    true, false, " from exile.", Zone.EXILE)) {
                return;
            }
            Permanent permanent = new Permanent(card);
            permanent.setCard(landFace);
            permanent.setEnteredFromExile(true);
            gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);
            gameLogService.append(gameData,
                    GameLog.playerPlays(player.getUsername(), landFace, " from exile."));
            log.info("Game {} - {} plays {} from exile", gameData.id, player.getUsername(), landFace.getName());
            if (cloneService.prepareCloneReplacementEffect(gameData, playerId, landFace, null, effectiveXValue, true)) {
                return;
            }
            battlefieldEntryService.putLandOntoBattlefield(gameData, playerId, permanent, Zone.EXILE);

            if (!gameData.interaction.isAwaitingInput()) {
                battlefieldEntryService.processLandETBEffects(gameData, playerId, landFace);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.checkControllerPlaysLandTriggers(gameData, playerId, landFace, true);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        // Pay mana cost — unless this card was granted a free play (e.g. Oracle's Vault), in which
        // case it is cast without paying its mana cost. If anyManaType, any mana can pay for any color.
        List<ManaColor> convokeContributions = waterbendCast ? List.of() : applyConvokeOrImprovise(
                gameData, playerId, card, convokeCreatureIds, Zone.EXILE);
        int phyrexianManaPaidWithLife = 0;
        ManaPool pool = gameData.playerManaPools.get(playerId);
        Map<ManaColor, Integer> exileSpellOnly = pool.promoteExiledSpellOnlyMana();
        Map<ManaColor, Integer> exactExileOnly = pool.promoteExiledCardOnlyMana(exileCardId);
        EnumMap<ManaColor, Integer> promotedPool = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            promotedPool.put(color, pool.get(color));
        }
        try {
            if (waterbendCast) {
                waterbendPaymentService.pay(gameData, playerId, card.getManaValue(), card, waterbendPermanentIds);
            } else if (playWithoutPaying) {
                payFreeExileCastCostIncrease(gameData, playerId, card, effectiveXValue);
                // Cast without paying its mana cost — no payment.
            } else if (castForForetell) {
                ManaCost cost = gameData.foretoldCardCosts.get(exileCardId);
                if (cost == null) {
                    throw new IllegalStateException("Foretell cost is unavailable");
                }
                cost = castingCostService.applyColoredManaCostReductions(gameData, playerId, card, cost);
                ManaPool.ForetellPaymentState paymentState = pool.beginForetellPayment();
                try {
                    int additionalCost = castingCostService.getCastCostModifier(
                            gameData, playerId, card, effectiveXValue);
                    if (!cost.canPayWithAdditionalGenericCost(pool, effectiveXValue, additionalCost,
                            false, false, false, false, true)) {
                        throw new IllegalStateException("Not enough mana to pay spell");
                    }
                    int manaBefore = pool.getTotalAllMana();
                    cost.payWithAdditionalGenericCost(pool, effectiveXValue, additionalCost,
                            false, false, false, false, true);
                    gameData.addSpellCastManaSpent(card.getId(), manaBefore - pool.getTotalAllMana());
                } finally {
                    pool.endForetellPayment(paymentState);
                }
            } else if (snowManaAsAnyColor && card.getManaCost() != null) {
                pool.setSnowManaSpendableAsAnyColor(true);
                try {
                    phyrexianManaPaidWithLife = paySpellManaCostFromNonHandZone(
                            gameData, playerId, card, effectiveXValue, Zone.EXILE);
                } finally {
                    pool.setSnowManaSpendableAsAnyColor(false);
                }
            } else if (anyManaType && card.getManaCost() != null) {
                ManaCost cost = castingCostService.applyColoredManaCostReductions(
                        gameData, playerId, card, new ManaCost(card.getManaCost()));
                cost.payAsGeneric(pool, effectiveXValue,
                        castingCostService.getCastCostModifier(gameData, playerId, card));
                if (gameData.hasPendingAnyManaTypeForNextSpellThisTurn(playerId)) {
                    gameData.markSpellPaidUsingPendingAnyManaType(card.getId());
                }
            } else {
                phyrexianManaPaidWithLife = paySpellManaCostFromNonHandZone(
                        gameData, playerId, card, effectiveXValue, Zone.EXILE, convokeContributions);
            }
        } finally {
            restoreExileRestrictedMana(pool, exileCardId, exactExileOnly, exileSpellOnly, promotedPool);
        }

        payExileCounterCost(gameData, player, card, additionalCounterCost, exileCounterCostPermanentIds);

        StackEntryType entryType = cardTypeToStackEntryType(card.getType());

        if (isModalSpell(card)) {
            card = card.createRuntimeCopy();
        }

        // Sorceries and instants need their spell effects for resolution;
        // permanent spells (creature, enchantment, artifact, planeswalker) use List.of()
        // because they resolve by entering the battlefield, not via effects.
        // Strip additional-cost markers before putting the spell's resolution effects on the stack.
        List<CardEffect> effectsToResolve;
        if (card.hasType(CardType.SORCERY) || card.hasType(CardType.INSTANT)) {
            effectsToResolve = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
            additionalSpellCostService.extractAndRemove(effectsToResolve);
            effectiveXValue = unwrapChooseOneEffect(gameData, playerId, card, effectsToResolve, effectiveXValue);
        } else {
            effectsToResolve = List.of();
        }

        ReturnCardFromGraveyardEffect graveyardReturnEffect = effectsToResolve.stream()
                .map(SpellCastingService::unwrapConditional)
                .filter(e -> e instanceof ReturnCardFromGraveyardEffect returnEffect
                        && returnEffect.targetGraveyard())
                .map(e -> (ReturnCardFromGraveyardEffect) e)
                .findFirst()
                .orElse(null);
        boolean needsSingleGraveyardTargeting = graveyardReturnEffect != null;
        boolean needsGraveyardEffectTargeting = !needsSingleGraveyardTargeting
                && effectsToResolve.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));

        CardEffect exileTargetingEffect = effectsToResolve.stream()
                .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD))
                .findFirst()
                .orElse(null);
        boolean needsExileTargeting = exileTargetingEffect != null;
        boolean targetIsExiledCard = targetId != null && gameData.findExiledCard(targetId) != null;

        if (targetId == null && targetIds.isEmpty() && EffectResolution.needsTarget(card)
                && !EffectResolution.needsSpellTarget(effectsToResolve)
                && !EffectResolution.needsDamageDistribution(effectsToResolve)
                && !(needsSingleGraveyardTargeting || needsGraveyardEffectTargeting || needsExileTargeting)) {
            throw new IllegalStateException("Spell requires a target");
        }

        if (!targetIds.isEmpty() && EffectResolution.needsSpellTarget(effectsToResolve)) {
            targetLegalityService.validateMultiSpellTargetsOnStack(gameData, card, targetIds, playerId);
        } else if (!targetIds.isEmpty()) {
            targetLegalityService.validateMultiSpellTargets(gameData, card, targetIds, playerId, effectiveXValue);
        } else if (targetId != null && EffectResolution.needsSpellTarget(effectsToResolve)) {
            targetLegalityService.validateSpellTargetOnStack(gameData, targetId, card.getTargetFilter(), playerId);
        } else if (targetId != null && needsExileTargeting && targetIsExiledCard) {
            targetLegalityService.validateEffectTargetInZone(gameData, card, targetId, Zone.EXILE, playerId);
        } else if (targetId != null && (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting)) {
            if (needsSingleGraveyardTargeting
                    && graveyardReturnEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
                String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                boolean inControllersGraveyard = gameData.playerGraveyards
                        .getOrDefault(playerId, List.of())
                        .stream()
                        .anyMatch(c -> c.getId().equals(targetId));
                if (!inControllersGraveyard) {
                    throw new IllegalStateException("Target must be a " + filterLabel + " in your graveyard");
                }
            }
            targetLegalityService.validateEffectTargetInZone(
                    gameData, card, targetId, Zone.GRAVEYARD, effectiveXValue, playerId);
        } else if (targetId != null && EffectResolution.needsTarget(card)) {
            targetLegalityService.validateSpellTargeting(gameData, card, targetId, null, playerId, true);
        } else if (EffectResolution.needsTarget(card) && targetId == null
                && (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting || needsExileTargeting)) {
            throw new IllegalStateException("Spell requires a target");
        }

        commitExileCast(gameData, playerId, exileCardId, sourceFreeCast && !waterbendCast, copy);

        if (sourceEntryCounterType.isPresent() && card.hasType(CardType.CREATURE)) {
            gameData.spellEntryCounters
                    .computeIfAbsent(card.getId(), ignored -> new ConcurrentHashMap<>())
                    .merge(sourceEntryCounterType.get(), 1, Integer::sum);
        }

        StackEntry stackEntry;
        if (!targetIds.isEmpty()) {
            stackEntry = new StackEntry(
                    entryType, card, playerId, card.getName(),
                    effectsToResolve, effectiveXValue, targetIds
            );
        } else if (EffectResolution.needsSpellTarget(effectsToResolve) && targetId != null) {
            stackEntry = new StackEntry(
                    entryType, card, playerId, card.getName(),
                    effectsToResolve, effectiveXValue, targetId,
                    null, Map.of(), Zone.STACK, List.of(), List.of()
            );
        } else if (needsExileTargeting && targetIsExiledCard) {
            stackEntry = new StackEntry(
                    entryType, card, playerId, card.getName(),
                    effectsToResolve, effectiveXValue, targetId, null,
                    Map.of(), Zone.EXILE, List.of(), List.of()
            );
        } else if (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting) {
            stackEntry = new StackEntry(
                    entryType, card, playerId, card.getName(),
                    effectsToResolve, effectiveXValue, targetId, null,
                    Map.of(), Zone.GRAVEYARD, List.of(), List.of()
            );
        } else {
            stackEntry = new StackEntry(
                    entryType, card, playerId, card.getName(),
                    effectsToResolve, effectiveXValue, targetId, null
            );
        }
        stackEntry.setSourceZone(Zone.EXILE);
        stackEntry.setCastForForetell(castForForetell);
        if (!exiledEntry.ownerId().equals(playerId)) {
            stackEntry.setOwnerIdOverride(exiledEntry.ownerId());
        }
        stackEntry.setPhyrexianManaPaidWithLife(phyrexianManaPaidWithLife);
        stackEntry.setExileInsteadOfGraveyard(exileInsteadOfGraveyard);
        stackEntry.setPutOnBottomOfOwnersLibraryInsteadOfGraveyard(
                putOnBottomOfOwnersLibraryInsteadOfGraveyard || putOnBottomOfOwnersLibrary);
        stackEntry.setCopy(copy);
        stackEntry.setManaSpentToCast(gameData.getSpellCastManaSpent(card.getId()));
        boolean controlledMount = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .anyMatch(permanent -> gameQueryService.effectiveCreatureSubtypes(gameData, permanent)
                        .contains(CardSubtype.MOUNT));
        stackEntry.setControlledMountAsCast(controlledMount);
        stampCastDuringMainPhase(gameData, stackEntry, playerId);
        gameData.stack.add(stackEntry);

        // Use null hand list — card was already removed from exile
        gameData.recordSpellCast(playerId, card);
        // "Prepared" (Secrets of Strixhaven): casting an exiled prepare-spell copy unprepares its
        // linked permanent as part of casting (not resolution), so a counter doesn't undo it.
        unprepareSourceOfCastSpell(gameData, exileCardId);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " casts " , card, " from exile."));
        log.info("Game {} - {} casts {} from exile", gameData.id, player.getUsername(), card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, Zone.EXILE);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        mutationCoordinator.invalidateAllPlayerViews(gameData);
        if (autoPass) {
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void payFreeExileCastCostIncrease(GameData gameData, UUID playerId, Card card, int effectiveXValue) {
        int additionalCost = castingCostService.getCastCostModifier(
                gameData, playerId, card, effectiveXValue, Zone.EXILE);
        if (additionalCost <= 0) {
            return;
        }

        ManaPool pool = gameData.playerManaPools.get(playerId);
        ManaCost freeCost = new ManaCost("{0}");
        ManaRestrictionFlags flags = computeManaRestrictionFlags(gameData, playerId, card, false);
        boolean powerstoneContext = gameQueryService.cardHasType(card, CardType.ARTIFACT, gameData, playerId)
                && pool.getPowerstoneOnlyColorless() > 0;
        int manaSpent = withMulticoloredSpellMana(gameData, playerId, card, () -> {
            if (!freeCost.canPayWithAdditionalGenericCost(pool, 0, additionalCost,
                    flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext(),
                    flags.kickedOnlyGreen(), flags.instantSorceryOnlyColorless(),
                    flags.subtypeCreatureContext(), flags.subtypeSpellOrAbilityContext(),
                    flags.creatureSpellOnly(), false, flags.legendarySpellOnly(),
                    flags.manaValueAtLeastFour(), flags.subtypeOrPlaneswalkerSpellContext(),
                    flags.subtypeCreatureSourceSpellOrAbilityContext(), powerstoneContext,
                    flags.subtypeSpellOnlyContext())) {
                throw new IllegalStateException("Not enough mana to pay spell cost increase");
            }
            int before = pool.getTotalAllMana();
            freeCost.payWithAdditionalGenericCost(pool, 0, additionalCost,
                    flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext(),
                    flags.kickedOnlyGreen(), flags.instantSorceryOnlyColorless(),
                    flags.subtypeCreatureContext(), flags.subtypeSpellOrAbilityContext(),
                    flags.creatureSpellOnly(), false, flags.legendarySpellOnly(),
                    flags.manaValueAtLeastFour(), flags.subtypeOrPlaneswalkerSpellContext(),
                    flags.subtypeCreatureSourceSpellOrAbilityContext(), powerstoneContext,
                    flags.subtypeSpellOnlyContext());
            return before - pool.getTotalAllMana();
        });
        gameData.addSpellCastManaSpent(card.getId(), manaSpent);
    }

    /** Commits a successful cast by moving the card out of exile and consuming its permissions. */
    private void commitExileCast(GameData gameData, UUID playerId, UUID exileCardId,
                                 boolean sourceFreeCast, boolean copy) {
        if (sourceFreeCast
                && !castingPermissionService.consumeFreeCastFromExiledWithSource(
                        gameData, playerId, exileCardId)) {
            throw new IllegalStateException("Exile cast permission is no longer available");
        }
        gameData.clearDelayedActions(ReturnExiledCardToHandAtNextEndStep.class,
                action -> action.cardId().equals(exileCardId));
        gameData.removeFromExile(exileCardId);
        if (!copy) {
            gameData.recordCardPlayedFromExile(playerId);
        }
    }

    /**
     * If the just-cast exiled card was a prepared permanent's prepare-spell copy, unprepares that
     * permanent (clearing its prepared designation and link). No-op when no permanent is linked.
     */
    private void unprepareSourceOfCastSpell(GameData gameData, UUID exileCardId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent perm : battlefield) {
                if (perm.isPrepared() && exileCardId.equals(perm.getPreparedSpellCardId())) {
                    perm.setPrepared(false);
                    perm.setPreparedSpellCardId(null);
                    return;
                }
            }
        }
    }

    private void validateExileCounterCost(GameData gameData, Player player, Card card, int count,
                                          List<UUID> selectedPermanentIds) {
        if (count <= 0) return;
        if (selectedPermanentIds == null || selectedPermanentIds.size() != count) {
            throw new IllegalStateException("Must choose exactly " + count
                    + " creatures to remove counters from for " + card.getName());
        }
        Map<UUID, Integer> selectedCounts = new HashMap<>();
        for (UUID permanentId : selectedPermanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            boolean controlled = gameData.playerBattlefields.getOrDefault(player.getId(), List.of()).stream()
                    .anyMatch(controlledPermanent -> controlledPermanent.getId().equals(permanentId));
            if (permanent == null || !controlled || !gameQueryService.isCreature(gameData, permanent)) {
                throw new IllegalStateException("Each counter must be removed from a creature you control");
            }
            selectedCounts.merge(permanentId, 1, Integer::sum);
            if (countAnyCounters(permanent) < selectedCounts.get(permanentId)) {
                throw new IllegalStateException("Not enough counters on " + permanent.getCard().getName());
            }
        }
    }

    private void payExileCounterCost(GameData gameData, Player player, Card card, int count,
                                     List<UUID> selectedPermanentIds) {
        if (count <= 0) return;
        for (UUID permanentId : selectedPermanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            removeAnyCounter(gameData, permanent);
        }
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " removes " + count + " counters from creatures to cast ", card, "."));
    }

    private int countAnyCounters(Permanent permanent) {
        int count = 0;
        for (CounterType counterType : CounterType.values()) {
            if (counterType != CounterType.ANY && counterType != CounterType.SILVER) {
                count += permanent.getCounterCount(counterType);
            }
        }
        return count;
    }

    private void removeAnyCounter(GameData gameData, Permanent permanent) {
        CounterType[] preferredTypes = {
                CounterType.MINUS_ONE_MINUS_ONE,
                CounterType.PLUS_ONE_PLUS_ONE
        };
        for (CounterType counterType : preferredTypes) {
            if (permanent.getCounterCount(counterType) > 0) {
                permanent.setCounterCount(counterType, permanent.getCounterCount(counterType) - 1);
                if (counterType == CounterType.OIL) {
                    gameData.recordOilCounterRemoved(permanent, 1);
                }
                return;
            }
        }
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER
                    || permanent.getCounterCount(counterType) <= 0) {
                continue;
            }
            permanent.setCounterCount(counterType, permanent.getCounterCount(counterType) - 1);
            if (counterType == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, 1);
            }
            return;
        }
        throw new IllegalStateException("No counter to remove");
    }

    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            throw new IllegalStateException("Library is empty");
        }

        Card card = deck.getFirst();
        if (card.isCastOnlyFromGraveyard()) {
            throw new IllegalStateException("Card cannot be cast from library");
        }
        boolean freeTopPlay = castingPermissionService.hasLibraryTopCardFreePlayPermission(gameData, playerId, card);
        if (card.hasType(CardType.LAND)) {
            if (!freeTopPlay && !castingPermissionService.canPlayLandsFromTopOfLibrary(gameData, playerId)) {
                throw new IllegalStateException("Card is not playable: no effect allowing play of lands from library top");
            }
            if (!castingPermissionService.canPlayLandNow(gameData, playerId, card)) {
                throw new IllegalStateException("Card is not playable");
            }

            deck.removeFirst();
            if (freeTopPlay) {
                gameData.libraryTopCardFreePlayPermissionsUntilEndOfTurn.remove(playerId);
            }
            Card landFace = selectedModalDoubleFacedLandFace(card, effectiveXValue);
            if (landCopyOnEnterService.prepare(gameData, playerId, card, landFace,
                    true, false, " from the top of their library.", Zone.LIBRARY)) {
                return;
            }
            Permanent permanent = new Permanent(card);
            permanent.setCard(landFace);
            gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);
            gameLogService.append(gameData,
                    GameLog.playerPlays(player.getUsername(), landFace, " from the top of their library."));
            log.info("Game {} - {} plays {} from library top", gameData.id, player.getUsername(), landFace.getName());
            if (cloneService.prepareCloneReplacementEffect(gameData, playerId, landFace, null, effectiveXValue, true)) {
                return;
            }
            battlefieldEntryService.putLandOntoBattlefield(gameData, playerId, permanent, Zone.LIBRARY);
            if (!gameData.interaction.isAwaitingInput()) {
                battlefieldEntryService.processLandETBEffects(gameData, playerId, landFace);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.checkControllerPlaysLandTriggers(gameData, playerId, landFace,
                        Zone.LIBRARY);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (castingPermissionService.canPlotNonlandCardsFromTopOfLibrary(gameData, playerId, card)) {
            if (!playerId.equals(gameData.activePlayerId)
                    || (gameData.currentStep != TurnStep.PRECOMBAT_MAIN
                    && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN)
                    || !gameData.stack.isEmpty()) {
                throw new IllegalStateException("A card can be plotted only at sorcery speed");
            }
            ManaCost plotCost = card.getManaCost() != null
                    ? new ManaCost(card.getManaCost()) : new ManaCost("{0}");
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (pool == null || !plotCost.canPay(pool)) {
                throw new IllegalStateException("Not enough mana to pay plot cost");
            }
            plotCost.pay(pool);
            deck.removeFirst();
            markCardPlotted(gameData, playerId, card);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " plots ", card, " from the top of their library."));
            mutationCoordinator.invalidateAllPlayerViews(gameData);
            return;
        }

        // Grafdigger's Cage etc.: players can't cast spells from libraries.
        if (!gameQueryService.canPlayersCastSpellsFromZone(gameData, Zone.LIBRARY)) {
            throw new IllegalStateException("Spells can't be cast from libraries");
        }
        if (!gameQueryService.canCastSpellFromZone(gameData, card, Zone.LIBRARY, playerId)) {
            throw new IllegalStateException("Card can't be cast from the library");
        }

        // Verify the player can cast from top of library
        if (!freeTopPlay && !castingPermissionService.canCastFromTopOfLibrary(gameData, playerId, card)) {
            throw new IllegalStateException("Top card is not castable from library");
        }

        boolean canUseManaValueLifeAlternative = castingPermissionService
                .hasLibraryTopCardLifePlayPermission(gameData, playerId, card)
                || castingPermissionService
                .canCastFromTopOfLibraryByPayingLifeEqualToManaValue(gameData, playerId, card);
        boolean canCastNormallyFromLibraryTop = castingPermissionService
                .canCastFromTopOfLibraryNormally(gameData, playerId, card);
        if (canUseManaValueLifeAlternative && !canCastNormallyFromLibraryTop
                && gameData.getLife(playerId) < card.getManaValue()) {
            throw new IllegalStateException("Not enough life to cast the top card from the library");
        }
        boolean useManaValueLifeAlternative = canUseManaValueLifeAlternative
                && (!canCastNormallyFromLibraryTop || gameData.getLife(playerId) >= card.getManaValue());
        if (useManaValueLifeAlternative
                && (gameData.getLife(playerId) < card.getManaValue()
                || !gameQueryService.canPlayerLifeChange(gameData, playerId)
                || !gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData))) {
            throw new IllegalStateException("Not enough life to cast the top card from the library");
        }

        effectiveXValue = freeTopPlay || useManaValueLifeAlternative
                ? 0 : resolveCastTimeXValue(gameData, card, playerId, effectiveXValue);
        validateXValueCap(gameData, card, playerId, effectiveXValue);
        if (castingPermissionService.isOpponentsChosenColorSpellCastRestricted(gameData, playerId, card)
                || castingPermissionService.isOpponentsSpellMatchingPredicateRestricted(gameData, playerId, card)
                || castingPermissionService.isSpellCastingRestrictedByMostRecentSpell(gameData, card)
                || castingPermissionService.isOpponentsManaValueSpellCastRestricted(gameData, playerId, card, effectiveXValue)) {
            throw new IllegalStateException("Card is not playable");
        }

        if (!castingPermissionService.canCastFromTopOfLibrary(gameData, playerId, card)) {
            throw new IllegalStateException("Top card type is not castable from library");
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        if (!castingPermissionService.canCastWithTiming(gameData, playerId, card,
                isActivePlayer, isMainPhase, gameData.stack.isEmpty())) {
            throw new IllegalStateException("Card is not playable");
        }

        // CR 601.2b: this path has no wire for additional-cost selections and pays no additional
        // costs — reject such casts up front (before any payment) instead of silently casting the
        // spell without its cost.
        AdditionalSpellCostService.ExtractedCosts additionalCosts =
                additionalSpellCostService.peek(gameData, playerId, card);
        if ((additionalCosts.any() && additionalCosts.chooseXValueCost() == null)
                || additionalCosts.delveCost() != null) {
            throw new IllegalStateException("Cannot cast " + card.getName()
                    + " from the library — paying its additional cast cost is not supported from this zone");
        }
        if (additionalCosts.chooseXValueCost() != null) {
            additionalSpellCostService.validateChooseXValueCost(card, additionalCosts.chooseXValueCost(), effectiveXValue);
        }

        // Remove from library
        deck.removeFirst();
        if (freeTopPlay) {
            gameData.libraryTopCardFreePlayPermissionsUntilEndOfTurn.remove(playerId);
        }

        int phyrexianManaPaidWithLife = 0;
        if (useManaValueLifeAlternative) {
            payLifeForLibraryTopAlternative(gameData, player, card);
        } else if (!freeTopPlay) {
            phyrexianManaPaidWithLife = paySpellManaCostFromNonHandZone(
                    gameData, playerId, card, effectiveXValue, Zone.LIBRARY);
        }

        StackEntryType entryType = cardTypeToStackEntryType(card.getType());

        if (isModalSpell(card)) {
            card = card.createRuntimeCopy();
        }

        List<CardEffect> effectsToResolve;
        if (card.hasType(CardType.SORCERY) || card.hasType(CardType.INSTANT)) {
            effectsToResolve = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
            additionalSpellCostService.extractAndRemove(effectsToResolve);
            effectiveXValue = unwrapChooseOneEffect(gameData, playerId, card, effectsToResolve, effectiveXValue);
        } else {
            effectsToResolve = List.of();
        }

        StackEntry stackEntry = new StackEntry(
                entryType, card, playerId, card.getName(),
                effectsToResolve, effectiveXValue, targetId, null
        );
        stackEntry.setPhyrexianManaPaidWithLife(phyrexianManaPaidWithLife);
        stackEntry.setAlternateCost(useManaValueLifeAlternative);
        stampCastDuringMainPhase(gameData, stackEntry, playerId);
        gameData.stack.add(stackEntry);
        if (!freeTopPlay && !useManaValueLifeAlternative) {
            castingPermissionService.markOncePerTurnLibraryCastPermissionUsed(gameData, playerId, card);
        }

        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " casts " , card, " from the top of their library."));
        log.info("Game {} - {} casts {} from library top", gameData.id, player.getUsername(), card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, Zone.LIBRARY);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        mutationCoordinator.invalidateAllPlayerViews(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    /** Casts a card from its owner's library during an active library search. */
    public void castCardFromLibraryWhileSearching(GameData gameData, Player player, Card card) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.stream().noneMatch(libraryCard -> libraryCard.getId().equals(card.getId()))) {
            throw new IllegalStateException("Card is no longer in the library");
        }
        if (card.isCastOnlyFromGraveyard()) {
            throw new IllegalStateException("Card cannot be cast from the library");
        }
        if (!gameQueryService.canPlayersCastSpellsFromZone(gameData, Zone.LIBRARY)) {
            throw new IllegalStateException("Spells cannot be cast from libraries");
        }
        if (!gameQueryService.canCastSpellFromZone(gameData, card, Zone.LIBRARY, playerId)) {
            throw new IllegalStateException("Card can't be cast from the library");
        }
        if (castingPermissionService.isOpponentsChosenColorSpellCastRestricted(gameData, playerId, card)
                || castingPermissionService.isOpponentsSpellMatchingPredicateRestricted(gameData, playerId, card)
                || castingPermissionService.isSpellCastingRestrictedByMostRecentSpell(gameData, card)
                || castingPermissionService.isOpponentsManaValueSpellCastRestricted(gameData, playerId, card, 0)) {
            throw new IllegalStateException("Card is not playable");
        }

        int effectiveXValue = resolveCastTimeXValue(gameData, card, playerId, 0);
        validateXValueCap(gameData, card, playerId, effectiveXValue);

        AdditionalSpellCostService.ExtractedCosts additionalCosts = additionalSpellCostService.peek(card);
        if (additionalCosts.any() && additionalCosts.chooseXValueCost() == null) {
            throw new IllegalStateException("Cannot cast " + card.getName()
                    + " from the library while searching because its additional cast cost is not supported");
        }
        if (additionalCosts.chooseXValueCost() != null) {
            additionalSpellCostService.validateChooseXValueCost(card, additionalCosts.chooseXValueCost(), effectiveXValue);
        }

        paySpellManaCostFromNonHandZone(gameData, playerId, card, effectiveXValue, Zone.LIBRARY);
        deck.removeIf(libraryCard -> libraryCard.getId().equals(card.getId()));

        Card castCard = isModalSpell(card) ? card.createRuntimeCopy() : card;
        List<CardEffect> effectsToResolve = castCard.hasType(CardType.SORCERY)
                || castCard.hasType(CardType.INSTANT)
                ? new ArrayList<>(castCard.getEffects(EffectSlot.SPELL))
                : new ArrayList<>();
        additionalSpellCostService.extractAndRemove(effectsToResolve);
        effectiveXValue = unwrapChooseOneEffect(gameData, playerId, castCard, effectsToResolve, effectiveXValue);

        StackEntry stackEntry = new StackEntry(
                cardTypeToStackEntryType(castCard.getType()), castCard, playerId, castCard.getName(),
                effectsToResolve, effectiveXValue, (UUID) null, null
        );
        stampCastDuringMainPhase(gameData, stackEntry, playerId);
        gameData.stack.add(stackEntry);
        gameData.recordSpellCast(playerId, castCard);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " casts ", castCard, " from their library while searching."));
        log.info("Game {} - {} casts {} from their library while searching",
                gameData.id, player.getUsername(), castCard.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, castCard, playerId, Zone.LIBRARY);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    // --- Mana payment ---

    private Permanent validateWebSlingingCreature(GameData gameData, UUID playerId,
                                                   List<UUID> permanentIds) {
        if (permanentIds.size() != 1) {
            throw new IllegalStateException("Must return exactly one tapped creature for web-slinging");
        }
        Permanent permanent = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(candidate -> candidate.getId().equals(permanentIds.getFirst()))
                .findFirst()
                .orElse(null);
        if (permanent == null) {
            throw new IllegalStateException("Web-slinging creature must be controlled by the caster");
        }
        if (!gameQueryService.isCreature(gameData, permanent)) {
            throw new IllegalStateException("Web-slinging requires returning a creature");
        }
        if (!permanent.isTapped()) {
            throw new IllegalStateException("Web-slinging requires returning a tapped creature");
        }
        return permanent;
    }

    private void restoreExileRestrictedMana(ManaPool pool, UUID exileCardId,
                                             Map<ManaColor, Integer> exactExileOnly,
                                             Map<ManaColor, Integer> exileSpellOnly,
                                             EnumMap<ManaColor, Integer> promotedPool) {
        EnumMap<ManaColor, Integer> exactUnspent = new EnumMap<>(ManaColor.class);
        EnumMap<ManaColor, Integer> exileSpellUnspent = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            int spent = Math.max(0, promotedPool.getOrDefault(color, 0) - pool.get(color));
            int exactAmount = exactExileOnly.getOrDefault(color, 0);
            int exactSpent = Math.min(exactAmount, spent);
            int exileSpellAmount = exileSpellOnly.getOrDefault(color, 0);
            int exileSpellSpent = Math.min(exileSpellAmount, Math.max(0, spent - exactSpent));
            if (exactAmount > exactSpent) {
                exactUnspent.put(color, exactAmount - exactSpent);
            }
            if (exileSpellAmount > exileSpellSpent) {
                exileSpellUnspent.put(color, exileSpellAmount - exileSpellSpent);
            }
        }
        pool.returnExiledCardOnlyMana(exileCardId, exactUnspent);
        pool.returnExiledSpellOnlyMana(exileSpellUnspent);
    }

    private void validateWebSlingingManaCost(GameData gameData, UUID playerId, Card card,
                                             WebSlingingEffect webSlinging, int targetingTax,
                                             int additionalGenericCost, String additionalManaCost) {
        if (webSlinging == null) {
            throw new IllegalStateException("Web-slinging cost is not available");
        }
        ManaCost cost = castingCostService.applyColoredManaCostReductions(
                gameData, playerId, card,
                new ManaCost(webSlinging.manaCost() + (additionalManaCost == null ? "" : additionalManaCost)));
        int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, card)
                + targetingTax + additionalGenericCost;
        withMulticoloredSpellMana(gameData, playerId, card, () -> {
            if (!cost.canPay(gameData.playerManaPools.get(playerId), additionalCost)) {
                throw new IllegalStateException("Not enough mana to pay web-slinging cost");
            }
            return null;
        });
    }

    private int payWebSlingingCost(GameData gameData, Player player, Card card,
                                   WebSlingingEffect webSlinging, UUID permanentId,
                                   int targetingTax, int additionalGenericCost,
                                   String additionalManaCost) {
        Permanent toReturn = validateWebSlingingCreature(gameData, player.getId(), List.of(permanentId));
        validateWebSlingingManaCost(gameData, player.getId(), card, webSlinging, targetingTax,
                additionalGenericCost, additionalManaCost);
        ManaCost cost = castingCostService.applyColoredManaCostReductions(
                gameData, player.getId(), card,
                new ManaCost(webSlinging.manaCost() + (additionalManaCost == null ? "" : additionalManaCost)));
        int additionalCost = castingCostService.getCastCostModifier(gameData, player.getId(), card)
                + targetingTax + additionalGenericCost;
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        int before = pool.getTotalAllMana();
        withMulticoloredSpellMana(gameData, player.getId(), card, () -> {
            cost.payWithAdditionalGenericCost(pool, 0, additionalCost);
            return null;
        });
        gameData.addSpellCastManaSpent(card.getId(), before - pool.getTotalAllMana());
        if (!permanentRemovalService.removePermanentToHand(gameData, toReturn)) {
            throw new IllegalStateException("Could not return the web-slinging creature to its owner's hand");
        }
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " returns ")
                .card(toReturn.getCard())
                .text(" to hand to cast ")
                .card(card)
                .text(" using web-slinging.")
                .build());
        return toReturn.getCard().getManaValue();
    }

    private void payLifeForLibraryTopAlternative(GameData gameData, Player player, Card card) {
        int amount = card.getManaValue();
        if (amount <= 0) return;
        UUID playerId = player.getId();
        gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - amount);
        gameData.lifeLostThisTurn.merge(playerId, amount, Integer::sum);
        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " pays " + amount + " life to cast " + card.getName() + " from the top of their library."));
    }

    /**
     * Pays escalate's mana-per-extra-mode cost when the spell's mana cost itself was waived
     * (alternate / free cast). Cost reducers still apply to this payment.
     */
    private void payEscalateManaOnly(GameData gameData, UUID playerId, Card card, String escalateManaSuffix,
                                     int targetingTax) {
        if (escalateManaSuffix == null || escalateManaSuffix.isEmpty()) {
            return;
        }
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, card) + targetingTax;
        ManaCost escalateOnly = new ManaCost(escalateManaSuffix);
        int manaSpent = withMulticoloredSpellMana(gameData, playerId, card, () -> {
            if (!escalateOnly.canPayWithAdditionalGenericCost(pool, 0, additionalCost)) {
                throw new IllegalStateException("Not enough mana to pay escalate cost");
            }
            int before = pool.getTotalAllMana();
            escalateOnly.payWithAdditionalGenericCost(pool, 0, additionalCost);
            return before - pool.getTotalAllMana();
        });
        gameData.addSpellCastManaSpent(card.getId(), manaSpent);
    }

    private <T> T withMulticoloredSpellMana(GameData gameData, UUID playerId, Card card,
                                             Supplier<T> action) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        ManaPool.MulticoloredSpellManaState state =
                gameQueryService.getEffectiveCardColors(gameData, card).size() >= 2
                        ? pool.promoteMulticoloredSpellOnlyMana() : null;
        try {
            return action.get();
        } finally {
            if (state != null) {
                pool.restorePromotedMulticoloredSpellOnlyMana(state);
            }
        }
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, null, false);
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions, Integer phyrexianLifeCount) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, phyrexianLifeCount, false);
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions, Integer phyrexianLifeCount, boolean kicked) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, phyrexianLifeCount, kicked, 0);
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions, Integer phyrexianLifeCount, boolean kicked, int extraCostReduction) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, phyrexianLifeCount, kicked, extraCostReduction, 0);
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions, Integer phyrexianLifeCount, boolean kicked, int extraCostReduction, int targetingTax) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, phyrexianLifeCount, kicked, extraCostReduction, targetingTax, "");
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions, Integer phyrexianLifeCount, boolean kicked, int extraCostReduction, int targetingTax, String escalateManaSuffix) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, phyrexianLifeCount,
                kicked, extraCostReduction, targetingTax, 0, "", escalateManaSuffix);
    }

    public int paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue,
                                 List<ManaColor> convokeContributions, Integer phyrexianLifeCount,
                                 boolean kicked, int extraCostReduction, int targetingTax,
                                 int additionalGenericCost, String additionalManaCost,
                                 String escalateManaSuffix) {
        return paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions,
                phyrexianLifeCount, kicked, extraCostReduction, targetingTax, additionalGenericCost,
                additionalManaCost, escalateManaSuffix, false);
    }

    public int paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue,
                                 List<ManaColor> convokeContributions, Integer phyrexianLifeCount,
                                 boolean kicked, int extraCostReduction, int targetingTax,
                                 int additionalGenericCost, String additionalManaCost,
                                 String escalateManaSuffix, boolean collectEvidenceCostPaid) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        var snowManaBefore = pool.getSnowManaTotals();
        var caveManaBefore = pool.getCaveManaTotals();
        var spellCastManaSourcesBefore = pool.getSpellCastTriggerManaTotals();
        int creatureManaBefore = creatureSourceManaAvailable(pool);
        int hasteGrantingBefore = hasteGrantingManaAvailable(gameData, playerId);
        int uncounterableGrantingBefore = uncounterableGrantingManaAvailable(gameData, playerId);
        int additionalCounterGrantingBefore = additionalCounterGrantingManaAvailable(gameData, playerId);
        int riotGrantingBefore = riotGrantingManaAvailable(gameData, playerId);
        SpellManaPayment payment = computeSpellManaPayment(gameData, playerId, card, effectiveXValue, convokeContributions,
                        phyrexianLifeCount, kicked, extraCostReduction, targetingTax, additionalGenericCost,
                        additionalManaCost, escalateManaSuffix, Zone.HAND, false, collectEvidenceCostPaid);
        gameData.addSpellCastManaSpent(card.getId(), payment.manaSpent());
        gameData.setSpellCastCreatureManaSpent(card.getId(),
                Math.max(0, creatureManaBefore - creatureSourceManaAvailable(pool)));
        recordSnowManaSpent(gameData, card, snowManaBefore, pool.getSnowManaTotals());
        recordCaveManaSpent(gameData, card, caveManaBefore, pool.getCaveManaTotals());
        recordSpellCastManaSources(gameData, card, spellCastManaSourcesBefore,
                pool.getSpellCastTriggerManaTotals());
        applyUncounterableGrantingMana(gameData, playerId, card);
        applyInstantSorceryUncounterableGrantingMana(gameData, playerId, card, uncounterableGrantingBefore);
        applyHasteGrantingMana(gameData, playerId, card, hasteGrantingBefore);
        applyAdditionalCounterGrantingMana(gameData, playerId, card, additionalCounterGrantingBefore);
        applyRiotGrantingMana(gameData, playerId, card, riotGrantingBefore);
        return payment.phyrexianManaPaidWithLife();
    }

    /**
     * Pays a spell's mana cost for a cast from a zone other than the caster's hand. The source zone
     * is supplied so zone-restricted alternative costs are applied correctly. Identical to
     * {@link #paySpellManaCost(GameData, UUID, Card, int, List, Integer)} otherwise.
     */
    public int paySpellManaCostFromNonHandZone(GameData gameData, UUID playerId, Card card, int effectiveXValue,
                                                Zone sourceZone) {
        return paySpellManaCostFromNonHandZone(gameData, playerId, card, effectiveXValue, sourceZone, false);
    }

    public int paySpellManaCostFromNonHandZone(GameData gameData, UUID playerId, Card card, int effectiveXValue,
                                                 Zone sourceZone, boolean anyManaType) {
        return paySpellManaCostFromNonHandZone(
                gameData, playerId, card, effectiveXValue, sourceZone, List.of(), anyManaType);
    }

    public int paySpellManaCostFromNonHandZone(GameData gameData, UUID playerId, Card card,
                                                 int effectiveXValue, Zone sourceZone,
                                                 List<ManaColor> convokeContributions) {
        return paySpellManaCostFromNonHandZone(
                gameData, playerId, card, effectiveXValue, sourceZone, convokeContributions, false);
    }

    public int paySpellManaCostFromNonHandZone(GameData gameData, UUID playerId, Card card,
                                                int effectiveXValue, Zone sourceZone,
                                                List<ManaColor> convokeContributions,
                                                boolean anyManaType) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        var snowManaBefore = pool.getSnowManaTotals();
        var caveManaBefore = pool.getCaveManaTotals();
        var spellCastManaSourcesBefore = pool.getSpellCastTriggerManaTotals();
        int creatureManaBefore = creatureSourceManaAvailable(pool);
        int hasteGrantingBefore = hasteGrantingManaAvailable(gameData, playerId);
        int uncounterableGrantingBefore = uncounterableGrantingManaAvailable(gameData, playerId);
        int additionalCounterGrantingBefore = additionalCounterGrantingManaAvailable(gameData, playerId);
        SpellManaPayment payment = computeSpellManaPayment(gameData, playerId, card, effectiveXValue, convokeContributions,
                        null, false, 0, 0, 0, "", "", sourceZone, anyManaType, false);
        int riotGrantingBefore = riotGrantingManaAvailable(gameData, playerId);
        gameData.addSpellCastManaSpent(card.getId(), payment.manaSpent());
        gameData.setSpellCastCreatureManaSpent(card.getId(),
                Math.max(0, creatureManaBefore - creatureSourceManaAvailable(pool)));
        recordSnowManaSpent(gameData, card, snowManaBefore, pool.getSnowManaTotals());
        recordCaveManaSpent(gameData, card, caveManaBefore, pool.getCaveManaTotals());
        recordSpellCastManaSources(gameData, card, spellCastManaSourcesBefore,
                pool.getSpellCastTriggerManaTotals());
        applyUncounterableGrantingMana(gameData, playerId, card);
        applyInstantSorceryUncounterableGrantingMana(gameData, playerId, card, uncounterableGrantingBefore);
        applyHasteGrantingMana(gameData, playerId, card, hasteGrantingBefore);
        applyAdditionalCounterGrantingMana(gameData, playerId, card, additionalCounterGrantingBefore);
        applyRiotGrantingMana(gameData, playerId, card, riotGrantingBefore);
        return payment.phyrexianManaPaidWithLife();
    }

    private void recordSnowManaSpent(GameData gameData, Card card,
                                     EnumMap<ManaColor, Integer> before,
                                     EnumMap<ManaColor, Integer> after) {
        EnumMap<ManaColor, Integer> spent = new EnumMap<>(ManaColor.class);
        int total = 0;
        for (ManaColor color : ManaColor.values()) {
            int amount = Math.max(0, before.getOrDefault(color, 0) - after.getOrDefault(color, 0));
            if (amount > 0) {
                spent.put(color, amount);
                total += amount;
            }
        }
        gameData.setSpellCastSnowManaSpent(card.getId(), total);
        gameData.setSpellCastSnowManaSpentByColor(card.getId(), spent);
    }

    private void recordCaveManaSpent(GameData gameData, Card card,
                                     EnumMap<ManaColor, Integer> before,
                                     EnumMap<ManaColor, Integer> after) {
        int total = 0;
        for (ManaColor color : ManaColor.values()) {
            total += Math.max(0, before.getOrDefault(color, 0) - after.getOrDefault(color, 0));
        }
        gameData.setSpellCastCaveManaSpent(card.getId(), total);
    }

    private void recordSpellCastManaSources(GameData gameData, Card card,
                                            Map<UUID, Integer> before,
                                            Map<UUID, Integer> after) {
        before.forEach((sourceId, amountBefore) -> {
            if (amountBefore > after.getOrDefault(sourceId, 0)) {
                gameData.addSpellCastManaSource(card.getId(), sourceId);
            }
        });
    }

    private int creatureSourceManaAvailable(ManaPool pool) {
        return pool.getCreatureManaTotal() + pool.getCreatureSourceCreatureSpellOnlyManaTotal();
    }

    private int hasteGrantingManaAvailable(GameData gameData, UUID playerId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        return pool != null ? pool.getHasteGrantingManaTotal() : 0;
    }

    private int uncounterableGrantingManaAvailable(GameData gameData, UUID playerId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        return pool != null ? pool.getUncounterableGrantingManaTotal() : 0;
    }

    private int additionalCounterGrantingManaAvailable(GameData gameData, UUID playerId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        return pool != null ? pool.getAdditionalCounterGrantingManaTotal() : 0;
    }

    private int riotGrantingManaAvailable(GameData gameData, UUID playerId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        return pool != null ? pool.getRiotGrantingManaTotal() : 0;
    }

    private String repeatAdditionalTargetManaCost(Card card, int extraTargetCount) {
        String perTargetCost = card.getAdditionalManaCostPerExtraTarget();
        if (perTargetCost == null || perTargetCost.isEmpty() || extraTargetCount == 0) {
            return "";
        }
        return perTargetCost.repeat(extraTargetCount);
    }

    /**
     * Boseiju, Who Shelters All: mana tagged with the uncounterable rider makes the instant or sorcery
     * spell it was spent on uncounterable. Like the haste rider, the pool spends tagged mana before
     * untagged mana of the same color, so any drop in the tagged total across the payment means this
     * spell was (at least partly) paid for with it.
     */
    private void applyInstantSorceryUncounterableGrantingMana(GameData gameData, UUID playerId, Card card,
                                                              int uncounterableGrantingBefore) {
        if (!card.hasType(CardType.INSTANT) && !card.hasType(CardType.SORCERY)) {
            return;
        }
        if (uncounterableGrantingManaAvailable(gameData, playerId) < uncounterableGrantingBefore) {
            gameData.spellsMadeUncounterable.add(card.getId());
        }
    }

    /**
     * Generator Servant: mana carrying the haste rider makes the creature spell it was spent on enter
     * the battlefield with haste until end of turn. The pool spends tagged mana before untagged mana
     * of the same color, so any drop in the tagged total between the two snapshots means this spell
     * was (at least partly) paid for with it.
     */
    private void applyHasteGrantingMana(GameData gameData, UUID playerId, Card card, int hasteGrantingBefore) {
        if (!card.hasType(CardType.CREATURE)) {
            return;
        }
        if (hasteGrantingManaAvailable(gameData, playerId) < hasteGrantingBefore) {
            gameData.spellsGrantedHasteOnEntry.add(card.getId());
        }
    }

    /**
     * Cavern of Souls: mana that carries an uncounterable rider makes the spell it paid for
     * uncounterable for as long as it stays on the stack.
     */
    private void applyUncounterableGrantingMana(GameData gameData, UUID playerId, Card card) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (pool != null && pool.consumeSpentUncounterableGrantingMana()) {
            gameData.spellsMadeUncounterable.add(card.getId());
        }
    }

    /**
     * The mana contributions the selected permanents would make via convoke or improvise. Convoke
     * sources contribute their color when possible; improvise-only artifacts contribute generic
     * mana. Throws if a source is missing, not a legal source, or already tapped.
     */
    private List<ManaColor> collectConvokeContributions(GameData gameData, UUID playerId,
                                                         List<UUID> convokeCreatureIds,
                                                         boolean hasConvoke, boolean hasImprovise) {
        List<ManaColor> contributions = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        for (UUID creatureId : convokeCreatureIds) {
            Permanent creature = battlefield.stream()
                    .filter(p -> p.getId().equals(creatureId))
                    .findFirst()
                    .orElse(null);
            if (creature == null) {
                throw new IllegalStateException(hasConvoke
                        ? "Convoke creature not found on your battlefield"
                        : "Improvise artifact not found on your battlefield");
            }
            boolean isCreature = gameQueryService.isCreature(gameData, creature);
            boolean isArtifact = gameQueryService.isArtifact(gameData, creature);
            if ((!hasConvoke || !isCreature) && (!hasImprovise || !isArtifact)) {
                throw new IllegalStateException(creature.getCard().getName()
                        + (hasConvoke && !hasImprovise ? " is not a creature" : " is not an artifact"));
            }
            if (creature.isTapped()) {
                throw new IllegalStateException(creature.getCard().getName() + " is already tapped");
            }
            if (hasImprovise && isArtifact && (!hasConvoke || !isCreature)) {
                contributions.add(null);
                continue;
            }
            Set<CardColor> creatureColors = gameQueryService.getEffectiveColors(gameData, creature);
            ManaColor contribution = creatureColors == null || creatureColors.isEmpty()
                    ? null
                    : creatureColors.stream()
                            .map(color -> ManaColor.fromCode(color.getCode()))
                            .filter(java.util.Objects::nonNull)
                            .findFirst()
                            .orElse(null);
            if (contribution == null) {
                CardColor creatureColor = gameQueryService.getEffectiveColor(gameData, creature);
                contribution = creatureColor != null ? ManaColor.fromCode(creatureColor.getCode()) : null;
            }
            contributions.add(contribution);
        }
        return contributions;
    }

    private List<ManaColor> applyConvokeOrImprovise(GameData gameData, UUID playerId, Card card,
                                                     List<UUID> sourceIds, Zone sourceZone) {
        if (sourceIds == null || sourceIds.isEmpty()) return List.of();

        boolean hasConvoke = card.getKeywords().contains(Keyword.CONVOKE)
                || hasSpellCastingAbilityGrantForCard(gameData, playerId, card, Keyword.CONVOKE, sourceZone);
        boolean hasImprovise = card.getKeywords().contains(Keyword.IMPROVISE)
                || hasSpellCastingAbilityGrantForCard(gameData, playerId, card, Keyword.IMPROVISE, sourceZone);
        if (!hasConvoke && !hasImprovise) {
            throw new IllegalStateException(card.getName() + " doesn't have convoke or improvise");
        }

        List<ManaColor> contributions = collectConvokeContributions(
                gameData, playerId, sourceIds, hasConvoke, hasImprovise);
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Permanent> validatedSources = sourceIds.stream()
                .map(sourceId -> battlefield.stream()
                        .filter(p -> p.getId().equals(sourceId)).findFirst().orElseThrow())
                .toList();
        int stackBeforeTriggers = gameData.stack.size();
        for (Permanent source : validatedSources) {
            source.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, source);
        }
        if (gameData.stack.size() > stackBeforeTriggers) {
            List<StackEntry> deferred = new ArrayList<>(
                    gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()));
            gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()).clear();
            gameData.pendingManaAbilityTriggers.addAll(deferred);
        }
        return contributions;
    }

    /**
     * Casting-assistance contributions previewed before the cast is committed, for affordability
     * checks. Returns an empty list when the card has no assistance keyword or no sources were offered.
     */
    private List<ManaColor> planConvokeContributions(GameData gameData, UUID playerId, Card card, List<UUID> convokeCreatureIds) {
        if (convokeCreatureIds == null || convokeCreatureIds.isEmpty()) return List.of();
        boolean hasConvoke = card.getKeywords().contains(Keyword.CONVOKE)
                || hasSpellCastingAbilityGrantForCard(gameData, playerId, card, Keyword.CONVOKE);
        boolean hasImprovise = card.getKeywords().contains(Keyword.IMPROVISE)
                || hasSpellCastingAbilityGrantForCard(gameData, playerId, card, Keyword.IMPROVISE);
        if (!hasConvoke && !hasImprovise) {
            return List.of();
        }
        return collectConvokeContributions(gameData, playerId, convokeCreatureIds, hasConvoke, hasImprovise);
    }

    private record SpellManaPayment(int manaSpent, int phyrexianManaPaidWithLife) {
    }

    private SpellManaPayment computeSpellManaPayment(GameData gameData, UUID playerId, Card card, int effectiveXValue,
                                        List<ManaColor> convokeContributions, Integer phyrexianLifeCount,
                                        boolean kicked, int extraCostReduction, int targetingTax,
                                        int additionalGenericCost, String additionalManaCost,
                                        String escalateManaSuffix, Zone sourceZone) {
        return computeSpellManaPayment(gameData, playerId, card, effectiveXValue, convokeContributions,
                phyrexianLifeCount, kicked, extraCostReduction, targetingTax, additionalGenericCost,
                additionalManaCost, escalateManaSuffix, sourceZone, false, false);
    }

    private SpellManaPayment computeSpellManaPayment(GameData gameData, UUID playerId, Card card, int effectiveXValue,
                                        List<ManaColor> convokeContributions, Integer phyrexianLifeCount,
                                        boolean kicked, int extraCostReduction, int targetingTax,
                                        int additionalGenericCost, String additionalManaCost,
                                        String escalateManaSuffix, Zone sourceZone,
                                        boolean anyManaType, boolean collectEvidenceCostPaid) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        Set<CardSubtype> subtypeOrLegendaryCreatureContext = card.hasType(CardType.CREATURE)
                ? (card.getSupertypes().contains(CardSupertype.LEGENDARY)
                || card.hasKeyword(Keyword.CHANGELING))
                ? EnumSet.allOf(CardSubtype.class)
                : nullToEmpty(gameQueryService.getCardSubtypes(card, gameData, playerId))
                : Set.of();
        ManaPool.SubtypeOrLegendaryCreatureManaState subtypeOrLegendaryMana =
                subtypeOrLegendaryCreatureContext.isEmpty()
                        ? null
                        : pool.promoteSubtypeOrLegendaryCreatureMana(subtypeOrLegendaryCreatureContext);
        boolean creatureOrEnchantment = card.hasType(CardType.CREATURE)
                || card.hasType(CardType.ENCHANTMENT);
        ManaPool.CreatureOrEnchantmentSpellManaState creatureOrEnchantmentMana = creatureOrEnchantment
                ? pool.promoteCreatureOrEnchantmentSpellOnlyMana()
                : null;
        ManaPool.NoncreatureSpellManaState noncreatureMana = !card.hasType(CardType.CREATURE)
                ? pool.promoteNoncreatureSpellOnlyMana()
                : null;
        ManaPool.MulticoloredSpellManaState multicoloredMana =
                gameQueryService.getEffectiveCardColors(gameData, card).size() >= 2
                        ? pool.promoteMulticoloredSpellOnlyMana() : null;
        try {
            if (!card.hasType(CardType.CREATURE)) {
                return computeSpellManaPaymentInternal(gameData, playerId, card, effectiveXValue,
                        convokeContributions, phyrexianLifeCount, kicked, extraCostReduction, targetingTax,
                        additionalGenericCost, additionalManaCost, escalateManaSuffix, sourceZone,
                        anyManaType, collectEvidenceCostPaid);
            }

            EnumMap<ManaColor, Integer> regularManaBefore = new EnumMap<>(ManaColor.class);
            for (ManaColor color : ManaColor.values()) {
                regularManaBefore.put(color, pool.get(color));
            }
            EnumMap<ManaColor, Integer> promotedCreatureSourceMana = pool.promoteCreatureSpellOrAbilityMana();
            try {
                return computeSpellManaPaymentInternal(gameData, playerId, card, effectiveXValue,
                        convokeContributions, phyrexianLifeCount, kicked, extraCostReduction, targetingTax,
                        additionalGenericCost, additionalManaCost, escalateManaSuffix, sourceZone,
                        anyManaType, collectEvidenceCostPaid);
            } finally {
                pool.restorePromotedCreatureSpellOrAbilityMana(promotedCreatureSourceMana, regularManaBefore);
            }
        } finally {
            if (noncreatureMana != null) {
                pool.restorePromotedNoncreatureSpellOnlyMana(noncreatureMana);
            }
            if (creatureOrEnchantmentMana != null) {
                pool.restorePromotedCreatureOrEnchantmentSpellOnlyMana(creatureOrEnchantmentMana);
            }
            if (subtypeOrLegendaryMana != null) {
                pool.restorePromotedSubtypeOrLegendaryCreatureMana(subtypeOrLegendaryMana);
            }
            if (multicoloredMana != null) {
                pool.restorePromotedMulticoloredSpellOnlyMana(multicoloredMana);
            }
        }
    }

    private SpellManaPayment computeSpellManaPaymentInternal(GameData gameData, UUID playerId, Card card, int effectiveXValue,
                                        List<ManaColor> convokeContributions, Integer phyrexianLifeCount,
                                        boolean kicked, int extraCostReduction, int targetingTax,
                                        int additionalGenericCost, String additionalManaCost,
                                        String escalateManaSuffix, Zone sourceZone,
                                        boolean anyManaType, boolean collectEvidenceCostPaid) {
        String suffix = escalateManaSuffix != null ? escalateManaSuffix : "";
        String baseMana = card.getManaCost() != null ? card.getManaCost() : "";
        String extraMana = additionalManaCost != null ? additionalManaCost : "";
        String additionalCostsMana = extraMana + suffix;
        String totalMana = baseMana + additionalCostsMana;
        if (totalMana.isEmpty()) return new SpellManaPayment(0, 0);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int before = pool.getTotalAllMana();
        int additionalCost = castingCostService.getCastCostModifier(
                gameData, playerId, card, effectiveXValue, sourceZone, collectEvidenceCostPaid)
                - extraCostReduction + targetingTax + additionalGenericCost;

        // Alternative zero cost (e.g. Rooftop Storm, As Foretold): skip the mana cost, but escalate
        // is still paid (CR 702.124c — free cast waives the mana cost, not additional costs).
        if (castingCostService.consumeFreeCastFromBattlefield(gameData, playerId, card, sourceZone)) {
            if (additionalCostsMana.isEmpty()) return new SpellManaPayment(0, 0);
            ManaCost additionalCosts = new ManaCost(additionalCostsMana);
            if (!additionalCosts.canPayWithAdditionalGenericCost(pool, 0, additionalCost)) {
                throw new IllegalStateException("Not enough mana to pay additional spell costs");
            }
            additionalCosts.payWithAdditionalGenericCost(pool, 0, additionalCost);
            return new SpellManaPayment(before - pool.getTotalAllMana(), 0);
        }

        ManaCost cost = castingCostService.applyColoredManaCostReductions(
                gameData, playerId, card, new ManaCost(totalMana));

        if (card.isRequiresBasicLandMana()) {
            if (!cost.canPayBasicLandOnly(pool, effectiveXValue, additionalCost)) {
                throw new IllegalStateException("Can only spend mana produced by basic lands to cast this spell");
            }
            cost.payBasicLandOnly(pool, effectiveXValue, additionalCost);
            return new SpellManaPayment(before - pool.getTotalAllMana(), 0);
        }

        // Vizier of the Menagerie: eligible spells (e.g. creature spells) may be paid with mana of any
        // type — pay the whole cost as generic. Convoke handles its own colour selection, so defer to
        // the normal path when creatures were tapped for it.
        boolean pendingAnyManaType = gameData.hasPendingAnyManaTypeForNextSpellThisTurn(playerId);
        boolean battlefieldAnyManaType = castingPermissionService.canSpendAnyManaTypeFromBattlefield(
                gameData, playerId, card);
        boolean usesPendingAnyManaType = pendingAnyManaType && !anyManaType && !battlefieldAnyManaType
                && !baseMana.isEmpty();
        if ((convokeContributions == null || convokeContributions.isEmpty())
                && (anyManaType || battlefieldAnyManaType || usesPendingAnyManaType)) {
            if (usesPendingAnyManaType && !additionalCostsMana.isEmpty()) {
                return payBaseManaCostWithPendingAnyManaType(gameData, playerId, card, baseMana,
                        additionalCostsMana, effectiveXValue, additionalCost, before, pool);
            }
            if (!cost.canPayAsGeneric(pool, effectiveXValue, additionalCost)) {
                throw new IllegalStateException("Not enough mana to pay spell");
            }
            cost.payAsGeneric(pool, effectiveXValue, additionalCost);
            if (gameData.hasPendingAnyManaTypeForNextSpellThisTurn(playerId)) {
                gameData.markSpellPaidUsingPendingAnyManaType(card.getId());
            }
        return new SpellManaPayment(before - pool.getTotalAllMana(), 0);
    }

        ManaRestrictionFlags flags = computeManaRestrictionFlags(gameData, playerId, card, kicked);
        boolean powerstoneContext = gameQueryService.cardHasType(card, CardType.ARTIFACT, gameData, playerId)
                && pool.getPowerstoneOnlyColorless() > 0;

        // Check if we should use a non-zero alternative cost from the battlefield (e.g. Jodah)
        // Use the alternative cost if the normal cost can't be paid but the alternative can
        boolean normallyPayable;
        if (convokeContributions != null && !convokeContributions.isEmpty()) {
            normallyPayable = cost.canPayWithConvoke(
                    pool, additionalCost + (cost.hasX() ? effectiveXValue : 0), convokeContributions);
        } else if (cost.hasX()) {
            normallyPayable = flags.hasRestricted() || powerstoneContext
                    ? cost.canPayWithAdditionalGenericCost(pool, effectiveXValue, additionalCost,
                            flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext(),
                            flags.kickedOnlyGreen(), flags.instantSorceryOnlyColorless(),
                            flags.subtypeCreatureContext(), flags.subtypeSpellOrAbilityContext(),
                            flags.creatureSpellOnly(), false, flags.legendarySpellOnly(),
                            flags.manaValueAtLeastFour(), flags.subtypeOrPlaneswalkerSpellContext(),
                            flags.subtypeCreatureSourceSpellOrAbilityContext(), powerstoneContext,
                            flags.subtypeSpellOnlyContext())
                    : cost.canPayWithAdditionalGenericCost(pool, effectiveXValue, additionalCost);
        } else {
            normallyPayable = flags.hasRestricted() || powerstoneContext
                    ? cost.canPayWithAdditionalGenericCost(pool, 0, additionalCost,
                            flags.isArtifact(), flags.isMyr(),
                            flags.hasRestrictedRedContext(), flags.kickedOnlyGreen(),
                            flags.instantSorceryOnlyColorless(), flags.subtypeCreatureContext(),
                            flags.subtypeSpellOrAbilityContext(), flags.creatureSpellOnly(), false,
                            flags.legendarySpellOnly(), flags.manaValueAtLeastFour(),
                            flags.subtypeOrPlaneswalkerSpellContext(),
                            flags.subtypeCreatureSourceSpellOrAbilityContext(), powerstoneContext,
                            flags.subtypeSpellOnlyContext())
                    : cost.canPayWithAdditionalGenericCost(pool, 0, additionalCost);
        }
        if (!normallyPayable) {
            String altCostStr = castingCostService.findAffordableAlternativeCostFromBattlefield(
                    gameData, playerId, card, pool, additionalCost);
            if (altCostStr != null) {
                ManaCost altCost = castingCostService.applyColoredManaCostReductions(
                        gameData, playerId, card, new ManaCost(altCostStr + additionalCostsMana));
                if (!altCost.canPayWithAdditionalGenericCost(pool, 0, additionalCost)) {
                    throw new IllegalStateException("Not enough mana to pay additional spell costs");
                }
                altCost.payWithAdditionalGenericCost(pool, 0, additionalCost);
                return new SpellManaPayment(before - pool.getTotalAllMana(), 0);
            }
            if (targetingTax > 0) {
                throw new IllegalStateException("Not enough mana to pay targeting tax");
            }
            if (additionalGenericCost > 0) {
                throw new IllegalStateException("Not enough mana to pay additional generic cost");
            }
            if (!extraMana.isEmpty()) {
                throw new IllegalStateException("Not enough mana to pay additional mana cost");
            }
            if (!suffix.isEmpty()) {
                throw new IllegalStateException("Not enough mana to pay escalate cost");
            }
            throw new IllegalStateException("Not enough mana to pay spell");
        }

        // Pay Phyrexian mana first so colored mana is reserved for Phyrexian symbols before
        // generic costs consume it. Without an explicit player choice, use mana only where the
        // rest of the cost stays payable, falling back to life otherwise (playability assumes
        // life is always an option).
        int phyrexianLifeCost = 0;
        if (cost.hasPhyrexianMana()) {
            if (phyrexianLifeCount != null) {
                phyrexianLifeCost = cost.payPhyrexianMana(pool, phyrexianLifeCount);
            } else {
                int restDemand = cost.hasX() ? effectiveXValue + additionalCost : additionalCost;
                phyrexianLifeCost = cost.payPhyrexianManaAuto(pool, restDemand);
            }
        }

        if (convokeContributions != null && !convokeContributions.isEmpty()) {
            // X is part of the generic demand convoke can help pay (Chord of Calling).
            cost.payWithConvoke(pool, additionalCost + (cost.hasX() ? effectiveXValue : 0), convokeContributions);
        } else if (cost.hasX() && card.hasXColorRestriction()) {
            var spentOnX = cost.pay(pool, effectiveXValue, card.getXColorRestrictions(), additionalCost);
            gameData.setSpellCastManaSpentOnX(card.getId(), spentOnX);
        } else if (cost.hasX()) {
            if (flags.hasRestricted() || powerstoneContext) {
                cost.payWithAdditionalGenericCost(pool, effectiveXValue, additionalCost,
                        flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext(),
                        flags.kickedOnlyGreen(), flags.instantSorceryOnlyColorless(),
                        flags.subtypeCreatureContext(), flags.subtypeSpellOrAbilityContext(),
                        flags.creatureSpellOnly(), false, flags.legendarySpellOnly(),
                        flags.manaValueAtLeastFour(),
                        flags.subtypeOrPlaneswalkerSpellContext(),
                        flags.subtypeCreatureSourceSpellOrAbilityContext(), powerstoneContext,
                        flags.subtypeSpellOnlyContext());
            } else {
                cost.payWithAdditionalGenericCost(pool, effectiveXValue, additionalCost);
            }
        } else {
            if (flags.hasRestricted() || powerstoneContext) {
                cost.payWithAdditionalGenericCost(pool, 0, additionalCost,
                        flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext(),
                        flags.kickedOnlyGreen(), flags.instantSorceryOnlyColorless(),
                        flags.subtypeCreatureContext(), flags.subtypeSpellOrAbilityContext(),
                        flags.creatureSpellOnly(), false, flags.legendarySpellOnly(),
                        flags.manaValueAtLeastFour(), flags.subtypeOrPlaneswalkerSpellContext(),
                        flags.subtypeCreatureSourceSpellOrAbilityContext(), powerstoneContext,
                        flags.subtypeSpellOnlyContext());
            } else {
                cost.payWithAdditionalGenericCost(pool, 0, additionalCost);
            }
        }

        if (phyrexianLifeCost > 0) {
            int lifeLoss = phyrexianLifeCost
                    * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
            int currentLife = gameData.getLife(playerId);
            gameData.playerLifeTotals.put(playerId, currentLife - lifeLoss);
            gameData.lifeLostThisTurn.merge(playerId, lifeLoss, Integer::sum);
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " pays " + lifeLoss + " life for Phyrexian mana."));
        }

        int fromPool = before - pool.getTotalAllMana();
        int fromConvoke = convokeContributions != null ? convokeContributions.size() : 0;
        return new SpellManaPayment(fromPool + fromConvoke, phyrexianLifeCost / 2);
    }

    private SpellManaPayment payBaseManaCostWithPendingAnyManaType(
            GameData gameData, UUID playerId, Card card, String baseMana, String additionalCostsMana,
            int effectiveXValue, int additionalCost, int before, ManaPool pool) {
        ManaCost baseCost = castingCostService.applyColoredManaCostReductions(
                gameData, playerId, card, new ManaCost(baseMana));
        ManaCost additionalCostMana = castingCostService.applyColoredManaCostReductions(
                gameData, playerId, card, new ManaCost(additionalCostsMana));

        ManaPool paymentPool = new ManaPool(pool);
        if (!additionalCostMana.canPay(paymentPool, effectiveXValue)) {
            throw new IllegalStateException("Not enough mana to pay spell");
        }
        additionalCostMana.pay(paymentPool, effectiveXValue);
        if (!baseCost.canPayAsGeneric(paymentPool, effectiveXValue, additionalCost)) {
            throw new IllegalStateException("Not enough mana to pay spell");
        }

        additionalCostMana.pay(pool, effectiveXValue);
        baseCost.payAsGeneric(pool, effectiveXValue, additionalCost);
        gameData.markSpellPaidUsingPendingAnyManaType(card.getId());
        return new SpellManaPayment(before - pool.getTotalAllMana(), 0);
    }

    /**
     * The spell's "distribute N counters among any number of target creatures" effect, whose
     * per-target amounts the controller announces onto {@code StackEntry.damageAssignments}.
     */
    private DistributeCountersAmongTargetsEffect findChosenCounterDistribution(List<CardEffect> effects) {
        for (CardEffect e : effects) {
            if (e instanceof DistributeCountersAmongTargetsEffect d && d.mode() == DivisionMode.CHOSEN) {
                return d;
            }
        }
        return null;
    }

    private DealDividedDamageEffect findChosenDividedDamageEffect(List<CardEffect> effects) {
        for (CardEffect e : effects) {
            if (e instanceof DealDividedDamageEffect d
                    && d.mode() == DivisionMode.CHOSEN && !d.etbAssignments()) {
                return d;
            }
            if (e instanceof ConditionalEffect conditional) {
                DealDividedDamageEffect nested =
                        findChosenDividedDamageEffect(List.of(conditional.wrapped()));
                if (nested != null) {
                    return nested;
                }
            }
            if (e instanceof ConditionalReplacementEffect conditional) {
                if (conditional.baseEffect() != null) {
                    DealDividedDamageEffect nested =
                            findChosenDividedDamageEffect(List.of(conditional.baseEffect()));
                    if (nested != null) {
                        return nested;
                    }
                }
                if (conditional.upgradedEffect() != null) {
                    DealDividedDamageEffect nested =
                            findChosenDividedDamageEffect(List.of(conditional.upgradedEffect()));
                    if (nested != null) {
                        return nested;
                    }
                }
            }
        }
        return null;
    }

    private void validateDividedDamageAssignments(GameData gameData, Card card, UUID playerId,
                                                  int resolvedXValue, UUID targetId,
                                                  Map<UUID, Integer> assignments,
                                                  DealDividedDamageEffect dividedEffect) {
        int totalDamage = assignments.values().stream().mapToInt(Integer::intValue).sum();
        if (dividedEffect.totalDamage() instanceof Fixed fixedTotal) {
            if (totalDamage != fixedTotal.value()) {
                throw new IllegalStateException("Damage assignments must sum to " + fixedTotal.value());
            }
            if (!dividedEffect.canTargetPlayers()) {
                for (UUID assignedTargetId : assignments.keySet()) {
                    Permanent target = gameQueryService.findPermanentById(gameData, assignedTargetId);
                    if (target == null || !gameQueryService.isCreature(gameData, target)) {
                        throw new IllegalStateException("All targets must be creatures");
                    }
                }
            } else {
                for (UUID assignedTargetId : assignments.keySet()) {
                    if (gameData.playerIds.contains(assignedTargetId)) {
                        continue;
                    }
                    Permanent target = gameQueryService.findPermanentById(gameData, assignedTargetId);
                    if (target == null || !gameQueryService.isCreature(gameData, target)) {
                        throw new IllegalStateException("All targets must be creatures");
                    }
                    if (card.getTargetFilter() != null
                            && !(card.getTargetFilter() instanceof PlayerPredicateTargetFilter)) {
                        predicateEvaluationService.validateTargetFilter(gameData, card.getTargetFilter(), target);
                    }
                }
            }
            int maxTargets = dividedEffect.maxTargets() > 0
                    ? dividedEffect.maxTargets() : fixedTotal.value();
            if (assignments.size() > maxTargets) {
                throw new IllegalStateException("Too many targets");
            }
        } else if (!(dividedEffect.totalDamage() instanceof Fixed)) {
            int expectedTotal = amountEvaluationService.evaluate(gameData, dividedEffect.totalDamage(),
                    com.github.laxika.magicalvibes.service.effect.AmountContext.forCasting(playerId, resolvedXValue));
            if (totalDamage != expectedTotal) {
                throw new IllegalStateException("Damage assignments must sum to " + expectedTotal);
            }
            for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
                UUID assignedTargetId = assignment.getKey();
                boolean isPlayer = gameData.playerIds.contains(assignedTargetId);
                if (isPlayer) {
                    if (!dividedEffect.canTargetPlayers()) {
                        throw new IllegalStateException("All targets must be creatures");
                    }
                } else {
                    Permanent target = gameQueryService.findPermanentById(gameData, assignedTargetId);
                    if (target == null) {
                        throw new IllegalStateException("Invalid target");
                    }
                    if (!dividedEffect.canTargetPlayers() && !gameQueryService.isCreature(gameData, target)) {
                        throw new IllegalStateException("All targets must be creatures");
                    }
                    if (dividedEffect.targetRestriction() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(
                            gameData, target, dividedEffect.targetRestriction())) {
                        throw new IllegalStateException("Illegal target for divided damage");
                    }
                }
                if (assignment.getValue() <= 0) {
                    throw new IllegalStateException("Each damage assignment must be positive");
                }
            }
        } else {
            if (totalDamage != resolvedXValue) {
                throw new IllegalStateException("Damage assignments must sum to X (" + resolvedXValue + ")");
            }
            for (UUID assignedTargetId : assignments.keySet()) {
                Permanent target = gameQueryService.findPermanentById(gameData, assignedTargetId);
                if (target == null || !gameQueryService.isCreature(gameData, target)) {
                    throw new IllegalStateException("All targets must be creatures");
                }
                if (dividedEffect.targetRestriction() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, target, dividedEffect.targetRestriction())) {
                    throw new IllegalStateException("Illegal target for divided damage");
                }
            }
        }
        for (int amount : assignments.values()) {
            if (amount <= 0) {
                throw new IllegalStateException("Each damage assignment must be positive");
            }
        }
        if (card.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter) {
            targetLegalityService.validateSpellPlayerTarget(gameData, targetId, playerId, playerFilter);
        }
    }

    private DealDividedDamageEffect findKickedDividedDamageEffect(List<CardEffect> effects) {
        for (CardEffect e : effects) {
            if (e instanceof ConditionalReplacementEffect kre && kre.condition() instanceof Kicked
                    && kre.upgradedEffect() instanceof DealDividedDamageEffect ddae) {
                return ddae;
            }
        }
        return null;
    }

    private KickerEffect findKickerEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof KickerEffect)
                .map(e -> (KickerEffect) e)
                .findFirst().orElse(null);
    }

    private BuybackEffect findBuybackEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof BuybackEffect)
                .map(e -> (BuybackEffect) e)
                .findFirst().orElse(null);
    }

    /**
     * Pays the buyback cost right after the base mana payment. On a mana shortfall the whole mana
     * payment is rolled back from {@code preManaPaymentPool} (the snapshot taken before the base
     * payment).
     */
    private void payBuybackCost(GameData gameData, Player player, Card card, BuybackEffect buybackEffect,
                                UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                List<Integer> discardHandCardIndices,
                                int spellCardIndex, ManaPool preManaPaymentPool) {
        try {
            int manaSpent = 0;
            if (buybackEffect.hasManaCost()) {
                ManaCost buybackCost = new ManaCost(buybackEffect.cost());
                ManaPool pool = gameData.playerManaPools.get(player.getId());
                int costModifier = castingCostService.getBuybackCostModifier(gameData, player.getId(), card);
                if (!buybackCost.canPay(pool, costModifier)) {
                    throw new IllegalStateException("Not enough mana to pay buyback cost");
                }
                int before = pool.getTotalAllMana();
                buybackCost.pay(pool, costModifier);
                manaSpent = before - pool.getTotalAllMana();
            }
            if (buybackEffect.hasLifeCost()) {
                payBuybackLifeCost(gameData, player, card, buybackEffect.lifeCost());
            }
            if (buybackEffect.hasSacrificeCost()) {
                payBuybackSacrificeCost(gameData, player, card, buybackEffect, sacrificePermanentId,
                        additionalCostSacrificePermanentIds);
            }
            if (buybackEffect.hasDiscardCost()) {
                if (buybackEffect.hasRandomDiscardCost()) {
                    payRandomBuybackDiscardCost(gameData, player, card, buybackEffect.discardCount());
                } else {
                    payDiscardXCardsCost(gameData, player, card, new DiscardXCardsCost(),
                            buybackEffect.discardCount(), discardHandCardIndices, spellCardIndex);
                }
            }
            gameData.addSpellCastManaSpent(card.getId(), manaSpent);
        } catch (IllegalStateException e) {
            if (preManaPaymentPool != null) {
                gameData.playerManaPools.put(player.getId(), preManaPaymentPool);
            }
            throw e;
        }
    }

    private void payBuybackLifeCost(GameData gameData, Player player, Card card, PayLifeCost cost) {
        UUID playerId = player.getId();
        int currentLife = gameData.getLife(playerId);
        int amount = cost.effectiveAmount(currentLife);
        if (currentLife < amount) {
            throw new IllegalStateException("Not enough life to pay buyback cost");
        }
        if (amount > 0) {
            int lifeLoss = amount * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
            gameData.playerLifeTotals.put(playerId, currentLife - lifeLoss);
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " pays " + lifeLoss + " life for buyback of " + card.getName() + "."));
        }
    }

    private void payRandomBuybackDiscardCost(GameData gameData, Player player, Card card, int count) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.size() < count) {
            throw new IllegalStateException("Must discard a card at random to pay buyback cost");
        }
        triggerCollectionService.beginDiscardEvent(gameData, playerId);
        for (int i = 0; i < count; i++) {
            Card discarded = hand.remove(ThreadLocalRandom.current().nextInt(hand.size()));
            graveyardService.addCardToGraveyard(gameData, playerId, discarded);
            gameData.discardCausedByOpponent = false;
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, discarded);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " discards ")
                    .card(discarded)
                    .text(" at random as a buyback cost for ")
                    .card(card)
                    .text(".")
                    .build());
        }
        triggerCollectionService.finishDiscardEvent(gameData);
    }

    /**
     * Resolves splice (CR 702.47): validates each chosen hand card has a matching {@link SpliceEffect},
     * appends that card's SPELL effects onto the host spell, and returns the splice costs to pay.
     * Indices are pre-removal hand indices (same convention as discard-as-cost). Cards remain in hand.
     */
    private List<SpliceEffect> resolveAndAppendSpliceEffects(GameData gameData, UUID playerId, List<Card> hand,
                                                             Card hostSpell, int spellCardIndex,
                                                             List<Integer> spliceHandCardIndices,
                                                             List<CardEffect> filteredSpellEffects) {
        if (spliceHandCardIndices.isEmpty()) {
            return List.of();
        }
        if (!(hostSpell.hasType(CardType.INSTANT) || hostSpell.hasType(CardType.SORCERY))) {
            throw new IllegalStateException("Splice can only be used when casting an instant or sorcery");
        }
        Set<Integer> seen = new HashSet<>();
        List<SpliceEffect> costs = new ArrayList<>();
        List<String> splicedNames = new ArrayList<>();
        for (Integer rawIndex : spliceHandCardIndices) {
            if (rawIndex == null || rawIndex == spellCardIndex || !seen.add(rawIndex)) {
                throw new IllegalStateException("Invalid splice card index");
            }
            int effectiveIndex = rawIndex > spellCardIndex ? rawIndex - 1 : rawIndex;
            // Hand still contains the spell at this point; adjust as if it were already removed.
            List<Card> remaining = new ArrayList<>(hand);
            remaining.remove(spellCardIndex);
            if (effectiveIndex < 0 || effectiveIndex >= remaining.size()) {
                throw new IllegalStateException("Invalid splice card index");
            }
            Card spliceCard = remaining.get(effectiveIndex);
            SpliceEffect splice = findMatchingSpliceEffect(spliceCard, hostSpell);
            if (splice == null) {
                throw new IllegalStateException(spliceCard.getName() + " cannot be spliced onto " + hostSpell.getName());
            }
            List<CardEffect> splicedEffects = new ArrayList<>(spliceCard.getEffects(EffectSlot.SPELL));
            splicedEffects.addAll(splice.splicedEffects());
            additionalSpellCostService.extractAndRemove(splicedEffects);
            hostSpell.appendSpellTargetingFrom(spliceCard);
            filteredSpellEffects.addAll(splicedEffects);
            costs.add(splice);
            splicedNames.add(spliceCard.getName());
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(playerId) + " splices ")
                    .card(spliceCard)
                    .text(" onto ")
                    .card(hostSpell)
                    .text(".")
                    .build());
        }
        gameData.setSpellCastSplicedNames(hostSpell.getId(), splicedNames);
        return costs;
    }

    private SpliceEffect findMatchingSpliceEffect(Card spliceCard, Card hostSpell) {
        return spliceCard.getEffects(EffectSlot.STATIC).stream()
                .filter(SpliceEffect.class::isInstance)
                .map(SpliceEffect.class::cast)
                .filter(s -> hostSpell.getSubtypes().contains(s.ontoSubtype()))
                .findFirst()
                .orElse(null);
    }

    private void validateSpliceCosts(GameData gameData, Player player, Card hostSpell,
                                     List<SpliceEffect> spliceCosts,
                                     List<UUID> splicePermanentIds) {
        List<UUID> ids = splicePermanentIds != null ? splicePermanentIds : List.of();
        int requiredPermanentIds = 0;
        int requiredSacrificeIds = 0;
        for (SpliceEffect splice : spliceCosts) {
            for (CastingCost cost : splice.costs()) {
                if (cost instanceof SacrificePermanentsCost sacrificeCost) {
                    requiredPermanentIds += sacrificeCost.count();
                    requiredSacrificeIds += sacrificeCost.count();
                } else if (cost instanceof TapUntappedPermanentsCost tapCost) {
                    requiredPermanentIds += tapCost.count();
                } else if (cost instanceof ReturnPermanentsCost returnCost) {
                    requiredPermanentIds += returnCost.count();
                } else if (!(cost instanceof ManaCastingCost)
                        && !(cost instanceof ExileTopCardsFromGraveyardCastingCost)) {
                    throw new IllegalStateException("Unsupported splice cost component: "
                            + cost.getClass().getSimpleName());
                }
            }
        }
        if (ids.size() != requiredPermanentIds) {
            String message = requiredPermanentIds > 0 && requiredPermanentIds == requiredSacrificeIds
                    ? "Must sacrifice " + requiredPermanentIds + " permanents for the splice cost of "
                    + hostSpell.getName()
                    : "Must choose " + requiredPermanentIds + " permanents for the splice cost of "
                    + hostSpell.getName();
            throw new IllegalStateException(message);
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate permanents chosen for the splice cost of "
                    + hostSpell.getName());
        }

        int idIndex = 0;
        for (SpliceEffect splice : spliceCosts) {
            for (CastingCost cost : splice.costs()) {
                if (cost instanceof SacrificePermanentsCost sacrificeCost) {
                    for (int i = 0; i < sacrificeCost.count(); i++) {
                        UUID permanentId = ids.get(idIndex++);
                        additionalSpellCostService.validateSingleSacrificeCost(gameData, player, hostSpell,
                                permanentId, "a matching permanent",
                                permanent -> predicateEvaluationService.matchesPermanentPredicate(
                                        gameData, permanent, sacrificeCost.filter()));
                    }
                } else if (cost instanceof TapUntappedPermanentsCost tapCost) {
                    for (int i = 0; i < tapCost.count(); i++) {
                        additionalSpellCostService.validateSingleTapCost(gameData, player, hostSpell,
                                tapCost.filter(), ids.get(idIndex++));
                    }
                } else if (cost instanceof ReturnPermanentsCost returnCost) {
                    for (int i = 0; i < returnCost.count(); i++) {
                        validateSpliceReturnCost(gameData, player, hostSpell, returnCost.filter(), ids.get(idIndex++));
                    }
                }
            }
        }
    }

    private void paySpliceCosts(GameData gameData, Player player, Card hostSpell,
                                List<SpliceEffect> spliceCosts, List<UUID> splicePermanentIds,
                                ManaPool preManaPaymentPool) {
        if (spliceCosts.isEmpty()) {
            return;
        }
        UUID playerId = player.getId();
        List<UUID> permanentIds = splicePermanentIds != null ? splicePermanentIds : List.of();
        try {
            int graveyardExileTotal = spliceCosts.stream().mapToInt(SpliceEffect::exileFromGraveyardCount).sum();
            validateSpliceGraveyardExileCost(gameData, player, hostSpell, graveyardExileTotal);
            for (SpliceEffect splice : spliceCosts) {
                for (CastingCost cost : splice.costs()) {
                    if (cost instanceof ManaCastingCost manaCost) {
                        ManaCost mana = new ManaCost(manaCost.manaCost());
                        ManaPool pool = gameData.playerManaPools.get(playerId);
                        if (!mana.canPay(pool)) {
                            throw new IllegalStateException("Not enough mana to pay splice cost");
                        }
                        int before = pool.getTotalAllMana();
                        mana.pay(pool, 0);
                        gameData.addSpellCastManaSpent(hostSpell.getId(), before - pool.getTotalAllMana());
                    }
                }
            }

            int permanentIndex = 0;
            for (SpliceEffect splice : spliceCosts) {
                for (CastingCost cost : splice.costs()) {
                    if (cost instanceof SacrificePermanentsCost sacrificeCost) {
                        for (int i = 0; i < sacrificeCost.count(); i++) {
                            UUID permanentId = permanentIds.get(permanentIndex++);
                            paySingleSacrificeCost(gameData, player, hostSpell, permanentId,
                                    "a matching permanent",
                                    permanent -> predicateEvaluationService.matchesPermanentPredicate(
                                            gameData, permanent, sacrificeCost.filter()));
                        }
                    } else if (cost instanceof TapUntappedPermanentsCost tapCost) {
                        for (int i = 0; i < tapCost.count(); i++) {
                            Permanent permanent = additionalSpellCostService.validateSingleTapCost(
                                    gameData, player, hostSpell, tapCost.filter(), permanentIds.get(permanentIndex++));
                            permanent.tap();
                            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
                            gameLogService.append(gameData, GameLog.builder()
                                    .text(player.getUsername() + " taps ")
                                    .card(permanent.getCard())
                                    .text(" to splice onto ")
                                    .card(hostSpell)
                                    .text(".")
                                    .build());
                        }
                    } else if (cost instanceof ReturnPermanentsCost returnCost) {
                        for (int i = 0; i < returnCost.count(); i++) {
                            Permanent permanent = validateSpliceReturnCost(gameData, player, hostSpell,
                                    returnCost.filter(), permanentIds.get(permanentIndex++));
                            permanentRemovalService.removePermanentToHand(gameData, permanent);
                            gameLogService.append(gameData, GameLog.builder()
                                    .text(player.getUsername() + " returns ")
                                    .card(permanent.getCard())
                                    .text(" to hand to splice onto ")
                                    .card(hostSpell)
                                    .text(".")
                                    .build());
                        }
                    }
                }
            }
            paySpliceGraveyardExileCost(gameData, player, hostSpell, graveyardExileTotal);
        } catch (IllegalStateException e) {
            if (preManaPaymentPool != null) {
                gameData.playerManaPools.put(playerId, preManaPaymentPool);
            }
            throw e;
        }
    }

    /**
     * Validates the "exile N cards from your graveyard" splice cost (Horobi's Whisper) before any
     * cost is consumed. {@code count} is the total across every spliced card that has such a cost.
     */
    private void validateSpliceGraveyardExileCost(GameData gameData, Player player, Card hostSpell, int count) {
        if (count <= 0) {
            return;
        }
        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        if (graveyard == null || graveyard.size() < count) {
            throw new IllegalStateException("Not enough cards in graveyard to splice onto " + hostSpell.getName());
        }
    }

    /**
     * Pays the "exile N cards from your graveyard" splice cost. The oldest cards are taken because
     * splice has no client-side choice UI; see {@link SpliceEffect} for why the pick is deterministic.
     */
    private void paySpliceGraveyardExileCost(GameData gameData, Player player, Card hostSpell, int count) {
        if (count <= 0) {
            return;
        }
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Card> exiledCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (int i = 0; i < count; i++) {
                Card exiledCard = graveyard.remove(0);
                exiledCards.add(exiledCard);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiledCard);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        for (Card exiledCard : exiledCards) {
            gameData.addToExile(playerId, exiledCard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " exiles ")
                    .card(exiledCard)
                    .text(" from graveyard to splice onto ")
                    .card(hostSpell)
                    .text(".")
                    .build());
        }
    }

    /**
     * Validates the non-mana permanent components of the splice costs being paid (CR 702.47b) before
     * any cost is consumed, so a bad choice rewinds the cast instead of eating mana. Ids are consumed
     * in order, one per spliced card that has a tap or return cost; a permanent may only be chosen
     * once. The returned list is index-aligned with {@code permanentSplices}.
     */
    private List<Permanent> validateSplicePermanentCosts(GameData gameData, Player player, Card hostSpell,
                                                         List<SpliceEffect> permanentSplices, List<UUID> spliceCostPermanentIds) {
        if (permanentSplices.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = spliceCostPermanentIds != null ? spliceCostPermanentIds : List.of();
        if (ids.size() != permanentSplices.size()) {
            throw new IllegalStateException("A permanent must be chosen for each splice cost");
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate permanents chosen for splice");
        }
        List<Permanent> chosen = new ArrayList<>();
        for (int i = 0; i < permanentSplices.size(); i++) {
            SpliceEffect splice = permanentSplices.get(i);
            chosen.add(splice.tapCost() != null
                    ? additionalSpellCostService.validateSingleTapCost(gameData, player, hostSpell, splice.tapCost(), ids.get(i))
                    : validateSpliceReturnCost(gameData, player, hostSpell, splice.returnCost(), ids.get(i)));
        }
        return chosen;
    }

    /**
     * Validates the "return a matching permanent you control to its owner's hand" splice cost
     * (Veil of Secrecy) without mutating anything.
     */
    private Permanent validateSpliceReturnCost(GameData gameData, Player player, Card hostSpell,
                                               PermanentPredicate filter, UUID permanentId) {
        if (permanentId == null) {
            throw new IllegalStateException("A permanent must be chosen to return for the splice cost");
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null) {
            throw new IllegalStateException("Permanent to return not found on battlefield");
        }
        if (!player.getId().equals(gameQueryService.findPermanentController(gameData, permanentId))) {
            throw new IllegalStateException("Can only return permanents you control to splice onto " + hostSpell.getName());
        }
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
            throw new IllegalStateException("Permanent does not match the splice cost of " + hostSpell.getName());
        }
        return permanent;
    }

    /**
     * Pays the kicker cost right after the base mana payment. The kicker's mana affordability can
     * only be checked against the pool as it stands after the base payment, so on a kicker
     * shortfall the whole mana payment is rolled back from {@code preManaPaymentPool} (a snapshot
     * taken before the base payment; null when the kicker has no mana component) — the failed
     * cast rewinds instead of eating the base mana (CR 601.2h). The kicker's sacrifice component
     * is pre-validated by the caller and paid only after the kicker mana succeeded.
     */
    private void validateKickerSacrificeCost(GameData gameData, Player player, Card card,
                                              KickerEffect kickerEffect, UUID sacrificePermanentId,
                                              List<UUID> sacrificePermanentIds) {
        if (kickerEffect.sacrificeCount() == 1) {
            additionalSpellCostService.validateSingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    kickerEffect.sacrificeDescription(),
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, kickerEffect.sacrificePredicate()));
            return;
        }
        additionalSpellCostService.validateMultipleSacrificeCost(gameData, player, card,
                new SacrificeMultiplePermanentsCost(kickerEffect.sacrificeCount(), kickerEffect.sacrificePredicate()),
                sacrificePermanentIds);
    }

    private void payKickerCost(GameData gameData, Player player, Card card, KickerEffect kickerEffect,
                               UUID sacrificePermanentId, Integer discardHandCardIndex,
                               List<UUID> sacrificePermanentIds, int spellCardIndex,
                               ManaPool preManaPaymentPool, int kickerXValue) {
        try {
            gameData.addSpellCastManaSpent(card.getId(), computeKickerManaPayment(gameData, player, card, kickerEffect,
                    sacrificePermanentId, discardHandCardIndex, sacrificePermanentIds, spellCardIndex, kickerXValue));
        } catch (IllegalStateException e) {
            if (preManaPaymentPool != null) {
                gameData.playerManaPools.put(player.getId(), preManaPaymentPool);
            }
            throw e;
        }
    }

    private int computeKickerManaPayment(GameData gameData, Player player, Card card, KickerEffect kickerEffect,
                                         UUID sacrificePermanentId, Integer discardHandCardIndex,
                                         List<UUID> sacrificePermanentIds, int spellCardIndex,
                                         int kickerXValue) {
        UUID playerId = player.getId();
        int manaSpent = 0;

        // Pay mana cost if any
        if (kickerEffect.hasManaCost()) {
            ManaCost kickerCost = new ManaCost(kickerEffect.cost());
            int xValue = kickerCost.hasX() ? kickerXValue : 0;
            if (xValue < 0) {
                throw new IllegalStateException("X value cannot be negative");
            }
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int before = pool.getTotalAllMana();
            ManaPool.MulticoloredSpellManaState multicoloredMana =
                    gameQueryService.getEffectiveCardColors(gameData, card).size() >= 2
                            ? pool.promoteMulticoloredSpellOnlyMana() : null;
            try {
                if (kickerCost.hasX() && kickerEffect.hasXColorRestriction()) {
                    if (kickerEffect.xUsesEachColorAtMostOnce()
                            && xValue > countAvailableColors(pool, kickerEffect.xColorRestrictions())) {
                        throw new IllegalStateException("No more than one mana of each color may be spent on kicker X");
                    }
                    if (!kickerCost.canPay(pool, xValue, kickerEffect.xColorRestrictions(), 0)) {
                        throw new IllegalStateException("Not enough colored mana to pay kicker X");
                    }
                    kickerCost.pay(pool, xValue, kickerEffect.xColorRestrictions(), 0);
                } else if (pool.getKickedOnlyGreen() > 0) {
                    if (!kickerCost.canPay(pool, xValue, false, false, false, true)) {
                        throw new IllegalStateException("Not enough mana to pay kicker cost");
                    }
                    kickerCost.pay(pool, xValue, false, false, false, true);
                } else {
                    if (!kickerCost.canPay(pool, xValue)) {
                        throw new IllegalStateException("Not enough mana to pay kicker cost");
                    }
                    kickerCost.pay(pool, xValue);
                }
            } finally {
                if (multicoloredMana != null) {
                    pool.restorePromotedMulticoloredSpellOnlyMana(multicoloredMana);
                }
            }
            manaSpent = before - pool.getTotalAllMana();
        }

        if (kickerEffect.hasLifeCost()) {
            payLifeCost(gameData, player, card, kickerEffect.lifeCost());
        }

        // Pay sacrifice cost if any
        if (kickerEffect.hasSacrificeCost()) {
            if (kickerEffect.sacrificeCount() == 1) {
                paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                        kickerEffect.sacrificeDescription(),
                        p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, kickerEffect.sacrificePredicate()));
            } else {
                payMultipleSacrificeCost(gameData, player, card,
                        new SacrificeMultiplePermanentsCost(kickerEffect.sacrificeCount(), kickerEffect.sacrificePredicate()),
                        sacrificePermanentIds);
            }
        }
        if (kickerEffect.hasTapCost()) {
            Permanent tapped = additionalSpellCostService.validateSingleTapCost(
                    gameData, player, card, kickerEffect.tapPredicate(), sacrificePermanentId);
            tapped.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, tapped);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " taps ")
                    .card(tapped.getCard())
                    .text(" to pay kicker cost for ")
                    .card(card)
                    .text(".")
                    .build());
        }
        if (kickerEffect.hasReturnCost()) {
            payReturnPermanentToHandCost(gameData, player, card,
                    new ReturnPermanentToHandCost(kickerEffect.returnPredicate()), sacrificePermanentId);
        }
        if (kickerEffect.hasDiscardCost()) {
            payDiscardCost(gameData, player, card,
                    new DiscardCardTypeCost(kickerEffect.discardPredicate(), kickerEffect.discardDescription()),
                    discardHandCardIndex, spellCardIndex);
        }
        return manaSpent;
    }

    private int countAvailableColors(ManaPool pool, Set<ManaColor> colors) {
        int count = 0;
        for (ManaColor color : colors) {
            if (pool.get(color) > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Consumes the one-shot graveyard cast permission the card was cast with and applies its rider:
     * Havengul Lich queues the "gains all activated abilities of that card" trigger. Returns the
     * consumed permission, or {@code null} if the card was not cast via such a permission.
     */
    private GameData.GraveyardCardCastPermission consumeGraveyardCardCastPermission(GameData gameData, UUID playerId,
                                                                                    Card castCard) {
        GameData.GraveyardCardCastPermission permission =
                gameData.graveyardCardCastPermissionsUntilEndOfTurn.remove(castCard.getId());
        if (permission == null || !permission.copySourceActivatedAbilities()) {
            return permission;
        }

        UUID sourcePermanentId = permission.sourcePermanentId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        Card sourceCard = source != null ? source.getCard() : castCard;
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                playerId,
                sourceCard.getName() + "'s ability",
                List.of(new GrantSourceActivatedAbilitiesUntilEndOfTurnEffect(
                        new ArrayList<>(castCard.getActivatedAbilities()),
                        castCard.getName())),
                0,
                null,
                sourcePermanentId,
                Map.of(),
                null,
                List.of(),
                List.of()
        ));
        return permission;
    }

    private GraveyardCardLocation findGraveyardCardLocation(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            for (int i = 0; i < graveyard.size(); i++) {
                if (cardId.equals(graveyard.get(i).getId())) {
                    return new GraveyardCardLocation(graveyard, i);
                }
            }
        }
        return null;
    }

    private record GraveyardCardLocation(List<Card> graveyard, int index) {}

    /**
     * Pays retrace's additional cost (CR 702.81): discard a land card from the caster's hand.
     * {@code discardHandCardIndex} indexes directly into the caster's hand (the retraced spell
     * itself is in the graveyard, so no index adjustment is needed). Fires discard triggers.
     */
    private void payRetraceDiscardCost(GameData gameData, Player player, Card card, Integer discardHandCardIndex) {
        additionalSpellCostService.validateRetraceDiscardCost(gameData, player, card, discardHandCardIndex);
        payGraveyardDiscardCost(gameData, player, card, discardHandCardIndex, "to retrace ");
    }

    private void validateJumpStartDiscardCost(GameData gameData, Player player, Card card,
                                               Integer discardHandCardIndex) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        if (discardHandCardIndex == null || hand == null
                || discardHandCardIndex < 0 || discardHandCardIndex >= hand.size()) {
            throw new IllegalStateException("Must discard a card to jump-start " + card.getName());
        }
    }

    /** Guildmages' Forum: each tagged mana spent on a multicolored creature spell grants one additional counter. */
    private void applyAdditionalCounterGrantingMana(GameData gameData, UUID playerId, Card card,
                                                    int additionalCounterGrantingBefore) {
        if (!card.hasType(CardType.CREATURE) || card.getColors() == null || card.getColors().size() < 2) {
            return;
        }
        int spent = additionalCounterGrantingBefore
                - additionalCounterGrantingManaAvailable(gameData, playerId);
        if (spent > 0) {
            gameData.spellAdditionalEnterCounters.merge(card.getId(), spent, Integer::sum);
        }
    }

    private void addCreatureSpellAdditionalCounters(GameData gameData, Card card,
                                                    List<String> repeatedAdditionalCosts,
                                                    boolean applies) {
        if (!applies || repeatedAdditionalCosts == null) {
            return;
        }
        int counters = (int) repeatedAdditionalCosts.stream()
                .filter("{1}"::equals)
                .count();
        if (counters > 0) {
            gameData.spellAdditionalEnterCounters.merge(card.getId(), counters, Integer::sum);
        }
    }

    private void applyRiotGrantingMana(GameData gameData, UUID playerId, Card card,
                                       int riotGrantingBefore) {
        if (!card.hasType(CardType.CREATURE)) {
            return;
        }
        int spent = riotGrantingBefore - riotGrantingManaAvailable(gameData, playerId);
        if (spent > 0) {
            gameData.spellsGrantedRiotOnEntry.add(card.getId());
        }
    }

    private void payJumpStartDiscardCost(GameData gameData, Player player, Card card,
                                         Integer discardHandCardIndex) {
        validateJumpStartDiscardCost(gameData, player, card, discardHandCardIndex);
        payGraveyardDiscardCost(gameData, player, card, discardHandCardIndex, "to jump-start ");
    }

    private void payGraveyardDiscardCost(GameData gameData, Player player, Card card,
                                         int discardHandCardIndex, String reason) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card toDiscard = hand.get(discardHandCardIndex);
        hand.remove((int) discardHandCardIndex);
        graveyardService.addCardToGraveyard(gameData, playerId, toDiscard);
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " discards ")
                .card(toDiscard)
                .text(reason)
                .card(card)
                .text(".")
                .build());
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, toDiscard);
    }

    private void validateGraveyardCastAdditionalCosts(GameData gameData, UUID playerId, Card card,
                                                       GraveyardCast graveyardCast,
                                                       Integer discardHandCardIndex,
                                                       List<Integer> exileGraveyardCardIndices) {
        graveyardCast.getCost(LifeCastingCost.class).ifPresent(lifeCost -> {
            if (gameData.getLife(playerId) < lifeCost.amount()) {
                throw new IllegalStateException("Not enough life to pay graveyard cast cost");
            }
        });
        if (graveyardCast.getCost(DiscardCardCastingCost.class).isPresent()) {
            validateGraveyardDiscardSelection(gameData, playerId, discardHandCardIndex);
        }
        graveyardCast.getCost(ExileNCardsFromGraveyardCastingCost.class).ifPresent(exileCost -> {
            List<Integer> indices = exileGraveyardCardIndices == null ? List.of() : exileGraveyardCardIndices;
            if (indices.size() != exileCost.count()) {
                throw new IllegalStateException("Must exile exactly " + exileCost.count()
                        + " cards from your graveyard to cast " + card.getName() + " from the graveyard");
            }
            if (indices.stream().distinct().count() != indices.size()) {
                throw new IllegalStateException("Duplicate cards chosen for the graveyard cast of " + card.getName());
            }
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
            for (int index : indices) {
                if (index < 0 || index >= graveyard.size()) {
                    throw new IllegalStateException("Invalid graveyard card chosen for the cost of " + card.getName());
                }
                Card selected = graveyard.get(index);
                if (selected.getId().equals(card.getId())) {
                    throw new IllegalStateException("A spell cannot exile itself for its graveyard cast cost");
                }
                if (exileCost.predicate() != null
                        && !predicateEvaluationService.matchesCardPredicate(
                        selected, exileCost.predicate(), selected.getId())) {
                    String label = exileCost.label() == null ? "a matching" : "a " + exileCost.label();
                    throw new IllegalStateException("Each card exiled for the graveyard cast cost must be " + label);
                }
            }
        });
    }

    private void validateGraveyardCastAdditionalCosts(GameData gameData, UUID playerId,
                                                       List<CastingCost> additionalCosts,
                                                       Integer discardHandCardIndex) {
        for (CastingCost cost : additionalCosts) {
            if (cost instanceof LifeCastingCost lifeCost) {
                if (!gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData)) {
                    throw new IllegalStateException("Cannot pay life as a cost");
                }
                if (gameData.getLife(playerId) < lifeCost.amount()) {
                    throw new IllegalStateException("Not enough life to pay graveyard cast cost");
                }
            } else if (cost instanceof DiscardCardCastingCost) {
                validateGraveyardDiscardSelection(gameData, playerId, discardHandCardIndex);
            } else {
                throw new IllegalStateException("Cannot pay this graveyard cast cost");
            }
        }
    }

    private void validateGraveyardDiscardSelection(GameData gameData, UUID playerId,
                                                   Integer discardHandCardIndex) {
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        if (discardHandCardIndex == null
                || discardHandCardIndex < 0
                || discardHandCardIndex >= hand.size()) {
            throw new IllegalStateException("Must discard a card to cast from the graveyard");
        }
    }

    private void payGraveyardCastAdditionalCosts(GameData gameData, Player player, Card card,
                                                  GraveyardCast graveyardCast,
                                                  Integer discardHandCardIndex,
                                                  List<Integer> exileGraveyardCardIndices) {
        UUID playerId = player.getId();
        ExileNCardsFromGraveyardCastingCost exileCost =
                graveyardCast.getCost(ExileNCardsFromGraveyardCastingCost.class).orElse(null);
        if (exileCost != null) {
            validateGraveyardCastAdditionalCosts(gameData, playerId, card, graveyardCast,
                    discardHandCardIndex, exileGraveyardCardIndices);
        }
        graveyardCast.getCost(LifeCastingCost.class).ifPresent(lifeCost -> {
            int lifeLoss = lifeCost.amount()
                    * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
            gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - lifeLoss);
            gameData.lifeLostThisTurn.merge(playerId, lifeLoss, Integer::sum);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + lifeLoss + " life for ", card, "."));
        });
        if (graveyardCast.getCost(DiscardCardCastingCost.class).isPresent()) {
            Card discarded = gameData.playerHands.get(playerId).remove((int) discardHandCardIndex);
            graveyardService.addCardToGraveyard(gameData, playerId, discarded);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " discards ")
                    .card(discarded)
                    .text(" to cast ")
                    .card(card)
                    .text(" from the graveyard.")
                    .build());
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, discarded);
        }
        if (exileCost != null) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            List<Integer> sortedDescending = exileGraveyardCardIndices.stream()
                    .sorted(java.util.Comparator.reverseOrder()).toList();
            List<Card> exiledCards = new ArrayList<>();
            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                for (int index : sortedDescending) {
                    Card exiledCard = graveyard.remove(index);
                    graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiledCard);
                    exiledCards.add(exiledCard);
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }
            for (Card exiledCard : exiledCards) {
                gameData.addToExile(playerId, exiledCard);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " exiles ")
                        .card(exiledCard)
                        .text(" from graveyard to cast ")
                        .card(card)
                        .text(" from the graveyard.")
                        .build());
            }
        }
    }

    private void payGraveyardCastAdditionalCosts(GameData gameData, Player player, Card card,
                                                  List<CastingCost> additionalCosts,
                                                  Integer discardHandCardIndex) {
        UUID playerId = player.getId();
        for (CastingCost cost : additionalCosts) {
            if (cost instanceof LifeCastingCost lifeCost) {
                gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - lifeCost.amount());
                gameData.lifeLostThisTurn.merge(playerId, lifeCost.amount(), Integer::sum);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + lifeCost.amount() + " life for ", card, "."));
            } else if (cost instanceof DiscardCardCastingCost) {
                Card discarded = gameData.playerHands.get(playerId).remove((int) discardHandCardIndex);
                graveyardService.addCardToGraveyard(gameData, playerId, discarded);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " discards ")
                        .card(discarded)
                        .text(" to cast ")
                        .card(card)
                        .text(" from the graveyard.")
                        .build());
                triggerCollectionService.checkDiscardTriggers(gameData, playerId, discarded);
            } else {
                throw new IllegalStateException("Cannot pay this graveyard cast cost");
            }
        }
    }

    private int payFlashbackOrGraveyardCastCost(GameData gameData, Player player, Card card,
                                                Optional<FlashbackCast> flashbackOpt,
                                                Optional<FlashbackCast> grantedFlashbackOpt,
                                                Optional<HarmonizeCast> harmonizeOpt,
                                                Optional<DisturbCast> disturbOpt,
                                                Optional<GraveyardCast> graveyardCastOpt,
                                                boolean grantedFlashback, boolean emblemFlashback,
                                                boolean grantedGraveyardCardCast, boolean isGrantedGraveyardCast,
                                                boolean isGrantedGraveyardPlay, boolean isGraveyardCast,
                                                boolean isHarmonize, boolean isRetrace, boolean isJumpStart, boolean isDisturb, boolean isGrantedCyclingGraveyardCast,
                                                boolean isMayCastTopInstantOrSorcery,
                                                boolean withoutPayingManaCost,
                                                int effectiveXValue, int additionalCost,
                                                List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                                UUID sacrificePermanentId,
                                                List<UUID> additionalCostSacrificePermanentIds,
                                                List<Integer> discardHandCardIndices) {
        UUID playerId = player.getId();
        // GraveyardCast may override the normal mana cost with an alternate one ("by paying {W}{U}{B}{R}{G}
        // rather than paying its mana cost" — Worldheart Phoenix). When present, it is paid like a normal
        // mana cost (no flashback mana restriction).
        String graveyardAlternateManaCost = isGraveyardCast
                ? graveyardCastOpt.map(GraveyardCast::alternateManaCost).orElse(null)
                : null;
        boolean usesNormalManaCost = (isGraveyardCast && graveyardAlternateManaCost == null)
                || grantedFlashback || emblemFlashback || grantedGraveyardCardCast
                || isGrantedGraveyardCast || isGrantedGraveyardPlay || isRetrace || isJumpStart
                || isGrantedCyclingGraveyardCast || isMayCastTopInstantOrSorcery;
        int manaSpent = 0;

        // Retrace (CR 702.81): as an additional cost, discard a land card from hand. Validated
        // up front but paid only after the mana cost succeeds, so a rejected cast leaks neither
        // the discard nor the mana (CR 601.2h).
        if (isRetrace) {
            additionalSpellCostService.validateRetraceDiscardCost(gameData, player, card, retraceDiscardHandCardIndex);
        }
        if (isJumpStart) {
            validateJumpStartDiscardCost(gameData, player, card, retraceDiscardHandCardIndex);
        }

        if (isHarmonize) {
            String harmonizeManaCost = harmonizeOpt
                    .flatMap(h -> h.getCost(ManaCastingCost.class).map(ManaCastingCost::manaCost))
                    .orElse(card.getManaCost());
            if (harmonizeManaCost == null) {
                throw new IllegalStateException("Harmonize has no mana cost");
            }
            ManaCost cost = castingCostService.applyColoredManaCostReductions(
                    gameData, playerId, card, new ManaCost(harmonizeManaCost));
            int reduction = harmonizeTapPower(gameData, tapPermanentIds);
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int before = pool.getTotalAllMana();
            withMulticoloredSpellMana(gameData, playerId, card, () -> {
                if (!cost.canPayFromGraveyard(pool, effectiveXValue, additionalCost - reduction)) {
                    throw new IllegalStateException("Not enough mana to pay harmonize cost");
                }
                cost.payFromGraveyard(pool, effectiveXValue, additionalCost - reduction);
                return null;
            });
            manaSpent = before - pool.getTotalAllMana();
            payHarmonizeTapCost(gameData, player, card, tapPermanentIds);
            gameData.addSpellCastManaSpent(card.getId(), manaSpent);
            return effectiveXValue;
        }

        if (graveyardAlternateManaCost != null) {
            ManaCost cost = castingCostService.applyColoredManaCostReductions(
                    gameData, playerId, card, new ManaCost(graveyardAlternateManaCost));
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int before = pool.getTotalAllMana();
            withMulticoloredSpellMana(gameData, playerId, card, () -> {
                if (!cost.canPayFromGraveyard(pool, effectiveXValue + additionalCost)) {
                    throw new IllegalStateException("Not enough mana to pay graveyard cast cost");
                }
                cost.payFromGraveyard(pool, effectiveXValue + additionalCost);
                return null;
            });
            manaSpent = before - pool.getTotalAllMana();
            if (isRetrace) {
                payRetraceDiscardCost(gameData, player, card, retraceDiscardHandCardIndex);
            }
            if (isJumpStart) {
                payJumpStartDiscardCost(gameData, player, card, retraceDiscardHandCardIndex);
            }
            if (isGraveyardCast) {
                payGraveyardCastPermanentSacrificeCosts(
                        gameData, player, card, graveyardCastOpt.orElseThrow(), tapPermanentIds);
            }
            gameData.addSpellCastManaSpent(card.getId(), manaSpent);
            return effectiveXValue;
        }

        if (isDisturb) {
            DisturbCast disturb = disturbOpt.orElseThrow(() -> new IllegalStateException("Disturb has no cost"));
            var manaCostOpt = disturb.getCost(ManaCastingCost.class);
            if (manaCostOpt.isEmpty()) {
                throw new IllegalStateException("Disturb has no mana cost");
            }
            ManaCost cost = castingCostService.applyColoredManaCostReductions(
                    gameData, playerId, card, new ManaCost(manaCostOpt.get().manaCost()));
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int before = pool.getTotalAllMana();
            withMulticoloredSpellMana(gameData, playerId, card, () -> {
                if (!cost.canPayForDisturbFromGraveyard(pool, effectiveXValue, additionalCost)) {
                    throw new IllegalStateException("Not enough mana to pay disturb cost");
                }
                cost.payForDisturbFromGraveyard(pool, effectiveXValue, additionalCost);
                return null;
            });
            manaSpent = before - pool.getTotalAllMana();
            gameData.addSpellCastManaSpent(card.getId(), manaSpent);
            return effectiveXValue;
        }

        if (usesNormalManaCost) {
            boolean cardHasFlashback = flashbackOpt.isPresent() || grantedFlashback || emblemFlashback;
            String grantedFlashbackManaCost = emblemFlashback
                    ? grantedFlashbackOpt.flatMap(flashback -> flashback.getCost(ManaCastingCost.class)
                    .map(ManaCastingCost::manaCost)).orElse(card.getManaCost())
                    : card.getManaCost();
            ManaCost cost = castingCostService.applyColoredManaCostReductions(
                    gameData, playerId, card,
                    new ManaCost(withoutPayingManaCost ? "" : grantedFlashbackManaCost), cardHasFlashback);
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int before = pool.getTotalAllMana();
            boolean anyManaType = castingPermissionService.hasAnyManaTypePermission(
                    gameData, playerId, card.getId());
            withMulticoloredSpellMana(gameData, playerId, card, () -> {
                if (anyManaType) {
                    if (!cost.canPayAsGeneric(pool, effectiveXValue, additionalCost)) {
                        throw new IllegalStateException("Not enough mana to pay casting cost");
                    }
                    cost.payAsGeneric(pool, effectiveXValue, additionalCost);
                    if (gameData.hasPendingAnyManaTypeForNextSpellThisTurn(playerId)) {
                        gameData.markSpellPaidUsingPendingAnyManaType(card.getId());
                    }
                } else if (cardHasFlashback) {
                    int manaXValue = cost.hasX() ? effectiveXValue : 0;
                    if (!cost.canPayFlashbackFromGraveyard(pool, manaXValue + additionalCost)) {
                        throw new IllegalStateException("Not enough mana to pay flashback cost");
                    }
                    cost.payFlashbackFromGraveyard(pool, manaXValue + additionalCost);
                } else {
                    if (!cost.canPayFromGraveyard(pool, effectiveXValue + additionalCost)) {
                        throw new IllegalStateException("Not enough mana to pay casting cost");
                    }
                    cost.payFromGraveyard(pool, effectiveXValue + additionalCost);
                }
                return null;
            });
            manaSpent = before - pool.getTotalAllMana();
        } else {
            FlashbackCast flashback = flashbackOpt.orElseThrow(() -> new IllegalStateException("Flashback has no cost"));
            var manaCostOpt = flashback.getCost(ManaCastingCost.class);
            if (manaCostOpt.isPresent()) {
                ManaCost cost = castingCostService.applyColoredManaCostReductions(
                        gameData, playerId, card, new ManaCost(manaCostOpt.get().manaCost()), true);
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int before = pool.getTotalAllMana();
                int manaXValue = cost.hasX() ? effectiveXValue : 0;
                withMulticoloredSpellMana(gameData, playerId, card, () -> {
                    if (!cost.canPayFlashbackFromGraveyard(pool, manaXValue + additionalCost)) {
                        throw new IllegalStateException("Not enough mana to pay flashback cost");
                    }
                    cost.payFlashbackFromGraveyard(pool, manaXValue + additionalCost);
                    return null;
                });
                manaSpent = before - pool.getTotalAllMana();
            }

            var tapCost = flashback.getCost(TapUntappedPermanentsCost.class);
            if (tapCost.isPresent()) {
                int requiredCount = tapCost.get().count();
                if (tapPermanentIds.size() != requiredCount) {
                    throw new IllegalStateException("Must tap exactly " + requiredCount + " permanents");
                }
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                for (UUID tapId : tapPermanentIds) {
                    Permanent toTap = battlefield.stream()
                            .filter(p -> p.getId().equals(tapId))
                            .findFirst()
                            .orElse(null);
                    if (toTap == null) {
                        throw new IllegalStateException("Tap target not found on your battlefield");
                    }
                    if (toTap.isTapped()) {
                        throw new IllegalStateException("Permanent is already tapped");
                    }
                    if (!predicateEvaluationService.matchesPermanentPredicate(toTap, tapCost.get().filter(),
                            FilterContext.of(gameData).withSourceControllerId(playerId))) {
                        throw new IllegalStateException("Tap target does not match the required filter");
                    }
                    toTap.tap();
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " taps ")
                            .card(toTap.getCard())
                            .text(" for ")
                            .card(card)
                            .text(".")
                            .build());
                }
            }

            var lifeCost = flashback.getCost(LifeCastingCost.class);
            lifeCost.ifPresent(cost -> {
                gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - cost.amount());
                gameData.lifeLostThisTurn.merge(playerId, cost.amount(), Integer::sum);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + cost.amount() + " life for ", card, "."));
            });
            List<UUID> sacrificePermanentIds = new ArrayList<>();
            if (sacrificePermanentId != null) {
                sacrificePermanentIds.add(sacrificePermanentId);
            }
            if (additionalCostSacrificePermanentIds != null) {
                sacrificePermanentIds.addAll(additionalCostSacrificePermanentIds);
            }
            boolean hasSacrificeCost = !flashback.getCosts(SacrificePermanentsCost.class).isEmpty()
                    || !flashback.getCosts(SacrificeXPermanentsCastingCost.class).isEmpty();
            if (hasSacrificeCost) {
                payFlashbackPermanentSacrificeCosts(gameData, player, card, flashback,
                        effectiveXValue, sacrificePermanentIds);
            }
            flashback.getCost(DiscardXCardsCastingCost.class).ifPresent(discardCost ->
                    payDiscardXCardsCost(gameData, player, card,
                            new DiscardXCardsCost(discardCost.predicate(), discardCost.label(), false, false), effectiveXValue,
                            discardHandCardIndices, -1));
            if (manaCostOpt.isEmpty() && tapCost.isEmpty()
                    && lifeCost.isEmpty()
                    && !hasSacrificeCost
                    && flashback.getCost(DiscardXCardsCastingCost.class).isEmpty()) {
                throw new IllegalStateException("Flashback has no cost");
            }
        }

        if (isRetrace) {
            payRetraceDiscardCost(gameData, player, card, retraceDiscardHandCardIndex);
        }
        if (isJumpStart) {
            payJumpStartDiscardCost(gameData, player, card, retraceDiscardHandCardIndex);
        }
        if (isGraveyardCast) {
            payGraveyardCastPermanentSacrificeCosts(
                    gameData, player, card, graveyardCastOpt.orElseThrow(), tapPermanentIds);
        }
        gameData.addSpellCastManaSpent(card.getId(), manaSpent);
        return effectiveXValue;
    }

    private boolean targetsCreatureControlledBy(GameData gameData, UUID controllerId,
                                                 UUID targetId, List<UUID> targetIds) {
        List<UUID> allTargets = new ArrayList<>();
        if (targetId != null) {
            allTargets.add(targetId);
        }
        if (targetIds != null) {
            allTargets.addAll(targetIds);
        }
        return allTargets.stream().anyMatch(candidateId -> {
            Permanent target = gameQueryService.findPermanentById(gameData, candidateId);
            return target != null
                    && gameQueryService.isCreature(gameData, target)
                    && controllerId.equals(gameQueryService.findPermanentController(gameData, candidateId));
        });
    }

    private void validateHarmonizeTapCost(GameData gameData, Player player, List<UUID> tapPermanentIds) {
        List<UUID> selectedIds = tapPermanentIds == null ? List.of() : tapPermanentIds;
        if (selectedIds.size() > 1) {
            throw new IllegalStateException("Harmonize can tap at most one creature");
        }
        if (selectedIds.isEmpty()) {
            return;
        }
        UUID permanentId = selectedIds.getFirst();
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null || !player.getId().equals(gameQueryService.findPermanentController(gameData, permanentId))) {
            throw new IllegalStateException("Harmonize creature is not on your battlefield");
        }
        if (!gameQueryService.isCreature(gameData, permanent) || permanent.isTapped()) {
            throw new IllegalStateException("Harmonize requires an untapped creature you control");
        }
    }

    private int harmonizeTapPower(GameData gameData, List<UUID> tapPermanentIds) {
        if (tapPermanentIds == null || tapPermanentIds.isEmpty()) {
            return 0;
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, tapPermanentIds.getFirst());
        return Math.max(0, gameQueryService.getEffectivePower(gameData, permanent));
    }

    private void payHarmonizeTapCost(GameData gameData, Player player, Card card, List<UUID> tapPermanentIds) {
        if (tapPermanentIds == null || tapPermanentIds.isEmpty()) {
            return;
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, tapPermanentIds.getFirst());
        permanent.tap();
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " taps ")
                .card(permanent.getCard())
                .text(" for ")
                .card(card)
                .text(".")
                .build());
    }

    private void validateGraveyardCastPermanentSacrificeCosts(
            GameData gameData, Player player, Card card, GraveyardCast graveyardCast,
            List<UUID> sacrificePermanentIds) {
        List<UUID> selectedIds = sacrificePermanentIds == null ? List.of() : sacrificePermanentIds;
        int requiredSacrificeCount = graveyardCast.additionalCosts().stream()
                .filter(SacrificePermanentsCost.class::isInstance)
                .mapToInt(cost -> ((SacrificePermanentsCost) cost).count())
                .sum();
        if (selectedIds.size() != requiredSacrificeCount) {
            throw new IllegalStateException("Must sacrifice exactly " + requiredSacrificeCount
                    + " permanents for the graveyard cast");
        }
        if (new HashSet<>(selectedIds).size() != selectedIds.size()) {
            throw new IllegalStateException("Duplicate permanents cannot be sacrificed");
        }
        if (!gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData)
                && graveyardCast.additionalCosts().stream().anyMatch(LifeCastingCost.class::isInstance)) {
            throw new IllegalStateException("Cannot pay life or sacrifice creatures as a cost");
        }

        UUID playerId = player.getId();
        if (graveyardCast.additionalCosts().stream()
                .filter(LifeCastingCost.class::isInstance)
                .map(LifeCastingCost.class::cast)
                .anyMatch(cost -> gameData.getLife(playerId) < cost.amount())) {
            throw new IllegalStateException("Not enough life to pay graveyard cast cost");
        }

        int selectedIndex = 0;
        for (CastingCost cost : graveyardCast.additionalCosts()) {
            if (cost instanceof SacrificePermanentsCost sacrificeCost) {
                for (int i = 0; i < sacrificeCost.count(); i++) {
                    UUID permanentId = selectedIds.get(selectedIndex++);
                    Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
                    if (permanent == null || !playerId.equals(gameQueryService.findPermanentController(gameData, permanentId))) {
                        throw new IllegalStateException("Sacrifice target is not on your battlefield");
                    }
                    if (!predicateEvaluationService.matchesPermanentPredicate(permanent,
                            sacrificeCost.filter(), FilterContext.of(gameData).withSourceControllerId(playerId))) {
                        throw new IllegalStateException("Sacrifice target does not match the required filter");
                    }
                }
            } else if (!(cost instanceof LifeCastingCost)
                    && !(cost instanceof DiscardCardCastingCost)
                    && !(cost instanceof RemoveCountersFromControlledCreaturesCastingCost)
                    && !(cost instanceof ExileNCardsFromGraveyardCastingCost)) {
                throw new IllegalStateException("Cannot pay this graveyard cast cost");
            }
        }
    }

    private void validateFlashbackDiscardCost(GameData gameData, Player player, Card card,
                                              FlashbackCast flashback, int xValue,
                                              List<Integer> discardHandCardIndices) {
        flashback.getCost(DiscardXCardsCastingCost.class).ifPresent(discardCost ->
                additionalSpellCostService.validateDiscardXCardsCost(
                        gameData, player, card,
                        new DiscardXCardsCost(discardCost.predicate(), discardCost.label(), false, false), xValue,
                        discardHandCardIndices, -1));
    }

    private void payGraveyardCastPermanentSacrificeCosts(
            GameData gameData, Player player, Card card, GraveyardCast graveyardCast,
            List<UUID> sacrificePermanentIds) {
        int selectedIndex = 0;
        for (CastingCost cost : graveyardCast.additionalCosts()) {
            if (cost instanceof SacrificePermanentsCost sacrificeCost) {
                for (int i = 0; i < sacrificeCost.count(); i++) {
                    Permanent permanent = gameQueryService.findPermanentById(gameData,
                            sacrificePermanentIds.get(selectedIndex++));
                    if (permanentRemovalService.removePermanentToGraveyard(gameData, permanent)) {
                        gameLogService.append(gameData, GameLog.builder()
                                .text(player.getUsername() + " sacrifices ")
                                .card(permanent.getCard())
                                .text(" for ")
                                .card(card)
                                .text(".")
                                .build());
                        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                                gameData, player.getId(), permanent.getCard());
                    }
                }
            }
        }
    }

    private void validateBestowManaCost(GameData gameData, UUID playerId, Card card, int targetingTax) {
        BestowCast bestowCast = card.getCastingOption(BestowCast.class)
                .orElseThrow(() -> new IllegalStateException("Card does not have a bestow cost"));
        ManaCastingCost manaCost = bestowCast.getCost(ManaCastingCost.class)
                .orElseThrow(() -> new IllegalStateException("Bestow cost has no mana component"));
        ManaCost cost = castingCostService.applyColoredManaCostReductions(
                gameData, playerId, card, new ManaCost(manaCost.manaCost()));
        int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, card) + targetingTax;
        withMulticoloredSpellMana(gameData, playerId, card, () -> {
            if (!cost.canPay(gameData.playerManaPools.get(playerId), additionalCost)) {
                throw new IllegalStateException("Not enough mana to pay bestow cost");
            }
            return null;
        });
    }

    private void payBestowCastingCost(GameData gameData, Player player, Card card, int targetingTax) {
        BestowCast bestowCast = card.getCastingOption(BestowCast.class)
                .orElseThrow(() -> new IllegalStateException("Card does not have a bestow cost"));
        ManaCastingCost manaCost = bestowCast.getCost(ManaCastingCost.class)
                .orElseThrow(() -> new IllegalStateException("Bestow cost has no mana component"));
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        int before = pool.getTotalAllMana();
        ManaCost cost = castingCostService.applyColoredManaCostReductions(
                gameData, player.getId(), card, new ManaCost(manaCost.manaCost()));
        withMulticoloredSpellMana(gameData, player.getId(), card, () -> {
            cost.pay(pool, castingCostService.getCastCostModifier(gameData, player.getId(), card) + targetingTax);
            return null;
        });
        gameData.addSpellCastManaSpent(card.getId(), before - pool.getTotalAllMana());
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " pays " + manaCost.manaCost() + " for ", card, "."));
    }

    private Integer payAlternateCastingCost(GameData gameData, Player player, Card card, List<UUID> sacrificePermanentIds,
                                            Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                                            Integer exileGraveyardCardIndex, int spellCardIndex, int xValue) {
        Integer returnedCreatureManaValue = webSlingingReturnedCreatureManaValue(
                gameData, player.getId(), card, sacrificePermanentIds);
        gameData.addSpellCastManaSpent(card.getId(),
                computeAlternateCastingManaPayment(gameData, player, card, sacrificePermanentIds,
                        discardHandCardIndex, discardHandCardIndices, exileGraveyardCardIndex,
                        spellCardIndex, xValue));
        return returnedCreatureManaValue;
    }

    private Integer webSlingingReturnedCreatureManaValue(GameData gameData, UUID playerId, Card card,
                                                         List<UUID> alternateCostPermanentIds) {
        if (!card.getKeywords().contains(Keyword.WEB_SLINGING)) {
            return null;
        }
        AlternateHandCast alternateHandCast = card.getCastingOption(AlternateHandCast.class).orElse(null);
        if (alternateHandCast == null) {
            return null;
        }
        ReturnPermanentsCost returnCost = alternateHandCast.getCost(ReturnPermanentsCost.class).orElse(null);
        if (returnCost == null || returnCost.count() != 1) {
            return null;
        }
        int returnIndex = alternateHandCast.getCost(SacrificePermanentsCost.class)
                .map(SacrificePermanentsCost::count).orElse(0)
                + alternateHandCast.getCost(TapUntappedPermanentsCost.class)
                .map(TapUntappedPermanentsCost::count).orElse(0);
        if (alternateCostPermanentIds.size() <= returnIndex) {
            return null;
        }
        UUID returnedPermanentId = alternateCostPermanentIds.get(returnIndex);
        Permanent returnedPermanent = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> permanent.getId().equals(returnedPermanentId))
                .findFirst()
                .orElse(null);
        return returnedPermanent == null ? null : returnedPermanent.getCard().getManaValue();
    }

    private void paySharedColorDiscardAlternativeCost(GameData gameData, Player player, Card card,
                                                      Integer discardHandCardIndex, int spellCardIndex) {
        int effectiveIndex = castingCostService.validateSharedColorDiscardAlternativeCost(
                gameData, player.getId(), card, discardHandCardIndex, spellCardIndex);
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card toDiscard = hand.remove(effectiveIndex);
        graveyardService.addCardToGraveyard(gameData, player.getId(), toDiscard);
        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " discards ")
                .card(toDiscard)
                .text(" to cast ")
                .card(card)
                .text(".")
                .build());
        triggerCollectionService.checkDiscardTriggers(gameData, player.getId(), toDiscard);
    }

    /**
     * The single "extra generic" argument {@link ManaCost#canPay(ManaPool, int)} / {@link ManaCost#pay(ManaPool, int)}
     * take. For an alternate cost with {@code X} in it (Street Spasm's overload {X}{X}{R}{R}) it is
     * the chosen X, which the cost multiplies by its {X} symbol count (CR 107.3b); otherwise it is
     * the negated emerge reduction. No alternate cost combines the two.
     */
    private int alternateCostXArgument(ManaCost cost, int xValue, int emergeReduction) {
        return cost.hasX() ? xValue : -emergeReduction;
    }

    /** Emerge (CR 702.123): total cost reduced by generic mana equal to sacrificed permanents' mana values. */
    private int computeEmergeManaReduction(GameData gameData, AlternateHandCast altCast, List<UUID> sacrificePermanentIds) {
        if (!altCast.reduceManaBySacrificedManaValue()) {
            return 0;
        }
        int reduction = 0;
        for (UUID sacId : sacrificePermanentIds) {
            Permanent toSacrifice = gameQueryService.findPermanentById(gameData, sacId);
            if (toSacrifice != null) {
                reduction += toSacrifice.getCard().getManaValue();
            }
        }
        return reduction;
    }

    private ManaCost computeSacrificedManaCost(GameData gameData, AlternateHandCast altCast,
                                                List<UUID> sacrificePermanentIds) {
        if (!altCast.reduceManaBySacrificedManaCost() || sacrificePermanentIds.isEmpty()) {
            return null;
        }
        Permanent sacrificed = gameQueryService.findPermanentById(gameData, sacrificePermanentIds.getFirst());
        if (sacrificed == null || sacrificed.getCard().getManaCost() == null) {
            return new ManaCost("{0}");
        }
        return new ManaCost(sacrificed.getCard().getManaCost());
    }

    private int computeAlternateCastingManaPayment(GameData gameData, Player player, Card card, List<UUID> sacrificePermanentIds,
                                                   Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                                                   Integer exileGraveyardCardIndex, int spellCardIndex, int xValue) {
        AlternateHandCast altCast = card.getCastingOption(AlternateHandCast.class)
                .orElseThrow(() -> new IllegalStateException("Card does not have an alternate casting cost"));
        UUID playerId = player.getId();

        // Snapshot emerge reduction before sacrifice (creature must still be on the battlefield
        // when its mana value is read — CR 702.123).
        int emergeReduction = computeEmergeManaReduction(gameData, altCast, sacrificePermanentIds);
        ManaCost sacrificedManaCost = computeSacrificedManaCost(gameData, altCast, sacrificePermanentIds);

        // Sacrifice all required permanents
        if (altCast.getCost(SacrificePermanentsCost.class).isPresent()) {
            for (UUID sacId : sacrificePermanentIds) {
                Permanent toSacrifice = gameQueryService.findPermanentById(gameData, sacId);
                if (toSacrifice != null && permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice)) {
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " sacrifices ")
                            .card(toSacrifice.getCard())
                            .text(" for ")
                            .card(card)
                            .text(".")
                            .build());
                    triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, player.getId(), toSacrifice.getCard(), card);
                }
            }
        }

        // Pay life
        altCast.getCost(LifeCastingCost.class).ifPresent(lifeCost -> {
            int currentLife = gameData.getLife(playerId);
            int lifeLoss = lifeCost.amount()
                    * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
            gameData.playerLifeTotals.put(playerId, currentLife - lifeLoss);
            gameData.lifeLostThisTurn.merge(playerId, lifeLoss, Integer::sum);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + lifeLoss + " life for ", card, "."));
        });

        // Have each other player gain life for the alternate cost.
        altCast.getCost(EachOpponentGainsLifeCastingCost.class).ifPresent(lifeCost -> {
            for (UUID otherPlayerId : gameData.orderedPlayerIds) {
                if (!otherPlayerId.equals(playerId)) {
                    lifeSupport.applyGainLife(gameData, otherPlayerId, lifeCost.amount(), card.getName());
                }
            }
        });

        // Tap untapped permanents
        var tapCost = altCast.getCost(TapUntappedPermanentsCost.class);
        if (tapCost.isPresent()) {
            int sacCount = altCast.getCost(SacrificePermanentsCost.class).map(SacrificePermanentsCost::count).orElse(0);
            List<UUID> tapIds = sacrificePermanentIds.subList(sacCount, sacrificePermanentIds.size());
            for (UUID tapId : tapIds) {
                Permanent toTap = gameQueryService.findPermanentById(gameData, tapId);
                if (toTap != null) {
                    toTap.tap();
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " taps ")
                            .card(toTap.getCard())
                            .text(" for ")
                            .card(card)
                            .text(".")
                            .build());
                }
            }
        }

        // Return permanents to their owner's hand (tail of the ID list, after sacrifice and tap IDs)
        var returnCost = altCast.getCost(ReturnPermanentsCost.class);
        if (returnCost.isPresent()) {
            int sacCount = altCast.getCost(SacrificePermanentsCost.class).map(SacrificePermanentsCost::count).orElse(0);
            int tapCount = altCast.getCost(TapUntappedPermanentsCost.class).map(TapUntappedPermanentsCost::count).orElse(0);
            List<UUID> returnIds = sacrificePermanentIds.subList(sacCount + tapCount, sacrificePermanentIds.size());
            for (UUID returnId : returnIds) {
                Permanent toReturn = gameQueryService.findPermanentById(gameData, returnId);
                if (toReturn != null && permanentRemovalService.removePermanentToHand(gameData, toReturn)) {
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " returns ")
                            .card(toReturn.getCard())
                            .text(" to hand for ")
                            .card(card)
                            .text(".")
                            .build());
                }
            }
        }

        // Exile card(s) from hand (spell already removed — adjust index like discard additional costs)
        var exileHandCost = altCast.getCost(ExileCardsFromHandCastingCost.class);
        if (exileHandCost.isPresent()) {
            List<Integer> effectiveIndices = validateExileFromHandAlternateCost(gameData, playerId, card,
                    exileHandCost.get(), discardHandCardIndex, discardHandCardIndices, spellCardIndex, xValue);
            List<Card> hand = gameData.playerHands.get(playerId);
            for (int effectiveIndex : effectiveIndices) {
                Card toExile = hand.remove(effectiveIndex);
                exileService.exileCard(gameData, playerId, toExile);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " exiles ")
                        .card(toExile)
                        .text(" from their hand for ")
                        .card(card)
                        .text(".")
                        .build());
                log.info("Game {} - {} exiles {} from hand as alternate casting cost",
                        gameData.id, player.getUsername(), toExile.getName());
            }
        }

        List<AlternateDiscardSelection> discardSelections = validateAlternateDiscardCosts(
                gameData, playerId, card, altCast.getCosts(DiscardCardCastingCost.class),
                discardHandCardIndex, discardHandCardIndices, spellCardIndex);
        discardSelections = new ArrayList<>(discardSelections);
        discardSelections.sort((left, right) -> Integer.compare(right.handIndex(), left.handIndex()));
        List<Card> hand = gameData.playerHands.get(playerId);
        for (AlternateDiscardSelection selection : discardSelections) {
            int effectiveIndex = validateDiscardFromHandAlternateCost(gameData, playerId, card,
                    selection.cost(), selection.handIndex(), spellCardIndex);
            Card toDiscard = hand.remove(effectiveIndex);
            graveyardService.addCardToGraveyard(gameData, playerId, toDiscard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " discards ")
                    .card(toDiscard)
                    .text(" from their hand for ")
                    .card(card)
                    .text(".")
                    .build());
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, toDiscard);
        }

        var revealHandCost = altCast.getCost(RevealCardsFromHandCastingCost.class);
        if (revealHandCost.isPresent()) {
            if (revealHandCost.get().revealEntireHand()) {
                cardRevealService.revealHandToAllPlayers(gameData, playerId);
            } else {
                int effectiveIndex = validateRevealFromHandAlternateCost(gameData, playerId, card,
                        revealHandCost.get(), discardHandCardIndex, spellCardIndex);
                Card toReveal = gameData.playerHands.get(playerId).get(effectiveIndex);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " reveals ")
                        .card(toReveal)
                        .text(" from their hand for ")
                        .card(card)
                        .text(".")
                        .build());
                cardRevealService.revealToAllPlayers(gameData, playerId,
                        GameEventFact.RevealZone.HAND, List.of(toReveal));
            }
        }

        // Exile the top matching cards of the caster's graveyard (determined, not chosen)
        var exileGraveyardCost = altCast.getCost(ExileTopCardsFromGraveyardCastingCost.class);
        if (exileGraveyardCost.isPresent()) {
            List<Card> toExile = findTopMatchingGraveyardCards(gameData, playerId, card, exileGraveyardCost.get());
            for (Card graveyardCard : toExile) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, graveyardCard.getId());
                exileService.exileCard(gameData, playerId, graveyardCard);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " exiles ")
                        .card(graveyardCard)
                        .text(" from their graveyard for ")
                        .card(card)
                        .text(".")
                        .build());
            }
            log.info("Game {} - {} exiles {} cards from graveyard as alternate casting cost",
                    gameData.id, player.getUsername(), toExile.size());
        }

        var chosenGraveyardExileCost = altCast.getCost(ExileCardFromGraveyardCastingCost.class);
        if (chosenGraveyardExileCost.isPresent()) {
            Card graveyardCard = validateChosenGraveyardExileCost(gameData, playerId, card,
                    chosenGraveyardExileCost.get(), exileGraveyardCardIndex);
            permanentRemovalService.removeCardFromGraveyardById(gameData, graveyardCard.getId());
            exileService.exileCard(gameData, playerId, graveyardCard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " exiles ")
                    .card(graveyardCard)
                    .text(" from their graveyard for ")
                    .card(card)
                    .text(".")
                    .build());
        }

        // Pay mana (for alternate costs that include a mana component)
        var manaCostOpt = altCast.getCost(ManaCastingCost.class);
        if (manaCostOpt.isEmpty()) {
            return 0;
        }
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int before = pool.getTotalAllMana();
        ManaCost cost = castingCostService.applyColoredManaCostReductions(
                gameData, playerId, card, new ManaCost(manaCostOpt.get().manaCost()));
        ManaPool.FaceDownSpellsOrTurnFaceUpManaState restrictedMana = card.getMorphCost() != null
                ? pool.promoteFaceDownSpellsOrTurnFaceUpMana() : null;
        ManaPool.MulticoloredSpellManaState multicoloredMana =
                gameQueryService.getEffectiveCardColors(gameData, card).size() >= 2
                        ? pool.promoteMulticoloredSpellOnlyMana() : null;
        try {
            if (altCast.reduceManaBySacrificedManaCost()) {
                cost.payAfterReduction(pool, sacrificedManaCost);
            } else {
                int faceDownCostReduction = card.getMorphCost() != null
                        ? castingCostService.getCastCostModifierForFaceDownSpell(gameData, playerId, card) : 0;
                cost.pay(pool, alternateCostXArgument(cost, xValue, emergeReduction) + faceDownCostReduction);
            }
        } finally {
            if (restrictedMana != null) {
                pool.restorePromotedFaceDownSpellsOrTurnFaceUpMana(restrictedMana);
            }
            if (multicoloredMana != null) {
                pool.restorePromotedMulticoloredSpellOnlyMana(multicoloredMana);
            }
        }
        int manaSpent = before - pool.getTotalAllMana();
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " pays " + manaCostOpt.get().manaCost() + " for ", card, "."));
        return manaSpent;
    }

    private Card validateChosenGraveyardExileCost(GameData gameData, UUID playerId, Card spell,
                                                   ExileCardFromGraveyardCastingCost cost,
                                                   Integer graveyardCardIndex) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyardCardIndex == null || graveyard == null
                || graveyardCardIndex < 0 || graveyardCardIndex >= graveyard.size()) {
            throw new IllegalStateException("Must choose a card from your graveyard to exile");
        }
        Card chosen = graveyard.get(graveyardCardIndex);
        if (cost.predicate() != null
                && !predicateEvaluationService.matchesCardPredicate(
                        chosen, cost.predicate(), spell.getId(), gameData, playerId)) {
            throw new IllegalStateException("Chosen graveyard card is not " + cost.label());
        }
        return chosen;
    }

    /**
     * Validates exile-from-hand alternate casting cost. The supplied indices are into the
     * pre-removal hand (including the spell at {@code spellCardIndex}); the returned indices are
     * into the current hand after the spell has been removed, in descending order for safe removal.
     */
    private List<Integer> validateExileFromHandAlternateCost(GameData gameData, UUID playerId, Card card,
                                                             ExileCardsFromHandCastingCost cost,
                                                             Integer discardHandCardIndex,
                                                             List<Integer> discardHandCardIndices,
                                                             int spellCardIndex, int xValue) {
        String label = cost.label() != null ? cost.label() + " card" : "a card";
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> requestedIndices;
        if (cost.count() == 1 && discardHandCardIndex != null) {
            requestedIndices = List.of(discardHandCardIndex);
        } else if (discardHandCardIndices != null && !discardHandCardIndices.isEmpty()) {
            requestedIndices = discardHandCardIndices;
        } else if (discardHandCardIndex != null) {
            requestedIndices = List.of(discardHandCardIndex);
        } else {
            requestedIndices = List.of();
        }

        if (hand == null || requestedIndices.size() != cost.count()
                || requestedIndices.stream().anyMatch(index -> index == null)
                || requestedIndices.stream().distinct().count() != requestedIndices.size()) {
            throw new IllegalStateException("Must exile " + cost.count() + " " + label
                    + (cost.count() == 1 ? "" : "s") + " from your hand to cast " + card.getName());
        }

        boolean spellStillInHand = spellCardIndex >= 0 && spellCardIndex < hand.size()
                && hand.get(spellCardIndex) == card;
        int preRemovalHandSize = hand.size() + (spellStillInHand ? 0 : 1);
        List<Integer> effectiveIndices = new ArrayList<>();
        for (Integer requestedIndex : requestedIndices) {
            if (requestedIndex < 0 || requestedIndex >= preRemovalHandSize || requestedIndex == spellCardIndex) {
                throw new IllegalStateException("Must exile " + cost.count() + " " + label
                        + (cost.count() == 1 ? "" : "s") + " from your hand to cast " + card.getName());
            }
            int validationIndex = spellStillInHand ? requestedIndex : requestedIndex > spellCardIndex
                    ? requestedIndex - 1 : requestedIndex;
            Card toExile = hand.get(validationIndex);
            if (cost.predicate() != null
                    && !predicateEvaluationService.matchesCardPredicate(toExile, cost.predicate(), toExile.getId())) {
                throw new IllegalStateException("Exiled card must be " + label);
            }
            // Shoal cycle: the exiled card's mana value must be the X chosen for the spell.
            if (cost.manaValueEqualsX() && toExile.getManaValue() != xValue) {
                throw new IllegalStateException("Exiled card must have mana value " + xValue);
            }
            effectiveIndices.add(requestedIndex > spellCardIndex ? requestedIndex - 1 : requestedIndex);
        }
        effectiveIndices.sort((first, second) -> Integer.compare(second, first));
        return effectiveIndices;
    }

    private List<AlternateDiscardSelection> validateAlternateDiscardCosts(
            GameData gameData, UUID playerId, Card card, List<DiscardCardCastingCost> costs,
            Integer firstHandCardIndex, List<Integer> additionalHandCardIndices, int spellCardIndex) {
        if (costs.isEmpty()) {
            return List.of();
        }
        List<Integer> handCardIndices = new ArrayList<>();
        if (firstHandCardIndex != null) {
            handCardIndices.add(firstHandCardIndex);
        }
        if (additionalHandCardIndices != null) {
            handCardIndices.addAll(additionalHandCardIndices);
        }
        if (handCardIndices.size() != costs.size()
                || handCardIndices.stream().distinct().count() != handCardIndices.size()) {
            throw new IllegalStateException("Must choose one card for each discard cost to cast " + card.getName());
        }
        List<AlternateDiscardSelection> selections = new ArrayList<>();
        for (int i = 0; i < costs.size(); i++) {
            int handCardIndex = handCardIndices.get(i);
            validateDiscardFromHandAlternateCost(gameData, playerId, card, costs.get(i),
                    handCardIndex, spellCardIndex);
            selections.add(new AlternateDiscardSelection(costs.get(i), handCardIndex));
        }
        return selections;
    }

    private int validateDiscardFromHandAlternateCost(GameData gameData, UUID playerId, Card card,
                                                    DiscardCardCastingCost cost,
                                                    Integer discardHandCardIndex, int spellCardIndex) {
        String label = cost.label() != null ? cost.label() : "a card";
        List<Card> hand = gameData.playerHands.get(playerId);
        if (discardHandCardIndex == null || discardHandCardIndex == spellCardIndex || hand == null) {
            throw new IllegalStateException("Must discard " + label + " from your hand to cast " + card.getName());
        }
        boolean spellStillInHand = spellCardIndex >= 0 && spellCardIndex < hand.size()
                && hand.get(spellCardIndex) == card;
        int effectiveIndex = (!spellStillInHand && spellCardIndex >= 0 && discardHandCardIndex > spellCardIndex)
                ? discardHandCardIndex - 1
                : discardHandCardIndex;
        if (spellStillInHand && (discardHandCardIndex < 0 || discardHandCardIndex >= hand.size())) {
            throw new IllegalStateException("Must discard " + label + " from your hand to cast " + card.getName());
        }
        if (!spellStillInHand && (effectiveIndex < 0 || effectiveIndex >= hand.size())) {
            throw new IllegalStateException("Must discard " + label + " from your hand to cast " + card.getName());
        }
        Card toDiscard = hand.get(spellStillInHand ? discardHandCardIndex : effectiveIndex);
        if (cost.predicate() != null
                && !predicateEvaluationService.matchesCardPredicate(toDiscard, cost.predicate(), toDiscard.getId())) {
            throw new IllegalStateException("Discarded card must be " + label);
        }
        return spellStillInHand ? discardHandCardIndex : effectiveIndex;
    }

    private record AlternateDiscardSelection(DiscardCardCastingCost cost, int handIndex) {
    }

    private int validateRevealFromHandAlternateCost(GameData gameData, UUID playerId, Card card,
                                                    RevealCardsFromHandCastingCost cost,
                                                    Integer handCardIndex, int spellCardIndex) {
        String label = cost.label() != null ? cost.label() + " card" : "a card";
        List<Card> hand = gameData.playerHands.get(playerId);
        if (cost.revealEntireHand()) {
            if (hand == null) {
                throw new IllegalStateException("Must reveal your hand to cast " + card.getName());
            }
            return -1;
        }
        boolean spellStillInHand = spellCardIndex >= 0 && spellCardIndex < (hand == null ? 0 : hand.size())
                && hand.get(spellCardIndex) == card;
        if (handCardIndex == null || hand == null || (spellStillInHand && handCardIndex == spellCardIndex)) {
            throw new IllegalStateException("Must reveal " + label + " from your hand to cast " + card.getName());
        }
        int effectiveIndex = !spellStillInHand && spellCardIndex >= 0 && handCardIndex > spellCardIndex
                ? handCardIndex - 1 : handCardIndex;
        if (effectiveIndex < 0 || effectiveIndex >= hand.size()) {
            throw new IllegalStateException("Must reveal " + label + " from your hand to cast " + card.getName());
        }
        Card toReveal = hand.get(effectiveIndex);
        if (cost.predicate() != null
                && !predicateEvaluationService.matchesCardPredicate(toReveal, cost.predicate(), toReveal.getId())) {
            throw new IllegalStateException("Revealed card must be " + label);
        }
        return effectiveIndex;
    }

    /**
     * Resolves the cards an {@link ExileTopCardsFromGraveyardCastingCost} would exile: the topmost
     * matching cards of the caster's graveyard, top first (the graveyard list's tail is its top).
     * Throws if there are not enough matching cards, so this doubles as the cost's validation.
     */
    private List<Card> findTopMatchingGraveyardCards(GameData gameData, UUID playerId, Card card,
                                                     ExileTopCardsFromGraveyardCastingCost cost) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Card> matching = new ArrayList<>();
        if (graveyard != null) {
            for (int i = graveyard.size() - 1; i >= 0 && matching.size() < cost.count(); i--) {
                Card candidate = graveyard.get(i);
                if (cost.predicate() == null
                        || predicateEvaluationService.matchesCardPredicate(candidate, cost.predicate(), candidate.getId())) {
                    matching.add(candidate);
                }
            }
        }
        if (matching.size() < cost.count()) {
            String label = cost.label() != null ? cost.label() + " cards" : "cards";
            throw new IllegalStateException("Must exile the top " + cost.count() + " " + label
                    + " of your graveyard to cast " + card.getName());
        }
        return matching;
    }

    private Zone findOwnedGraveyardOrExileTargetZone(GameData gameData, UUID playerId, UUID targetId) {
        boolean inGraveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                .anyMatch(card -> card.getId().equals(targetId));
        if (inGraveyard) {
            return Zone.GRAVEYARD;
        }
        boolean inExile = gameData.getPlayerExiledCards(playerId).stream()
                .anyMatch(card -> card.getId().equals(targetId));
        return inExile ? Zone.EXILE : null;
    }

    public void finishSpellCast(GameData gameData, UUID playerId, Player player, List<Card> hand, Card card) {
        finishSpellCast(gameData, playerId, player, hand, card, true);
    }

    public void finishSpellCast(GameData gameData, UUID playerId, Player player, List<Card> hand, Card card, boolean castFromHand) {
        StackEntry castEntry = null;
        if (!gameData.stack.isEmpty()) {
            StackEntry latestEntry = gameData.stack.getLast();
            if (latestEntry.getCard() != null && latestEntry.getCard().getId().equals(card.getId())) {
                castEntry = latestEntry;
            }
        }
        Card castCharacteristics = card;
        if (castEntry != null && card.getBackFaceCard() != null
                && (castEntry.isCastWithAdventure() || castEntry.isCastWithOmen()
                || castEntry.isCastWithDisturb() || castEntry.isCastTransformed())) {
            castCharacteristics = card.createRuntimeCopyWithFace(card.getBackFaceCard());
            castCharacteristics.freeze();
        }

        if (castEntry != null && castEntry.wasKicked()) {
            for (CardEffect staticEffect : card.getEffects(EffectSlot.STATIC)) {
                if (staticEffect instanceof GrantKeywordsToKickedSpellEffect marker) {
                    castEntry.getGrantedKeywordsWhileOnStack().addAll(marker.keywords());
                }
            }
        }

        stampLatestCastDuringMainPhase(gameData, playerId, card);
        gameData.recordSpellCast(playerId, castCharacteristics);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " casts ")
                .card(castCharacteristics)
                .text(".")
                .build());

        log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), castCharacteristics.getName());

        if (castEntry != null) {
            castEntry.setManaSpentToCast(gameData.getSpellCastManaSpent(card.getId()));
            boolean controlledMount = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                    .anyMatch(permanent -> gameQueryService.effectiveCreatureSubtypes(gameData, permanent)
                            .contains(CardSubtype.MOUNT));
            castEntry.setControlledMountAsCast(controlledMount);
            triggerCollectionService.checkCrimeTriggers(gameData, castEntry);
        }
        triggerCollectionService.checkSpellCastTriggers(gameData, castCharacteristics, playerId, castFromHand);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        if (!gameData.pendingSpellCastCostTriggers.isEmpty()) {
            gameData.stack.addAll(gameData.pendingSpellCastCostTriggers);
            gameData.pendingSpellCastCostTriggers.clear();
        }
        // CR 603.3: Flush triggers deferred from mana abilities activated to pay for this spell.
        // They go on top of the spell (and spell-cast triggers) so they resolve first.
        if (!gameData.pendingManaAbilityTriggers.isEmpty()) {
            gameData.stack.addAll(gameData.pendingManaAbilityTriggers);
            gameData.pendingManaAbilityTriggers.clear();
        }
        stateBasedActionService.performStateBasedActions(gameData);
        mutationCoordinator.invalidateAllPlayerViews(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void stampCastDuringMainPhase(GameData gameData, StackEntry entry, UUID playerId) {
        entry.setCastDuringMainPhase(playerId.equals(gameData.activePlayerId)
                && (gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN));
    }

    private void stampLatestCastDuringMainPhase(GameData gameData, UUID playerId, Card card) {
        for (int i = gameData.stack.size() - 1; i >= 0; i--) {
            StackEntry entry = gameData.stack.get(i);
            if (entry.getCard() != null && entry.getCard().getId().equals(card.getId())) {
                stampCastDuringMainPhase(gameData, entry, playerId);
                return;
            }
        }
    }

    private void validateBuybackSacrificeCost(GameData gameData, Player player, Card card,
                                              BuybackEffect buybackEffect, UUID sacrificePermanentId,
                                              List<UUID> additionalCostSacrificePermanentIds) {
        Predicate<Permanent> typeCheck = p -> predicateEvaluationService.matchesPermanentPredicate(
                gameData, p, buybackEffect.sacrificePredicate());
        if (buybackEffect.sacrificeCount() == 1) {
            additionalSpellCostService.validateSingleSacrificeCost(gameData, player, card,
                    sacrificePermanentId, buybackEffect.sacrificeDescription(), typeCheck);
        } else {
            additionalSpellCostService.validateMultipleSacrificeCost(gameData, player, card,
                    buybackEffect.sacrificeCount(), buybackEffect.sacrificeDescription(), typeCheck,
                    additionalCostSacrificePermanentIds);
        }
    }

    private void payBuybackSacrificeCost(GameData gameData, Player player, Card card,
                                         BuybackEffect buybackEffect, UUID sacrificePermanentId,
                                         List<UUID> additionalCostSacrificePermanentIds) {
        Predicate<Permanent> typeCheck = p -> predicateEvaluationService.matchesPermanentPredicate(
                gameData, p, buybackEffect.sacrificePredicate());
        if (buybackEffect.sacrificeCount() == 1) {
            paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    buybackEffect.sacrificeDescription(), typeCheck);
        } else {
            for (UUID id : additionalCostSacrificePermanentIds) {
                paySingleSacrificeCost(gameData, player, card, id,
                        buybackEffect.sacrificeDescription(), typeCheck);
            }
        }
    }

}
