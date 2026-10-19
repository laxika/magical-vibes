package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.ShrinkEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Static handler for {@link ShrinkEnchantedCreatureEffect}: applies -X to the enchanted creature's
 * power and -Y to its toughness in layer 7c (CR 613.4c), where Y is X clamped so the creature's
 * toughness never drops below 1.
 *
 * <p>The amount is evaluated with the enchanted creature (not the Aura) as the amount source and
 * its controller as the amount controller, which is what the wording of these cards asks for:
 * Snowblind's X counts snow lands the <em>defending</em> player controls while the enchanted
 * creature is attacking and snow lands <em>its controller</em> controls otherwise.
 *
 * <p>The toughness the cap is measured against is the creature's toughness as computed so far in
 * the pass — its own effective toughness (or the characteristic-defining toughness already recorded as the
 * base override) plus the bonuses already in the accumulator — so other
 * pumps count while this effect's own reduction does not. Reading it off the accumulator instead
 * of re-querying {@code getEffectiveToughness} is also what keeps this self-referential value from
 * recursing back into static bonus assembly. When the accumulator has no base override yet, the
 * base is the creature's own characteristic-defining toughness (Lhurgoyf) if it has one and no layer 7b
 * effect sets its base, else its layer 7b toughness read from the layered board, since the raw card
 * toughness is not yet the layered value.
 */
@Component
@RequiredArgsConstructor
public class ShrinkEnchantedCreatureEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShrinkEnchantedCreatureEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var shrink = (ShrinkEnchantedCreatureEffect) effect;
        if (!support.matchesCreatureScope(context, GrantScope.ENCHANTED_CREATURE, null)) {
            return;
        }
        Permanent enchanted = context.target();
        UUID enchantedControllerId = context.gameData().findControllerOf(enchanted);
        AmountContext ctx = AmountContext.forStaticEffect(enchanted, enchantedControllerId);
        int x = Math.max(0, amountEvaluationService.evaluate(context.gameData(), shrink.amount(), ctx));
        int baseToughness = accumulator.isBasePTOverridden() && accumulator.getBaseToughnessOverride() != null
                ? accumulator.getBaseToughnessOverride() : baseToughnessWithoutOverride(enchanted, ctx, context);
        int toughnessSoFar = baseToughness + accumulator.getToughness();
        int y = Math.min(x, toughnessSoFar - 1);
        accumulator.addPower(-x);
        accumulator.addToughness(-y);
    }

    /**
     * The enchanted creature's toughness before layer 7c when the accumulator holds no base override yet.
     * A creature's own characteristic-defining toughness (Lhurgoyf) is applied after other permanents'
     * effects in the assembly, so it is evaluated here unless a layer 7b effect sets the base instead.
     */
    private int baseToughnessWithoutOverride(Permanent enchanted, AmountContext ctx, StaticEffectContext context) {
        var pass = LayerSystemService.activePass();
        var board = pass == null ? null : pass.board();
        var layeredBase = board == null ? null : board.basePt7b().get(enchanted.getId());
        if (layeredBase == null || layeredBase.toughness() == null) {
            for (CardEffect cardEffect : enchanted.getCard().getEffects(EffectSlot.STATIC)) {
                if (cardEffect instanceof SetPowerToughnessToAmountEffect characteristic) {
                    return amountEvaluationService.evaluate(context.gameData(), characteristic.toughness(), ctx)
                            + enchanted.getToughnessModifiers();
                }
            }
        }
        return gameQueryService.toughnessFromStaticBoard(enchanted);
    }
}
