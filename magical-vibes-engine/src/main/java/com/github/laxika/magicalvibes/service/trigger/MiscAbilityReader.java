package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.AttachTrigger;
import com.github.laxika.magicalvibes.model.trigger.ControlChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.DayNightTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlanarTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.StateTrigger;
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
 * Reads the abilities of family 11's second half (the {@link MiscWatch} collectors: control change, attaching, crew,
 * saddle and station, Room doors and full unlocks, day and night and state triggers) and the planar abilities
 * ({@link PlanarTrigger}, read by {@code PlanechaseService}), as {@link WatchedAbilities} finds them; none of them has a
 * legacy view. The collectors judge the ability's trigger-level parts from the ability itself: which door of a Room
 * ({@link MiscEffect#door()}, "when you unlock this door", CR 709.5h), "becomes crewed / saddled for the first time
 * each turn" ({@link MiscEffect#firstTimeEachTurn()}) and "triggers only once each turn" ({@link MiscEffect#limit()},
 * marked by {@link TriggeredAbility#limitKey()} on the source).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) reach the collectors as the
 * {@link ConditionalEffect}s they judge, as the legacy view gave them, until step 15 of effect-slot-refactor.md makes
 * the effects read the event: the intervening "if" is a {@link ConditionalEffect} around each effect, and the event
 * condition the {@link ConditionalEffect#atTriggerTime} inside it. A state trigger (CR 603.8) is read as one
 * {@link StateTriggerEffect} of its game state and its effects ({@link #stateTriggers}).
 */
public final class MiscAbilityReader {

    private MiscAbilityReader() {
    }

    /**
     * One effect of a family 11 ability as its collector reads it.
     *
     * @param effect            the effect, wrapped in the ability's event condition and intervening "if" as the class
     *                          says
     * @param ability           the ability the effect belongs to
     * @param door              the door of the Room the watched condition is on ("when you unlock this door", 0 for
     *                          the left one, 1 for the right one), or {@code null} for every other condition
     * @param firstTimeEachTurn the watched condition is "becomes crewed / saddled for the first time each turn"
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record MiscEffect(CardEffect effect, TriggeredAbility ability, Integer door, boolean firstTimeEachTurn,
                             boolean simultaneousWithPrevious) {

        public MiscEffect {
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

        /**
         * Whether the ability can't trigger for {@code sourceId} now: its limit is reached, or the source has already
         * triggered for "becomes crewed / saddled for the first time each turn" this turn (the unkeyed once-each-turn
         * mark on the source, as the collectors judged it before).
         */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            if (firstTimeEachTurn && gameData.oncePerTurnTriggersFiredThisTurn.contains(sourceId)) {
                return true;
            }
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Marks the ability's limit, or its "for the first time each turn", as used by {@code sourceId}. */
        public void markLimit(GameData gameData, UUID sourceId) {
            if (firstTimeEachTurn) {
                gameData.oncePerTurnTriggersFiredThisTurn.add(sourceId);
            }
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }

        /** This read with {@code effect} in place of its effect. */
        public MiscEffect withEffect(CardEffect effect) {
            return new MiscEffect(effect, ability, door, firstTimeEachTurn, simultaneousWithPrevious);
        }
    }

    /** The abilities of {@code card} that {@code watch} reads, in the order they were added. */
    public static List<TriggeredAbility> printed(Card card, MiscWatch watch) {
        return WatchedAbilities.printed(card, watch);
    }

    /**
     * The effects of the abilities of {@code card} that {@code watch} reads, for a collector that judges no door, first
     * time or limit: none of the abilities it reads is written with one, and this throws on one.
     */
    public static List<CardEffect> printedEffects(Card card, MiscWatch watch) {
        return effects(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of {@code abilities} for the condition watched as {@code watched}, for a collector that judges no
     * door, first time or limit: none of its abilities is written with one, and this throws on one.
     */
    public static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
        List<MiscEffect> effects = read(abilities, watched);
        effects.forEach(effect -> requireNothingToJudge(effect, watched));
        return effects.stream().map(MiscEffect::effect).toList();
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    public static List<MiscEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<MiscEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /**
     * The effects of {@code ability}, as its collector reads it for the condition watched as {@code watched}. These
     * collectors read of the limits only "triggers only once each turn" ("one or more" the ability rejects as it is
     * built), not next to "for the first time each turn", which is a limit of its own, and no trigger mode.
     */
    public static List<MiscEffect> read(TriggeredAbility ability, Trigger watched) {
        Trigger condition = watchedCondition(ability, watched);
        TriggerOptions options = ability.options();
        if (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN) {
            throw new IllegalStateException("No family 11 collector reads " + options.limit() + ": " + ability);
        }
        boolean firstTimeEachTurn = firstTimeEachTurn(condition);
        if (firstTimeEachTurn && options.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("A first-time-each-turn trigger takes no limit: " + ability);
        }
        Integer door = condition instanceof ObjectActionTrigger object ? object.door() : null;
        Condition eventCondition = options.eventCondition();
        Condition interveningIf = options.interveningIf();
        List<MiscEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            if (registration.triggerMode() != TriggerMode.NORMAL) {
                throw new IllegalStateException("The family 11 collectors read no trigger mode ("
                        + registration.triggerMode() + "): " + ability);
            }
            effects.add(new MiscEffect(wrapped(registration.effect(), eventCondition, interveningIf), ability, door,
                    firstTimeEachTurn, registration.simultaneousWithPrevious()));
        }
        return effects;
    }

    /**
     * The state triggers of {@code card} (CR 603.8), its printed abilities with a {@link StateTrigger} that function
     * on the battlefield, each as one {@link StateTriggerEffect} of its game state and all its effects, one stack
     * object (CR 603.3), with no description (the reader names it after the card). The intervening "if" wraps each
     * effect in a {@link ConditionalEffect} the reader checks as the game state triggers the ability and the effect
     * checks again as it resolves (CR 603.4); a state trigger takes no other option.
     */
    public static List<StateTriggerEffect> stateTriggers(Card card) {
        List<StateTriggerEffect> triggers = new ArrayList<>();
        for (TriggeredAbility ability : printed(card, MiscWatch.STATE_TRIGGERED)) {
            StateTrigger state = (StateTrigger) watchedCondition(ability, MiscWatch.STATE_TRIGGERED.trigger());
            Condition interveningIf = ability.options().interveningIf();
            List<CardEffect> effects = new ArrayList<>(ability.effects().size());
            for (EffectRegistration registration : ability.effects()) {
                if (registration.triggerMode() != TriggerMode.NORMAL) {
                    throw new IllegalStateException("The state-trigger reader reads no trigger mode ("
                            + registration.triggerMode() + "): " + ability);
                }
                effects.add(wrapped(registration.effect(), null, interveningIf));
            }
            triggers.add(StateTriggerEffect.of(state, effects));
        }
        return triggers;
    }

    /**
     * Throws when {@code effect}'s ability has a door, a first time or a limit, which the collector of {@code watched}
     * doesn't read.
     */
    public static void requireNothingToJudge(MiscEffect effect, Trigger watched) {
        if (effect.door() != null || effect.firstTimeEachTurn() || effect.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The collector of " + watched + " judges no door, first time or limit: "
                    + effect.ability());
        }
    }

    private static CardEffect wrapped(CardEffect effect, Condition eventCondition, Condition interveningIf) {
        if (eventCondition != null) {
            effect = ConditionalEffect.atTriggerTime(eventCondition, effect);
        }
        if (interveningIf != null) {
            effect = new ConditionalEffect(interveningIf, effect, true);
        }
        return effect;
    }

    /**
     * {@code ability}'s family 11 condition watched as {@code watched} (one whose {@link Trigger#watchKey()} it is).
     * Several are read as one, so they must have the same door and first time.
     */
    private static Trigger watchedCondition(TriggeredAbility ability, Trigger watched) {
        List<Trigger> conditions = ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(MiscAbilityReader::isMiscTrigger)
                .filter(trigger -> trigger.watchKey().equals(watched)).distinct().toList();
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(ability + " has no family 11 condition watched as " + watched);
        }
        Trigger first = conditions.getFirst();
        if (conditions.stream().anyMatch(trigger -> !Objects.equals(door(trigger), door(first))
                || firstTimeEachTurn(trigger) != firstTimeEachTurn(first))) {
            throw new IllegalStateException("The conditions of one ability watched as " + watched
                    + " must have the same door and first time: " + ability);
        }
        return first;
    }

    private static boolean isMiscTrigger(Trigger trigger) {
        return trigger instanceof ControlChangeTrigger || trigger instanceof AttachTrigger
                || trigger instanceof ObjectActionTrigger || trigger instanceof PlayerActionTrigger
                || trigger instanceof DayNightTrigger || trigger instanceof StateTrigger
                || trigger instanceof PlanarTrigger;
    }

    private static Integer door(Trigger trigger) {
        return trigger instanceof ObjectActionTrigger object ? object.door() : null;
    }

    private static boolean firstTimeEachTurn(Trigger trigger) {
        return trigger instanceof ObjectActionTrigger object
                && (object.action() == ObjectActionTrigger.ObjectAction.BECOMES_CREWED_FOR_FIRST_TIME_EACH_TURN
                || object.action() == ObjectActionTrigger.ObjectAction.BECOMES_SADDLED_FOR_FIRST_TIME_EACH_TURN);
    }
}
