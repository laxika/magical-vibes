package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInHand;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnLinkedPermanent;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToOpponentsThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlledCreaturesTotalToughnessAtLeast;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlsAllNamed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreaturePower;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreatureToughness;
import com.github.laxika.magicalvibes.model.amount.LandsMatchingImprintedName;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.OpponentPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.OtherAttackersSharingCreatureTypeWithTarget;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
            case XValue ignored ->
                    ctx.xValue();
            case ManaSpentToCast ignored ->
                    ctx.xValue();
            case EventValue ignored ->
                    ctx.eventValue();
            case Scaled s ->
                    s.factor() * evaluate(gameData, s.amount(), ctx);
            case Divided d ->
                    evaluate(gameData, d.amount(), ctx) / d.divisor();
            case Sum s ->
                    s.amounts().stream().mapToInt(a -> evaluate(gameData, a, ctx)).sum();
            case PermanentCount c ->
                    countPermanents(gameData, c, ctx);
            case BasicLandTypesAmongControlledLands ignored ->
                    countBasicLandTypesAmongControlledLands(gameData, ctx);
            case CardsInGraveyard c ->
                    countGraveyardCards(gameData, c, ctx);
            case CardsInHand c ->
                    countHandCards(gameData, c, ctx);
            case ColorManaSymbolsAmongControlledPermanents c ->
                    countColorManaSymbolsAmongControlledPermanents(gameData, c, ctx);
            case ColorManaSymbolsInGraveyard c ->
                    countColorManaSymbolsInGraveyard(gameData, c, ctx);
            case ColorManaSymbolsInHand c ->
                    countColorManaSymbolsInHand(gameData, c, ctx);
            case CountersOnSource c ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getCounterCount(c.counterType());
            case CountersOnLinkedPermanent c ->
                    countCountersOnLinkedPermanent(gameData, c);
            case ControllerLifeTotal ignored ->
                    gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 0);
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
            case DamageDealtToTargetPlayerThisTurn ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.damageDealtToPlayersThisTurn.getOrDefault(ctx.targetPermanentId(), 0);
            case CardsDiscardedByTargetPlayerThisTurn ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.cardsDiscardedThisTurn.getOrDefault(ctx.targetPermanentId(), 0);
            case DamageDealtToOpponentsThisTurn ignored ->
                    damageDealtToOpponentsThisTurn(gameData, ctx);
            case ImprintedCreaturePower ignored ->
                    imprintedCreaturePT(gameData, ctx, true);
            case ImprintedCreatureToughness ignored ->
                    imprintedCreaturePT(gameData, ctx, false);
            case LandsMatchingImprintedName ignored ->
                    countLandsMatchingImprintedName(gameData, ctx);
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
            case ChosenPermanentPower ignored ->
                    chosenPermanentEffectivePower(gameData, ctx);
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

    private int targetEffectivePower(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        // No legal target at resolution -> 0, matching the fizzle behaviour of the handlers this replaces.
        return target == null ? 0 : Math.max(0, gameQueryService.getEffectivePower(gameData, target));
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
            case Sum s -> s.amounts().stream().anyMatch(this::referencesXValue);
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
            case Sum s -> s.amounts().stream().anyMatch(this::referencesEventValue);
            default -> false;
        };
    }

    private int countPermanents(GameData gameData, PermanentCount count, AmountContext ctx) {
        // In static evaluation, match with a null FilterContext: type/keyword checks then
        // use only intrinsic values, so counting never calls computeStaticBonus on other
        // permanents (which could recurse back into the count being computed).
        FilterContext filterContext;
        if (ctx.staticEvaluation()) {
            filterContext = null;
        } else {
            filterContext = FilterContext.of(gameData).withSourceControllerId(ctx.controllerId());
            // Source-relative predicates (e.g. PermanentHasSameNameAsSourcePredicate on
            // Powerstone Shard's "for each artifact you control named ~") need the source card.
            if (ctx.sourcePermanent() != null) {
                filterContext = filterContext.withSourceCardId(ctx.sourcePermanent().getCard().getId());
            }
        }
        int matches = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(playerId, count.scope(), ctx)) continue;
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

    private static final java.util.Set<CardSubtype> BASIC_LAND_SUBTYPES = java.util.EnumSet.of(
            CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
            CardSubtype.MOUNTAIN, CardSubtype.FOREST);

    /**
     * Domain (CR 702.42): the number of distinct basic land types among lands the controller
     * controls. During static evaluation only intrinsic printed types are read (no
     * {@code computeStaticBonus}, avoiding recursion); otherwise CR 305.7 land-type overrides count.
     */
    private int countBasicLandTypesAmongControlledLands(GameData gameData, AmountContext ctx) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        java.util.Set<CardSubtype> found = java.util.EnumSet.noneOf(CardSubtype.class);
        for (Permanent permanent : battlefield) {
            if (!permanent.getCard().hasType(CardType.LAND)) continue;
            if (ctx.staticEvaluation()) {
                for (CardSubtype st : permanent.getCard().getSubtypes()) {
                    if (BASIC_LAND_SUBTYPES.contains(st)) found.add(st);
                }
            } else {
                found.addAll(gameQueryService.effectiveBasicLandTypes(gameData, permanent));
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
            if (!isPlayerInScope(playerId, amount.scope(), ctx)) continue;
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
            if (!isPlayerInScope(playerId, count.scope(), ctx)) continue;
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
            if (!isPlayerInScope(playerId, count.scope(), ctx)) continue;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand != null) {
                total += hand.size();
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
                // In static evaluation, sum intrinsic toughness so we never call computeStaticBonus
                // (which could recurse back into the amount being computed). At cast time
                // (staticEvaluation=false), use full effective toughness so anthems count.
                total += ctx.staticEvaluation()
                        ? permanent.getEffectiveToughness()
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
            if (!isPlayerInScope(playerId, count.scope(), ctx)) continue;
            total += gameData.creatureDeathCountThisTurn.getOrDefault(playerId, 0);
        }
        return total;
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

    private boolean isPlayerInScope(UUID playerId, CountScope scope, AmountContext ctx) {
        return switch (scope) {
            case CONTROLLER -> playerId.equals(ctx.controllerId());
            case OPPONENTS -> !playerId.equals(ctx.controllerId());
            case ANY_PLAYER -> true;
            // The target channel carries the target player's id for player-targeting effects.
            case TARGET_PLAYER -> playerId.equals(ctx.targetPermanentId());
        };
    }
}
