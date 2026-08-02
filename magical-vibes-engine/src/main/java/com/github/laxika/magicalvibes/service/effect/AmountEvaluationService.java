package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.AttachedPermanentColorCount;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CardsInExile;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInHand;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnLinkedPermanent;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.TimesSourceRegeneratedThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreatureSubtypeDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.CreaturesDevoured;
import com.github.laxika.magicalvibes.model.amount.DevouredCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToControllerThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToOpponentsThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsPutIntoGraveyardByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.DuringControllerTurn;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EnchantedPermanentManaValue;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlledCreaturesTotalToughnessAtLeast;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlsAllNamed;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetMatches;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetPlayerControlsMoreLands;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.HighestOpponentLifeTotal;
import com.github.laxika.magicalvibes.model.amount.IfSourceAttacking;
import com.github.laxika.magicalvibes.model.amount.ImprintedCardManaValue;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreaturePower;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.TotalToughnessOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreatureToughness;
import com.github.laxika.magicalvibes.model.amount.LandsMatchingImprintedName;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Min;
import com.github.laxika.magicalvibes.model.amount.OpponentPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.OtherAttackersSharingCreatureTypeWithTarget;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.UntappedLandsAtTurnStart;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.SourceCardPower;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.amount.TargetSpellPower;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The single evaluation point for every {@link DynamicAmount} in the engine (the numeric
 * sibling of {@link ConditionEvaluationService}).
 *
 * <p>The switch in {@link #evaluate} is exhaustive over the sealed {@link DynamicAmount}
 * hierarchy — adding an amount without an evaluation is a compile error, never a silent 0.
 * All evaluation contexts (stack resolution, static bonus computation, AI estimation) call
 * this service with an {@link AmountContext} describing where the values (source permanent,
 * controller, x value, …) come from at that site.</p>
 */
@Service
@RequiredArgsConstructor
public class AmountEvaluationService {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;

    /**
     * Evaluates the current value of the given amount.
     */
    public int evaluate(GameData gameData, DynamicAmount amount, AmountContext ctx) {
        return switch (amount) {
            case Fixed f ->
                    f.value();
            case FixedIfControlMoreCreaturesThanEachOtherPlayer a ->
                    controlsMoreCreaturesThanEachOtherPlayer(gameData, ctx) ? a.amount() : a.otherwise();
            case FixedIfControlledCreaturesTotalToughnessAtLeast a ->
                    totalToughnessOfControlledCreatures(gameData, ctx) >= a.minTotalToughness() ? a.amount() : 0;
            case FixedIfControlsAllNamed a ->
                    controlsAllNamed(gameData, a, ctx) ? a.amount() : a.otherwise();
            case FixedIfTargetMatches a ->
                    targetMatches(gameData, a, ctx) ? a.amount() : a.otherwise();
            case FixedIfTargetPlayerControlsMoreLands a ->
                    targetPlayerControlsMoreLands(gameData, ctx) ? a.amount() : a.otherwise();
            case XValue ignored ->
                    ctx.xValue();
            case ManaSpentToCast ignored ->
                    ctx.xValue();
            case EventValue ignored ->
                    ctx.eventValue();
            case RepeatedAdditionalCostCount a ->
                    (int) ctx.repeatedAdditionalCosts().stream().filter(a.manaCost()::equals).count();
            case Scaled s ->
                    s.factor() * evaluate(gameData, s.amount(), ctx);
            case Divided d ->
                    evaluate(gameData, d.amount(), ctx) / d.divisor();
            case Sum s ->
                    s.amounts().stream().mapToInt(a -> evaluate(gameData, a, ctx)).sum();
            case Min m ->
                    m.amounts().stream().mapToInt(a -> evaluate(gameData, a, ctx)).min().orElse(0);
            case Max m ->
                    m.amounts().stream().mapToInt(a -> evaluate(gameData, a, ctx)).max().orElse(0);
            case DuringControllerTurn d ->
                    ctx.controllerId() != null && ctx.controllerId().equals(gameData.activePlayerId)
                            ? evaluate(gameData, d.amount(), ctx) : 0;
            case PermanentCount c ->
                    countPermanents(gameData, c, ctx);
            case AttachedPermanentColorCount ignored ->
                    attachedPermanentColorCount(gameData, ctx);
            case BasicLandTypesAmongControlledLands ignored ->
                    countBasicLandTypesAmongControlledLands(gameData, ctx);
            case CardTypesAmongCardsInGraveyard c ->
                    countCardTypesAmongCardsInGraveyard(gameData, c, ctx);
            case CardsInExile c ->
                    countExileCards(gameData, c, ctx);
            case CardsInGraveyard c ->
                    countGraveyardCards(gameData, c, ctx);
            case CardsInHand c ->
                    countHandCards(gameData, c, ctx);
            case MatchingCardsInHand c ->
                    countMatchingHandCards(gameData, c, ctx);
            case CardsInLibrary c ->
                    countLibraryCards(gameData, c, ctx);
            case ColorManaSymbolsAmongControlledPermanents c ->
                    countColorManaSymbolsAmongControlledPermanents(gameData, c, ctx);
            case ColorManaSymbolsInGraveyard c ->
                    countColorManaSymbolsInGraveyard(gameData, c, ctx);
            case ColorManaSymbolsInHand c ->
                    countColorManaSymbolsInHand(gameData, c, ctx);
            case CountersOnSource c ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getCounterCount(c.counterType());
            case TimesSourceRegeneratedThisTurn ignored ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getTimesRegeneratedThisTurn();
            case CreaturesDevoured ignored ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getDevouredCreatures().size();
            case DevouredCreaturesOfSubtype d ->
                    countDevouredCreaturesOfSubtype(ctx, d.subtype());
            case CountersOnLinkedPermanent c ->
                    countCountersOnLinkedPermanent(gameData, c);
            case ControllerLifeTotal ignored ->
                    // Null controller happens transiently while the source is still entering the
                    // battlefield (e.g. a CDA evaluated from an entry-time query); playerLifeTotals
                    // is a ConcurrentHashMap, which rejects null keys.
                    ctx.controllerId() == null ? 0 : gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 0);
            case HighestOpponentLifeTotal ignored ->
                    highestOpponentLifeTotal(gameData, ctx);
            case TargetPlayerLifeTotal ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.playerLifeTotals.getOrDefault(ctx.targetPermanentId(), 0);
            case HalvedRoundedUp h ->
                    Math.floorDiv(evaluate(gameData, h.amount(), ctx) + 1, 2);
            case HalfControllerLifeRoundedUp ignored ->
                    ctx.controllerId() == null ? 0
                            : (gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 0) + 1) / 2;
            case IfSourceAttacking a ->
                    ctx.sourcePermanent() != null && ctx.sourcePermanent().isAttacking()
                            ? evaluate(gameData, a.whileAttacking(), ctx)
                            : evaluate(gameData, a.otherwise(), ctx);
            case GreatestPowerAmongControlled ignored ->
                    greatestPowerAmongControlled(gameData, ctx);
            case AttachmentsOnSource a ->
                    countAttachmentsOnSource(gameData, a, ctx);
            case CreaturesBlockingSource ignored ->
                    countCreaturesBlockingSource(gameData, ctx);
            case OpponentPoisonCounters ignored ->
                    countOpponentPoisonCounters(gameData, ctx);
            case OtherAttackersSharingCreatureTypeWithTarget ignored ->
                    countOtherAttackersSharingCreatureTypeWithTarget(gameData, ctx);
            case CreatureDeathsThisTurn c ->
                    countCreatureDeathsThisTurn(gameData, c, ctx);
            case CreatureSubtypeDeathsThisTurn c ->
                    countCreatureSubtypeDeathsThisTurn(gameData, c, ctx);
            case LifeGainedThisTurn c ->
                    countLifeGainedThisTurn(gameData, c, ctx);
            case LifeLostThisTurn c ->
                    countLifeLostThisTurn(gameData, c, ctx);
            case TargetPlayerPoisonCounters ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.playerPoisonCounters.getOrDefault(ctx.targetPermanentId(), 0);
            case DamageDealtToTargetPlayerThisTurn ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.damageDealtToPlayersThisTurn.getOrDefault(ctx.targetPermanentId(), 0);
            case UntappedLandsAtTurnStart ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.untappedLandsAtTurnStart.getOrDefault(ctx.targetPermanentId(), 0);
            case CardsDiscardedByTargetPlayerThisTurn ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.cardsDiscardedThisTurn.getOrDefault(ctx.targetPermanentId(), 0);
            case CardsDiscardedOrCycledThisTurn ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.cardsDiscardedThisTurn.getOrDefault(ctx.controllerId(), 0);
            case CardsPutIntoGraveyardByTargetPlayerThisTurn ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                                    .getOrDefault(ctx.targetPermanentId(), java.util.Set.of()).size();
            case DamageDealtToControllerThisTurn ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.damageDealtToPlayersThisTurn.getOrDefault(ctx.controllerId(), 0);
            case DamageDealtToOpponentsThisTurn ignored ->
                    damageDealtToOpponentsThisTurn(gameData, ctx);
            case TotalPowerOfCardsExiledWithSource ignored ->
                    totalPTOfCardsExiledWithSource(gameData, ctx, true);
            case TotalToughnessOfCardsExiledWithSource ignored ->
                    totalPTOfCardsExiledWithSource(gameData, ctx, false);
            case ImprintedCardManaValue ignored ->
                    imprintedCardManaValue(gameData, ctx);
            case ImprintedCreaturePower ignored ->
                    imprintedCreaturePT(gameData, ctx, true);
            case ImprintedCreatureToughness ignored ->
                    imprintedCreaturePT(gameData, ctx, false);
            case LandsMatchingImprintedName ignored ->
                    countLandsMatchingImprintedName(gameData, ctx);
            case SourceCardPower ignored ->
                    ctx.sourceCard() == null || ctx.sourceCard().getPower() == null ? 0
                            : Math.max(0, ctx.sourceCard().getPower());
            case SourcePower ignored ->
                    ctx.sourcePermanent() == null ? 0
                            : Math.max(0, gameQueryService.getEffectivePower(gameData, ctx.sourcePermanent()));
            case SourceToughness ignored ->
                    ctx.sourcePermanent() == null ? 0
                            : Math.max(0, gameQueryService.getEffectiveToughness(gameData, ctx.sourcePermanent()));
            case TargetToughness ignored ->
                    targetEffectiveToughness(gameData, ctx);
            case TargetPower ignored ->
                    targetEffectivePower(gameData, ctx);
            case TargetManaValue ignored ->
                    targetManaValue(gameData, ctx);
            case EnchantedPermanentManaValue ignored ->
                    enchantedPermanentManaValue(gameData, ctx);
            case TargetSpellManaValue ignored ->
                    targetSpellManaValue(gameData, ctx);
            case TargetSpellPower ignored ->
                    targetSpellPower(gameData, ctx);
            case ChosenPermanentPower ignored ->
                    chosenPermanentEffectivePower(gameData, ctx);
            case ChosenNumberOnSource ignored ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getChosenNumber();
        };
    }

    private int chosenPermanentEffectivePower(GameData gameData, AmountContext ctx) {
        if (ctx.chosenPermanentId() == null) return 0;
        Permanent chosen = gameQueryService.findPermanentById(gameData, ctx.chosenPermanentId());
        // Checked as the ability resolves; 0 if the chosen permanent has left (matches SourcePower).
        return chosen == null ? 0 : Math.max(0, gameQueryService.getEffectivePower(gameData, chosen));
    }

    private int countOtherAttackersSharingCreatureTypeWithTarget(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        if (target == null) return 0;
        // Each other attacking creature that shares a creature type with the target counts once,
        // regardless of how many types it shares (CR 700.x, Shared Animosity ruling: counted as the
        // ability resolves). Changeling handling lives in GameQueryService.shareCreatureType.
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttacking() && !permanent.getId().equals(target.getId())
                    && gameQueryService.shareCreatureType(gameData, target, permanent)) {
                count[0]++;
            }
        });
        return count[0];
    }

    private int targetEffectiveToughness(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        // No legal target at resolution -> 0, matching the fizzle behaviour of the handlers this replaces.
        return target == null ? 0 : Math.max(0, gameQueryService.getEffectiveToughness(gameData, target));
    }

    private int attachedPermanentColorCount(GameData gameData, AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source == null || source.getAttachedTo() == null) {
            return 0;
        }
        var layered = LayerSystemService.activeStateFor(source.getAttachedTo());
        if (layered != null) {
            return layered.getColors().size();
        }
        Permanent attached = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        return attached == null ? 0 : gameQueryService.getEffectiveColors(gameData, attached).size();
    }

    private int targetEffectivePower(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        // No legal target at resolution -> 0, matching the fizzle behaviour of the handlers this replaces.
        return target == null ? 0 : Math.max(0, gameQueryService.getEffectivePower(gameData, target));
    }

    private int targetManaValue(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        // No legal target at resolution -> 0, matching the fizzle behaviour of the targeted handlers.
        return target == null ? 0 : target.getCard().getManaValue();
    }

    /** Mana value of the permanent the source Aura enchants (Soul Tithe); 0 if there is none. */
    private int enchantedPermanentManaValue(GameData gameData, AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source == null || source.getAttachedTo() == null) return 0;
        Permanent enchanted = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        return enchanted == null ? 0 : enchanted.getCard().getManaValue();
    }

    /** Mana value of the targeted spell on the stack (Refuse); 0 if it has already left. */
    private int targetSpellManaValue(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(ctx.targetPermanentId())) {
                return se.getCard().getManaValue() + se.getXValue();
            }
        }
        return 0;
    }

    /** Printed power of the targeted creature spell on the stack (Essence Backlash); 0 if gone. */
    private int targetSpellPower(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(ctx.targetPermanentId())) {
                Integer power = se.getCard().getPower();
                return power == null ? 0 : Math.max(0, power);
            }
        }
        return 0;
    }

    /**
     * Whether the amount (recursively) reads the stack entry's snapshotted x value —
     * used by trigger collectors to decide if an entry needs {@code xValue} populated.
     */
    public boolean referencesXValue(DynamicAmount amount) {
        return switch (amount) {
            case XValue ignored -> true;
            case ManaSpentToCast ignored -> true;
            case Scaled s -> referencesXValue(s.amount());
            case Divided d -> referencesXValue(d.amount());
            case HalvedRoundedUp h -> referencesXValue(h.amount());
            case Sum s -> s.amounts().stream().anyMatch(this::referencesXValue);
            case Min m -> m.amounts().stream().anyMatch(this::referencesXValue);
            case Max m -> m.amounts().stream().anyMatch(this::referencesXValue);
            default -> false;
        };
    }

    /**
     * Whether the amount (recursively) reads the stack entry's snapshotted event value — used by
     * trigger collectors (and the excess-damage producer) to decide if an entry needs its
     * {@code eventValue} populated. The event-value analogue of {@link #referencesXValue}.
     */
    public boolean referencesEventValue(DynamicAmount amount) {
        return switch (amount) {
            case EventValue ignored -> true;
            case Scaled s -> referencesEventValue(s.amount());
            case Divided d -> referencesEventValue(d.amount());
            case HalvedRoundedUp h -> referencesEventValue(h.amount());
            case Sum s -> s.amounts().stream().anyMatch(this::referencesEventValue);
            case Min m -> m.amounts().stream().anyMatch(this::referencesEventValue);
            case Max m -> m.amounts().stream().anyMatch(this::referencesEventValue);
            default -> false;
        };
    }

    private int countPermanents(GameData gameData, PermanentCount count, AmountContext ctx) {
        // In static evaluation the filter context carries a null GameData: type and keyword checks then
        // use intrinsic values, so counting never calls computeStaticBonus on other permanents
        // (which could recurse back into the count being computed). The P/T leaves are exempt —
        // they route through GameQueryService's recursion-safe accessors. The source identity is
        // supplied either way: it costs no query, and without it source-relative predicates
        // silently match nothing.
        FilterContext filterContext = GameQueryService.isStaticEvaluationActive()
                ? FilterContext.empty()
                : FilterContext.of(gameData);
        filterContext = filterContext.withSourceControllerId(ctx.controllerId());
        // Source-relative predicates (e.g. PermanentHasSameNameAsSourcePredicate on
        // Powerstone Shard's "for each artifact you control named ~") need the source card.
        // PermanentIsHostOfSourceAuraPredicate also needs the live source permanent when
        // staticEvaluation nulls GameData (Vampirism: "+1/+1 for each other creature you control").
        if (ctx.sourcePermanent() != null) {
            filterContext = filterContext
                    .withSourceCardId(ctx.sourcePermanent().getCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent());
        }
        int matches = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (count.excludeSource() && ctx.sourcePermanent() != null
                        && permanent.getId().equals(ctx.sourcePermanent().getId())) {
                    continue;
                }
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, count.filter(), filterContext)) {
                    matches++;
                }
            }
        }
        return matches;
    }

    /**
     * Domain (an ability word, CR 207.2c): the number of distinct basic land types among lands
     * the controller controls. CR 305.7 land-type overrides (Blood Moon, Urborg, Prismatic Omen)
     * count in both branches; static evaluation reads them through the recursion-safe accessor,
     * which falls back to printed types only for a land whose own static bonus is being assembled.
     */
    private int countBasicLandTypesAmongControlledLands(GameData gameData, AmountContext ctx) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        java.util.Set<CardSubtype> found = java.util.EnumSet.noneOf(CardSubtype.class);
        for (Permanent permanent : battlefield) {
            if (!permanent.getCard().hasType(CardType.LAND)) continue;
            found.addAll(GameQueryService.isStaticEvaluationActive()
                    ? gameQueryService.basicLandTypesForStaticEvaluation(gameData, permanent)
                    : gameQueryService.effectiveBasicLandTypes(gameData, permanent));
        }
        return found.size();
    }

    /**
     * Distinct card types among non-token cards in the scoped graveyard(s). Multi-type cards
     * contribute each printed type (artifact creature → Artifact + Creature).
     */
    private int countCardTypesAmongCardsInGraveyard(
            GameData gameData, CardTypesAmongCardsInGraveyard amount, AmountContext ctx) {
        java.util.Set<CardType> found = java.util.EnumSet.noneOf(CardType.class);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (card.getType() != null) {
                    found.add(card.getType());
                }
                found.addAll(card.getAdditionalTypes());
            }
        }
        return found.size();
    }

    private int countColorManaSymbolsAmongControlledPermanents(
            GameData gameData, ColorManaSymbolsAmongControlledPermanents amount, AmountContext ctx) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        int total = 0;
        for (Permanent permanent : battlefield) {
            ManaCost cost = permanent.getCard().getParsedManaCost();
            if (cost != null) {
                total += cost.countColorSymbols(amount.color());
            }
        }
        return total;
    }

    private int countColorManaSymbolsInGraveyard(
            GameData gameData, ColorManaSymbolsInGraveyard amount, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                ManaCost cost = card.getParsedManaCost();
                if (cost != null) {
                    total += cost.countColorSymbols(amount.color());
                }
            }
        }
        return total;
    }

    private int countGraveyardCards(GameData gameData, CardsInGraveyard count, AmountContext ctx) {
        int matches = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (predicateEvaluationService.matchesCardPredicate(card, count.filter(), null)) {
                    matches++;
                }
            }
        }
        return matches;
    }

    private int countExileCards(GameData gameData, CardsInExile count, AmountContext ctx) {
        int matches = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            for (Card card : gameData.getPlayerExiledCards(playerId)) {
                if (card.isToken()) continue;
                if (predicateEvaluationService.matchesCardPredicate(card, count.filter(), null)) {
                    matches++;
                }
            }
        }
        return matches;
    }

    private int countCountersOnLinkedPermanent(GameData gameData, CountersOnLinkedPermanent count) {
        Permanent linked = gameQueryService.findPermanentById(gameData, count.linkedPermanentId());
        return linked == null ? 0 : linked.getCounterCount(count.counterType());
    }

    private int countColorManaSymbolsInHand(
            GameData gameData, ColorManaSymbolsInHand amount, AmountContext ctx) {
        List<Card> hand = gameData.playerHands.get(ctx.controllerId());
        if (hand == null) return 0;
        int total = 0;
        for (Card card : hand) {
            ManaCost cost = card.getParsedManaCost();
            if (cost != null) {
                total += cost.countColorSymbols(amount.color());
            }
        }
        return total;
    }

    private int countHandCards(GameData gameData, CardsInHand count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand != null) {
                total += hand.size();
            }
        }
        return total;
    }

    private int countMatchingHandCards(GameData gameData, MatchingCardsInHand count, AmountContext ctx) {
        UUID sourceCardId = ctx.sourcePermanent() != null ? ctx.sourcePermanent().getCard().getId() : null;
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null) continue;
            for (Card card : hand) {
                if (predicateEvaluationService.matchesCardPredicate(card, count.predicate(), sourceCardId)) {
                    total++;
                }
            }
        }
        return total;
    }

    private int countLibraryCards(GameData gameData, CardsInLibrary count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck != null) {
                total += deck.size();
            }
        }
        return total;
    }

    private boolean controlsAllNamed(GameData gameData, FixedIfControlsAllNamed amount, AmountContext ctx) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        for (String requiredName : amount.requiredNames()) {
            boolean found = battlefield.stream()
                    .anyMatch(permanent -> requiredName.equals(permanent.getCard().getName()));
            if (!found) return false;
        }
        return true;
    }

    private boolean targetMatches(GameData gameData, FixedIfTargetMatches amount, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return false;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        return target != null
                && predicateEvaluationService.matchesPermanentPredicate(gameData, target, amount.filter());
    }

    private boolean controlsMoreCreaturesThanEachOtherPlayer(GameData gameData, AmountContext ctx) {
        int controllerCreatures = countCreaturesControlledBy(gameData, ctx.controllerId());
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            if (countCreaturesControlledBy(gameData, playerId) >= controllerCreatures) {
                return false;
            }
        }
        return true;
    }

    private boolean targetPlayerControlsMoreLands(GameData gameData, AmountContext ctx) {
        UUID targetId = targetPlayerId(gameData, ctx);
        if (targetId == null || ctx.controllerId() == null) {
            return false;
        }
        return countLandsControlledBy(gameData, targetId) > countLandsControlledBy(gameData, ctx.controllerId());
    }

    private int countLandsControlledBy(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().hasType(CardType.LAND)) {
                count++;
            }
        }
        return count;
    }

    private int countCreaturesControlledBy(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                count++;
            }
        }
        return count;
    }

    private int totalToughnessOfControlledCreatures(GameData gameData, AmountContext ctx) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        int total = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                // Anthems count in both branches. Static evaluation goes through the
                // recursion-safe accessor, which reads the layered toughness except for a
                // creature whose own static bonus is currently being assembled — the one case
                // where the amount would recurse back into the number it is computing.
                total += GameQueryService.isStaticEvaluationActive()
                        ? gameQueryService.toughnessForStaticFilter(permanent)
                        : gameQueryService.getEffectiveToughness(gameData, permanent);
            }
        }
        return total;
    }

    private int greatestPowerAmongControlled(GameData gameData, AmountContext ctx) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        int greatestPower = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    int power = gameQueryService.getEffectivePower(gameData, permanent);
                    if (power > greatestPower) {
                        greatestPower = power;
                    }
                }
            }
        }
        return greatestPower;
    }

    private int countAttachmentsOnSource(GameData gameData, AttachmentsOnSource amount, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        UUID sourceId = ctx.sourcePermanent().getId();
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttached() && permanent.getAttachedTo().equals(sourceId)) {
                boolean isAura = permanent.getCard().getSubtypes().contains(CardSubtype.AURA);
                boolean isEquipment = permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT);
                if ((amount.countAuras() && isAura) || (amount.countEquipment() && isEquipment)) {
                    count[0]++;
                }
            }
        });
        return count[0];
    }

    private int countCreaturesBlockingSource(GameData gameData, AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source == null) return 0;

        List<Permanent> sourceBattlefield = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(source)) {
                sourceBattlefield = battlefield;
                break;
            }
        }
        if (sourceBattlefield == null) return 0;

        int sourceIndex = sourceBattlefield.indexOf(source);
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isBlocking() && permanent.getBlockingTargets().contains(sourceIndex)) {
                count[0]++;
            }
        });
        return count[0];
    }

    private int countCreatureDeathsThisTurn(GameData gameData, CreatureDeathsThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.creatureDeathCountThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int countCreatureSubtypeDeathsThisTurn(GameData gameData,
                                                    CreatureSubtypeDeathsThisTurn count,
                                                    AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.creatureSubtypeDeathCountThisTurn
                    .getOrDefault(playerId, Map.of())
                    .getOrDefault(count.subtype(), 0);
        }
        return total;
    }

    private int countLifeLostThisTurn(GameData gameData, LifeLostThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.lifeLostThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int countLifeGainedThisTurn(GameData gameData, LifeGainedThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.getLifeGainedThisTurn(playerId);
        }
        return total;
    }

    /**
     * Highest life total among the controller's opponents (Malignus). Returns 0 while the controller
     * is unknown — that happens transiently for a CDA evaluated while the source is still entering
     * the battlefield.
     */
    private int highestOpponentLifeTotal(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int highest = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            highest = Math.max(highest, gameData.playerLifeTotals.getOrDefault(playerId, 0));
        }
        return highest;
    }

    private int countOpponentPoisonCounters(GameData gameData, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())) {
                total += gameData.playerPoisonCounters.getOrDefault(playerId, 0);
            }
        }
        return total;
    }

    /**
     * Number of creatures the source devoured (CR 702.82) that had the given subtype. A devoured
     * Changeling counts as every creature type, so it matches any subtype. Reads the intrinsic card
     * subtypes/keywords snapshotted at devour time (see {@code Permanent.recordDevouredCreature}).
     */
    private int countDevouredCreaturesOfSubtype(AmountContext ctx, CardSubtype subtype) {
        if (ctx.sourcePermanent() == null) return 0;
        return (int) ctx.sourcePermanent().getDevouredCreatures().stream()
                .filter(card -> card.getSubtypes().contains(subtype)
                        || card.getKeywords().contains(Keyword.CHANGELING))
                .count();
    }

    private int countLandsMatchingImprintedName(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        Card imprinted = gameData.getImprintedCard(ctx.sourcePermanent().getCard());
        if (imprinted == null) return 0;
        String imprintedName = imprinted.getName();
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().hasType(CardType.LAND)
                    && imprintedName.equals(permanent.getCard().getName())) {
                count[0]++;
            }
        });
        return count[0];
    }

    private int totalPTOfCardsExiledWithSource(GameData gameData, AmountContext ctx, boolean power) {
        if (ctx.sourcePermanent() == null) return 0;
        int total = 0;
        for (Card exiled : gameData.getCardsExiledByPermanent(ctx.sourcePermanent().getId())) {
            Integer value = power ? exiled.getPower() : exiled.getToughness();
            if (value != null) {
                total += value;
            }
        }
        return total;
    }

    private int imprintedCardManaValue(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        Card imprinted = gameData.getImprintedCard(ctx.sourcePermanent().getCard());
        return imprinted == null ? 0 : imprinted.getManaValue();
    }

    private int imprintedCreaturePT(GameData gameData, AmountContext ctx, boolean power) {
        if (ctx.sourcePermanent() == null) return 0;
        Card imprinted = gameData.getImprintedCard(ctx.sourcePermanent().getCard());
        if (imprinted == null || imprinted.getPower() == null || imprinted.getToughness() == null) {
            return 0;
        }
        return power ? imprinted.getPower() : imprinted.getToughness();
    }

    private int damageDealtToOpponentsThisTurn(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            total += gameData.damageDealtToPlayersThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private boolean isPlayerInScope(GameData gameData, UUID playerId, CountScope scope, AmountContext ctx) {
        return switch (scope) {
            case CONTROLLER -> playerId.equals(ctx.controllerId());
            case OPPONENTS -> !playerId.equals(ctx.controllerId());
            case ANY_PLAYER -> true;
            // The target channel carries the target player's id for player-targeting effects.
            case TARGET_PLAYER -> playerId.equals(targetPlayerId(gameData, ctx));
            case DEFENDING_PLAYER -> playerId.equals(defendingPlayerId(gameData, ctx));
            case ATTACHED_CONTROLLER -> playerId.equals(attachedControllerId(gameData, ctx));
        };
    }

    /**
     * The controller of the permanent the source Aura/Equipment is attached to, or {@code null}
     * when the source is missing or unattached. See {@link CountScope#ATTACHED_CONTROLLER}.
     */
    private UUID attachedControllerId(GameData gameData, AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source == null || !source.isAttached()) {
            return null;
        }
        return gameQueryService.findPermanentController(gameData, source.getAttachedTo());
    }

    /**
     * The player named by the target channel: the targeted player itself, or — when the target is a
     * permanent (a targeted planeswalker) — that permanent's controller. Models the
     * "that player or that planeswalker's controller" wordings (Goblin Lyre) with the same scope
     * that plain "target player" effects use.
     */
    private UUID targetPlayerId(GameData gameData, AmountContext ctx) {
        UUID targetId = ctx.targetPermanentId();
        if (targetId == null || gameData.playerIds.contains(targetId)) {
            return targetId;
        }
        return gameQueryService.findPermanentController(gameData, targetId);
    }

    /**
     * The player the source permanent is attacking (its attack target when that is a player,
     * otherwise the controller of the attacked planeswalker), or {@code null} when the source is
     * not attacking or has no attack target. See {@link CountScope#DEFENDING_PLAYER}.
     */
    private UUID defendingPlayerId(GameData gameData, AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source == null || !source.isAttacking() || source.getAttackTarget() == null) {
            return null;
        }
        UUID attackTarget = source.getAttackTarget();
        if (gameData.orderedPlayerIds.contains(attackTarget)) {
            return attackTarget;
        }
        // Attacking a planeswalker: the defending player is that planeswalker's controller.
        return gameQueryService.findPermanentController(gameData, attackTarget);
    }
}
