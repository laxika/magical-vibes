package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnPerCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.AttachTrigger;
import com.github.laxika.magicalvibes.model.trigger.ChapterTrigger;
import com.github.laxika.magicalvibes.model.trigger.CombatTrigger;
import com.github.laxika.magicalvibes.model.trigger.ControlChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.CounterTrigger;
import com.github.laxika.magicalvibes.model.trigger.DamageTrigger;
import com.github.laxika.magicalvibes.model.trigger.DayNightTrigger;
import com.github.laxika.magicalvibes.model.trigger.FinalChapterTrigger;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.LibraryTrigger;
import com.github.laxika.magicalvibes.model.trigger.LifeTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PlanarTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.StateTrigger;
import com.github.laxika.magicalvibes.model.trigger.TapTrigger;
import com.github.laxika.magicalvibes.model.trigger.TargetTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A triggered ability (CR 113.3c): its trigger conditions, the options that apply to them and its
 * effects. Each time it triggers it is one object on the stack (CR 603.3).
 *
 * @param conditions the trigger conditions, at least one; the ability triggers when any of them is
 *                   met ("When this creature enters or attacks"). A {@link LegacySlotTrigger} is the
 *                   only condition of its ability.
 * @param options    the intervening "if", limit and multiplicity. An ability with a
 *                   {@link LegacySlotTrigger} gets these from its slot's collectors and wrapper
 *                   effects instead, so its options are {@link TriggerOptions#DEFAULT}.
 * @param effects    the effects in the order they resolve, at least one. Effects marked simultaneous
 *                   happen at the same time as the effect before them, so the first one can't be.
 * @param chapterTargets the targets of a Saga chapter ability, or {@link SagaChapterTargets#NONE}.
 *                   The targets of other triggered abilities are still bound on the {@link Card}.
 * @param instance   which of several equal instances of the ability one object has this is (CR 113.2c: each
 *                   functions independently), or {@code null}: the granting permanent of an ability with a limit
 *                   that several permanents grant one permanent, so each instance keeps its own limit
 *                   ({@link #limitKey()}). A printed ability has none.
 */
public record TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options,
                               List<EffectRegistration> effects, SagaChapterTargets chapterTargets,
                               String instance)
        implements Ability {

    public TriggeredAbility {
        conditions = List.copyOf(Objects.requireNonNull(conditions, "conditions"));
        Objects.requireNonNull(options, "options");
        effects = List.copyOf(Objects.requireNonNull(effects, "effects"));
        Objects.requireNonNull(chapterTargets, "chapterTargets");
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("A triggered ability needs at least one trigger condition");
        }
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("A triggered ability needs at least one effect");
        }
        if (effects.getFirst().simultaneousWithPrevious()) {
            throw new IllegalArgumentException("The first effect of a triggered ability has no effect before it");
        }
        if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof LegacySlotTrigger)) {
            if (conditions.size() > 1) {
                throw new IllegalArgumentException("A legacy slot trigger is the only condition of its ability");
            }
            if (!options.equals(TriggerOptions.DEFAULT)) {
                throw new IllegalArgumentException("A legacy slot trigger takes its options from its slot's "
                        + "collectors and wrapper effects, not from TriggerOptions");
            }
        }
        requireReadableMultiplicity(conditions, options.multiplicity());
        if (!options.equals(TriggerOptions.DEFAULT)
                && conditions.stream().anyMatch(condition -> condition.trigger() instanceof ChapterTrigger)) {
            // "{rN}—[Effect]" is "when one or more lore counters are put onto this Saga, if the number of lore
            // counters on it was less than N and became at least N, [effect]" (CR 714.2b): the threshold is the
            // chapter's only intervening "if", and an "if" in its effect is a condition of the effect.
            throw new IllegalArgumentException("A chapter ability takes no options: " + conditions + " with "
                    + options);
        }
        requireFinalChapterOptions(conditions, options);
        requireStateOptions(conditions, options);
    }

    /**
     * Rejects a multiplicity none of {@code conditions}' events has: "each source" is a damage event's (CR 603.2c),
     * "each blocker" "becomes blocked"'s (CR 509.3d), and among the combat events "one or more" is that of "blocks" once
     * each combat (CR 509.3a), "becomes blocked", "a creature you control (or with the same name as this) deals combat
     * damage to a player [or battle, or planeswalker]" and "a creature deals combat damage to you", the combat
     * collectors that read it; no damage collector reads it (the "one or more" damage events are events of their own),
     * no counter collector does (a counter event is once for each permanent, or once for each counter, by its own
     * event), no becomes-the-target collector does ("one or more creatures you control become the target of an
     * activated ability" is an event of its own), and no library, draw or discard collector does ("whenever you discard
     * one or more cards" is an event of its own), of the tap and untap collectors only those of "a permanent you
     * control / an opponent controls becomes tapped" and "you tap a permanent an opponent controls" read it, as they
     * batch the permanents tapped together, and no life gain, loss or payment collector does (each life event triggers
     * once), and no collector of family 11 does: no player or keyword action collector ("whenever you roll one or more dice" and
     * "whenever you investigate one or more times" are events of their own), and no control change, attach, crew,
     * saddle, station, Room, day and night, state or planar collector (each event triggers once). The legacy view
     * checked these as a card's abilities were indexed; the combat, damage, counter, becomes-the-target, library, tap,
     * life and family 11 collectors read the ability itself, so the ability checks them as it is built.
     */
    private static void requireReadableMultiplicity(List<TriggerCondition> conditions,
                                                    TriggerMultiplicity multiplicity) {
        List<CombatTrigger> combat = conditions.stream().map(TriggerCondition::trigger)
                .filter(CombatTrigger.class::isInstance).map(CombatTrigger.class::cast).toList();
        switch (multiplicity) {
            case EACH_SOURCE -> {
                if (conditions.stream().noneMatch(condition -> condition.trigger() instanceof DamageTrigger)) {
                    throw new IllegalArgumentException("\"Each source\" is a multiplicity of damage triggers: "
                            + conditions);
                }
            }
            case EACH_BLOCKER -> {
                if (combat.stream().noneMatch(trigger -> trigger.event() == CombatTrigger.CombatEvent.BECOMES_BLOCKED)) {
                    throw new IllegalArgumentException("\"Each blocker\" is a multiplicity of \"becomes blocked\": "
                            + conditions);
                }
            }
            case ONE_OR_MORE -> {
                if (!combat.isEmpty() && combat.stream().noneMatch(TriggeredAbility::readsOneOrMore)) {
                    throw new IllegalArgumentException("No combat collector reads \"one or more\" for " + conditions);
                }
                if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof DamageTrigger)) {
                    throw new IllegalArgumentException("No damage collector reads \"one or more\" for " + conditions);
                }
                if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof CounterTrigger)) {
                    throw new IllegalArgumentException("No counter collector reads \"one or more\" for " + conditions);
                }
                if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof TargetTrigger)) {
                    throw new IllegalArgumentException("No becomes-the-target collector reads \"one or more\" for "
                            + conditions);
                }
                if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof LibraryTrigger)) {
                    throw new IllegalArgumentException("No library, draw or discard collector reads \"one or more\" "
                            + "for " + conditions);
                }
                if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof TapTrigger tap
                        && !readsOneOrMore(tap))) {
                    throw new IllegalArgumentException("No tap or untap collector reads \"one or more\" for "
                            + conditions);
                }
                if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof LifeTrigger)) {
                    throw new IllegalArgumentException("No life gain, loss or payment collector reads \"one or more\" "
                            + "for " + conditions);
                }
                if (conditions.stream().anyMatch(condition -> isFamilyElevenTrigger(condition.trigger()))) {
                    throw new IllegalArgumentException("No player or keyword action, control change, attach, Room, "
                            + "day and night, state or planar collector reads \"one or more\" for " + conditions);
                }
            }
            case EACH -> {
            }
        }
    }

    /**
     * Whether {@code trigger} is one of step 13's family 11 (a player or keyword action, control change, attach,
     * day and night, state or planar trigger), whose collectors read no "one or more", as
     * {@link #requireReadableMultiplicity} says.
     */
    private static boolean isFamilyElevenTrigger(Trigger trigger) {
        return trigger instanceof PlayerActionTrigger || trigger instanceof ObjectActionTrigger
                || trigger instanceof ControlChangeTrigger || trigger instanceof AttachTrigger
                || trigger instanceof DayNightTrigger || trigger instanceof StateTrigger
                || trigger instanceof PlanarTrigger;
    }

    /** Whether the collectors of {@code trigger} read "one or more", as {@link #requireReadableMultiplicity} says. */
    private static boolean readsOneOrMore(CombatTrigger trigger) {
        return switch (trigger.event()) {
            case BLOCKS, BECOMES_BLOCKED, COMBAT_DAMAGE_TO_YOU -> true;
            case COMBAT_DAMAGE_TO_PLAYER, COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE, COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER ->
                    trigger.relation() == ObjectRelation.YOU_CONTROL
                            || trigger.equals(Triggers.creatureWithSameNameDealsCombatDamageToPlayer());
            default -> false;
        };
    }

    /**
     * Whether the collector of {@code trigger} reads "one or more", as {@link #requireReadableMultiplicity} says: it
     * triggers once for the permanents tapped together, which "a permanent you control / an opponent controls becomes
     * tapped" and "you tap a permanent an opponent controls" do, but not for "this permanent" or a typed trigger.
     */
    private static boolean readsOneOrMore(TapTrigger trigger) {
        if (trigger.narrowed() || trigger.filter() != null) {
            return false;
        }
        return switch (trigger.event()) {
            case BECOMES_TAPPED -> trigger.relation() == ObjectRelation.YOU_CONTROL
                    || trigger.relation() == ObjectRelation.OPPONENT_CONTROLS;
            case YOU_TAP -> trigger.relation() == ObjectRelation.OPPONENT_CONTROLS;
            default -> false;
        };
    }

    /** An ability that is one instance of its kind on its object. */
    public TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options,
                            List<EffectRegistration> effects, SagaChapterTargets chapterTargets) {
        this(conditions, options, effects, chapterTargets, null);
    }

    /** An ability that isn't a Saga chapter ability. */
    public TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options,
                            List<EffectRegistration> effects) {
        this(conditions, options, effects, SagaChapterTargets.NONE);
    }

    /** An ability with one trigger condition, which functions from {@code trigger}'s default zones. */
    public TriggeredAbility(Trigger trigger, TriggerOptions options, List<EffectRegistration> effects) {
        this(List.of(new TriggerCondition(trigger)), options, effects);
    }

    /**
     * An ability with one trigger condition, which functions from {@code trigger}'s default zones, and no
     * options: "when this creature dies, draw a card" is {@code new TriggeredAbility(Triggers.dies(THIS),
     * new DrawCardEffect(1))}. The effects are one ability, in the order they resolve.
     */
    public TriggeredAbility(Trigger trigger, CardEffect... effects) {
        this(trigger, TriggerOptions.DEFAULT, Arrays.stream(effects).map(EffectRegistration::new).toList());
    }

    /**
     * An ability with one trigger condition, which functions from {@code trigger}'s default zones, and
     * {@code options}: a granted "whenever this creature attacks, if it's modified, …" is
     * {@code new TriggeredAbility(Triggers.attacks(), TriggerOptions.DEFAULT.withInterveningIf(…), effect)}.
     */
    public TriggeredAbility(Trigger trigger, TriggerOptions options, CardEffect... effects) {
        this(trigger, options, Arrays.stream(effects).map(EffectRegistration::new).toList());
    }

    /**
     * An ability with one trigger condition, which functions from the zones {@code condition} names, and no
     * options: "while this card is in your graveyard, whenever a creature you control dies".
     */
    public TriggeredAbility(TriggerCondition condition, CardEffect... effects) {
        this(List.of(condition), TriggerOptions.DEFAULT,
                Arrays.stream(effects).map(EffectRegistration::new).toList());
    }

    /**
     * An ability with one trigger condition, which functions from the zones {@code condition} names, and
     * {@code options}: a granted "at the beginning of your upkeep, if this card is exiled, …" is
     * {@code new TriggeredAbility(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.EXILE),
     * TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInExile()), effect)}.
     */
    public TriggeredAbility(TriggerCondition condition, TriggerOptions options, CardEffect... effects) {
        this(List.of(condition), options, Arrays.stream(effects).map(EffectRegistration::new).toList());
    }

    /** Whether {@code trigger} is one of this ability's trigger conditions. */
    public boolean hasTrigger(Trigger trigger) {
        return conditions.stream().anyMatch(condition -> condition.trigger().equals(trigger));
    }

    /**
     * Whether one of this ability's trigger conditions has {@code trigger} as its {@link Trigger#watchKey()} and
     * functions in {@code zone}: the abilities a collector that still reads effects by trigger reads.
     */
    public boolean hasWatchedTrigger(Trigger trigger, Zone zone) {
        return conditions.stream().anyMatch(condition -> condition.trigger().watchKey().equals(trigger)
                && condition.zones().functionsIn(zone));
    }

    /**
     * Whether one of this ability's trigger conditions has {@code trigger} as its {@link Trigger#watchKey()}, in any
     * zone: the abilities granted to a permanent that a collector reading effects by trigger reads.
     */
    public boolean hasWatchedTrigger(Trigger trigger) {
        return conditions.stream().anyMatch(condition -> condition.trigger().watchKey().equals(trigger));
    }

    /**
     * Whether {@code trigger} is one of this ability's trigger conditions and that condition functions in
     * {@code zone}.
     */
    public boolean hasTrigger(Trigger trigger, Zone zone) {
        return conditions.stream().anyMatch(condition -> condition.trigger().equals(trigger)
                && condition.zones().functionsIn(zone));
    }

    /**
     * The key "triggers only once each turn" marks for this ability on its source, so a limit covers every trigger
     * condition of the ability and no other ability of the source. Equal abilities of one source share it, but for
     * their {@link #instance()}.
     */
    public String limitKey() {
        return "trigger:" + Integer.toHexString(hashCode());
    }

    /**
     * Whether one of this ability's trigger conditions is a trigger whose collectors read the ability itself
     * ({@link Trigger#collectedAsAbility()}: every trigger but a zone change, permanent change or legacy slot
     * trigger).
     */
    public boolean collectedAsAbility() {
        return conditions.stream().anyMatch(condition -> condition.trigger().collectedAsAbility());
    }

    /**
     * Whether one of this ability's trigger conditions is a trigger whose collectors still read the legacy view
     * ({@link #legacyEffects()}). An ability with such a condition and one read as an ability (MACH-1's "when this
     * enters and whenever you gain life") has a legacy view for the first's collectors only: the second's read the
     * ability itself, and the ability's limit, marked by {@link #limitKey()}, is shared by both.
     */
    public boolean hasLegacyView() {
        return conditions.stream().anyMatch(condition -> !condition.trigger().collectedAsAbility());
    }

    /**
     * Rejects a lookup of the legacy view by {@code trigger} when its collectors read abilities themselves
     * ({@link Trigger#collectedAsAbility()}), so a reader left on the legacy view fails instead of reading the
     * effects without their options.
     */
    public static void requireLegacyView(Trigger trigger) {
        if (trigger.collectedAsAbility()) {
            throw new IllegalArgumentException("The collectors of " + trigger + " read its abilities themselves; "
                    + "the legacy view gives none");
        }
    }

    /**
     * This ability's registrations in the form the code that still reads effects instead of abilities takes them
     * (the "when this enters" collector and the code that inspects "when this enters" abilities, until step 15 of
     * effect-slot-refactor.md): each effect wrapped in this ability's options as the cards wrote them before step 14.
     * An intervening "if" is a {@link ConditionalEffect} of it, checked as the ability triggers and again as it
     * resolves (CR 603.4); the event condition the {@link ConditionalEffect#atTriggerTime} inside every other wrapper,
     * checked only as the ability triggers (CR 603.2); "triggers only once each turn" is a {@link OncePerTurnTriggerEffect} keyed by
     * {@link #limitKey()}, the key the trigger pipeline marks; "do this only once each turn" one marked as the action
     * is taken (CR 603.2h); "this ability triggers only once" ({@code ONCE}) an {@link OnceOnlyTriggerEffect} inside
     * the intervening "if", as the cards wrote it. Only an ability with a zone change, permanent change or legacy slot
     * trigger has a legacy view ({@link #hasLegacyView()}); the collectors of every other trigger
     * ({@link Trigger#collectedAsAbility()}) read the ability itself, and for an ability with only such triggers this
     * throws. "Once each turn for each creature" ({@link TriggerLimit#ONCE_EACH_TURN_FOR_EACH_CREATURE}) is an
     * {@link OncePerTurnPerCreatureTriggerEffect} where the other limits go. An ability without options returns its
     * registrations as they are.
     */
    public List<EffectRegistration> legacyEffects() {
        if (!hasLegacyView()) {
            throw new IllegalStateException("The collectors of " + conditions + " read the ability itself; it has no "
                    + "legacy view");
        }
        Condition interveningIf = options.interveningIf();
        TriggerLimit limit = options.limit();
        if (interveningIf == null && limit == TriggerLimit.NONE) {
            return legacyBaseEffects();
        }
        String key = limit == TriggerLimit.ONCE_EACH_TURN ? limitKey() : null;
        List<EffectRegistration> wrapped = new ArrayList<>(effects.size());
        for (EffectRegistration registration : legacyBaseEffects()) {
            CardEffect effect = registration.effect();
            if (limit == TriggerLimit.ONCE) {
                effect = new OnceOnlyTriggerEffect(effect);
            }
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect, true);
            }
            if (limit == TriggerLimit.ONCE_EACH_TURN) {
                effect = OncePerTurnTriggerEffect.keyed(effect, key);
            } else if (limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN) {
                effect = OncePerTurnTriggerEffect.markOnAcceptance(effect);
            } else if (limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
                effect = new OncePerTurnPerCreatureTriggerEffect(effect);
            }
            wrapped.add(new EffectRegistration(effect, registration.triggerMode(),
                    registration.simultaneousWithPrevious()));
        }
        return List.copyOf(wrapped);
    }

    /**
     * Rejects the options of an ability with a {@link FinalChapterTrigger} that the final-chapter collector doesn't
     * read. It judges "triggers only once each turn" from the ability as it triggers, and puts every effect on the stack
     * as it is: an intervening "if" would be checked only as the ability resolves (CR 603.4 checks it as the ability
     * triggers too), an event condition not at all, and it marks no "do this only once each turn" and reads no
     * "triggers only once" or "one or more". The legacy view checked these as a card's abilities were indexed; the
     * collector reads the ability itself, so the ability checks them as it is built.
     */
    private static void requireFinalChapterOptions(List<TriggerCondition> conditions, TriggerOptions options) {
        if (conditions.stream().noneMatch(condition -> condition.trigger() instanceof FinalChapterTrigger)) {
            return;
        }
        if (options.interveningIf() != null || options.eventCondition() != null
                || options.multiplicity() != TriggerMultiplicity.EACH
                || (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN)) {
            throw new IllegalArgumentException("The final-chapter collector doesn't read " + options + ": "
                    + conditions);
        }
    }

    /**
     * Rejects the options of a state trigger ({@link StateTrigger}, CR 603.8) that its reader doesn't read. The reader
     * checks the intervening "if" as the game state triggers the ability and the effects check it again as they resolve
     * (CR 603.4); it judges no limit, no multiplicity (a state trigger doesn't trigger again while it is on the stack)
     * and no event condition (CR 603.2: a state trigger has no event). The legacy view checked these as a card's
     * abilities were indexed; the reader reads the ability itself, so the ability checks them as it is built.
     */
    private static void requireStateOptions(List<TriggerCondition> conditions, TriggerOptions options) {
        if (conditions.stream().noneMatch(condition -> condition.trigger() instanceof StateTrigger)) {
            return;
        }
        if (options.limit() != TriggerLimit.NONE || options.multiplicity() != TriggerMultiplicity.EACH
                || options.eventCondition() != null) {
            throw new IllegalArgumentException("The state-trigger reader reads only an intervening \"if\": "
                    + options + ": " + conditions);
        }
    }

    /**
     * This ability's registrations with its event condition ({@link TriggerOptions#eventCondition()}) wrapped around
     * each effect as the {@link ConditionalEffect#atTriggerTime} the cards wrote before step 14, innermost of the
     * legacy view's wrappers: the collectors check it as the ability triggers and not as it resolves (CR 603.2).
     */
    private List<EffectRegistration> legacyBaseEffects() {
        Condition eventCondition = options.eventCondition();
        if (eventCondition == null) {
            return effects;
        }
        return effects.stream()
                .map(registration -> new EffectRegistration(
                        ConditionalEffect.atTriggerTime(eventCondition, registration.effect()),
                        registration.triggerMode(), registration.simultaneousWithPrevious()))
                .toList();
    }

    /** This ability with {@code effects} in place of its effects. */
    public TriggeredAbility withEffects(List<EffectRegistration> effects) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets, instance);
    }

    /** This ability with {@code chapterTargets} in place of its Saga chapter targets. */
    public TriggeredAbility withChapterTargets(SagaChapterTargets chapterTargets) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets, instance);
    }

    /** This ability as the instance {@code instance} of it ({@link #instance()}). */
    public TriggeredAbility withInstance(String instance) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets, instance);
    }
}
