package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromZoneConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
 * <p><b>Gate vs. unwrap asymmetry (preserved deliberately):</b> Metalcraft / Morbid / Raid /
 * ControlsAnother gates return the <em>conditional effect unchanged</em> when met (it stays wrapped
 * and is re-evaluated at stack resolution by {@code EffectResolutionService.evaluateCondition}),
 * whereas Kicked / CastFromZone <em>unwrap</em> to their inner effect. Dropping ({@code null}) applies
 * the intervening-if rule (CR 603.4): the ability never goes on the stack.
 */
@Component
public class EtbEffectResolver {

    private final Map<Class<? extends CardEffect>, EtbEffectHandler> handlers = new HashMap<>();

    public EtbEffectResolver(GameQueryService gameQueryService) {
        // "you lose the game unless this was cast from your hand" — no-op (drop) when cast from hand,
        // otherwise materialise into the controller losing the game.
        register(LoseGameIfNotCastFromHandEffect.class, (ctx, effect) ->
                ctx.wasCastFromHand() ? null : new TargetPlayerLosesGameEffect(ctx.controllerId()));

        // Modal ETB (choose one): unwrap the option picked at cast time (etbMode), defaulting to the
        // first option when the index is out of range.
        register(ChooseOneEffect.class, (ctx, effect) -> {
            ChooseOneEffect coe = (ChooseOneEffect) effect;
            if (ctx.etbMode() >= 0 && ctx.etbMode() < coe.options().size()) {
                return coe.options().get(ctx.etbMode()).effect();
            }
            return coe.options().getFirst().effect();
        });

        // Kicked intervening-if (CR 603.4): unwrap when kicked, otherwise drop.
        register(KickedConditionalEffect.class, (ctx, effect) ->
                ctx.kicked() ? ((KickedConditionalEffect) effect).wrapped() : null);

        // Cast-from-hand intervening-if (CR 603.4): unwrap only when cast from hand, otherwise drop.
        register(CastFromZoneConditionalEffect.class, (ctx, effect) -> {
            CastFromZoneConditionalEffect cfhce = (CastFromZoneConditionalEffect) effect;
            return ctx.wasCastFromHand() && cfhce.sourceZone() == Zone.HAND ? cfhce.wrapped() : null;
        });

        // "Gain life equal to that creature's toughness" — read toughness at trigger time.
        register(GainLifeEqualToToughnessEffect.class, (ctx, effect) ->
                new GainLifeEffect(ctx.card().getToughness()));

        // Intervening-if gates (CR 603.4): keep the conditional effect when met (re-checked at stack
        // resolution), drop it when not.
        register(MetalcraftConditionalEffect.class, (ctx, effect) ->
                gameQueryService.isMetalcraftMet(ctx.gameData(), ctx.controllerId()) ? effect : null);
        register(MorbidConditionalEffect.class, (ctx, effect) ->
                gameQueryService.isMorbidMet(ctx.gameData()) ? effect : null);
        register(ControlsAnotherPermanentConditionalEffect.class, (ctx, effect) -> {
            ControlsAnotherPermanentConditionalEffect capc = (ControlsAnotherPermanentConditionalEffect) effect;
            return gameQueryService.controlsAnotherPermanent(ctx.gameData(), ctx.controllerId(), ctx.card(), capc.filter())
                    ? effect : null;
        });
        register(RaidConditionalEffect.class, (ctx, effect) ->
                ctx.gameData().playersDeclaredAttackersThisTurn.contains(ctx.controllerId()) ? effect : null);
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
}
