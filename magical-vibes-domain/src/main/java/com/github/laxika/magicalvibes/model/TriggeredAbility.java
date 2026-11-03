package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.AbilityTargetChoiceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BatchedCombatDamageToYouTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CyclingTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ExceptFirstDrawStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.FirstOpponentLifeLossEachTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.FirstDrawStepDrawOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.FirstOrSecondCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.LandPlayFromExileTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnPerCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCausedDiscardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PerDamageSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringBoastAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringLoyaltyAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNinjutsuAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNonTapAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringOutlastAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPowerUpAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringTapAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.YouPutCounterOnControlledCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.YouRemoveTimeCounterFromExiledCardTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.CombatTrigger;
import com.github.laxika.magicalvibes.model.trigger.CounterTrigger;
import com.github.laxika.magicalvibes.model.trigger.DamageTrigger;
import com.github.laxika.magicalvibes.model.trigger.LandPlayTrigger;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.LibraryTrigger;
import com.github.laxika.magicalvibes.model.trigger.LifeTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
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
import java.util.Set;
import java.util.stream.Collectors;

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
 */
public record TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options,
                               List<EffectRegistration> effects, SagaChapterTargets chapterTargets)
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
     * condition of the ability and no other ability of the source.
     */
    public String limitKey() {
        return "trigger:" + Integer.toHexString(hashCode());
    }

    /**
     * This ability's registrations in the form the code that still reads effects instead of abilities takes them
     * (the "when this enters" collector and the code that inspects "when this enters" abilities, until step 15 of
     * effect-slot-refactor.md): each effect wrapped in this ability's options as the cards wrote them before step 14.
     * An intervening "if" is a {@link ConditionalEffect} of it, checked as the ability triggers and again as it
     * resolves (CR 603.4); "triggers only once each turn" is a {@link OncePerTurnTriggerEffect} keyed by
     * {@link #limitKey()}, the key the trigger pipeline marks; "do this only once each turn" one marked as the action
     * is taken (CR 603.2h); "this ability triggers only once" ({@code ONCE}) an {@link OnceOnlyTriggerEffect} inside
     * the intervening "if", as the "at the beginning of [step or phase]" cards wrote it. A land-play trigger's filter
     * is a {@link TriggeringCardConditionalEffect} inside the limit, an activation trigger's source filter a
     * {@link TriggeringPermanentConditionalEffect} there and its ability kind the matching
     * {@code Triggering…AbilityConditionalEffect} outside everything, as the spell, land-play and activation cards
     * wrote them. A cast trigger's {@link SpellFilter} makes the ability one {@link SpellCastTriggerEffect} of all its
     * effects, as {@link #legacySpellCastEffects} says, and a copy trigger's one {@link SpellCopyTriggerEffect}, as
     * {@link #legacySpellCopyEffects} says. A land-play trigger's zone makes the ability one
     * {@link LandPlayFromExileTriggerEffect} ("a land from exile") or {@link PlayFromOutsideHandTriggerEffect} ("from
     * anywhere other than your hand") of all its effects, the records the land-play collectors judge. A combat
     * trigger's filter is a {@link TriggeringPermanentConditionalEffect} inside the limit, as the combat cards wrote
     * it, but on "a creature you control deals combat damage to a player [or battle]", whose collectors read every
     * effect as an {@link AllyCombatDamageTriggerEffect}: there the filter, and "one or more"
     * ({@link TriggerMultiplicity#ONE_OR_MORE}), are that record's, wrapped around the intervening "if" (an authored
     * record takes them in). "One or more creatures deal combat damage to you" wraps each effect in a
     * {@link BatchedCombatDamageToYouTriggerEffect} there. "Whenever this creature blocks" once each combat
     * ({@code ONE_OR_MORE}, CR 509.3a) and "becomes blocked by a creature" ({@link TriggerMultiplicity#EACH_BLOCKER},
     * CR 509.3d) are the registrations' {@link TriggerMode#ONCE_PER_BLOCK} and {@link TriggerMode#PER_BLOCKER}. A
     * damage trigger's filter is a {@link TriggeringPermanentConditionalEffect} outside the limit, and "once for each
     * source" ({@link TriggerMultiplicity#EACH_SOURCE}) a {@link PerDamageSourceTriggerEffect} outside that, as the
     * damage cards wrote them. "Once each turn for each creature"
     * ({@link TriggerLimit#ONCE_EACH_TURN_FOR_EACH_CREATURE}) is an {@link OncePerTurnPerCreatureTriggerEffect} where
     * the other limits go. A counter trigger's filter ({@link CounterTrigger#qualifier()}) is a
     * {@link TriggeringPermanentConditionalEffect} outside the limit, and the counter triggers the collector of
     * another event reads ({@link CounterTrigger#watchedEvent()}) are that collector's wrapper outside that: "you put
     * counters" a {@link YouPutCounterOnControlledCreatureTriggerEffect}, "you remove a time counter" a
     * {@link YouRemoveTimeCounterFromExiledCardTriggerEffect}, and "a +1/+1 counter is put on this", once for each
     * counter, the registrations' {@link TriggerMode#PER_COUNTER}, as the counter cards wrote them. A becomes-the-target
     * trigger's filter ({@link TargetTrigger#qualifier()}) is a {@link TriggeringPermanentConditionalEffect} outside the
     * limit, and the target triggers the collector of another event reads ({@link TargetTrigger#watchedEvent()}) that
     * collector's wrapper outside that: "this becomes the target of a spell [or ability] you control" a
     * {@link TriggeringSpellControllerConditionalEffect}, "a player or permanent becomes the target of an ability you
     * control" an {@link AbilityTargetChoiceTriggerEffect}, as the target cards wrote them. A tap or untap trigger's
     * filter ("this permanent" for a {@link TapTrigger#narrowed()} trigger, and its {@link TapTrigger#qualifier()}) is
     * a {@link TriggeringPermanentConditionalEffect} outside the limit, "for the first time during your turn" the
     * global {@link OncePerTurnPerCreatureTriggerEffect} inside that filter, and "one or more" the registrations'
     * {@link TriggerMode#ONCE_PER_BATCH}, as {@link #tapView()} says and the tap cards wrote them. A library, draw or
     * discard trigger that its collectors read by another trigger ({@link LibraryTrigger#watchKey()}) is given in the
     * order {@link #legacyLibraryEffects} says. A life trigger's "for the first time each turn"
     * ({@link LifeTrigger#firstEachTurn()}) is the counting {@link OncePerTurnTriggerEffect} the life collectors judge,
     * where the limits go, as {@link #lifeView()} says, and "an opponent loses life for the first time during each of
     * their turns" makes the ability one {@link FirstOpponentLifeLossEachTurnTriggerEffect} of all its effects. An
     * ability without options or such filters returns its registrations as they are.
     */
    public List<EffectRegistration> legacyEffects() {
        SpellTrigger spell = filteredTrigger(SpellTrigger.class);
        if (spell != null) {
            if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof SpellTrigger other
                    && other.event() != spell.event())) {
                throw new IllegalStateException("The legacy view takes the spell conditions of one ability of one "
                        + "event: " + conditions);
            }
            return spell.event() == SpellTrigger.SpellEvent.COPIED
                    ? legacySpellCopyEffects(spell.filter())
                    : legacySpellCastEffects(spell.filter());
        }
        if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof LibraryTrigger library
                && !library.watchKey().equals(library))) {
            return legacyLibraryEffects();
        }
        if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof LifeTrigger life
                && life.event() == LifeTrigger.LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN)) {
            return legacyFirstOpponentLifeLossEffects();
        }
        Condition interveningIf = options.interveningIf();
        TriggerLimit limit = options.limit();
        LandPlayTrigger landPlay = filteredTrigger(LandPlayTrigger.class);
        if (landPlay != null && (landPlay.playedFrom() != null || landPlay.notPlayedFrom() != null)) {
            return legacyLandPlayZoneEffects(landPlay);
        }
        AbilityTrigger activation = filteredTrigger(AbilityTrigger.class);
        CombatView combat = combatView();
        DamageView damage = damageView();
        CounterView counter = counterView();
        TargetView target = targetView();
        TapView tap = tapView();
        LifeView life = lifeView();
        if (interveningIf == null && limit == TriggerLimit.NONE && landPlay == null && activation == null
                && (combat == null || combat.plain()) && (damage == null || damage.plain())
                && (counter == null || counter.plain()) && (target == null || target.plain())
                && (tap == null || tap.plain()) && (life == null || life.plain())) {
            return effects;
        }
        String key = limit == TriggerLimit.ONCE_EACH_TURN ? limitKey() : null;
        List<EffectRegistration> wrapped = new ArrayList<>(effects.size());
        for (EffectRegistration registration : effects) {
            CardEffect effect = registration.effect();
            // An authored ally combat damage record (an event binding) is the outermost of the effect's own
            // wrappers: the intervening "if" goes inside it, as the cards wrote it.
            AllyCombatDamageTriggerEffect authoredAlly = combat != null && combat.allyDamage()
                    && effect instanceof AllyCombatDamageTriggerEffect ally ? ally : null;
            if (authoredAlly != null) {
                effect = authoredAlly.effect();
            }
            if (limit == TriggerLimit.ONCE) {
                effect = new OnceOnlyTriggerEffect(effect);
            }
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect, true);
            }
            if (authoredAlly != null) {
                effect = effect == authoredAlly.effect() ? authoredAlly : authoredAlly.withEffect(effect);
            }
            if (combat != null) {
                effect = combat.wrap(effect);
            }
            if (landPlay != null && landPlay.filter() != null) {
                effect = new TriggeringCardConditionalEffect(landPlay.filter(), effect);
            }
            if (activation != null && activation.source() != null) {
                effect = new TriggeringPermanentConditionalEffect(activation.source(), effect);
            }
            if (limit == TriggerLimit.ONCE_EACH_TURN) {
                effect = OncePerTurnTriggerEffect.keyed(effect, key);
            } else if (limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN) {
                effect = OncePerTurnTriggerEffect.markOnAcceptance(effect);
            } else if (limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
                effect = new OncePerTurnPerCreatureTriggerEffect(effect);
            }
            if (life != null) {
                effect = life.wrap(effect, limitKey());
            }
            if (damage != null) {
                effect = damage.wrap(effect);
            }
            if (counter != null) {
                effect = counter.wrap(effect);
            }
            if (target != null) {
                effect = target.wrap(effect);
            }
            if (tap != null) {
                effect = tap.wrap(effect);
            }
            if (activation != null && activation.kind() != null) {
                effect = legacyKindWrapper(activation.kind(), effect);
            }
            TriggerMode mode = combat != null ? combat.mode(registration.triggerMode()) : registration.triggerMode();
            if (counter != null) {
                mode = counter.mode(mode);
            }
            if (tap != null) {
                mode = tap.mode(mode);
            }
            wrapped.add(new EffectRegistration(effect, mode, registration.simultaneousWithPrevious()));
        }
        return List.copyOf(wrapped);
    }

    /**
     * What the legacy view adds for this ability's combat conditions, or {@code null} when it has none: their filter
     * (the one {@link CombatTrigger#watchKey()} drops; the conditions must agree on it), whether the collectors read
     * the effects as {@link AllyCombatDamageTriggerEffect}s or {@link BatchedCombatDamageToYouTriggerEffect}s, and
     * the trigger mode the multiplicity is.
     */
    private CombatView combatView() {
        List<CombatTrigger> combat = conditions.stream().map(TriggerCondition::trigger)
                .filter(CombatTrigger.class::isInstance).map(CombatTrigger.class::cast).toList();
        if (combat.isEmpty()) {
            return null;
        }
        List<PermanentPredicate> filters = combat.stream()
                .map(trigger -> trigger.watchKey().equals(trigger) ? null : trigger.filter()).distinct().toList();
        if (filters.size() > 1) {
            throw new IllegalStateException("The combat conditions of one ability must have the same filter: "
                    + conditions);
        }
        boolean allyDamage = combat.stream().anyMatch(TriggeredAbility::readAsAllyCombatDamage);
        if (allyDamage && combat.size() > 1) {
            throw new IllegalStateException("A \"creature you control deals combat damage to a player\" ability has no "
                    + "other combat condition in the legacy view: " + conditions);
        }
        boolean damageToYou = combat.stream()
                .anyMatch(trigger -> trigger.event() == CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_YOU);
        boolean block = combat.stream().anyMatch(trigger -> trigger.event() == CombatTrigger.CombatEvent.BLOCKS
                || trigger.event() == CombatTrigger.CombatEvent.BECOMES_BLOCKED);
        TriggerMultiplicity multiplicity = options.multiplicity();
        boolean oneOrMore = multiplicity == TriggerMultiplicity.ONE_OR_MORE;
        if (oneOrMore && !allyDamage && !damageToYou && !block) {
            throw new IllegalStateException("The legacy view has no \"one or more\" form for " + conditions);
        }
        if (multiplicity == TriggerMultiplicity.EACH_BLOCKER && combat.stream()
                .noneMatch(trigger -> trigger.event() == CombatTrigger.CombatEvent.BECOMES_BLOCKED)) {
            throw new IllegalStateException("\"Each blocker\" is a multiplicity of \"becomes blocked\": " + conditions);
        }
        return new CombatView(filters.getFirst(), allyDamage, oneOrMore && !allyDamage && damageToYou,
                oneOrMore && !allyDamage, multiplicity);
    }

    /**
     * What the legacy view adds for this ability's damage conditions, or {@code null} when it has none: their filter
     * (the one {@link DamageTrigger#watchKey()} drops; the conditions must agree on it) and whether it triggers once for
     * each source. The damage collectors have no "one or more" or "each blocker" form, and only they have "each
     * source".
     */
    private DamageView damageView() {
        List<DamageTrigger> damage = conditions.stream().map(TriggerCondition::trigger)
                .filter(DamageTrigger.class::isInstance).map(DamageTrigger.class::cast).toList();
        TriggerMultiplicity multiplicity = options.multiplicity();
        if (damage.isEmpty()) {
            if (multiplicity == TriggerMultiplicity.EACH_SOURCE) {
                throw new IllegalStateException("\"Each source\" is a multiplicity of damage triggers: " + conditions);
            }
            return null;
        }
        List<PermanentPredicate> filters = damage.stream().map(DamageTrigger::filter).distinct().toList();
        if (filters.size() > 1) {
            throw new IllegalStateException("The damage conditions of one ability must have the same filter: "
                    + conditions);
        }
        if (multiplicity == TriggerMultiplicity.ONE_OR_MORE || multiplicity == TriggerMultiplicity.EACH_BLOCKER) {
            throw new IllegalStateException("The legacy view has no " + multiplicity + " form for " + conditions);
        }
        return new DamageView(filters.getFirst(), multiplicity == TriggerMultiplicity.EACH_SOURCE);
    }

    /**
     * The legacy view of an ability's damage conditions, as {@link #damageView()} builds it.
     *
     * @param filter     the conditions' filter, or {@code null}
     * @param eachSource each effect is wrapped in a {@link PerDamageSourceTriggerEffect}
     */
    private record DamageView(PermanentPredicate filter, boolean eachSource) {

        /** Whether the legacy view adds nothing to the effects. */
        boolean plain() {
            return filter == null && !eachSource;
        }

        CardEffect wrap(CardEffect effect) {
            CardEffect filtered = filter != null ? new TriggeringPermanentConditionalEffect(filter, effect) : effect;
            return eachSource ? new PerDamageSourceTriggerEffect(filtered) : filtered;
        }
    }

    /**
     * What the legacy view adds for this ability's counter conditions, or {@code null} when it has none: their filter
     * (the {@link CounterTrigger#qualifier()} {@link CounterTrigger#watchKey()} drops) and the event the collector
     * that reads them judges in a wrapper or trigger mode. The conditions must agree on both. The counter collectors
     * have no multiplicity option: "each counter" is an event of its own.
     */
    private CounterView counterView() {
        List<CounterTrigger> counter = conditions.stream().map(TriggerCondition::trigger)
                .filter(CounterTrigger.class::isInstance).map(CounterTrigger.class::cast).toList();
        if (counter.isEmpty()) {
            return null;
        }
        List<CounterView> views = counter.stream()
                .map(trigger -> new CounterView(trigger.qualifier(),
                        trigger.watchedEvent() == trigger.event() ? null : trigger.event()))
                .distinct().toList();
        if (views.size() > 1) {
            throw new IllegalStateException("The counter conditions of one ability must have the same filter and "
                    + "watched event: " + conditions);
        }
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The legacy view has no " + options.multiplicity() + " form for "
                    + conditions);
        }
        return views.getFirst();
    }

    /**
     * The legacy view of an ability's counter conditions, as {@link #counterView()} builds it.
     *
     * @param filter   the conditions' filter, or {@code null}
     * @param narrowed the event read by another event's collector, which judges it as a wrapper or a trigger mode, or
     *                 {@code null}
     */
    private record CounterView(PermanentPredicate filter, CounterTrigger.CounterEvent narrowed) {

        /** Whether the legacy view adds nothing to the effects and modes. */
        boolean plain() {
            return filter == null && narrowed == null;
        }

        CardEffect wrap(CardEffect effect) {
            CardEffect filtered = filter != null ? new TriggeringPermanentConditionalEffect(filter, effect) : effect;
            if (narrowed == CounterTrigger.CounterEvent.YOU_REMOVE_TIME_COUNTER_IN_EXILE) {
                return new YouRemoveTimeCounterFromExiledCardTriggerEffect(filtered);
            }
            if (narrowed == CounterTrigger.CounterEvent.YOU_PUT_COUNTERS
                    || narrowed == CounterTrigger.CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS) {
                return new YouPutCounterOnControlledCreatureTriggerEffect(filtered);
            }
            return filtered;
        }

        TriggerMode mode(TriggerMode authored) {
            if (narrowed != CounterTrigger.CounterEvent.EACH_PLUS_ONE_COUNTER_PUT) {
                return authored;
            }
            if (authored != TriggerMode.NORMAL) {
                throw new IllegalStateException("A registration's trigger mode " + authored
                        + " on a trigger that is once for each counter");
            }
            return TriggerMode.PER_COUNTER;
        }
    }

    /**
     * What the legacy view adds for this ability's becomes-the-target conditions, or {@code null} when it has none:
     * their filter (the {@link TargetTrigger#qualifier()} {@link TargetTrigger#watchKey()} drops) and the event the
     * collector that reads them judges in a wrapper. The conditions must agree on both. The becomes-the-target
     * collectors have no multiplicity option.
     */
    private TargetView targetView() {
        List<TargetTrigger> target = conditions.stream().map(TriggerCondition::trigger)
                .filter(TargetTrigger.class::isInstance).map(TargetTrigger.class::cast).toList();
        if (target.isEmpty()) {
            return null;
        }
        List<TargetView> views = target.stream()
                .map(trigger -> new TargetView(trigger.qualifier(),
                        trigger.watchedEvent() == trigger.event() ? null : trigger.event()))
                .distinct().toList();
        if (views.size() > 1) {
            throw new IllegalStateException("The becomes-the-target conditions of one ability must have the same "
                    + "filter and watched event: " + conditions);
        }
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The legacy view has no " + options.multiplicity() + " form for "
                    + conditions);
        }
        return views.getFirst();
    }

    /**
     * The legacy view of an ability's becomes-the-target conditions, as {@link #targetView()} builds it.
     *
     * @param filter   the conditions' filter, or {@code null}
     * @param narrowed the event read by another event's collector, which judges it as a wrapper, or {@code null}
     */
    private record TargetView(PermanentPredicate filter, TargetTrigger.TargetEvent narrowed) {

        /** Whether the legacy view adds nothing to the effects. */
        boolean plain() {
            return filter == null && narrowed == null;
        }

        CardEffect wrap(CardEffect effect) {
            CardEffect filtered = filter != null ? new TriggeringPermanentConditionalEffect(filter, effect) : effect;
            if (narrowed == null) {
                return filtered;
            }
            return switch (narrowed) {
                case BECOMES_TARGET_OF_YOUR_SPELL, BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY ->
                        new TriggeringSpellControllerConditionalEffect(filtered);
                case PLAYER_OR_PERMANENT_BECOMES_TARGET_OF_YOUR_ABILITY ->
                        new AbilityTargetChoiceTriggerEffect(filtered);
                default -> throw new IllegalStateException("No legacy wrapper for " + narrowed);
            };
        }
    }

    /**
     * What the legacy view adds for this ability's tap and untap conditions, or {@code null} when it has none: the
     * filter the collector that reads them judges ("this permanent", a {@link TapTrigger#narrowed()} trigger's type
     * and the {@link TapTrigger#qualifier()}, which {@link TapTrigger#watchKey()} drops), whether "for the first time
     * during your turn" is the global {@link OncePerTurnPerCreatureTriggerEffect}, and whether "one or more"
     * ({@link TriggerMultiplicity#ONE_OR_MORE}) is the registrations' {@link TriggerMode#ONCE_PER_BATCH}. The
     * conditions must agree on all three. A filter or "one or more" on a trigger whose collector doesn't judge it, and
     * a limit next to "for the first time during your turn", throw.
     */
    private TapView tapView() {
        List<TapTrigger> tap = conditions.stream().map(TriggerCondition::trigger)
                .filter(TapTrigger.class::isInstance).map(TapTrigger.class::cast).toList();
        if (tap.isEmpty()) {
            return null;
        }
        TriggerMultiplicity multiplicity = options.multiplicity();
        if (multiplicity != TriggerMultiplicity.EACH && multiplicity != TriggerMultiplicity.ONE_OR_MORE) {
            throw new IllegalStateException("The legacy view has no " + multiplicity + " form for " + conditions);
        }
        boolean oneOrMore = multiplicity == TriggerMultiplicity.ONE_OR_MORE;
        List<TapView> views = tap.stream().map(trigger -> {
            if (trigger.qualifier() != null && !judgesTapFilter(trigger)) {
                throw new IllegalStateException("No tap collector judges a filter on " + trigger);
            }
            if (oneOrMore && !batchesTaps(trigger)) {
                throw new IllegalStateException("No tap collector reads \"one or more\" on " + trigger);
            }
            return new TapView(legacyTapFilter(trigger),
                    trigger.event() == TapTrigger.TapEvent.BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN, oneOrMore);
        }).distinct().toList();
        if (views.size() > 1) {
            throw new IllegalStateException("The tap conditions of one ability must have the same filter and event: "
                    + conditions);
        }
        TapView view = views.getFirst();
        if (view.firstTime() && options.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The legacy view has no limit next to \"for the first time during your "
                    + "turn\": " + conditions);
        }
        return view;
    }

    /**
     * Whether the collector that reads {@code trigger}'s abilities judges a
     * {@link TriggeringPermanentConditionalEffect} on the tapped or untapped permanent: "a permanent you control / an
     * opponent controls becomes tapped" (and the {@link TapTrigger#narrowed()} triggers it reads), "you tap a
     * permanent an opponent controls", and "a permanent [you control] becomes untapped".
     */
    private static boolean judgesTapFilter(TapTrigger trigger) {
        if (trigger.narrowed()) {
            return true;
        }
        return switch (trigger.event()) {
            case BECOMES_TAPPED -> trigger.relation() == ObjectRelation.YOU_CONTROL
                    || trigger.relation() == ObjectRelation.OPPONENT_CONTROLS;
            case YOU_TAP -> trigger.relation() == ObjectRelation.OPPONENT_CONTROLS;
            case BECOMES_UNTAPPED -> trigger.relation() == ObjectRelation.YOU_CONTROL
                    || trigger.relation() == ObjectRelation.ANY;
            default -> false;
        };
    }

    /**
     * Whether the collector that reads {@code trigger}'s abilities triggers a {@link TriggerMode#ONCE_PER_BATCH}
     * registration once for the permanents tapped together: "a permanent you control / an opponent controls becomes
     * tapped" and "you tap a permanent an opponent controls".
     */
    private static boolean batchesTaps(TapTrigger trigger) {
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

    /**
     * The filter the tap collectors judge for {@code trigger}, or {@code null} for none: "this permanent" for
     * {@link ObjectRelation#THIS}, the type a {@link TapTrigger#narrowed()} trigger's watch key drops, and the
     * {@link TapTrigger#qualifier()}, all of them when there are several.
     */
    private static PermanentPredicate legacyTapFilter(TapTrigger trigger) {
        List<PermanentPredicate> parts = new ArrayList<>();
        if (trigger.narrowed() && trigger.relation() == ObjectRelation.THIS) {
            parts.add(new PermanentIsSourceCardPredicate());
        }
        if (trigger.narrowed() && trigger.filter() != null) {
            parts.add(trigger.filter());
        }
        if (trigger.qualifier() != null) {
            parts.add(trigger.qualifier());
        }
        return switch (parts.size()) {
            case 0 -> null;
            case 1 -> parts.getFirst();
            default -> new PermanentAllOfPredicate(List.copyOf(parts));
        };
    }

    /**
     * The legacy view of an ability's tap and untap conditions, as {@link #tapView()} builds it.
     *
     * @param filter    the filter the collector judges, or {@code null}
     * @param firstTime whether the conditions are "for the first time during your turn"
     * @param oneOrMore whether the ability triggers once for the permanents tapped together
     */
    private record TapView(PermanentPredicate filter, boolean firstTime, boolean oneOrMore) {

        /** Whether the legacy view adds nothing to the effects and modes. */
        boolean plain() {
            return filter == null && !firstTime && !oneOrMore;
        }

        CardEffect wrap(CardEffect effect) {
            CardEffect counted = firstTime ? new OncePerTurnPerCreatureTriggerEffect(effect, true) : effect;
            return filter != null ? new TriggeringPermanentConditionalEffect(filter, counted) : counted;
        }

        TriggerMode mode(TriggerMode authored) {
            if (!oneOrMore) {
                return authored;
            }
            if (authored != TriggerMode.NORMAL) {
                throw new IllegalStateException("A registration's trigger mode " + authored
                        + " on a trigger that is once for one or more permanents");
            }
            return TriggerMode.ONCE_PER_BATCH;
        }
    }

    /**
     * What the legacy view adds for this ability's life conditions, or {@code null} when it has none: the event whose
     * first occurrence each turn the conditions count ({@link LifeTrigger#firstEachTurn()}). Only the "you gain life"
     * and "you lose life" collectors count; they read no "one or more", per-creature limit or "triggers only once", and
     * a limit next to a count, conditions that count different events or that count and don't, throw.
     */
    private LifeView lifeView() {
        List<LifeTrigger> life = conditions.stream().map(TriggerCondition::trigger)
                .filter(LifeTrigger.class::isInstance).map(LifeTrigger.class::cast).toList();
        if (life.isEmpty()) {
            return null;
        }
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The legacy view has no " + options.multiplicity() + " form for "
                    + conditions);
        }
        TriggerLimit limit = options.limit();
        if (limit == TriggerLimit.ONCE || limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            throw new IllegalStateException("No life collector reads " + limit + ": " + conditions);
        }
        List<LifeTrigger> counted = life.stream().filter(LifeTrigger::firstEachTurn).toList();
        if (counted.isEmpty()) {
            return new LifeView(null);
        }
        LifeTrigger first = counted.getFirst();
        if (counted.size() != life.size() || counted.stream().anyMatch(trigger -> !trigger.equals(first))) {
            throw new IllegalStateException("The life conditions of one ability must all count the same event: "
                    + conditions);
        }
        if (first.player() != PlayerRelation.YOU || first.event() == LifeTrigger.LifeEvent.PAYS) {
            throw new IllegalStateException("No life collector counts " + first);
        }
        if (limit != TriggerLimit.NONE) {
            throw new IllegalStateException("The legacy view has one once-each-turn wrapper: " + conditions
                    + " with " + limit);
        }
        return new LifeView(first.event());
    }

    /**
     * The legacy view of an ability's life conditions, as {@link #lifeView()} builds it.
     *
     * @param firstTime the event whose first occurrence each turn the conditions count, or {@code null}
     */
    private record LifeView(LifeTrigger.LifeEvent firstTime) {

        /** Whether the legacy view adds nothing to the effects. */
        boolean plain() {
            return firstTime == null;
        }

        /**
         * {@code effect} in the counting wrapper of {@link #firstTime}: the "you gain life" collector's keyed by
         * {@code key}, so it marks no other ability of the source, and the "you lose life" collector's, which marks
         * nothing.
         */
        CardEffect wrap(CardEffect effect, String key) {
            if (firstTime == null) {
                return effect;
            }
            return firstTime == LifeTrigger.LifeEvent.GAINS
                    ? new OncePerTurnTriggerEffect(effect, false, key, false, true)
                    : OncePerTurnTriggerEffect.firstLifeLoss(effect);
        }
    }

    /**
     * The ability in the form the "an opponent loses life" collector judges a "whenever an opponent loses life for the
     * first time during each of their turns" ability in: one {@link FirstOpponentLifeLossEachTurnTriggerEffect} of all
     * its effects, one stack object (CR 603.3), the ability's only condition, without options.
     */
    private List<EffectRegistration> legacyFirstOpponentLifeLossEffects() {
        if (conditions.size() > 1) {
            throw new IllegalStateException("The legacy view takes \"loses life for the first time during their turn\" "
                    + "only as the ability's one condition: " + conditions);
        }
        requireNoOptions("first-life-loss");
        List<CardEffect> resolved = effects.stream().map(EffectRegistration::effect).toList();
        return List.of(new EffectRegistration(new FirstOpponentLifeLossEachTurnTriggerEffect(resolved),
                effects.getFirst().triggerMode(), false));
    }

    /** "A nonland card", the discarded card the "first nonland discard each turn" collector counts. */
    private static final CardPredicate NONLAND = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    /**
     * The ability in the form the library, draw and discard collectors judge it in when one of its conditions is read
     * by another trigger ({@link LibraryTrigger#watchKey()}). Each effect is wrapped, outermost first, in:
     * <ol>
     *     <li>"this ability triggers only once" ({@code ONCE}), an {@link OnceOnlyTriggerEffect};</li>
     *     <li>the discarded card's filter ({@link LibraryTrigger#qualifier()}), a
     *     {@link TriggeringCardConditionalEffect};</li>
     *     <li>"triggers only once each turn", an {@link OncePerTurnTriggerEffect} keyed by {@link #limitKey()}, or "do
     *     this only once each turn", one marked as the action is taken;</li>
     *     <li>"for the first time each turn" ({@link LibraryTrigger#nth()} {@code 1} off a draw), the
     *     {@link OncePerTurnTriggerEffect} that counts the player's events of the turn: the first cycling, the first
     *     nonland discard, the first discard event or the first surveil;</li>
     *     <li>the event's wrapper: "you cycle a card" a {@link CyclingTriggerEffect}, "an opponent causes you to
     *     discard" an {@link OpponentCausedDiscardTriggerEffect}, "your Nth card each turn" an
     *     {@link NthCardDrawTriggerEffect} ("your first or second card" a {@link FirstOrSecondCardDrawTriggerEffect},
     *     from two conditions), and the draws in or out of a draw step an {@link ExceptFirstDrawStepTriggerEffect} or
     *     {@link FirstDrawStepDrawOnlyTriggerEffect};</li>
     *     <li>the intervening "if", a {@link ConditionalEffect} checked as the ability triggers and again as it
     *     resolves.</li>
     * </ol>
     * That is the order the library cards nested them in. Every condition of the ability must be a library trigger,
     * of one event, player and filter; only a draw's count may differ, as "first or second". A count, filter or
     * multiplicity no collector reads throws.
     */
    private List<EffectRegistration> legacyLibraryEffects() {
        if (!conditions.stream().allMatch(condition -> condition.trigger() instanceof LibraryTrigger)) {
            throw new IllegalStateException("The legacy view takes a library trigger read by another trigger only "
                    + "with other library triggers: " + conditions);
        }
        List<LibraryTrigger> library = conditions.stream().map(condition -> (LibraryTrigger) condition.trigger())
                .toList();
        LibraryTrigger first = library.getFirst();
        Set<Integer> counts = library.stream().map(LibraryTrigger::nth).collect(Collectors.toSet());
        if (library.stream().anyMatch(trigger -> trigger.event() != first.event() || trigger.player() != first.player()
                || !Objects.equals(trigger.qualifier(), first.qualifier()))
                || (counts.size() > 1 && !(first.event() == LibraryTrigger.LibraryEvent.DRAWS
                && counts.equals(Set.of(1, 2))))) {
            throw new IllegalStateException("The library conditions of one ability must have one event, player and "
                    + "filter, and one count but a draw's first or second: " + conditions);
        }
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The legacy view has no " + options.multiplicity() + " form for "
                    + conditions);
        }
        if (first.qualifier() != null && first.event() != LibraryTrigger.LibraryEvent.DISCARDS
                && first.event() != LibraryTrigger.LibraryEvent.DISCARDS_ONE_OR_MORE
                && first.event() != LibraryTrigger.LibraryEvent.DISCARDS_BY_OPPONENT) {
            throw new IllegalStateException("Only the discard collectors judge a discarded card's filter: " + first);
        }
        boolean firstTime = first.nth() == 1 && first.event() != LibraryTrigger.LibraryEvent.DRAWS;
        TriggerLimit limit = options.limit();
        if (firstTime && (limit == TriggerLimit.ONCE_EACH_TURN || limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN)) {
            throw new IllegalStateException("The legacy view has one once-each-turn wrapper: " + conditions
                    + " with " + limit);
        }
        if (limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            throw new IllegalStateException("No library collector reads " + limit + ": " + conditions);
        }
        List<EffectRegistration> wrapped = new ArrayList<>(effects.size());
        for (EffectRegistration registration : effects) {
            CardEffect effect = registration.effect();
            if (options.interveningIf() != null) {
                effect = new ConditionalEffect(options.interveningIf(), effect, true);
            }
            effect = libraryEventWrapper(first, counts, effect);
            if (firstTime) {
                effect = firstTimeEachTurnWrapper(first, effect);
            } else if (first.nth() > 0 && first.event() != LibraryTrigger.LibraryEvent.DRAWS) {
                throw new IllegalStateException("No library collector counts " + first);
            }
            if (limit == TriggerLimit.ONCE_EACH_TURN) {
                effect = OncePerTurnTriggerEffect.keyed(effect, limitKey());
            } else if (limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN) {
                effect = OncePerTurnTriggerEffect.markOnAcceptance(effect);
            }
            if (first.qualifier() != null) {
                effect = new TriggeringCardConditionalEffect(first.qualifier(), effect);
            }
            if (limit == TriggerLimit.ONCE) {
                effect = new OnceOnlyTriggerEffect(effect);
            }
            wrapped.add(new EffectRegistration(effect, registration.triggerMode(),
                    registration.simultaneousWithPrevious()));
        }
        return List.copyOf(wrapped);
    }

    /** The wrapper the collector of {@code trigger}'s watched event judges its event, and a draw's count, by. */
    private static CardEffect libraryEventWrapper(LibraryTrigger trigger, Set<Integer> counts, CardEffect effect) {
        boolean draw = trigger.event() == LibraryTrigger.LibraryEvent.DRAWS;
        if (!draw && trigger.nth() > 0 && trigger.nth() != 1) {
            throw new IllegalStateException("Only a draw counts past the first event of the turn: " + trigger);
        }
        return switch (trigger.event()) {
            case DRAWS -> {
                if (counts.size() > 1) {
                    yield new FirstOrSecondCardDrawTriggerEffect(effect);
                }
                yield trigger.nth() > 0 ? new NthCardDrawTriggerEffect(trigger.nth(), effect) : effect;
            }
            case CYCLES -> trigger.player() == PlayerRelation.YOU ? new CyclingTriggerEffect(effect) : effect;
            case DISCARDS_BY_OPPONENT -> new OpponentCausedDiscardTriggerEffect(effect);
            case DRAWS_EXCEPT_FIRST_IN_DRAW_STEP -> new ExceptFirstDrawStepTriggerEffect(effect);
            case DRAWS_FIRST_IN_DRAW_STEP -> {
                if (trigger.player() != PlayerRelation.YOU) {
                    throw new IllegalStateException("Only the \"you draw\" collector judges a first draw-step draw: "
                            + trigger);
                }
                yield new FirstDrawStepDrawOnlyTriggerEffect(effect);
            }
            default -> effect;
        };
    }

    /**
     * The wrapper the collectors judge "for the first time each turn" by, counting the player's events before the
     * source was on the battlefield too.
     */
    private static CardEffect firstTimeEachTurnWrapper(LibraryTrigger trigger, CardEffect effect) {
        boolean you = trigger.player() == PlayerRelation.YOU;
        if (you && trigger.event() == LibraryTrigger.LibraryEvent.CYCLES && trigger.qualifier() == null) {
            return OncePerTurnTriggerEffect.firstCycling(effect);
        }
        if (you && trigger.event() == LibraryTrigger.LibraryEvent.DISCARDS && NONLAND.equals(trigger.qualifier())) {
            return OncePerTurnTriggerEffect.firstNonlandDiscard(effect);
        }
        if (you && trigger.event() == LibraryTrigger.LibraryEvent.DISCARDS_ONE_OR_MORE && trigger.qualifier() == null) {
            return OncePerTurnTriggerEffect.firstDiscardEvent(effect);
        }
        if (you && trigger.event() == LibraryTrigger.LibraryEvent.SURVEILS) {
            return OncePerTurnTriggerEffect.firstSurveil(effect);
        }
        throw new IllegalStateException("No library collector counts the first event of the turn for " + trigger);
    }

    /**
     * The combat triggers whose collectors read every effect as an {@link AllyCombatDamageTriggerEffect}: a creature
     * you control (or one with the same name as the source) deals combat damage to a player, a player or battle, or a
     * player or planeswalker.
     */
    private static boolean readAsAllyCombatDamage(CombatTrigger trigger) {
        CombatTrigger.CombatEvent event = trigger.event();
        boolean toPlayer = event == CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER
                || event == CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE
                || event == CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER;
        return toPlayer && (trigger.relation() == ObjectRelation.YOU_CONTROL
                || trigger.equals(Triggers.creatureWithSameNameDealsCombatDamageToPlayer()));
    }

    /**
     * The legacy view of an ability's combat conditions, as {@link #combatView()} builds it.
     *
     * @param filter       the conditions' filter, or {@code null}
     * @param allyDamage   the effects are read as {@link AllyCombatDamageTriggerEffect}s
     * @param damageToYou  each effect is wrapped in a {@link BatchedCombatDamageToYouTriggerEffect}
     * @param oneOrMore    "one or more" that isn't the ally record's: once per block, or the batched wrapper
     * @param multiplicity the ability's multiplicity
     */
    private record CombatView(PermanentPredicate filter, boolean allyDamage, boolean damageToYou, boolean oneOrMore,
                              TriggerMultiplicity multiplicity) {

        /** Whether the legacy view adds nothing to the effects and modes. */
        boolean plain() {
            return filter == null && !allyDamage && multiplicity == TriggerMultiplicity.EACH;
        }

        CardEffect wrap(CardEffect effect) {
            if (allyDamage) {
                boolean oncePerStep = multiplicity == TriggerMultiplicity.ONE_OR_MORE;
                return effect instanceof AllyCombatDamageTriggerEffect authored
                        ? authored.withTriggerOptions(filter, oncePerStep)
                        : new AllyCombatDamageTriggerEffect(filter, effect, false, oncePerStep);
            }
            CardEffect wrapped = damageToYou ? new BatchedCombatDamageToYouTriggerEffect(effect) : effect;
            return filter != null ? new TriggeringPermanentConditionalEffect(filter, wrapped) : wrapped;
        }

        TriggerMode mode(TriggerMode authored) {
            if (authored != TriggerMode.NORMAL) {
                if (!plain() && multiplicity != TriggerMultiplicity.EACH) {
                    throw new IllegalStateException("A registration's trigger mode " + authored
                            + " and the ability's multiplicity " + multiplicity + " are two multiplicities");
                }
                return authored;
            }
            if (multiplicity == TriggerMultiplicity.EACH_BLOCKER) {
                return TriggerMode.PER_BLOCKER;
            }
            return oneOrMore && !damageToYou ? TriggerMode.ONCE_PER_BLOCK : TriggerMode.NORMAL;
        }
    }

    /**
     * The first trigger of {@code type} with filters among this ability's conditions, or {@code null} for none. The
     * legacy view wraps every effect in one set of filters, so every condition of that type must have the same
     * filters.
     */
    private <T extends Trigger> T filteredTrigger(Class<T> type) {
        List<T> ofType = conditions.stream().map(TriggerCondition::trigger).filter(type::isInstance)
                .map(type::cast).toList();
        T filtered = ofType.stream().filter(trigger -> !trigger.watchKey().equals(trigger)).findFirst().orElse(null);
        if (filtered != null && ofType.stream().anyMatch(t -> !filters(t).equals(filters(filtered)))) {
            throw new IllegalStateException("The " + type.getSimpleName()
                    + " conditions of one ability must have the same filters: " + ofType);
        }
        return filtered;
    }

    private static List<Object> filters(Trigger trigger) {
        return switch (trigger) {
            case LandPlayTrigger landPlay -> Arrays.asList(landPlay.filter(), landPlay.playedFrom(),
                    landPlay.notPlayedFrom());
            case AbilityTrigger activation -> Arrays.asList(activation.source(), activation.kind());
            case SpellTrigger spell -> Arrays.asList(spell.filter());
            default -> List.of();
        };
    }

    /**
     * The ability in the form the spell-cast collectors judge a "whenever [a player] casts a [quality] spell"
     * ability in (until steps 14 and 15 of effect-slot-refactor.md replace them): one {@link SpellCastTriggerEffect}
     * of all the ability's effects, one stack object (CR 603.3), with the cast trigger's {@link SpellFilter} as its
     * spell filter and conditions and the intervening "if" as its {@code intervening} condition (CR 603.4), which the
     * collector checks as the ability triggers and wraps around the effects for resolution. "This ability triggers
     * only once" wraps each effect in an {@link OnceOnlyTriggerEffect}; the other limits wrap the whole as
     * {@link #legacyEffects()} says.
     * <p>
     * An ability whose only effect is a plain "you may pay {N}. If you do, …" ({@link MayPayManaEffect} with a mana
     * cost and nothing else) or, functioning on the battlefield or in exile, a plain "you may …" ({@link MayEffect}
     * with a prompt and nothing else) is given as the collectors read an optional ability: the cost is the
     * {@code SpellCastTriggerEffect}'s mana cost and, on the battlefield or in exile, the effect (a written {@code SequenceEffect} stays one effect) is wrapped in a
     * {@code MayEffect} of the prompt, which the collector asks as the ability resolves (CR 603.5) after choosing its
     * targets (CR 603.3d). The graveyard, hand and command-zone readers take the cost from the
     * {@code SpellCastTriggerEffect} and a "you may" as an effect inside it, as those cards wrote them.
     */
    private List<EffectRegistration> legacySpellCastEffects(SpellFilter filter) {
        TriggerLimit limit = options.limit();
        boolean onStackPath = conditions.stream().anyMatch(condition -> condition.trigger() instanceof SpellTrigger
                && (condition.zones().functionsIn(Zone.BATTLEFIELD) || condition.zones().functionsIn(Zone.EXILE)));
        List<CardEffect> resolved = effects.stream().map(EffectRegistration::effect).toList();
        String manaCost = null;
        String optionalPrompt = null;
        if (resolved.size() == 1 && limit != TriggerLimit.ONCE) {
            CardEffect only = resolved.getFirst();
            if (only instanceof MayPayManaEffect pay && isPlainManaPayment(pay)) {
                manaCost = pay.manaCost();
                resolved = List.of(pay.wrapped());
                optionalPrompt = onStackPath ? pay.prompt() : null;
            } else if (onStackPath && only instanceof MayEffect may && isPlainMay(may)) {
                resolved = List.of(may.wrapped());
                optionalPrompt = may.prompt();
            }
        }
        if (limit == TriggerLimit.ONCE) {
            resolved = resolved.stream().map(effect -> (CardEffect) new OnceOnlyTriggerEffect(effect)).toList();
        }
        CardEffect effect = new SpellCastTriggerEffect(filter.spell(), resolved, manaCost, null, filter.stackEntry(),
                filter.turn() == SpellFilter.Turn.OPPONENTS, filter.turn() == SpellFilter.Turn.YOURS,
                options.interveningIf(), filter.nthThisTurn(), filter.nthOrLaterThisTurn(), false,
                filter.paidWithManaFromThis(), filter.expend(), filter.paidWithTreasureMana(),
                filter.twoOrMoreCardTypes(), filter.turn() == SpellFilter.Turn.NOT_CASTERS, filter.duringCombat(),
                filter.castFrom(), filter.notCastFrom(), filter.kicker() != SpellFilter.Kicker.ANY,
                filter.kicker() == SpellFilter.Kicker.EACH_KICK, filter.everyPlayersSpells());
        if (optionalPrompt != null) {
            effect = new MayEffect(effect, optionalPrompt);
        }
        if (limit == TriggerLimit.ONCE_EACH_TURN) {
            effect = OncePerTurnTriggerEffect.keyed(effect, limitKey());
        } else if (limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN) {
            effect = OncePerTurnTriggerEffect.markOnAcceptance(effect);
        }
        return List.of(new EffectRegistration(effect, effects.getFirst().triggerMode(), false));
    }

    /**
     * The ability in the form the spell-copy collector judges a "whenever [a player] copies a [quality] spell" ability
     * in: one {@link SpellCopyTriggerEffect} of all its effects, one stack object (CR 603.3), with the filter's spell
     * and stack-object conditions. The filter alone says which copied spells count, so the record takes every spell
     * type; its target is the target group the effects are bound to, which the collector reads.
     */
    private List<EffectRegistration> legacySpellCopyEffects(SpellFilter filter) {
        requireNoOptions("copy");
        List<CardEffect> resolved = effects.stream().map(EffectRegistration::effect).toList();
        return List.of(new EffectRegistration(new SpellCopyTriggerEffect(filter.spell(), resolved, null,
                filter.stackEntry(), true, false), effects.getFirst().triggerMode(), false));
    }

    /**
     * The ability in the form the land-play collectors judge a "whenever [a player] plays a land from [zone]" ability
     * in: one {@link LandPlayFromExileTriggerEffect} ("from exile") or {@link PlayFromOutsideHandTriggerEffect} ("from
     * anywhere other than your hand") of all its effects, one stack object (CR 603.3). Its target is the target group
     * the effects are bound to, which the collector reads.
     */
    private List<EffectRegistration> legacyLandPlayZoneEffects(LandPlayTrigger landPlay) {
        requireNoOptions("zone-filtered land-play");
        if (landPlay.filter() != null) {
            throw new IllegalStateException("The legacy view has no land-play record for a land filter with a zone: "
                    + landPlay);
        }
        List<CardEffect> resolved = effects.stream().map(EffectRegistration::effect).toList();
        CardEffect effect;
        if (landPlay.playedFrom() == Zone.EXILE) {
            effect = new LandPlayFromExileTriggerEffect(resolved);
        } else if (landPlay.notPlayedFrom() == Zone.HAND) {
            effect = new PlayFromOutsideHandTriggerEffect(resolved);
        } else {
            throw new IllegalStateException("The legacy view has no land-play record for " + landPlay);
        }
        return List.of(new EffectRegistration(effect, effects.getFirst().triggerMode(), false));
    }

    private void requireNoOptions(String what) {
        if (!options.equals(TriggerOptions.DEFAULT)) {
            throw new IllegalStateException("The legacy view has no " + what + " record with options: " + options);
        }
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

    private static CardEffect legacyKindWrapper(AbilityTrigger.AbilityKind kind, CardEffect effect) {
        return switch (kind) {
            case LOYALTY -> new TriggeringLoyaltyAbilityConditionalEffect(effect);
            case NINJUTSU -> new TriggeringNinjutsuAbilityConditionalEffect(effect);
            case OUTLAST -> new TriggeringOutlastAbilityConditionalEffect(effect);
            case POWER_UP -> new TriggeringPowerUpAbilityConditionalEffect(effect);
            case BOAST -> new TriggeringBoastAbilityConditionalEffect(effect);
            case WITH_TAP_COST -> new TriggeringTapAbilityConditionalEffect(effect);
            case WITHOUT_TAP_COST -> new TriggeringNonTapAbilityConditionalEffect(effect);
        };
    }

    /** This ability with {@code effects} in place of its effects. */
    public TriggeredAbility withEffects(List<EffectRegistration> effects) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets);
    }

    /** This ability with {@code chapterTargets} in place of its Saga chapter targets. */
    public TriggeredAbility withChapterTargets(SagaChapterTargets chapterTargets) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets);
    }
}
