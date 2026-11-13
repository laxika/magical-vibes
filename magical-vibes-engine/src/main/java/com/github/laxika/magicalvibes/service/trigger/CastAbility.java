package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Objects;

/**
 * A "whenever [a player] casts a [quality] spell" ability as the generic spell-cast collector judges it
 * ({@link SpellCastTriggerCollectorService#collectCastAbility}): the cast trigger's {@link SpellFilter}, which the
 * collector judges, and the ability itself, which it hands whole to the stack door.
 *
 * @param filter         what the cast spell must be, and how and when it must have been cast
 * @param effects        the effects the ability puts on the stack, as the card wrote them
 * @param interveningIf  the intervening "if", or {@code null}; the stack door judges it as the spell is cast and
 *                       again as the ability resolves (CR 603.4)
 * @param targetFilter   what the ability may target, or {@code null} for the target group its effects are bound to
 * @param eventCondition a condition the cast event itself requires ("whenever you cast a spell <b>while</b> this
 *                       creature is attacking"), or {@code null}; checked only as the spell is cast (CR 603.2)
 * @param ability        the ability, with those effects and options
 */
public record CastAbility(SpellFilter filter, List<CardEffect> effects, Condition interveningIf,
                          TargetFilter targetFilter, Condition eventCondition, TriggeredAbility ability) {

    public CastAbility {
        Objects.requireNonNull(filter, "filter");
        effects = List.copyOf(effects);
        if (ability == null) {
            TriggerOptions options = TriggerOptions.DEFAULT;
            if (interveningIf != null) {
                options = options.withInterveningIf(interveningIf);
            }
            if (eventCondition != null) {
                options = options.withEventCondition(eventCondition);
            }
            TriggeredAbility authored = new TriggeredAbility(Triggers.youCastSpell(), options,
                    effects.toArray(CardEffect[]::new));
            ability = targetFilter == null ? authored : authored.withTargetFilter(targetFilter);
        }
    }

    /** An ability of {@code effects} without an event condition. */
    public CastAbility(SpellFilter filter, List<CardEffect> effects, Condition interveningIf,
                       TargetFilter targetFilter) {
        this(filter, effects, interveningIf, targetFilter, null, null);
    }

    /** An ability of {@code effects}. */
    public CastAbility(SpellFilter filter, List<CardEffect> effects, Condition interveningIf,
                       TargetFilter targetFilter, Condition eventCondition) {
        this(filter, effects, interveningIf, targetFilter, eventCondition, null);
    }

    /**
     * {@code ability}, whose cast trigger has {@code filter}, as the collector judges it. The ability's own target
     * filter, if it has one, is the target filter. Its limit is judged by the stack door.
     */
    public static CastAbility of(TriggeredAbility ability, SpellFilter filter) {
        return new CastAbility(filter, ability.effects().stream().map(EffectRegistration::effect).toList(),
                ability.options().interveningIf(), ability.targetFilter(), ability.options().eventCondition(),
                ability);
    }

    /** This ability with {@code filter} in place of its spell filter (a text change, CR 612). */
    public CastAbility withFilter(SpellFilter filter) {
        return new CastAbility(filter, effects, interveningIf, targetFilter, eventCondition, ability);
    }
}
