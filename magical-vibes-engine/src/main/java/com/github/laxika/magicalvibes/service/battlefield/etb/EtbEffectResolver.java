package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import org.springframework.stereotype.Component;

/**
 * Judges the conditions of an entering permanent's own "when this enters" abilities with the facts fixed as it
 * entered: how it was cast (kicked, the zone it was cast from, an alternative or additional cost paid, the mana spent),
 * its X, and an Aura's enchanted object. The collector judges an ability's event condition (CR 603.2) with
 * {@link #ownEntryConditionMet}; the stack door judges its intervening "if" (CR 603.4) and its effects' facts as it
 * triggers ({@code CardEffect.asItTriggersOnEntering}) in {@link #entryContext}.
 */
@Component
public class EtbEffectResolver {

    private final ConditionEvaluationService conditionEvaluationService;

    public EtbEffectResolver(ConditionEvaluationService conditionEvaluationService) {
        this.conditionEvaluationService = conditionEvaluationService;
    }

    /**
     * Whether a "when this enters" ability whose event condition (CR 603.2) is {@code condition} triggers as the
     * permanent enters, judged with the entering permanent's cast-time facts; a condition on the ability's target,
     * which isn't chosen yet (CR 603.3d), is judged as it resolves, but an Aura's, whose enchanted object is known as it
     * enters.
     */
    public boolean ownEntryConditionMet(EtbEffectContext ctx, Condition condition) {
        if (condition instanceof TargetPermanentMatches && !(ctx.targetId() != null && ctx.card().isAura())) {
            return true;
        }
        return conditionEvaluationService.isMet(ctx.gameData(), condition, entryContext(ctx));
    }

    /** The context the conditions of {@code ctx}'s entering permanent are read in, with its cast-time facts. */
    public ConditionContext entryContext(EtbEffectContext ctx) {
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
}
