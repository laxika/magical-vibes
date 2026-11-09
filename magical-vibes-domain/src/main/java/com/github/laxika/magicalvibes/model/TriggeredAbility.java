package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
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
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlanarTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
import com.github.laxika.magicalvibes.model.trigger.StateTrigger;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.TapTrigger;
import com.github.laxika.magicalvibes.model.trigger.TargetTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
 * @param grantingPermanentId the permanent that granted this ability to the object that has it, or {@code null}: the
 *                   Aura or Equipment of an "enchanted / equipped creature has '…'" grant, or the source of a one-shot
 *                   grant ("target creature gains '…' until end of turn"). Effects that name that permanent ("sacrifice
 *                   this Equipment", "other than Animal Friend") read it from the stack entry the ability's collector
 *                   builds ({@code StackEntry.getGrantingPermanentId()}). A printed ability has none.
 * @param targetFilter the restriction on the ability's target when no card's target groups hold it, or {@code null}:
 *                   an emblem's ability ("target opponent gets two poison counters") or a granted one has no card that
 *                   binds its effects to a target group, so it names its target filter itself. Only the spell-cast
 *                   collector, the emblems' life-gain reader and the your-upkeep reader's player-target choice read
 *                   it, so only an ability whose every trigger condition is one of theirs has one.
 */
public record TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options,
                               List<EffectRegistration> effects, SagaChapterTargets chapterTargets,
                               String instance, UUID grantingPermanentId, TargetFilter targetFilter)
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
        requireZoneChangeOptions(conditions, options);
        requireFirstTimeReader(conditions, options);
        requireOwnEntryOptions(conditions, options);
        requireTargetFilterReader(conditions, targetFilter);
    }

    /**
     * Rejects a {@link #targetFilter} on an ability with a trigger condition other than a cast trigger, "whenever you
     * gain life" or "at the beginning of your upkeep": only the spell-cast collector, the emblems' life-gain reader and
     * the your-upkeep reader's player-target choice (a granted ability's "target opponent", Custody Battle) read the
     * ability's own target filter (the permanents' life collectors reject one as they read the ability).
     */
    private static void requireTargetFilterReader(List<TriggerCondition> conditions, TargetFilter targetFilter) {
        if (targetFilter != null && conditions.stream().map(TriggerCondition::trigger).anyMatch(trigger
                -> !(trigger instanceof SpellTrigger spell && spell.event() == SpellTrigger.SpellEvent.CAST)
                && !(trigger instanceof LifeTrigger life && life.event() == LifeTrigger.LifeEvent.GAINS
                && life.player() == PlayerRelation.YOU && life.spellFilter() == null && !life.firstEachTurn())
                && !(trigger instanceof StepTrigger step && step.moment() == StepTrigger.Moment.UPKEEP
                && step.player() == ActivePlayer.YOU))) {
            throw new IllegalArgumentException("Only the spell-cast collector, the emblems' life-gain reader and the "
                    + "your-upkeep reader read an ability's own target filter: " + conditions);
        }
    }

    /** An ability without a target filter of its own, as {@code grantingPermanentId} granted it. */
    public TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options,
                            List<EffectRegistration> effects, SagaChapterTargets chapterTargets,
                            String instance, UUID grantingPermanentId) {
        this(conditions, options, effects, chapterTargets, instance, grantingPermanentId, null);
    }

    /**
     * Rejects what the trigger pipeline that reads the abilities of a zone change other than "when this enters" and of
     * a permanent change doesn't read: "once each turn for each creature" (only the damage and counter collectors know
     * it; the pipeline judges every other limit as the ability triggers). The legacy view checked this as a card's
     * abilities were indexed; the pipeline reads the ability itself, so the ability checks it as it is built.
     */
    private static void requireZoneChangeOptions(List<TriggerCondition> conditions, TriggerOptions options) {
        boolean zoneChange = conditions.stream().map(TriggerCondition::trigger).anyMatch(trigger
                -> trigger instanceof ZoneChangeTrigger move && !move.ownEntry()
                || trigger instanceof PermanentChangeTrigger);
        if (!zoneChange) {
            return;
        }
        if (options.limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            throw new IllegalArgumentException("No zone-change or permanent-change collector reads "
                    + options.limit() + ": " + conditions);
        }
    }

    /**
     * Rejects "for the first time each turn" ({@link TriggerLimit#FIRST_TIME_EACH_TURN}) on an ability with a trigger
     * other than a zone change or a combat event: only the token-creation and land-to-graveyard collectors of the
     * trigger pipeline and the "whenever you attack" collector read it.
     */
    private static void requireFirstTimeReader(List<TriggerCondition> conditions, TriggerOptions options) {
        if (options.limit() == TriggerLimit.FIRST_TIME_EACH_TURN && conditions.stream().map(TriggerCondition::trigger)
                .anyMatch(trigger -> !(trigger instanceof ZoneChangeTrigger) && !(trigger instanceof CombatTrigger))) {
            throw new IllegalArgumentException("No collector of " + conditions + " reads " + options.limit());
        }
    }

    /**
     * Rejects a multiplicity none of {@code conditions}' events has: "each source" is a damage event's (CR 603.2c),
     * "each blocker" "becomes blocked"'s (CR 509.3d), and among the combat events "one or more" is that of "blocks" once
     * each combat (CR 509.3a), "becomes blocked", "a creature you control (or with the same name as this) deals combat
     * damage to a player [or battle, or planeswalker]", "a creature deals combat damage to you" and "a creature deals
     * combat damage to one of your opponents" (once for the combat damage step), the combat collectors that read it; no damage collector reads it (the "one or more" damage events are events of their own),
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
            case BLOCKS, BECOMES_BLOCKED, COMBAT_DAMAGE_TO_YOU, COMBAT_DAMAGE_TO_OPPONENT -> true;
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

    /** An ability that wasn't granted, as the instance {@code instance} of it. */
    public TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options,
                            List<EffectRegistration> effects, SagaChapterTargets chapterTargets,
                            String instance) {
        this(conditions, options, effects, chapterTargets, instance, null);
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
     * Whether one of this ability's trigger conditions is "when this enters" ({@link ZoneChangeTrigger#ownEntry()},
     * CR 603.6a): the abilities {@code EtbTriggerService} puts on the stack as the permanent enters, and that the code
     * inspecting a card's enters abilities reads ({@code Card.getOwnEntryEffects()}).
     */
    public boolean hasOwnEntry() {
        return conditions.stream().anyMatch(condition -> condition.trigger() instanceof ZoneChangeTrigger move
                && move.ownEntry());
    }

    /**
     * This ability's registrations in the form the "when this enters" collector ({@code EtbTriggerService}) and the
     * code inspecting a card's enters abilities read them, until step 15 of effect-slot-refactor.md makes the effects
     * read the event: the intervening "if" a {@link ConditionalEffect} of it around each effect, so it is checked again
     * as the ability resolves (CR 603.4). The collector judges the intervening "if" and the event condition as the
     * ability triggers (CR 603.2), and the limit ({@link #limitKey()}), from the ability's options. An ability without
     * an intervening "if" returns its registrations as they are. Throws for an ability without a "when this enters"
     * condition ({@link #hasOwnEntry()}), whose collectors read the ability itself.
     */
    public List<EffectRegistration> ownEntryEffects() {
        if (!hasOwnEntry()) {
            throw new IllegalStateException("Only a \"when this enters\" ability is read as its effects: " + conditions);
        }
        Condition interveningIf = options.interveningIf();
        if (interveningIf == null) {
            return effects;
        }
        List<EffectRegistration> wrapped = new ArrayList<>(effects.size());
        for (EffectRegistration registration : effects) {
            wrapped.add(new EffectRegistration(new ConditionalEffect(interveningIf, registration.effect()),
                    registration.simultaneousWithPrevious()));
        }
        return List.copyOf(wrapped);
    }

    /**
     * Rejects the options of an ability with a "when this enters" condition ({@link ZoneChangeTrigger#ownEntry()}) that
     * its collector doesn't read. {@code EtbTriggerService} judges "triggers only once each turn", the intervening "if" and
     * the event condition from the ability as it triggers ({@link #limitKey()}, shared with the ability's other
     * conditions); it marks no "do this only once each turn" (CR 603.2h) and reads no
     * "triggers only once" or "once each turn for each creature", and the permanent's own entry is one event, so "one or
     * more" is the multiplicity of another condition only. The legacy view checked these as a card's abilities were
     * indexed; the collector reads the ability itself, so the ability checks them as it is built.
     */
    private static void requireOwnEntryOptions(List<TriggerCondition> conditions, TriggerOptions options) {
        boolean ownEntry = conditions.stream().anyMatch(condition -> condition.trigger() instanceof ZoneChangeTrigger move
                && move.ownEntry());
        if (!ownEntry) {
            return;
        }
        if (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN) {
            throw new IllegalArgumentException("The \"when this enters\" collector doesn't read " + options.limit()
                    + ": " + conditions);
        }
        if (options.multiplicity() != TriggerMultiplicity.EACH && conditions.size() == 1) {
            throw new IllegalArgumentException("\"When this enters\" is one event; it has no multiplicity "
                    + options.multiplicity() + ": " + conditions);
        }
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

    /** This ability with {@code effects} in place of its effects. */
    public TriggeredAbility withEffects(List<EffectRegistration> effects) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets, instance, grantingPermanentId, targetFilter);
    }

    /** This ability with {@code chapterTargets} in place of its Saga chapter targets. */
    public TriggeredAbility withChapterTargets(SagaChapterTargets chapterTargets) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets, instance, grantingPermanentId, targetFilter);
    }

    /** This ability as granted by {@code grantingPermanentId} ({@link #grantingPermanentId()}). */
    public TriggeredAbility withGrantingPermanentId(UUID grantingPermanentId) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets, instance, grantingPermanentId, targetFilter);
    }

    /** This ability with {@code targetFilter} as its own target filter ({@link #targetFilter()}). */
    public TriggeredAbility withTargetFilter(TargetFilter targetFilter) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets, instance, grantingPermanentId,
                targetFilter);
    }

    /** This ability as the instance {@code instance} of it ({@link #instance()}). */
    public TriggeredAbility withInstance(String instance) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets, instance, grantingPermanentId, targetFilter);
    }
}
