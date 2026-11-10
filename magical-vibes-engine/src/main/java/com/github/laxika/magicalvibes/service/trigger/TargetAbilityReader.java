package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
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
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) are the ability's options: its collector judges
 * them as the ability triggers, in the context of its event, from the records' {@code ability()} or through a
 * {@link TriggerJudge} on the abilities an effect list is read from. The reader wraps the intervening "if" around each
 * effect as the {@link ConditionalEffect} that checks it again as the ability resolves; the event condition is never
 * checked again.
 */
public final class TargetAbilityReader {

    /**
     * The watched trigger of the one collector that reads "one or more" ({@link TargetEffect#oneOrMore()}): "you and/or
     * at least one permanent you control becomes the target".
     */
    private static final Trigger ONE_OR_MORE_READER =
            TargetWatch.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.trigger();

    /** The watched trigger of the one collector that reads a {@link TargetTrigger#source()}. */
    private static final Trigger SOURCE_READER =
            TargetWatch.ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY.trigger();

    private TargetAbilityReader() {
    }

    /**
     * One effect of a becomes-the-target ability as its collector reads it.
     *
     * @param effect  the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param ability the ability the effect belongs to
     * @param filter  what the permanent that becomes the target must also be for the watched condition (its
     *                {@link TargetTrigger#qualifier()}), or {@code null}
     * @param event   the watched condition's own event, which may be narrower than the watch's
     *                ({@link TargetTrigger#watchedEvent()})
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     * @param source  what the permanent whose ability does the targeting must be (its {@link TargetTrigger#source()}),
     *                or {@code null}; only the "a creature becomes the target of a spell or ability" collector reads it
     */
    public record TargetEffect(CardEffect effect, TriggeredAbility ability,
                               PermanentPredicate filter, TargetEvent event, boolean simultaneousWithPrevious,
                               PermanentPredicate source) {

        public TargetEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(ability, "ability");
            Objects.requireNonNull(event, "event");
        }

        public TriggerLimit limit() {
            return ability.options().limit();
        }

        /**
         * "Whenever you and/or at least one permanent you control becomes the target of a spell or ability" (Leyline of
         * Combustion): once for the spell or ability however many of them it targets, where the default triggers once
         * for each such target (CR 603.2c).
         */
        public boolean oneOrMore() {
            return ability.options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
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

    /** {@link #printedEffects(Card, TargetWatch)} of the abilities {@code judge} lets trigger. */
    public static List<CardEffect> printedEffects(Card card, TargetWatch watch, TriggerJudge judge) {
        return effects(judge.filter(printed(card, watch)), watch.trigger());
    }

    /**
     * The effects of the abilities granted to {@code permanent} for a duration (until end of turn, and until it
     * leaves) that {@code watch} reads, for a collector that reads no filter, narrower event or limit (this throws on
     * one).
     */
    public static List<CardEffect> grantedToPermanentEffects(Permanent permanent, TargetWatch watch) {
        return grantedToPermanentEffects(permanent, watch, TriggerJudge.NONE);
    }

    /** {@link #grantedToPermanentEffects(Permanent, TargetWatch)} of the abilities {@code judge} lets trigger. */
    public static List<CardEffect> grantedToPermanentEffects(Permanent permanent, TargetWatch watch,
                                                             TriggerJudge judge) {
        List<TriggeredAbility> abilities = new ArrayList<>(WatchedAbilities.temporary(permanent, watch.trigger()));
        abilities.addAll(WatchedAbilities.persistent(permanent, watch.trigger()));
        return effects(judge.filter(abilities), watch.trigger());
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
     * The effects of {@code abilities}, one list for each ability that has any, in order, as their collector reads them
     * for the condition watched as {@code watched}: each list is one object on the stack (CR 603.3), whose
     * trigger-level parts (filter, event, source, limit) every read of it shares. Two equal abilities, or one ability
     * a permanent has twice (its own ward and a granted one), are two lists.
     */
    public static List<List<TargetEffect>> readByAbility(List<TriggeredAbility> abilities, Trigger watched) {
        List<List<TargetEffect>> byAbility = new ArrayList<>(abilities.size());
        for (TriggeredAbility ability : abilities) {
            List<TargetEffect> effects = read(ability, watched);
            if (!effects.isEmpty()) {
                byAbility.add(effects);
            }
        }
        return byAbility;
    }

    /** {@link #readByAbility(List, Trigger)} of the abilities of {@code card} that {@code watch} reads. */
    public static List<List<TargetEffect>> readPrintedByAbility(Card card, TargetWatch watch) {
        return readByAbility(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of {@code ability}, as its collector reads it for the condition watched as {@code watched}. The
     * becomes-the-target collectors trigger once for each targeting spell or ability and each permanent it targets, or
     * once for each ability for "one or more creatures you control become the target of an activated ability", by the
     * event; of the multiplicities only the "you or a permanent you control becomes the target" collector reads one
     * ("one or more": "you and/or at least one permanent you control"), and of the limits they read "triggers only once
     * each turn" and "this ability triggers only once".
     */
    public static List<TargetEffect> read(TriggeredAbility ability, Trigger watched) {
        TargetTrigger condition = watchedCondition(ability, watched);
        TriggerOptions options = ability.options();
        if (options.multiplicity() != TriggerMultiplicity.EACH
                && !(options.multiplicity() == TriggerMultiplicity.ONE_OR_MORE && watched.equals(ONE_OR_MORE_READER))) {
            throw new IllegalStateException("The becomes-the-target collector of " + watched + " reads no "
                    + options.multiplicity() + ": " + ability);
        }
        if (options.limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN
                || options.limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            throw new IllegalStateException("No becomes-the-target collector reads " + options.limit() + ": "
                    + ability);
        }
        if (condition.source() != null && !watched.equals(SOURCE_READER)) {
            throw new IllegalStateException("Only the collector of " + SOURCE_READER + " reads a source: " + ability);
        }
        Condition interveningIf = options.interveningIf();
        List<TargetEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            effects.add(new TargetEffect(effect, ability, condition.qualifier(),
                    condition.event(), registration.simultaneousWithPrevious(), condition.source()));
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
