package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.DamagePortion;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Shared base for families of effects that modify damage. Each family is a {@link Rule}: when it
 * applies, how its option reads, and what it does. A rule whose {@code instances} function returns
 * several ids offers one option per effect instance, so two Delaying Shields are two choices.
 *
 * <p>Combat damage to one recipient is one event with a portion per source (CR 510.2). A
 * {@link Scope#PER_SOURCE} rule ("if a source would deal damage to you…") is asked about each
 * portion as damage from that source alone and changes only that portion; a {@link Scope#WHOLE}
 * rule ("prevent all damage that would be dealt to you") is asked once about the whole event. A
 * whole-event prevention leaves damage that can't be prevented alone. A per-source rule is offered
 * for one portion at a time: applying it to each source's damage gives the same result in any
 * order, so only its order against other rules is the player's to choose, and the next portion's
 * option appears once the first is applied.
 *
 * @param <C> what a rule reads about the damage it may modify
 */
abstract class DamageCandidateSource<C extends DamageCandidateSource.DamageContext>
        implements ReplacementCandidateSource<DamageEvent> {

    /** The single instance of a rule that is one effect however many sources contribute to it. */
    protected static final List<String> ONCE = List.of("");

    private static final String PORTION_SEPARATOR = "@";

    /** The damage a rule is asked about. */
    interface DamageContext {
        DamageEvent event();

        default int amount() {
            return event().amount();
        }

        default boolean combat() {
            return event().combat();
        }

        GameData gameData();
    }

    /** Whether a rule is about each source's damage or about all the damage the recipient is dealt. */
    protected enum Scope {
        PER_SOURCE,
        WHOLE
    }

    /**
     * One family of damage replacement or prevention effects.
     *
     * @param instances the effect instances that apply now; empty when the family doesn't apply
     * @param transform the damage as the instance would leave it, without touching the game; only
     *                  read for pure rules
     * @param apply     applies the instance to the game and returns the modified damage
     */
    protected record Rule<C>(String family, ReplacementOption.Kind kind, boolean pure, Scope scope,
                             Function<C, List<String>> instances,
                             BiFunction<C, String, String> label,
                             BiFunction<C, String, DamageEvent> transform,
                             BiFunction<C, String, ApplyResult<DamageEvent>> apply) {

        protected Rule(String family, ReplacementOption.Kind kind, boolean pure,
                       Function<C, List<String>> instances, BiFunction<C, String, String> label,
                       BiFunction<C, String, DamageEvent> transform,
                       BiFunction<C, String, ApplyResult<DamageEvent>> apply) {
            this(family, kind, pure, Scope.PER_SOURCE, instances, label, transform, apply);
        }

        String key(String instance) {
            return instance.isEmpty() ? family : family + ":" + instance;
        }

        /** This rule, asked once about all the damage the recipient is dealt. */
        Rule<C> whole() {
            return new Rule<>(family, kind, pure, Scope.WHOLE, instances, label, transform, apply);
        }
    }

    /** A rule that only changes the damage: its preview is its effect, followed by {@code log}. */
    protected static <C extends DamageContext> Rule<C> pureRule(String family, ReplacementOption.Kind kind,
                                                                Predicate<C> applies, Function<C, String> label,
                                                                Function<C, DamageEvent> transform,
                                                                BiFunction<C, DamageEvent, Void> log) {
        return new Rule<>(family, kind, true,
                ctx -> applies.test(ctx) ? ONCE : List.of(),
                (ctx, instance) -> label.apply(ctx),
                (ctx, instance) -> transform.apply(ctx),
                (ctx, instance) -> {
                    DamageEvent result = transform.apply(ctx);
                    log.apply(ctx, result);
                    return new ApplyResult.Applied<>(result);
                });
    }

    /** A rule with effects beyond the damage itself (a consumed shield, a rider, a prompt). */
    protected static <C extends DamageContext> Rule<C> effectRule(String family, ReplacementOption.Kind kind,
                                                                  Predicate<C> applies, Function<C, String> label,
                                                                  Function<C, DamageEvent> apply) {
        return new Rule<>(family, kind, false,
                ctx -> applies.test(ctx) ? ONCE : List.of(),
                (ctx, instance) -> label.apply(ctx),
                (ctx, instance) -> ctx.event(),
                (ctx, instance) -> new ApplyResult.Applied<>(apply.apply(ctx)));
    }

    /**
     * A family of "deals that much damage plus N" effects with one option per effect instance, so
     * the affected player can apply a multiplier between two of them (CR 616.1).
     */
    protected static <C extends DamageContext> Rule<C> bonusRule(
            String family, Function<C, List<GameQueryService.DamageBonus>> bonuses) {
        return new Rule<C>(family, ReplacementOption.Kind.REPLACEMENT, true,
                ctx -> bonuses.apply(ctx).stream().map(GameQueryService.DamageBonus::instance).toList(),
                (ctx, instance) -> {
                    GameQueryService.DamageBonus bonus = bonus(bonuses.apply(ctx), instance);
                    return "Deal " + bonus.amount() + " more damage"
                            + (bonus.sourceName() == null ? "" : " (" + bonus.sourceName() + ")");
                },
                (ctx, instance) -> ctx.event().withAmount(ctx.amount() + bonus(bonuses.apply(ctx), instance).amount()),
                (ctx, instance) -> new ApplyResult.Applied<>(
                        ctx.event().withAmount(ctx.amount() + bonus(bonuses.apply(ctx), instance).amount())));
    }

    private static GameQueryService.DamageBonus bonus(List<GameQueryService.DamageBonus> bonuses, String instance) {
        return bonuses.stream().filter(bonus -> bonus.instance().equals(instance)).findFirst()
                .orElse(new GameQueryService.DamageBonus(instance, null, 0));
    }

    /** One effect instance: its option label and the number it multiplies or subtracts by. */
    protected record NumericInstance(String label, int value) {}

    /**
     * Furnace of Rath-style multipliers ({@link GlobalDamageMultiplyingEffect}), one option per
     * effect instance, for any damage whoever deals it.
     */
    protected static <C extends DamageContext> Rule<C> globalMultiplierRule(GameQueryService gameQueryService) {
        return new Rule<C>("GlobalDamageMultiplier", ReplacementOption.Kind.REPLACEMENT, true,
                ctx -> List.copyOf(globalMultipliers(ctx.gameData(), gameQueryService).keySet()),
                (ctx, instance) -> globalMultipliers(ctx.gameData(), gameQueryService).get(instance).label(),
                (ctx, instance) -> ctx.event().withAmount(
                        ctx.amount() * globalMultipliers(ctx.gameData(), gameQueryService).get(instance).value()),
                (ctx, instance) -> new ApplyResult.Applied<>(ctx.event().withAmount(
                        ctx.amount() * globalMultipliers(ctx.gameData(), gameQueryService).get(instance).value())));
    }

    private static Map<String, NumericInstance> globalMultipliers(GameData gameData, GameQueryService gameQueryService) {
        Map<String, NumericInstance> multipliers = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, permanent)) return;
            collectGlobalMultipliers(gameData, permanent.getId().toString(), permanent.getCard(), multipliers);
        });
        if (gameData.planechase != null) {
            for (var plane : gameData.planechase.faceUp) {
                collectGlobalMultipliers(gameData, plane.getCard().getId().toString(), plane.getCard(), multipliers);
            }
        }
        return multipliers;
    }

    private static void collectGlobalMultipliers(GameData gameData, String sourceKey, Card card,
                                                 Map<String, NumericInstance> multipliers) {
        List<CardEffect> statics = card.getEffects(EffectSlot.STATIC);
        for (int i = 0; i < statics.size(); i++) {
            if (statics.get(i) instanceof GlobalDamageMultiplyingEffect global) {
                int factor = MaroGoneNutsSupport.apply(gameData, global, global.damageMultiplierFactor());
                if (factor > 1) {
                    multipliers.put(sourceKey + "#" + i,
                            new NumericInstance(card.getName() + ": multiply damage by " + factor, factor));
                }
            }
        }
    }

    protected static DamageEvent prevented(DamageContext ctx) {
        return ctx.event().withAmount(0);
    }

    protected abstract List<Rule<C>> rules();

    /** Whether this source's rules are about {@code event}'s kind of recipient. */
    protected abstract boolean handles(DamageEvent event);

    /** What the rules read about {@code event}, or {@code null} when the recipient is gone. */
    protected abstract C context(GameData gameData, DamageEvent event);

    /** The permanent with {@code permanentId} while it is on the battlefield. */
    protected abstract Permanent findPermanent(GameData gameData, UUID permanentId);

    /** The controller of the permanent with {@code permanentId}, or {@code null}. */
    protected abstract UUID controllerOf(GameData gameData, UUID permanentId);

    @Override
    public Class<DamageEvent> eventType() {
        return DamageEvent.class;
    }

    @Override
    public List<ReplacementOption> applicable(GameData gameData, DamageEvent event) {
        if (event.amount() <= 0 || !handles(event)) {
            return List.of();
        }
        List<ReplacementOption> options = new ArrayList<>();
        for (Rule<C> rule : rules()) {
            if (!event.hasPortions()) {
                collect(gameData, rule, event, "", options);
            } else if (rule.scope() == Scope.WHOLE) {
                collect(gameData, rule, wholeView(gameData, event, rule), "", options);
            } else {
                for (DamagePortion portion : event.portions()) {
                    int before = options.size();
                    if (portion.amount() > 0) {
                        collect(gameData, rule, portionView(gameData, event, portion),
                                PORTION_SEPARATOR + portion.sourceId(), options);
                    }
                    if (options.size() > before) {
                        break;
                    }
                }
            }
        }
        return options;
    }

    private void collect(GameData gameData, Rule<C> rule, DamageEvent view, String suffix,
                         List<ReplacementOption> options) {
        if (view.amount() <= 0) {
            return;
        }
        withUnpreventable(gameData, view, () -> {
            C ctx = context(gameData, view);
            if (ctx == null) {
                return null;
            }
            for (String instance : rule.instances().apply(ctx)) {
                if (view.appliedKeys().contains(rule.key(instance) + suffix)) {
                    continue;
                }
                String label = rule.label().apply(ctx, instance);
                options.add(new ReplacementOption(rule.key(instance) + suffix,
                        suffix.isEmpty() ? label : label + " (" + sourceName(view) + ")", rule.kind(), rule.pure()));
            }
            return null;
        });
    }

    @Override
    public DamageEvent transform(GameData gameData, DamageEvent event, ReplacementOption option) {
        return run(gameData, event, option, false) instanceof ApplyResult.Applied<DamageEvent> applied
                ? applied.event() : event;
    }

    @Override
    public ApplyResult<DamageEvent> apply(GameData gameData, DamageEvent event, ReplacementOption option) {
        return run(gameData, event, option, true);
    }

    private ApplyResult<DamageEvent> run(GameData gameData, DamageEvent event, ReplacementOption option,
                                         boolean forReal) {
        String key = option.key();
        String portionSourceId = null;
        int separator = key.lastIndexOf(PORTION_SEPARATOR);
        if (event.hasPortions() && separator >= 0) {
            portionSourceId = key.substring(separator + 1);
            key = key.substring(0, separator);
        }
        Selected<C> selected = select(key);
        Rule<C> rule = selected.rule();
        if (!event.hasPortions()) {
            return invoke(gameData, rule, event, selected.instance(), forReal);
        }
        if (portionSourceId == null) {
            DamageEvent view = wholeView(gameData, event, rule);
            ApplyResult<DamageEvent> result = invoke(gameData, rule, view, selected.instance(), forReal);
            return mapResult(result, after -> rule.kind() == ReplacementOption.Kind.PREVENTION
                    ? event.withPreventableAmount(after.amount())
                    : event.withAmount(after.amount()));
        }
        for (int i = 0; i < event.portions().size(); i++) {
            DamagePortion portion = event.portions().get(i);
            if (portion.sourceId().toString().equals(portionSourceId)) {
                int index = i;
                ApplyResult<DamageEvent> result = invoke(gameData, rule, portionView(gameData, event, portion),
                        selected.instance(), forReal);
                return mapResult(result, after -> event.withPortionAmount(index, after.amount()));
            }
        }
        return new ApplyResult.Applied<>(event);
    }

    private ApplyResult<DamageEvent> invoke(GameData gameData, Rule<C> rule, DamageEvent view, String instance,
                                            boolean forReal) {
        return withUnpreventable(gameData, view, () -> {
            C ctx = context(gameData, view);
            if (ctx == null) {
                return new ApplyResult.Applied<>(view);
            }
            return forReal
                    ? rule.apply().apply(ctx, instance)
                    : new ApplyResult.Applied<>(rule.transform().apply(ctx, instance));
        });
    }

    private static ApplyResult<DamageEvent> mapResult(ApplyResult<DamageEvent> result,
                                                      Function<DamageEvent, DamageEvent> merge) {
        return switch (result) {
            case ApplyResult.Applied<DamageEvent> applied -> new ApplyResult.Applied<>(merge.apply(applied.event()));
            case ApplyResult.Suspended<DamageEvent> suspended ->
                    new ApplyResult.Suspended<>(merge.apply(suspended.event()));
        };
    }

    /**
     * The whole combat event as damage from its first source alone; a prevention sees only the
     * damage that can be prevented.
     */
    private DamageEvent wholeView(GameData gameData, DamageEvent event, Rule<C> rule) {
        int amount = rule.kind() == ReplacementOption.Kind.PREVENTION ? event.preventableAmount() : event.amount();
        return singleSource(gameData, event, event.portions().getFirst(), amount, false);
    }

    /** One portion of a combat event as damage from that source alone. */
    private DamageEvent portionView(GameData gameData, DamageEvent event, DamagePortion portion) {
        return singleSource(gameData, event, portion, portion.amount(), portion.unpreventable());
    }

    private DamageEvent singleSource(GameData gameData, DamageEvent event, DamagePortion portion, int amount,
                                     boolean unpreventable) {
        Permanent live = findPermanent(gameData, portion.sourceId());
        Permanent source = live != null ? live : portion.sourceSnapshot();
        UUID controllerId = live != null ? controllerOf(gameData, live.getId()) : null;
        StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source.getCard(),
                controllerId != null ? controllerId : event.entry().getControllerId(), "Combat damage", List.of(),
                event.toPermanent() ? event.recipientPermanentId() : event.recipientPlayerId(), source.getId());
        entry.setSourcePermanentSnapshot(portion.sourceSnapshot());
        return new DamageEvent(entry, event.recipientPlayerId(), event.recipientPermanentId(),
                event.toPermanent() ? source : null, amount, unpreventable || event.unpreventable(),
                event.cantBeRedirected(), event.journalSlot(), event.appliedKeys(), true, List.of(),
                event.redirected());
    }

    private static String sourceName(DamageEvent view) {
        return view.entry().getCard() == null ? "a source" : view.entry().getCard().getName();
    }

    private static <T> T withUnpreventable(GameData gameData, DamageEvent view, Supplier<T> body) {
        boolean previous = gameData.unpreventableDamageInProgress;
        gameData.unpreventableDamageInProgress = previous || view.unpreventable();
        try {
            return body.get();
        } finally {
            gameData.unpreventableDamageInProgress = previous;
        }
    }

    private record Selected<C>(Rule<C> rule, String instance) {}

    private Selected<C> select(String key) {
        for (Rule<C> rule : rules()) {
            if (key.equals(rule.family())) {
                return new Selected<>(rule, "");
            }
            if (key.startsWith(rule.family() + ":")) {
                return new Selected<>(rule, key.substring(rule.family().length() + 1));
            }
        }
        throw new IllegalArgumentException("Unknown damage replacement: " + key);
    }
}
