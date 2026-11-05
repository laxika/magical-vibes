package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Reads the player and keyword action abilities (a {@link PlayerActionTrigger} or {@link ObjectActionTrigger} that is
 * {@link Trigger#collectedAsAbility()}: dice, coin flips, crimes, the Ring, the monarch, dungeons, voting, investigate,
 * explore, connive, exploit, clash, mutate and the other keyword actions), as {@link WatchedAbilities} finds them, for
 * the {@link ActionWatch} collectors, which have no legacy view of them. The collectors judge the ability's
 * trigger-level parts from the ability itself: what the card the action is taken with must be
 * ({@link ActionEffect#filter()}: the explored card, the exploited creature, the scheme set in motion) and "triggers
 * only once each turn" ({@link ActionEffect#limit()}, marked by {@link TriggeredAbility#limitKey()} on the source).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) reach the collectors as the
 * {@link ConditionalEffect}s they judge, until step 15 of effect-slot-refactor.md makes the effects read the event: the
 * intervening "if" is a {@link ConditionalEffect} around each effect, and the event condition the
 * {@link ConditionalEffect#atTriggerTime} inside it.
 */
public final class ActionAbilityReader {

    private ActionAbilityReader() {
    }

    /**
     * One effect of a player or keyword action ability as its collector reads it.
     *
     * @param effect  the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param ability the ability the effect belongs to
     * @param filter  what the card the action is taken with must be for the watched condition, or {@code null} for
     *                any card. The collector judges it with the ability's source as the filter's source
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record ActionEffect(CardEffect effect, TriggeredAbility ability, CardPredicate filter,
                               boolean simultaneousWithPrevious) {

        public ActionEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(ability, "ability");
        }

        public TriggerLimit limit() {
            return ability.options().limit();
        }

        /** Whether the ability "triggers only once each turn", so nothing makes it trigger additional times. */
        public boolean triggersOnlyOnceEachTurn() {
            return limit() == TriggerLimit.ONCE_EACH_TURN;
        }

        /** Whether the ability's limit keeps it from triggering for {@code sourceId} now. */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Marks the ability's limit as used by {@code sourceId}. */
        public void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }

        /** This read with {@code effect} in place of its effect. */
        public ActionEffect withEffect(CardEffect effect) {
            return new ActionEffect(effect, ability, filter, simultaneousWithPrevious);
        }
    }

    /** The abilities of {@code card} that {@code watch} reads, in the order they were added. */
    public static List<TriggeredAbility> printed(Card card, ActionWatch watch) {
        return WatchedAbilities.printed(card, watch);
    }

    /**
     * The effects of the abilities of {@code card} that {@code watch} reads, for a collector that judges no filter or
     * limit: none of the abilities it reads is written with one, and this throws on one.
     */
    public static List<CardEffect> printedEffects(Card card, ActionWatch watch) {
        return effects(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of {@code abilities} for the condition watched as {@code watched}, for a collector that judges no
     * filter or limit: none of its abilities is written with one, and this throws on one.
     */
    public static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
        List<ActionEffect> effects = read(abilities, watched);
        effects.forEach(effect -> requireNothingToJudge(effect, watched));
        return effects.stream().map(ActionEffect::effect).toList();
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    public static List<ActionEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<ActionEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /**
     * The effects of {@code ability}, as its collector reads it for the condition watched as {@code watched}. The
     * action collectors read of the limits only "triggers only once each turn" ("one or more" the ability rejects as it
     * is built), and no trigger mode. A "you set a scheme in motion" ability takes no options: its collector puts all
     * its effects on the stack as one object and judges only the scheme.
     */
    public static List<ActionEffect> read(TriggeredAbility ability, Trigger watched) {
        Trigger condition = watchedCondition(ability, watched);
        TriggerOptions options = ability.options();
        if (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN) {
            throw new IllegalStateException("No player or keyword action collector reads " + options.limit() + ": "
                    + ability);
        }
        if (isScheme(condition) && !options.equals(TriggerOptions.DEFAULT)) {
            throw new IllegalStateException("The scheme collector reads no options: " + ability);
        }
        CardPredicate filter = filter(condition);
        Condition eventCondition = options.eventCondition();
        Condition interveningIf = options.interveningIf();
        List<ActionEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            if (registration.triggerMode() != TriggerMode.NORMAL) {
                throw new IllegalStateException("The player and keyword action collectors read no trigger mode ("
                        + registration.triggerMode() + "): " + ability);
            }
            CardEffect effect = registration.effect();
            if (eventCondition != null) {
                effect = ConditionalEffect.atTriggerTime(eventCondition, effect);
            }
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect, true);
            }
            effects.add(new ActionEffect(effect, ability, filter, registration.simultaneousWithPrevious()));
        }
        return effects;
    }

    /** Throws when {@code effect}'s ability has a filter or a limit, which the collector of {@code watched} doesn't read. */
    public static void requireNothingToJudge(ActionEffect effect, Trigger watched) {
        requireNoFilter(effect, watched);
        if (effect.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The collector of " + watched + " judges no limit: " + effect.ability());
        }
    }

    /** Throws when {@code effect}'s ability has a filter, which the collector of {@code watched} doesn't read. */
    public static void requireNoFilter(ActionEffect effect, Trigger watched) {
        if (effect.filter() != null) {
            throw new IllegalStateException("The collector of " + watched + " judges no filter: " + effect.ability());
        }
    }

    /**
     * {@code ability}'s action condition watched as {@code watched} (one whose {@link Trigger#watchKey()} it is).
     * Several are read as one, so they must have the same filter.
     */
    private static Trigger watchedCondition(TriggeredAbility ability, Trigger watched) {
        List<Trigger> conditions = ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(trigger -> trigger instanceof PlayerActionTrigger || trigger instanceof ObjectActionTrigger)
                .filter(trigger -> trigger.watchKey().equals(watched)).distinct().toList();
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(ability + " has no player or keyword action condition watched as "
                    + watched);
        }
        Trigger first = conditions.getFirst();
        if (conditions.stream().anyMatch(trigger -> !Objects.equals(filter(trigger), filter(first)))) {
            throw new IllegalStateException("The action conditions of one ability watched as " + watched
                    + " must have the same filter: " + ability);
        }
        return first;
    }

    private static boolean isScheme(Trigger trigger) {
        return trigger instanceof PlayerActionTrigger action
                && action.action() == PlayerActionTrigger.PlayerAction.SETS_SCHEME_IN_MOTION;
    }

    private static CardPredicate filter(Trigger trigger) {
        return switch (trigger) {
            case PlayerActionTrigger action -> action.filter();
            case ObjectActionTrigger object -> object.filter();
            default -> null;
        };
    }
}
