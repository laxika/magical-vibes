package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreaturePower;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreatureToughness;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.OpponentPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.amount.Sum;
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
            case XValue ignored ->
                    ctx.xValue();
            case ManaSpentToCast ignored ->
                    ctx.xValue();
            case Scaled s ->
                    s.factor() * evaluate(gameData, s.amount(), ctx);
            case Divided d ->
                    evaluate(gameData, d.amount(), ctx) / d.divisor();
            case Sum s ->
                    s.amounts().stream().mapToInt(a -> evaluate(gameData, a, ctx)).sum();
            case PermanentCount c ->
                    countPermanents(gameData, c, ctx);
            case CardsInGraveyard c ->
                    countGraveyardCards(gameData, c, ctx);
            case CardsInHand c ->
                    countHandCards(gameData, c, ctx);
            case CountersOnSource c ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getCounterCount(c.counterType());
            case GreatestPowerAmongControlled ignored ->
                    greatestPowerAmongControlled(gameData, ctx);
            case AttachmentsOnSource a ->
                    countAttachmentsOnSource(gameData, a, ctx);
            case CreaturesBlockingSource ignored ->
                    countCreaturesBlockingSource(gameData, ctx);
            case OpponentPoisonCounters ignored ->
                    countOpponentPoisonCounters(gameData, ctx);
            case CreatureDeathsThisTurn c ->
                    countCreatureDeathsThisTurn(gameData, c, ctx);
            case ImprintedCreaturePower ignored ->
                    imprintedCreaturePT(ctx, true);
            case ImprintedCreatureToughness ignored ->
                    imprintedCreaturePT(ctx, false);
            case SourcePower ignored ->
                    ctx.sourcePermanent() == null ? 0
                            : Math.max(0, gameQueryService.getEffectivePower(gameData, ctx.sourcePermanent()));
            case SourceToughness ignored ->
                    ctx.sourcePermanent() == null ? 0
                            : Math.max(0, gameQueryService.getEffectiveToughness(gameData, ctx.sourcePermanent()));
        };
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

    private int countPermanents(GameData gameData, PermanentCount count, AmountContext ctx) {
        // In static evaluation, match with a null FilterContext: type/keyword checks then
        // use only intrinsic values, so counting never calls computeStaticBonus on other
        // permanents (which could recurse back into the count being computed).
        FilterContext filterContext = ctx.staticEvaluation()
                ? null
                : FilterContext.of(gameData).withSourceControllerId(ctx.controllerId());
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

    private int imprintedCreaturePT(AmountContext ctx, boolean power) {
        if (ctx.sourcePermanent() == null) return 0;
        Card imprinted = ctx.sourcePermanent().getCard().getImprintedCard();
        if (imprinted == null || imprinted.getPower() == null || imprinted.getToughness() == null) {
            return 0;
        }
        return power ? imprinted.getPower() : imprinted.getToughness();
    }

    private boolean isPlayerInScope(UUID playerId, CountScope scope, AmountContext ctx) {
        return switch (scope) {
            case CONTROLLER -> playerId.equals(ctx.controllerId());
            case OPPONENTS -> !playerId.equals(ctx.controllerId());
            case ANY_PLAYER -> true;
        };
    }
}
