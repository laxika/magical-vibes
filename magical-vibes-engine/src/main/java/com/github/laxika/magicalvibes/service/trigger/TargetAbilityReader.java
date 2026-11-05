package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.TargetTrigger;
import com.github.laxika.magicalvibes.model.trigger.TargetTrigger.TargetEvent;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Reads the becomes-the-target abilities (a {@link TargetTrigger}: a player or permanent becomes the target of a spell
 * or ability, CR 601.2c, CR 602.2b, CR 603.3d, or a player chooses targets, CR 115.1), as {@link WatchedAbilities}
 * finds them, for the {@link TargetWatch} collectors, which have no legacy view of them
 *. The collectors judge the ability's trigger-level parts from the ability
 * itself: the further restriction on the permanent that becomes the target ({@link TargetEffect#filter()}, the
 * trigger's {@link TargetTrigger#qualifier()}), the event a collector of another event reads
 * ({@link TargetEffect#byController()}: "a spell [or ability] you control", "an ability you control") and the limit
 * ({@link TargetEffect#limit()}, marked by {@link TriggeredAbility#limitKey()} on the source).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) reach the collectors as the
 * {@link ConditionalEffect}s they judge (the ward collector checks the intervening "if" as the ability triggers), until
 * step 15 of effect-slot-refactor.md makes the effects read the event: the intervening "if" is a
 * {@link ConditionalEffect} around each effect, checked as the ability triggers and again as it resolves, and the event
 * condition the {@link ConditionalEffect#atTriggerTime} inside it, checked only as it triggers.
 */
public final class TargetAbilityReader {

    private TargetAbilityReader() {
    }

    /**
     * One effect of a becomes-the-target ability as its collector reads it.
     *
     * @param effect  the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param mode    the registration's trigger mode
     * @param ability the ability the effect belongs to
     * @param filter  what the permanent that becomes the target must also be for the watched condition (its
     *                {@link TargetTrigger#qualifier()}), or {@code null}
     * @param event   the watched condition's own event, which may be narrower than the watch's
     *                ({@link TargetTrigger#watchedEvent()})
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record TargetEffect(CardEffect effect, TriggerMode mode, TriggeredAbility ability,
                               PermanentPredicate filter, TargetEvent event, boolean simultaneousWithPrevious) {

        public TargetEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(mode, "mode");
            Objects.requireNonNull(ability, "ability");
            Objects.requireNonNull(event, "event");
        }

        public TriggerLimit limit() {
            return ability.options().limit();
        }

        /**
         * Whether only a spell or ability the ability's controller controls triggers it, read by the collector of the
         * event about any controller: "this becomes the target of a spell you control", "of a spell or ability you
         * control", "a player or permanent becomes the target of an ability you control".
         */
        public boolean byController() {
            return event == TargetEvent.BECOMES_TARGET_OF_YOUR_SPELL
                    || event == TargetEvent.BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY
                    || event == TargetEvent.PLAYER_OR_PERMANENT_BECOMES_TARGET_OF_YOUR_ABILITY;
        }

        /**
         * Whether the ability's limit keeps it from triggering for {@code sourceId} now: "triggers only once each
         * turn" ("for the first time each turn" included) and "this ability triggers only once", as
         * {@link TriggeredAbility#limitKey()} marked them.
         */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Marks the ability's limit as used by {@code sourceId}. */
        public void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }
    }

    /** The abilities of {@code card} that {@code watch} reads, in the order they were added. */
    public static List<TriggeredAbility> printed(Card card, TargetWatch watch) {
        return WatchedAbilities.printed(card, watch);
    }

    /** Every effect of the abilities of {@code card} that {@code watch} reads, as their collector reads them. */
    public static List<TargetEffect> readPrinted(Card card, TargetWatch watch) {
        return read(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of the abilities of {@code card} that {@code watch} reads, for a collector that reads no filter,
     * narrower event or limit: none of the abilities it reads is written with one, and this throws on one.
     */
    public static List<CardEffect> printedEffects(Card card, TargetWatch watch) {
        return effects(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of the abilities granted to {@code permanent} for a duration (until end of turn, and until it
     * leaves) that {@code watch} reads, for a collector that reads no filter, narrower event or limit (this throws on
     * one).
     */
    public static List<CardEffect> grantedToPermanentEffects(Permanent permanent, TargetWatch watch) {
        List<TriggeredAbility> abilities = new ArrayList<>(WatchedAbilities.temporary(permanent, watch.trigger()));
        abilities.addAll(WatchedAbilities.persistent(permanent, watch.trigger()));
        return effects(abilities, watch.trigger());
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    public static List<TargetEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<TargetEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /**
     * The effects of {@code ability}, as its collector reads it for the condition watched as {@code watched}. The
     * becomes-the-target collectors trigger once for each targeting spell or ability and each permanent it targets, or
     * once for each ability for "one or more creatures you control become the target of an activated ability", by the
     * event; they read no multiplicity, and of the limits "triggers only once each turn" and "this ability triggers
     * only once".
     */
    public static List<TargetEffect> read(TriggeredAbility ability, Trigger watched) {
        TargetTrigger condition = watchedCondition(ability, watched);
        TriggerOptions options = ability.options();
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("No becomes-the-target collector reads " + options.multiplicity() + ": "
                    + ability);
        }
        if (options.limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN
                || options.limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            throw new IllegalStateException("No becomes-the-target collector reads " + options.limit() + ": "
                    + ability);
        }
        Condition eventCondition = options.eventCondition();
        Condition interveningIf = options.interveningIf();
        List<TargetEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (eventCondition != null) {
                effect = ConditionalEffect.atTriggerTime(eventCondition, effect);
            }
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect, true);
            }
            effects.add(new TargetEffect(effect, registration.triggerMode(), ability, condition.qualifier(),
                    condition.event(), registration.simultaneousWithPrevious()));
        }
        return effects;
    }

    /**
     * The effects of {@code abilities} for the condition watched as {@code watched}, for a collector that reads no
     * filter, narrower event or limit: none of its abilities is written with one, and this throws on one.
     */
    public static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
        List<TargetEffect> effects = read(abilities, watched);
        effects.forEach(effect -> requireNothingToJudge(effect, watched));
        return effectsOf(effects);
    }

    /** The effects of {@code effects}, in order. */
    public static List<CardEffect> effectsOf(List<TargetEffect> effects) {
        return effects.stream().map(TargetEffect::effect).toList();
    }

    /**
     * Throws when {@code effect}'s ability has a filter, a narrower event or a limit, which the collector of
     * {@code watched} doesn't read.
     */
    public static void requireNothingToJudge(TargetEffect effect, Trigger watched) {
        requireNoFilter(effect, watched);
        requireWatchedEvent(effect, watched);
        requireNoLimit(effect, watched);
    }

    /** Throws when {@code effect}'s ability has a filter, which the collector of {@code watched} doesn't read. */
    public static void requireNoFilter(TargetEffect effect, Trigger watched) {
        if (effect.filter() != null) {
            throw new IllegalStateException("The collector of " + watched + " judges no filter: " + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability has an event narrower than {@code watched}'s ("a spell you control"),
     * which the collector of {@code watched} doesn't read.
     */
    public static void requireWatchedEvent(TargetEffect effect, Trigger watched) {
        if (effect.event() != ((TargetTrigger) watched).event()) {
            throw new IllegalStateException("The collector of " + watched + " reads no " + effect.event() + ": "
                    + effect.ability());
        }
    }

    /** Throws when {@code effect}'s ability has a limit, which the collector of {@code watched} doesn't read. */
    public static void requireNoLimit(TargetEffect effect, Trigger watched) {
        if (effect.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The collector of " + watched + " judges no limit: " + effect.ability());
        }
    }

    /**
     * {@code ability}'s target condition watched as {@code watched}: the one whose {@link Trigger#watchKey()} it is.
     * Two conditions of one ability the same watch reads with a different filter or event ("a spell you control" and
     * "a spell") would be read as one, so they are rejected.
     */
    private static TargetTrigger watchedCondition(TriggeredAbility ability, Trigger watched) {
        List<TargetTrigger> conditions = ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(TargetTrigger.class::isInstance).map(TargetTrigger.class::cast)
                .filter(target -> target.watchKey().equals(watched)).distinct().toList();
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(ability + " has no becomes-the-target condition watched as " + watched);
        }
        if (conditions.size() > 1) {
            throw new IllegalStateException("The becomes-the-target conditions of one ability watched as " + watched
                    + " must read alike: " + ability);
        }
        return conditions.getFirst();
    }
}
