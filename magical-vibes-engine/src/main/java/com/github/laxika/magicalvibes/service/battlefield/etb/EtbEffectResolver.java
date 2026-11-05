package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.condition.ControlledDragonAsCast;
import com.github.laxika.magicalvibes.model.condition.RevealCardFromHandCostPaid;
import com.github.laxika.magicalvibes.model.condition.CastForMadnessCost;
import com.github.laxika.magicalvibes.model.condition.CastForProwlCost;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.condition.CastForSpectacleCost;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.condition.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToCast;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.condition.SourceMatchesPermanentPredicate;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.condition.SourceEntryCostPaid;
import com.github.laxika.magicalvibes.model.condition.EnteredFromZone;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.amount.ColorManaPairsSpentToCast;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry that resolves a creature's own mandatory enter-the-battlefield effects into the form they
 * take at trigger time — replacing the {@code instanceof} cascade that used to conflate modal unwrap,
 * value materialisation, and intervening-if gating in {@code BattlefieldEntryService}.
 *
 * <p>Each concrete {@link CardEffect} class is mapped to a small {@link EtbEffectHandler}. Effects with
 * no registered handler pass through unchanged (the default identity). {@link #resolve} performs a
 * single exact-class lookup: this collapses the former two-pass "map (unwrap) then filter (gate)"
 * pipeline into one pass, which is behaviour-identical for every real effect because each effect
 * matches exactly one branch — a raw effect is either an unwrap type, a gate type, or neither, but
 * never both (no card nests a gate conditional inside a modal/kicked wrapper).
 *
 * <p><b>Conditions.</b> A {@link ConditionalEffect} among an ability's effects is the "if" of that effect, judged as
 * it resolves; one fixed as the permanent enters (Kicked / CastFromZone, a cost paid, the mana spent, whether it is a
 * token, {@code SourceUntapped} sampled at entry) is judged now instead and <em>unwraps</em> to its inner effect, or
 * drops ({@code null}) when false. Every other condition passes through unchanged and is checked by
 * {@code EffectResolutionService}. The ability's intervening "if" (CR 603.4) and event condition (CR 603.2) are its
 * options: {@link #ownEntryConditionMet} judges them as the ability triggers, through the same cases and then the game
 * state (Metalcraft, Morbid, Raid, "if you control another …"), and the intervening "if" wraps the ability's effects
 * ({@code TriggeredAbility.ownEntryEffects()}), so it reaches this handler too and is checked again as the ability
 * resolves. Only a condition on the ability's target, which isn't chosen yet, passes through unchecked.
 */
@Component
public class EtbEffectResolver {

    private final Map<Class<? extends CardEffect>, EtbEffectHandler> handlers = new HashMap<>();
    private final ConditionEvaluationService conditionEvaluationService;

    public EtbEffectResolver(ConditionEvaluationService conditionEvaluationService) {
        this.conditionEvaluationService = conditionEvaluationService;
        // "you lose the game unless this was cast from your hand" — no-op (drop) when cast from hand,
        // otherwise materialise into the controller losing the game.
        register(LoseGameIfNotCastFromHandEffect.class, (ctx, effect) -> {
            Permanent source = ctx.sourcePermanent();
            boolean castFromHand = source != null && source.isCast() && source.getCastFromZone() != null
                    ? source.getCastFromZone() == Zone.HAND
                    : ctx.wasCastFromHand();
            return castFromHand ? null : new TargetPlayerLosesGameEffect(ctx.controllerId());
        });

        // Tribute's "if tribute wasn't paid" ability is an intervening-if trigger. The choice is
        // recorded on the entering permanent before this resolver runs.
        register(TributeNotPaidEffect.class, (ctx, effect) -> {
            TributeNotPaidEffect tribute = (TributeNotPaidEffect) effect;
            return ctx.sourcePermanent() != null && !ctx.sourcePermanent().isTributePaid()
                    ? tribute.wrapped() : null;
        });

        // Modal ETB (choose one / choose up to one): unwrap the option picked at cast time (etbMode).
        // Optional modals with etbMode < 0 chose no mode and drop the trigger.
        register(ChooseOneEffect.class, (ctx, effect) -> {
            ChooseOneEffect coe = (ChooseOneEffect) effect;
            if (coe.optional() && ctx.etbMode() < 0) {
                return null;
            }
            if (ctx.etbMode() < 0) {
                CardEffect[] selectedEffects = coe.decodeModeIndices(ctx.etbMode()).stream()
                        .flatMap(modeIndex -> coe.options().get(modeIndex).effects().stream())
                        .toArray(CardEffect[]::new);
                return selectedEffects.length == 1
                        ? selectedEffects[0]
                        : SequenceEffect.of(selectedEffects);
            }
            if (ctx.etbMode() >= 0 && ctx.etbMode() < coe.options().size()) {
                return selectedModeEffect(coe.options().get(ctx.etbMode()));
            }
            return selectedModeEffect(coe.options().getFirst());
        });

        // Evoke sacrifice (CR 702.74a) — intervening-if (CR 603.4): resolve to a plain sacrifice
        // when the evoke cost was paid, otherwise drop the trigger entirely.
        register(SacrificeSelfIfEvokedEffect.class, (ctx, effect) ->
                ctx.evoked() ? new SacrificeSelfEffect(true) : null);

        // Dash's return to hand is queued when the permanent enters from a dash cast, so it is not an
        // enters-the-battlefield trigger and never goes on the stack.
        register(ReturnSelfToHandIfDashCostPaidEffect.class, (ctx, effect) -> null);

        // "Gain life equal to that creature's toughness" — the toughness is read on resolution, from the
        // entering creature's last known information if it has left the battlefield.
        register(GainLifeEqualToToughnessEffect.class, (ctx, effect) ->
                new GainLifeEffect(new SourceToughness()));

        // Conditional ETB effects: immutable cast-time conditions are evaluated while the trigger
        // is created, gate types (Metalcraft / Morbid / Raid / ControlsAnother) stay wrapped for
        // re-evaluation at stack resolution, and source-untapped clauses are sampled at entry.
        register(ConditionalEffect.class, (ctx, effect) -> {
            ConditionalEffect conditional = (ConditionalEffect) effect;
            return switch (verdict(ctx, conditional)) {
                case DROP -> null;
                case NOTHING -> new DrawCardEffect(0);
                case UNWRAP -> conditional.wrapped();
                case KEEP, UNJUDGED -> effect;
            };
        });
    }

    /**
     * What becomes of a {@link ConditionalEffect} of a "when this enters" ability as the ability triggers: a fact
     * fixed as the permanent entered drops it ({@link #DROP}), leaves an effect that does nothing in its place so the
     * ability still triggers ({@link #NOTHING}: the mana spent to cast it, which the ability can't read later) or
     * unwraps it ({@link #UNWRAP}), or is kept wrapped after it was judged ({@link #KEEP}); any other condition is
     * kept wrapped unjudged ({@link #UNJUDGED}), and checked as the effect resolves.
     */
    private enum Verdict {
        DROP, NOTHING, UNWRAP, KEEP, UNJUDGED
    }

    /** What becomes of {@code conditional} as the ability it is part of triggers ({@link Verdict}). */
    private Verdict verdict(EtbEffectContext ctx, ConditionalEffect conditional) {
        ConditionContext conditionContext = conditionContext(ctx);
        boolean collectEvidenceCostPaid = ctx.sourcePermanent() != null
                && ctx.sourcePermanent().isCollectEvidenceCostPaid();
        if (conditional.condition() instanceof ColorSpentToCast
                || conditional.condition() instanceof NotCondition not
                && not.inner() instanceof ColorSpentToCast) {
            boolean applies = conditionEvaluationService.isMet(
                    ctx.gameData(), conditional.condition(), conditionContext);
            return applies ? Verdict.UNWRAP : Verdict.NOTHING;
        }
        return switch (conditional.condition()) {
            // Kicked intervening-if (CR 603.4): unwrap when kicked, otherwise drop.
            case Kicked ignored -> ctx.kicked() ? Verdict.UNWRAP : Verdict.DROP;
            // Not-kicked ETB clauses use the same cast-time context.
            case NotKicked ignored -> !ctx.kicked() ? Verdict.UNWRAP : Verdict.DROP;
            case NotCondition notCondition when notCondition.inner() instanceof CastForAlternateCost ->
                    ctx.alternateCost() ? Verdict.DROP : Verdict.UNWRAP;
            // Independent additional-kicker clauses are intervening-if conditions whose
            // payment list is snapshotted on the spell's stack entry.
            case RepeatedAdditionalCostPaid paid ->
                    ctx.repeatedAdditionalCosts().stream().filter(paid.manaCost()::equals).count()
                            >= paid.minimumPayments() ? Verdict.UNWRAP : Verdict.DROP;
            // Prowl intervening-if (CR 603.4): unwrap when the prowl cost was paid, otherwise drop.
            case CastForProwlCost ignored -> ctx.prowl() ? Verdict.UNWRAP : Verdict.DROP;
            // Gift intervening-if (CR 603.4): the promise is recorded on the entering permanent when
            // the spell resolves, so an unpromised gift never puts the trigger on the stack.
            case GiftPromised ignored ->
                    ctx.sourcePermanent() != null && ctx.sourcePermanent().isGiftPromised()
                            ? Verdict.KEEP : Verdict.DROP;
            case CastForMadnessCost ignored -> ctx.madness() ? Verdict.UNWRAP : Verdict.DROP;
            // CR 702.152a: blitz's end-step sacrifice is a delayed trigger queued by StackResolutionService.
            case CastForAlternateCost ignored when conditional.wrapped() instanceof SacrificeSelfAtEndStepEffect -> Verdict.DROP;
            // CR 702.152a: blitz's haste is a static ability granted by StackResolutionService as the spell resolves.
            case CastForAlternateCost ignored when conditional.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect grant
                    && grant.scope() == com.github.laxika.magicalvibes.model.effect.GrantScope.SELF
                    && grant.keywords().equals(java.util.Set.of(com.github.laxika.magicalvibes.model.Keyword.HASTE)) -> Verdict.DROP;
            case CastForAlternateCost ignored -> ctx.alternateCost() ? Verdict.UNWRAP : Verdict.DROP;
            // Spectacle branch selection is fixed when the permanent enters.
            case CastForSpectacleCost ignored ->
                    ctx.sourcePermanent() != null && ctx.sourcePermanent().isSpectacle()
                            ? Verdict.UNWRAP : Verdict.DROP;
            // Cast-from-hand intervening-if (CR 603.4): unwrap only when cast from hand, otherwise drop.
            case CastFromZone castFromZone ->
                    conditionEvaluationService.isMet(ctx.gameData(), castFromZone, conditionContext)
                            ? Verdict.UNWRAP : Verdict.DROP;
            case EnteredFromZone enteredFromZone ->
                    conditionEvaluationService.isMet(ctx.gameData(), enteredFromZone, conditionContext)
                            ? Verdict.UNWRAP : Verdict.DROP;
            // "if it entered from your graveyard or you cast it from your graveyard" (CR 603.4): where it came from
            // and where it was cast from are fixed as it enters, so they are checked as it triggers only.
            case AnyOf anyOf when anyOf.conditions().stream().allMatch(inner ->
                    inner instanceof CastFromZone || inner instanceof EnteredFromZone) ->
                    conditionEvaluationService.isMet(ctx.gameData(), anyOf, conditionContext)
                            ? Verdict.UNWRAP : Verdict.DROP;
            case ColorSpentToCast colorSpent ->
                    conditionEvaluationService.isMet(ctx.gameData(), colorSpent, conditionContext)
                            ? Verdict.KEEP : Verdict.DROP;
            case SnowManaSpentToCast snowManaSpent ->
                    conditionEvaluationService.isMet(ctx.gameData(), snowManaSpent, conditionContext)
                            ? Verdict.KEEP : Verdict.DROP;
            case TreasureManaSpentToCast treasureManaSpent ->
                    conditionEvaluationService.isMet(ctx.gameData(), treasureManaSpent, conditionContext)
                            ? Verdict.KEEP : Verdict.DROP;
            case TargetPermanentMatches targetMatches when ctx.targetId() != null && ctx.card().isAura() ->
                    conditionEvaluationService.isMet(ctx.gameData(), targetMatches, conditionContext)
                            ? Verdict.KEEP : Verdict.DROP;
            // "if it isn't a token" (CR 603.4): token-ness is fixed as the permanent enters.
            case NotCondition notCondition when notCondition.inner() instanceof SourceIsToken ->
                    conditionEvaluationService.isMet(ctx.gameData(), notCondition, conditionContext)
                            ? Verdict.UNWRAP : Verdict.DROP;
            case ControllerMainPhase controllerMainPhase ->
                    conditionEvaluationService.isMet(ctx.gameData(), controllerMainPhase, conditionContext)
                            ? Verdict.UNWRAP : Verdict.DROP;
            // "When this enters, if you have fewer than four cards in hand" (Krang, Master Mind): the
            // hand size is an intervening-if (CR 603.4), so the trigger is never put on the stack when false.
            case CardsInHandAtMost cardsInHandAtMost ->
                    conditionEvaluationService.isMet(ctx.gameData(), cardsInHandAtMost, conditionContext)
                            ? Verdict.UNWRAP : Verdict.DROP;
            // "When this enters, if it's a creature" (CR 603.4): a token copy that is not a creature
            // never triggers, so no target is chosen for it.
            case SourceIsCreature creatureGate -> (ctx.sourcePermanent() != null
                    ? conditionEvaluationService.isMet(ctx.gameData(), creatureGate, conditionContext)
                    : ctx.card().hasType(CardType.CREATURE)) ? Verdict.KEEP : Verdict.DROP;
            // An enters trigger that only matches when the entering permanent itself has some characteristic
            // (CR 603.6a, e.g. Weldfast Wingsmith entering as an artifact) never triggers otherwise.
            case SourceMatchesPermanentPredicate sourceMatches when ctx.sourcePermanent() != null ->
                    conditionEvaluationService.isMet(ctx.gameData(), sourceMatches, conditionContext)
                            ? Verdict.KEEP : Verdict.DROP;
            case SourceUntapped ignored ->
                    conditionEvaluationService.isMet(ctx.gameData(), ignored, conditionContext)
                            ? Verdict.UNWRAP : Verdict.DROP;
            case com.github.laxika.magicalvibes.model.condition.CollectEvidenceCostPaid ignored ->
                    collectEvidenceCostPaid ? Verdict.UNWRAP : Verdict.DROP;
            case SourceEntryCostPaid ignored ->
                    conditionEvaluationService.isMet(ctx.gameData(), ignored, conditionContext)
                            ? Verdict.UNWRAP : Verdict.DROP;
            // "if you cast it" is true for a spell cast from any zone, but not for a copy or
            // a permanent put onto the battlefield by an effect.
            case WasCast wasCastCondition ->
                    ctx.sourcePermanent() != null
                            ? (conditionEvaluationService.isMet(ctx.gameData(), wasCastCondition,
                            conditionContext) ? Verdict.UNWRAP : Verdict.DROP)
                            : (ctx.wasCastFromHand() && !wasCastCondition.byController()
                            ? Verdict.UNWRAP : Verdict.DROP);
            // "if you revealed a Dragon card or controlled a Dragon as you cast this spell" (CR 603.4):
            // both halves are cast-time facts recorded on the entering permanent, so the trigger is
            // never put on the stack when neither was true.
            case AnyOf anyOf when ctx.sourcePermanent() != null
                    && anyOf.conditions().stream().allMatch(inner ->
                    inner instanceof RevealCardFromHandCostPaid || inner instanceof ControlledDragonAsCast) ->
                    ctx.sourcePermanent().isRevealCardFromHandCostPaid()
                            || ctx.sourcePermanent().isControlledDragonAsCast() ? Verdict.KEEP : Verdict.DROP;
            // "if you cast it during your main phase" (Addendum, CR 603.4): both halves are facts fixed
            // when the permanent enters, so the trigger is never put on the stack when either is false.
            case AllOf allOf when allOf.conditions().stream().allMatch(inner ->
                    inner instanceof WasCast || inner instanceof ControllerMainPhase) ->
                    conditionEvaluationService.isMet(ctx.gameData(), allOf, conditionContext)
                            ? Verdict.UNWRAP : Verdict.DROP;
            // No target is chosen yet as the ability triggers (CR 603.3d), so a condition on it is
            // checked only as the ability resolves.
            case TargetPermanentMatches ignored -> Verdict.UNJUDGED;
            // Any other condition — Metalcraft, Morbid, Raid, ravenous's "if X is 5 or more", "if there is no
            // monarch", "if the sacrificed creature was …" — is checked as the effect resolves; as an intervening
            // "if" (CR 603.4) it was checked as the ability triggered too (ownEntryConditionMet).
            default -> Verdict.UNJUDGED;
        };
    }

    /**
     * Whether a "when this enters" ability whose intervening "if" (CR 603.4) or event condition (CR 603.2) is
     * {@code condition} triggers as the permanent enters: a fact fixed as it entered is judged as the effects' own
     * conditions are (a kicker, the zone it was cast from, a cost paid, the mana spent), any other condition on the
     * game state now, but for one on the ability's target, which isn't chosen yet.
     */
    public boolean ownEntryConditionMet(EtbEffectContext ctx, Condition condition) {
        return switch (verdict(ctx, new ConditionalEffect(condition, new DrawCardEffect(0)))) {
            case DROP, NOTHING -> false;
            case UNWRAP, KEEP -> true;
            case UNJUDGED -> condition instanceof TargetPermanentMatches
                    || conditionEvaluationService.isMet(ctx.gameData(), condition, conditionContext(ctx));
        };
    }

    /** The context the conditions of {@code ctx}'s entering permanent are read in, with its cast-time facts. */
    private static ConditionContext conditionContext(EtbEffectContext ctx) {
        Zone sourceZone = ctx.sourcePermanent() == null
                ? (ctx.wasCastFromHand() ? Zone.HAND : null)
                : (ctx.sourcePermanent().isCast() ? ctx.sourcePermanent().getCastFromZone() : null);
        boolean collectEvidenceCostPaid = ctx.sourcePermanent() != null
                && ctx.sourcePermanent().isCollectEvidenceCostPaid();
        Card sacrificedCard = ctx.sourcePermanent() == null ? null : ctx.sourcePermanent().getCastSacrificedCard();
        return new ConditionContext(ctx.controllerId(),
                ctx.sourcePermanent() == null ? null : ctx.sourcePermanent().getId(),
                ctx.sourcePermanent(), ctx.card(), ctx.kicked(), false, ctx.prowl(), ctx.madness(), false, false,
                sourceZone, ctx.xValue(), ctx.targetId(), null, false, false, false, null, null, sacrificedCard,
                ctx.repeatedAdditionalCosts(), ctx.alternateCost(),
                ctx.sourcePermanent() != null && ctx.sourcePermanent().isSpectacle(),
                false, collectEvidenceCostPaid, false, 0, false);
    }

    private void register(Class<? extends CardEffect> effectClass, EtbEffectHandler handler) {
        handlers.put(effectClass, handler);
    }

    /**
     * Resolves a single mandatory ETB effect into its trigger-time form.
     *
     * @return the resolved effect to queue, or {@code null} if the trigger should be dropped
     *         (intervening-if failed, or a conditional no-op)
     */
    public CardEffect resolve(EtbEffectContext ctx, CardEffect effect) {
        EtbEffectHandler handler = handlers.get(effect.getClass());
        CardEffect resolved = handler != null ? handler.resolve(ctx, effect) : effect;
        return resolved == null ? null : snapshotManaPairs(ctx, resolved);
    }

    /** Retains the casting payment across the independent abilities of an entering permanent. */
    private CardEffect snapshotManaPairs(EtbEffectContext ctx, CardEffect effect) {
        if (effect instanceof DrawCardEffect draw && draw.amount() instanceof ColorManaPairsSpentToCast) {
            return new DrawCardEffect(snapshotManaPairs(ctx, draw.amount()),
                    draw.castTimeXValue());
        }
        if (effect instanceof DiscardEffect discard && discard.amount() instanceof ColorManaPairsSpentToCast) {
            return new DiscardEffect(snapshotManaPairs(ctx, discard.amount()), discard.recipient(),
                    discard.random(), discard.stopAfterDiscardingType(),
                    discard.onlyCardsDrawnThisResolution(), discard.stopAfterDiscardingPredicate());
        }
        if (effect instanceof BoostSelfEffect boost
                && (boost.powerBoost() instanceof ColorManaPairsSpentToCast
                || boost.toughnessBoost() instanceof ColorManaPairsSpentToCast)) {
            return new BoostSelfEffect(snapshotManaPairs(ctx, boost.powerBoost()),
                    snapshotManaPairs(ctx, boost.toughnessBoost()), boost.duration());
        }
        if (effect instanceof SequenceEffect sequence) {
            var steps = sequence.steps().stream().map(child -> snapshotManaPairs(ctx, child)).toList();
            return steps.equals(sequence.steps()) ? effect
                    : sequence.withSteps(steps);
        }
        if (effect instanceof ConditionalEffect conditional) {
            CardEffect wrapped = snapshotManaPairs(ctx, conditional.wrapped());
            return wrapped.equals(conditional.wrapped()) ? effect
                    : new ConditionalEffect(conditional.condition(), wrapped);
        }
        return effect;
    }

    /** Other dynamic values remain live and are evaluated when the ability resolves. */
    private DynamicAmount snapshotManaPairs(EtbEffectContext ctx, DynamicAmount amount) {
        if (amount instanceof ColorManaPairsSpentToCast pairs) {
            return new Fixed(ctx.gameData().getSpellCastManaSpentByColor(ctx.card().getId(), pairs.color()) / 2);
        }
        return amount;
    }

    private static CardEffect selectedModeEffect(ChooseOneEffect.ChooseOneOption option) {
        return option.effects().size() == 1
                ? option.effects().getFirst()
                : SequenceEffect.of(option.effects().toArray(CardEffect[]::new));
    }
}
