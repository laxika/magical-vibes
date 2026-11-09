package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.LifeTrigger;
import com.github.laxika.magicalvibes.model.trigger.LifeTrigger.LifeEvent;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
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
 * Reads the life gain, loss and payment abilities (a {@link LifeTrigger}: a player gains, loses or pays life,
 * CR 119.3, 119.4), as {@link WatchedAbilities} finds them, for the {@link LifeWatch} collectors, which have no legacy
 * view of them. The collectors judge the ability's trigger-level parts from
 * the ability itself: its own event ({@link LifeEffect#event()}: "an opponent loses life for the first time during
 * each of their turns" is read by the "an opponent loses life" collector), "for the first time each turn"
 * ({@link LifeEffect#firstTimeEachTurn()}, a count of the player's events), the spell that causes the life gain
 * ({@link LifeEffect#spellFilter()}, judged by the "you gain life" collector) and the limit
 * ({@link LifeEffect#limit()}, marked by {@link TriggeredAbility#limitKey()} on the source).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) are the ability's options: its collector judges
 * them as the ability triggers, in the context of its event, from the records' {@code ability()} or through a
 * {@link TriggerJudge} on the abilities an effect list is read from. The reader wraps the intervening "if" around each
 * effect as the {@link ConditionalEffect} that checks it again as the ability resolves; the event condition is never
 * checked again.
 */
public final class LifeAbilityReader {

    private LifeAbilityReader() {
    }

    /**
     * One effect of a life gain, loss or payment ability as its collector reads it.
     *
     * @param effect            the effect, wrapped in the ability's event condition and intervening "if" as the class
     *                          says
     * @param ability           the ability the effect belongs to
     * @param event             the event of the ability's condition the collector reads: the watched one, or
     *                          {@link LifeEvent#LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN}, which the "an opponent loses
     *                          life" collector judges
     * @param firstTimeEachTurn whether only the player's first such event each turn triggers the ability, counting
     *                          those before the source was on the battlefield ({@link LifeTrigger#firstEachTurn()})
     * @param spellFilter       what the spell that causes the life gain must be ({@link LifeTrigger#spellFilter()}),
     *                          or {@code null} for any source
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record LifeEffect(CardEffect effect, TriggeredAbility ability, LifeEvent event, boolean firstTimeEachTurn,
                             CardPredicate spellFilter, boolean simultaneousWithPrevious) {

        public LifeEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(ability, "ability");
            Objects.requireNonNull(event, "event");
        }

        public TriggerLimit limit() {
            return ability.options().limit();
        }

        /** Whether taking the action uses up the ability's "do this only once each turn" (CR 603.2h). */
        public boolean marksLimitOnAcceptance() {
            return limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        }

        /**
         * Whether the ability's limit lets it trigger once a turn at most ("triggers only once each turn", "do this
         * only once each turn"), so nothing makes it trigger additional times. "For the first time each turn" is a
         * count of the player's events, not a limit on the ability.
         */
        public boolean triggersOnlyOnceEachTurn() {
            return limit() == TriggerLimit.ONCE_EACH_TURN || limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        }

        /**
         * Whether the ability's limit ("triggers only once each turn" as {@link TriggeredAbility#limitKey()} marked
         * it, or "do this only once each turn" as taking the action marked the source) keeps it from triggering for
         * {@code sourceId} now.
         */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Marks the ability's limit as used by {@code sourceId}; "do this only once each turn" is marked as taken. */
        public void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }

        /** This read with {@code effect} in place of its effect. */
        public LifeEffect withEffect(CardEffect effect) {
            return new LifeEffect(effect, ability, event, firstTimeEachTurn, spellFilter, simultaneousWithPrevious);
        }
    }

    /** The abilities of {@code card} that {@code watch} reads, in the order they were added. */
    public static List<TriggeredAbility> printed(Card card, LifeWatch watch) {
        return WatchedAbilities.printed(card, watch);
    }

    /** Every effect of the abilities of {@code card} that {@code watch} reads, as their collector reads them. */
    public static List<LifeEffect> readPrinted(Card card, LifeWatch watch) {
        return read(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of the abilities of {@code card} that {@code watch} reads, for a collector that judges no narrower
     * event, count or limit: none of the abilities it reads is written with one, and this throws on one.
     */
    public static List<CardEffect> printedEffects(Card card, LifeWatch watch) {
        return effects(printed(card, watch), watch.trigger());
    }

    /** {@link #printedEffects(Card, LifeWatch)} of the abilities {@code judge} lets trigger. */
    public static List<CardEffect> printedEffects(Card card, LifeWatch watch, TriggerJudge judge) {
        return effects(judge.filter(printed(card, watch)), watch.trigger());
    }

    /**
     * The registrations of the abilities of {@code card} that {@code watch} reads, for a collector that judges no
     * narrower event, count or limit (this throws on one).
     */
    public static List<EffectRegistration> printedRegistrations(Card card, LifeWatch watch) {
        return printedRegistrations(card, watch, TriggerJudge.NONE);
    }

    /** {@link #printedRegistrations(Card, LifeWatch)} of the abilities {@code judge} lets trigger. */
    public static List<EffectRegistration> printedRegistrations(Card card, LifeWatch watch, TriggerJudge judge) {
        List<LifeEffect> effects = read(judge.filter(printed(card, watch)), watch.trigger());
        effects.forEach(effect -> requireNothingToJudge(effect, watch.trigger()));
        return effects.stream().map(effect -> new EffectRegistration(effect.effect(), effect.simultaneousWithPrevious())).toList();
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    public static List<LifeEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<LifeEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /**
     * The effects of {@code ability}, as its collector reads it for the condition watched as {@code watched}. The life
     * collectors read of the limits "triggers only once each turn" and "do this only once each turn", neither next to
     * "for the first time each turn"; only the "you gain life" and "you lose life" collectors count events; "an
     * opponent loses life for the first time during each of their turns" takes no options, and the
     * ability rejects "one or more" as it is built.
     */
    public static List<LifeEffect> read(TriggeredAbility ability, Trigger watched) {
        LifeTrigger condition = watchedCondition(ability, watched);
        TriggerOptions options = ability.options();
        TriggerLimit limit = options.limit();
        if (limit != TriggerLimit.NONE && limit != TriggerLimit.ONCE_EACH_TURN
                && limit != TriggerLimit.DO_ONLY_ONCE_EACH_TURN) {
            throw new IllegalStateException("No life collector reads " + limit + ": " + ability);
        }
        if (ability.targetFilter() != null) {
            throw new IllegalStateException("No permanent's life collector reads an ability's own target filter: "
                    + ability);
        }
        if (condition.spellFilter() != null && condition.player() != PlayerRelation.YOU) {
            throw new IllegalStateException("No life collector judges the spell of " + condition + ": " + ability);
        }
        if (condition.firstEachTurn()) {
            if (condition.player() != PlayerRelation.YOU || condition.event() == LifeEvent.PAYS) {
                throw new IllegalStateException("No life collector counts " + condition + ": " + ability);
            }
            if (limit != TriggerLimit.NONE) {
                throw new IllegalStateException("No life collector reads a limit next to \"for the first time each "
                        + "turn\": " + ability);
            }
        }
        if (condition.event() == LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN
                && !options.equals(TriggerOptions.DEFAULT)) {
            throw new IllegalStateException("The \"an opponent loses life\" collector reads no options on \"for the "
                    + "first time during each of their turns\": " + ability);
        }
        Condition interveningIf = options.interveningIf();
        List<LifeEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            effects.add(new LifeEffect(effect, ability, condition.event(), condition.firstEachTurn(),
                    condition.spellFilter(), registration.simultaneousWithPrevious()));
        }
        return effects;
    }

    /**
     * The effects of {@code abilities} for the condition watched as {@code watched}, for a collector that judges no
     * narrower event, count or limit: none of its abilities is written with one, and this throws on one.
     */
    public static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
        List<LifeEffect> effects = read(abilities, watched);
        effects.forEach(effect -> requireNothingToJudge(effect, watched));
        return effectsOf(effects);
    }

    /** The effects of {@code effects}, in order. */
    public static List<CardEffect> effectsOf(List<LifeEffect> effects) {
        return effects.stream().map(LifeEffect::effect).toList();
    }

    /**
     * Throws when {@code effect}'s ability has a narrower event, a count, a spell filter or a limit, which the
     * collector of {@code watched} doesn't read.
     */
    public static void requireNothingToJudge(LifeEffect effect, Trigger watched) {
        requireWatchedEvent(effect, watched);
        requireNoCount(effect, watched);
        requireNoSpellFilter(effect, watched);
        requireNoLimit(effect, watched);
    }

    /**
     * Throws when {@code effect}'s ability triggers only on a life gain a matching spell causes, which the collector
     * of {@code watched} doesn't judge.
     */
    public static void requireNoSpellFilter(LifeEffect effect, Trigger watched) {
        if (effect.spellFilter() != null) {
            throw new IllegalStateException("The collector of " + watched + " judges no spell filter: "
                    + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability is of a narrower event than {@code watched}'s ("for the first time during
     * each of their turns"), which its collector doesn't read.
     */
    public static void requireWatchedEvent(LifeEffect effect, Trigger watched) {
        if (effect.event() != ((LifeTrigger) watched).event()) {
            throw new IllegalStateException("The collector of " + watched + " reads no " + effect.event() + ": "
                    + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability counts the player's events ("for the first time each turn"), which the
     * collector of {@code watched} doesn't read.
     */
    public static void requireNoCount(LifeEffect effect, Trigger watched) {
        if (effect.firstTimeEachTurn()) {
            throw new IllegalStateException("The collector of " + watched + " counts no events: " + effect.ability());
        }
    }

    /** Throws when {@code effect}'s ability has a limit, which the collector of {@code watched} doesn't read. */
    public static void requireNoLimit(LifeEffect effect, Trigger watched) {
        if (effect.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The collector of " + watched + " judges no limit: " + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability has "do this only once each turn", which the collector of {@code watched}
     * doesn't mark as the action is taken.
     */
    public static void requireNoLimitOnAcceptance(LifeEffect effect, Trigger watched) {
        if (effect.marksLimitOnAcceptance()) {
            throw new IllegalStateException("The collector of " + watched + " marks no \"do this only once each "
                    + "turn\": " + effect.ability());
        }
    }

    /**
     * {@code ability}'s life condition watched as {@code watched} (one whose {@link Trigger#watchKey()} it is). Several
     * are read as one, so they must be the same condition.
     */
    private static LifeTrigger watchedCondition(TriggeredAbility ability, Trigger watched) {
        List<LifeTrigger> conditions = ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(LifeTrigger.class::isInstance).map(LifeTrigger.class::cast)
                .filter(life -> life.watchKey().equals(watched)).distinct().toList();
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(ability + " has no life condition watched as " + watched);
        }
        if (conditions.size() > 1) {
            throw new IllegalStateException("The life conditions of one ability watched as " + watched
                    + " must be the same: " + ability);
        }
        return conditions.getFirst();
    }
}
