package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CastForProwlCost;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.CastForSpectacleCost;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.condition.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.EnteredFromZone;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAtEndStepEffect;
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
 * <p><b>Gate vs. unwrap asymmetry (preserved deliberately):</b> trigger-time gates
 * ({@link Condition#isEtbTriggerGate()} — Metalcraft / Morbid / Raid / ControlsAnother / OpponentControlsMoreLands) return the
 * <em>conditional effect unchanged</em> when met (it stays wrapped and is re-evaluated at stack
 * resolution by {@code EffectResolutionService}), whereas Kicked / CastFromZone <em>unwrap</em> to
 * their inner effect. {@code SourceUntapped} is also unwrapped after being sampled at entry;
 * conditions with no other ETB policy pass through unchanged. Dropping ({@code null})
 * applies the intervening-if rule (CR 603.4): the ability never goes on the stack.
 */
@Component
public class EtbEffectResolver {

    private final Map<Class<? extends CardEffect>, EtbEffectHandler> handlers = new HashMap<>();

    public EtbEffectResolver(ConditionEvaluationService conditionEvaluationService) {
        // "you lose the game unless this was cast from your hand" — no-op (drop) when cast from hand,
        // otherwise materialise into the controller losing the game.
        register(LoseGameIfNotCastFromHandEffect.class, (ctx, effect) ->
                ctx.wasCastFromHand() ? null : new TargetPlayerLosesGameEffect(ctx.controllerId()));

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

        // Evoke sacrifice (CR 702.75e) — intervening-if (CR 603.4): resolve to a plain sacrifice
        // when the evoke cost was paid, otherwise drop the trigger entirely.
        register(SacrificeSelfIfEvokedEffect.class, (ctx, effect) ->
                ctx.evoked() ? new SacrificeSelfEffect() : null);

        register(ReturnSelfToHandIfDashCostPaidEffect.class, (ctx, effect) ->
                ctx.alternateCost() && ctx.card().getKeywords().contains(Keyword.DASH)
                        ? new ReturnSelfToHandAtEndStepEffect() : null);

        // "Gain life equal to that creature's toughness" — read toughness at trigger time.
        register(GainLifeEqualToToughnessEffect.class, (ctx, effect) ->
                new GainLifeEffect(ctx.card().getToughness()));

        // Conditional ETB effects: immutable cast-time conditions are evaluated while the trigger
        // is created, gate types (Metalcraft / Morbid / Raid / ControlsAnother) stay wrapped for
        // re-evaluation at stack resolution, and source-untapped clauses are sampled at entry.
        register(ConditionalEffect.class, (ctx, effect) -> {
            ConditionalEffect conditional = (ConditionalEffect) effect;
            Zone sourceZone = ctx.sourcePermanent() == null
                    ? (ctx.wasCastFromHand() ? Zone.HAND : null)
                    : (ctx.sourcePermanent().isCast() ? ctx.sourcePermanent().getCastFromZone() : null);
            boolean collectEvidenceCostPaid = ctx.sourcePermanent() != null
                    && ctx.sourcePermanent().isCollectEvidenceCostPaid();
            ConditionContext conditionContext = new ConditionContext(ctx.controllerId(),
                    ctx.sourcePermanent() == null ? null : ctx.sourcePermanent().getId(),
                    ctx.sourcePermanent(), ctx.card(), ctx.kicked(), false, ctx.prowl(), false, false, false,
                    sourceZone, 0, null, null, false, false, false, null, null, null,
                    ctx.repeatedAdditionalCosts(), ctx.alternateCost(),
                    ctx.sourcePermanent() != null && ctx.sourcePermanent().isSpectacle(),
                    false, collectEvidenceCostPaid, false, 0, false);
            return switch (conditional.condition()) {
                // Kicked intervening-if (CR 603.4): unwrap when kicked, otherwise drop.
                case Kicked ignored -> ctx.kicked() ? conditional.wrapped() : null;
                // Not-kicked ETB clauses use the same cast-time context.
                case NotKicked ignored -> !ctx.kicked() ? conditional.wrapped() : null;
                // Independent additional-kicker clauses are intervening-if conditions whose
                // payment list is snapshotted on the spell's stack entry.
                case RepeatedAdditionalCostPaid paid ->
                        ctx.repeatedAdditionalCosts().contains(paid.manaCost()) ? conditional.wrapped() : null;
                // Prowl intervening-if (CR 603.4): unwrap when the prowl cost was paid, otherwise drop.
                case CastForProwlCost ignored -> ctx.prowl() ? conditional.wrapped() : null;
                case CastForAlternateCost ignored -> ctx.alternateCost() ? conditional.wrapped() : null;
                // Spectacle branch selection is fixed when the permanent enters.
                case CastForSpectacleCost ignored ->
                        ctx.sourcePermanent() != null && ctx.sourcePermanent().isSpectacle()
                                ? conditional.wrapped() : null;
                // Cast-from-hand intervening-if (CR 603.4): unwrap only when cast from hand, otherwise drop.
                case CastFromZone castFromZone ->
                        conditionEvaluationService.isMet(ctx.gameData(), castFromZone, conditionContext)
                                ? conditional.wrapped() : null;
                case EnteredFromZone enteredFromZone ->
                        conditionEvaluationService.isMet(ctx.gameData(), enteredFromZone, conditionContext)
                                ? conditional.wrapped() : null;
                case ColorSpentToCast colorSpent ->
                        conditionEvaluationService.isMet(ctx.gameData(), colorSpent, conditionContext)
                                ? effect : null;
                case SnowManaSpentToCast snowManaSpent ->
                        conditionEvaluationService.isMet(ctx.gameData(), snowManaSpent, conditionContext)
                                ? effect : null;
                case ControllerMainPhase controllerMainPhase ->
                        conditionEvaluationService.isMet(ctx.gameData(), controllerMainPhase, conditionContext)
                                ? conditional.wrapped() : null;
                case SourceUntapped ignored ->
                        conditionEvaluationService.isMet(ctx.gameData(), ignored, conditionContext)
                                ? conditional.wrapped() : null;
                // "if you cast it" is true for a spell cast from any zone, but not for a copy or
                // a permanent put onto the battlefield by an effect.
                case WasCast ignored ->
                        ctx.sourcePermanent() != null
                                ? (ctx.sourcePermanent().isCast() ? conditional.wrapped() : null)
                                : (ctx.wasCastFromHand() ? conditional.wrapped() : null);
                // Intervening-if gates (CR 603.4) — Metalcraft, Morbid, Raid, ControlsAnother: keep
                // the conditional effect when met (re-checked at stack resolution), drop it when not.
                case Condition gate when gate.isEtbTriggerGate() ->
                        conditionEvaluationService.isMet(ctx.gameData(), gate, conditionContext)
                                ? effect : null;
                default -> effect;
            };
        });
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
        return handler != null ? handler.resolve(ctx, effect) : effect;
    }

    private static CardEffect selectedModeEffect(ChooseOneEffect.ChooseOneOption option) {
        return option.effects().size() == 1
                ? option.effects().getFirst()
                : SequenceEffect.of(option.effects().toArray(CardEffect[]::new));
    }
}
