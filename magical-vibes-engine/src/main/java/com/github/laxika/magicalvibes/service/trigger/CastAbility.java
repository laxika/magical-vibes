package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;

import java.util.List;
import java.util.Objects;

/**
 * A "whenever [a player] casts a [quality] spell" ability as the generic spell-cast collector judges it
 * ({@link SpellCastTriggerCollectorService#collectCastAbility}): the cast trigger's {@link SpellFilter}, the effects the
 * ability puts on the stack as one object (CR 603.3), as the card wrote them, and its intervening "if" (CR 603.4).
 * <p>
 * A lone plain "you may pay {N}. If you do, …" or "you may …" effect goes on the stack as it is, its question asked as
 * the ability resolves (CR 603.5); the targets of what it offers are chosen as the ability goes on the stack
 * (CR 603.3d), which {@link #effectsTargetsAreChosenFor()} gives the collector.
 *
 * @param filter         what the cast spell must be, and how and when it must have been cast
 * @param effects        the effects the ability puts on the stack
 * @param interveningIf  the intervening "if", or {@code null}; checked as the spell is cast and wrapped around the
 *                       effects for the check as the ability resolves
 * @param eventCondition a condition the cast event itself requires ("whenever you cast a spell <b>while</b> this
 *                       creature is attacking"), or {@code null}; checked only as the spell is cast (CR 603.2)
 * @param targetFilter   what the ability may target, or {@code null} to read it from the target group its effects are
 *                       bound to: the ability's own ({@link TriggeredAbility#targetFilter()}, an emblem's or a granted
 *                       ability's)
 */
public record CastAbility(SpellFilter filter, List<CardEffect> effects, Condition interveningIf,
                          TargetFilter targetFilter, Condition eventCondition) {

    public CastAbility {
        Objects.requireNonNull(filter, "filter");
        effects = List.copyOf(effects);
    }

    /** An ability without an event condition. */
    public CastAbility(SpellFilter filter, List<CardEffect> effects, Condition interveningIf,
                       TargetFilter targetFilter) {
        this(filter, effects, interveningIf, targetFilter, null);
    }

    /**
     * {@code ability}, whose cast trigger has {@code filter}, as the collector judges it. The ability's own target
     * filter, if it has one, is the target filter. The ability's limit is judged by whoever collects it, not here.
     */
    public static CastAbility of(TriggeredAbility ability, SpellFilter filter) {
        return new CastAbility(filter, ability.effects().stream().map(EffectRegistration::effect).toList(),
                ability.options().interveningIf(), ability.targetFilter(), ability.options().eventCondition());
    }

    /**
     * The effects whose targets are chosen as the ability goes on the stack (CR 603.3d): what a lone plain "you may
     * pay {N}. If you do, …" or "you may …" offers (the controller's choice, with no "otherwise"), else the effects. The
     * effects themselves go on the stack unchanged.
     */
    public List<CardEffect> effectsTargetsAreChosenFor() {
        if (effects.size() == 1) {
            CardEffect only = effects.getFirst();
            if (only instanceof MayPayManaEffect pay && isPlainManaPayment(pay)) {
                return List.of(pay.wrapped());
            }
            if (only instanceof MayEffect may && isPlainMay(may)) {
                return List.of(may.wrapped());
            }
        }
        return effects;
    }

    /** This ability with {@code effects} in place of its effects. */
    public CastAbility withEffects(List<CardEffect> effects) {
        return new CastAbility(filter, effects, interveningIf, targetFilter, eventCondition);
    }

    /** This ability with {@code filter} in place of its spell filter (a text change, CR 612). */
    public CastAbility withFilter(SpellFilter filter) {
        return new CastAbility(filter, effects, interveningIf, targetFilter, eventCondition);
    }

    private static boolean isPlainManaPayment(MayPayManaEffect pay) {
        return pay.manaCost() != null && pay.dynamicManaCost() == null && pay.wrapped() != null
                && pay.payer() == MayPayPayer.CONTROLLER
                && pay.elseEffect() == null && pay.lifeCost() == 0 && !pay.sourceIsTriggeringPermanent();
    }

    private static boolean isPlainMay(MayEffect may) {
        return may.elseEffect() == null && may.choicePlayerId() == null
                && may.choicePlayer() == MayChoicePlayer.CONTROLLER;
    }
}
