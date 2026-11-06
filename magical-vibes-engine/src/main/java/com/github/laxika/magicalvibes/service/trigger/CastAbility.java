package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;

import java.util.List;
import java.util.Objects;

/**
 * A "whenever [a player] casts a [quality] spell" ability as the generic spell-cast collector judges it
 * ({@link SpellCastTriggerCollectorService#collectCastAbility}): the cast trigger's {@link SpellFilter}, the effects the
 * ability puts on the stack as one object (CR 603.3), its intervening "if" (CR 603.4) and its optional part.
 * <p>
 * A lone plain "you may pay {N}. If you do, …" or "you may …" effect is the optional part, so the collector can choose
 * the targets of what it wraps as the ability triggers (CR 603.3d) and ask as the ability resolves (CR 603.5): the cost
 * is {@link #manaCost}, the prompt {@link #optionalPrompt} and the wrapped effect the ability's effect.
 *
 * @param filter         what the cast spell must be, and how and when it must have been cast
 * @param effects        the effects the ability puts on the stack, the optional part taken off
 * @param interveningIf  the intervening "if", or {@code null}; checked as the spell is cast and wrapped around the
 *                       effects for the check as the ability resolves
 * @param eventCondition a condition the cast event itself requires ("whenever you cast a spell <b>while</b> this
 *                       creature is attacking"), or {@code null}; checked only as the spell is cast (CR 603.2)
 * @param manaCost       the mana a "you may pay" asks for, or {@code null}
 * @param optionalPrompt the prompt of the "you may [pay]", or {@code null} for an ability that isn't optional (a mana
 *                       cost without a prompt is asked with "Pay {N}?")
 * @param targetFilter   what the ability may target, or {@code null} to read it from the target group its effects are
 *                       bound to; only an authored {@link SpellCastTriggerEffect} names one
 */
public record CastAbility(SpellFilter filter, List<CardEffect> effects, Condition interveningIf, String manaCost,
                          String optionalPrompt, TargetFilter targetFilter, Condition eventCondition) {

    public CastAbility {
        Objects.requireNonNull(filter, "filter");
        effects = List.copyOf(effects);
    }

    /** An ability without an event condition. */
    public CastAbility(SpellFilter filter, List<CardEffect> effects, Condition interveningIf, String manaCost,
                       String optionalPrompt, TargetFilter targetFilter) {
        this(filter, effects, interveningIf, manaCost, optionalPrompt, targetFilter, null);
    }

    /**
     * {@code ability}, whose cast trigger has {@code filter}, as the collector judges it. {@code onStackPath} says
     * whether a lone plain "you may" is the optional part: on the battlefield and in exile it is; the graveyard, hand
     * and command-zone readers take it as an effect of the ability. The ability's limit is judged by whoever collects
     * it, not here.
     */
    public static CastAbility of(TriggeredAbility ability, SpellFilter filter, boolean onStackPath) {
        List<CardEffect> effects = ability.effects().stream().map(EffectRegistration::effect).toList();
        String manaCost = null;
        String optionalPrompt = null;
        if (effects.size() == 1) {
            CardEffect only = effects.getFirst();
            if (only instanceof MayPayManaEffect pay && isPlainManaPayment(pay)) {
                manaCost = pay.manaCost();
                effects = List.of(pay.wrapped());
                optionalPrompt = onStackPath ? pay.prompt() : null;
            } else if (onStackPath && only instanceof MayEffect may && isPlainMay(may)) {
                effects = List.of(may.wrapped());
                optionalPrompt = may.prompt();
            }
        }
        return new CastAbility(filter, effects, ability.options().interveningIf(), manaCost, optionalPrompt, null,
                ability.options().eventCondition());
    }

    /**
     * An authored {@link SpellCastTriggerEffect} (a kept granted ability, an emblem's) as the collector judges it,
     * with the prompt of the "you may" {@code optional} around it, or {@code null}.
     */
    public static CastAbility of(SpellCastTriggerEffect trigger, MayEffect optional) {
        SpellFilter.Turn turn = trigger.onlyDuringOpponentTurn() ? SpellFilter.Turn.OPPONENTS
                : trigger.onlyDuringControllerTurn() ? SpellFilter.Turn.YOURS
                : trigger.onlyWhenCasterNotActiveTurn() ? SpellFilter.Turn.NOT_CASTERS
                : SpellFilter.Turn.ANY;
        SpellFilter.Kicker kicker = !trigger.kicked() ? SpellFilter.Kicker.ANY
                : trigger.triggerPerKick() ? SpellFilter.Kicker.EACH_KICK : SpellFilter.Kicker.KICKED;
        SpellFilter filter = new SpellFilter(trigger.spellFilter(), trigger.castSpellTargetCondition(), turn,
                trigger.onlyDuringCombat(), trigger.nthSpellNumber(), trigger.minimumSpellNumber(),
                trigger.requiresManaProducedBySource(), trigger.requiresTreasureMana(),
                trigger.requiresTwoOrMoreCardTypes(), trigger.expendThreshold(), trigger.castFrom(),
                trigger.notCastFrom(), kicker, trigger.countEveryPlayersSpells());
        return new CastAbility(filter, trigger.resolvedEffects(), trigger.intervening(), trigger.manaCost(),
                optional == null ? null : optional.prompt(), trigger.targetFilter());
    }

    /** This ability with {@code effects} in place of its effects. */
    public CastAbility withEffects(List<CardEffect> effects) {
        return new CastAbility(filter, effects, interveningIf, manaCost, optionalPrompt, targetFilter, eventCondition);
    }

    /** This ability with {@code filter} in place of its spell filter (a text change, CR 612). */
    public CastAbility withFilter(SpellFilter filter) {
        return new CastAbility(filter, effects, interveningIf, manaCost, optionalPrompt, targetFilter, eventCondition);
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
