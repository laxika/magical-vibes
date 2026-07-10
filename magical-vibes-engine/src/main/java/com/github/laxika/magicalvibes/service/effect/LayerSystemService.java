package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.layer.Layer;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The CR 613 layered computation engine (see {@code agent-docs/LAYER_SYSTEM.md}).
 *
 * <p>Current migration state: the whole-battlefield pass runs real CR 613 semantics for
 * <b>layer 4</b> (types), <b>layer 5</b> (colors), and <b>layer 6</b> (ability adding/removing) —
 * one {@link CharacteristicState} per permanent, effects collected from every source (static
 * abilities, floating effects) and applied across the whole board in CDA-first, then timestamp
 * order (CR 613.2b/613.7, battlefield-position fallback for equal timestamps). <b>Layer 3</b>
 * (text changes, CR 612) is applied at collection time: each source's {@code TextReplacement}s
 * rewrite the color and basic-land-type words of the effect instances that source contributes
 * (via {@link TextChangeTransformer}), and rewrite the source's own printed land types and
 * protection colors when its state is seeded — so layers 4-7, protection, and mana abilities
 * all see the rewritten text. Layer 5/6 static
 * effects are applied by invoking their legacy staticfx handlers into a throwaway accumulator and
 * harvesting the result into the states, so all scope/filter logic stays in one place; the
 * static-bonus assembly then suppresses those handlers' layered outputs (they are "managed") and
 * merges the finished states into the {@code StaticBonus}. <b>Sublayer 7b</b> (base P/T setting)
 * is resolved by {@link #applyLayer7b}: every setter — static, floating one-shot, animation,
 * permanent exchange — applies in one timestamp order and the per-component winner lands in
 * {@code LayeredBoardState.basePt7b}, which the assembly merges over the 7a/intrinsic base.
 * <b>Sublayer 7d</b> (P/T switching) is resolved by {@link #applyLayer7d}: the parity of the
 * active floating switch effects per permanent lands in {@code LayeredBoardState.switchedPt7d}
 * and {@code GameQueryService} swaps the finished 7a-7c values (CR 613.4d). Sublayers 7a/7c
 * still run through the legacy {@code StaticBonusAccumulator} in
 * {@link GameQueryService#computeStaticBonus}, with scope and filter checks reading the layered
 * states via {@link #activeStateFor} while a pass is active.
 *
 * <p>Unmanaged layer 5/6 sources (conditional wrappers, emblems) keep their legacy additive
 * behavior: their grants union in on top of the layered result, outside timestamp order.
 *
 * <p><b>Dependency (CR 613.8)</b> overrides timestamp order within layers 4, 5, and 6: before a
 * layer's non-CDA instances apply, {@link #orderByDependency} trial-applies each ordered pair
 * onto {@link RecordingCharacteristicState} copies and compares the per-permanent operation
 * fingerprints — effect A depends on effect B when applying B first changes whether A exists
 * (its source's abilities are gone, see {@link #staticSourceAbilitiesGone}), what A applies to,
 * or what A attempts to do. Dependent effects apply after what they depend on (topological
 * order, timestamp tie-break); dependency loops fall back to pure timestamp order (CR 613.8c).
 * See agent-docs/LAYER_SYSTEM.md "Dependency" for the algorithm and its limits.
 */
@Component
public class LayerSystemService {

    private static final Set<CardSubtype> BASIC_LAND_SUBTYPES = EnumSet.of(
            CardSubtype.SWAMP, CardSubtype.ISLAND, CardSubtype.FOREST,
            CardSubtype.MOUNTAIN, CardSubtype.PLAINS);

    /** Land types cleared by a land-type-setting effect (CR 305.7). LOCUS is the only nonbasic
     *  land type in the {@code CardSubtype} enum today. */
    private static final Set<CardSubtype> LAND_SUBTYPES = EnumSet.of(
            CardSubtype.SWAMP, CardSubtype.ISLAND, CardSubtype.FOREST,
            CardSubtype.MOUNTAIN, CardSubtype.PLAINS, CardSubtype.LOCUS);

    private static final ThreadLocal<Pass> ACTIVE_PASS = new ThreadLocal<>();

    /**
     * Evaluates scope filters of layer-4 effects against the in-progress states. Injected lazily:
     * the evaluation service depends on {@code GameQueryService}, which depends on this service.
     */
    @Autowired
    @Lazy
    private PredicateEvaluationService predicateEvaluationService;

    /**
     * The legacy staticfx handlers, reused by the layer 5/6 passes to compute one effect's
     * per-target contribution (scope matching, filters, chosen-color/parity lookups) into a
     * throwaway accumulator. Injected lazily: handlers depend on {@code GameQueryService}.
     */
    @Autowired
    @Lazy
    private StaticEffectHandlerRegistry staticEffectRegistry;

    /**
     * Used by the layer-7b pass to gate March of the Machines' MV-based base P/T off for
     * artifacts that animate themselves (conditional self-animations like Rusted Relic).
     * Injected lazily: {@code GameQueryService} depends on this service.
     */
    @Autowired
    @Lazy
    private GameQueryService gameQueryService;

    /**
     * The type-changing decision one layer-4 effect made for one permanent, expressed as the
     * legacy accumulator operations to replay during static-bonus assembly. Membership was
     * decided against the characteristic states <em>as of the effect's own application</em>
     * (CR 613.7 ordering) — re-evaluating the effect's filter against the finished states would
     * let self-referencing filters negate their own output (e.g. Bludgeon Brawl's
     * "each non-Equipment artifact is an Equipment").
     */
    public record L4Contribution(CardSubtype grantedSubtype, boolean subtypeOverriding,
                                 boolean landSubtypeOverriding, CardType grantedCardType,
                                 CardSupertype grantedSupertype) {

        public void replay(StaticBonusAccumulator accumulator) {
            if (grantedSubtype != null) {
                accumulator.addGrantedSubtype(grantedSubtype);
            }
            if (subtypeOverriding) {
                accumulator.setSubtypeOverriding(true);
            }
            if (landSubtypeOverriding) {
                accumulator.setLandSubtypeOverriding(true);
            }
            if (grantedCardType != null) {
                accumulator.addGrantedCardType(grantedCardType);
            }
            if (grantedSupertype != null) {
                accumulator.addGrantedSupertype(grantedSupertype);
            }
        }
    }

    /**
     * The finished layered board computation: per-permanent characteristic states, the resolved
     * land-type override per permanent (drives the land's intrinsic mana ability per CR 305.7),
     * the permanents animated by an {@link AnimateNoncreatureArtifactsEffect} (whose MV-based
     * base P/T is consumed as a 7b entry by the legacy accumulator assembly), the recorded
     * per-target decisions of the purely-type-changing effects the pass took over from the
     * legacy handlers (identity-keyed by effect instance), the layer 5/6 effect instances whose
     * color/ability outputs the pass applied in timestamp order ({@code managedL56Effects} —
     * their legacy handlers run with layered outputs suppressed during assembly), the ids of
     * permanents whose layer 5/6 characteristics were modified ({@code l56Touched} — vetoes the
     * assembly's {@code StaticBonus.NONE} early-out), the timestamp-resolved sublayer-7b
     * base P/T per permanent ({@code basePt7b} — merged over the 7a/intrinsic base by the
     * assembly; also vetoes the early-out), and the permanents with an ODD number of active
     * sublayer-7d switch effects ({@code switchedPt7d} — the assembly's P/T swap flag; also
     * vetoes the early-out).
     */
    /**
     * The layer-7b (base P/T setting) result for one permanent: the last-applied value per
     * component after all 7b entries were applied in CR 613.7 timestamp order. A {@code null}
     * component was not set by any 7b entry (a power-only exchange leaves toughness to the
     * earlier layers / intrinsic base).
     */
    public record BasePt(Integer power, Integer toughness) {
    }

    public record LayeredBoardState(Map<UUID, CharacteristicState> states,
                                    Map<UUID, CardSubtype> landTypeOverrides,
                                    Set<UUID> marchAnimatedIds,
                                    Set<CardEffect> managedL4Effects,
                                    Map<CardEffect, Map<UUID, L4Contribution>> l4Contributions,
                                    Map<PermanentPredicate, Map<UUID, Boolean>> l4FilterVerdicts,
                                    Set<CardEffect> managedL56Effects,
                                    Set<UUID> l56Touched,
                                    Map<UUID, BasePt> basePt7b,
                                    Set<UUID> switchedPt7d) {

        /** True if the layer-4 pass owns this effect's application — the legacy static handler
         *  must be skipped and {@link #replayL4Contribution} used instead. */
        public boolean isManagedL4(CardEffect effect) {
            return managedL4Effects.contains(effect);
        }

        /** True if the layer 5/6 pass applied this effect's color/ability contribution — the
         *  legacy handler must run with layered accumulator outputs suppressed. */
        public boolean isManagedL56(CardEffect effect) {
            return managedL56Effects.contains(effect);
        }

        /** Replays the layer-4 pass's decision of the given effect for the given permanent
         *  (no-op when the effect did not apply to it). */
        public void replayL4Contribution(CardEffect effect, UUID targetId, StaticBonusAccumulator accumulator) {
            Map<UUID, L4Contribution> perTarget = l4Contributions.get(effect);
            if (perTarget == null) return;
            L4Contribution contribution = perTarget.get(targetId);
            if (contribution != null) {
                contribution.replay(accumulator);
            }
        }
    }

    /**
     * One in-flight layered computation for a {@code GameData}. Registered on a ThreadLocal
     * <em>before</em> the board is computed so nested {@code computeStaticBonus} calls made by
     * handlers during the layer 5/6 passes reuse the in-flight board (reading the states as of
     * the layers applied so far) instead of starting a recursive pass. The bonus memo is only
     * consulted once the board is finished ({@code boardReady}) — mid-pass results reflect
     * partially applied layers and must not be cached.
     */
    public static final class Pass {
        private final GameData gameData;
        private final Pass parent;
        private LayeredBoardState board;
        private boolean boardReady;
        private final Map<UUID, GameQueryService.StaticBonus> bonusMemo = new HashMap<>();

        private Pass(GameData gameData, Pass parent) {
            this.gameData = gameData;
            this.parent = parent;
        }

        public LayeredBoardState board() {
            return board;
        }

        public boolean isBoardReady() {
            return boardReady;
        }

        public Map<UUID, GameQueryService.StaticBonus> bonusMemo() {
            return bonusMemo;
        }
    }

    /** Returns the active pass for the given game state on this thread, or {@code null}. */
    public Pass activePass(GameData gameData) {
        Pass pass = ACTIVE_PASS.get();
        return pass != null && pass.gameData == gameData ? pass : null;
    }

    /** Computes the layered board state and registers it as the active pass on this thread. */
    public Pass beginPass(GameData gameData) {
        Pass pass = new Pass(gameData, ACTIVE_PASS.get());
        ACTIVE_PASS.set(pass);
        boolean computed = false;
        try {
            computeBoardState(gameData, pass);
            pass.boardReady = true;
            computed = true;
        } finally {
            if (!computed) {
                endPass(pass);
            }
        }
        return pass;
    }

    public void endPass(Pass pass) {
        if (ACTIVE_PASS.get() == pass) {
            if (pass.parent != null) {
                ACTIVE_PASS.set(pass.parent);
            } else {
                ACTIVE_PASS.remove();
            }
        }
    }

    /**
     * Ambient hook for predicate evaluation: the layered state of the given permanent while a
     * pass is active on this thread, or {@code null} outside a pass. Subtype/color/keyword leaf
     * predicates route through this so later-layer filters see the characteristics decided in
     * earlier layers (during the pass itself, the state as of the layers applied so far).
     */
    public static CharacteristicState activeStateFor(UUID permanentId) {
        Pass pass = ACTIVE_PASS.get();
        return pass == null || pass.board == null ? null : pass.board.states().get(permanentId);
    }

    /**
     * The basic land type this permanent's land types were set to by the latest-timestamp
     * land-type-setting effect (Sea's Claim, Blood Moon, Tideshaper Mystic, ...), or {@code null}
     * if no such effect applies. Determines the land's mana ability per CR 305.7.
     */
    public CardSubtype landTypeOverrideFor(GameData gameData, UUID permanentId) {
        Pass active = activePass(gameData);
        if (active != null && active.board != null) {
            return active.board.landTypeOverrides().get(permanentId);
        }
        Pass pass = beginPass(gameData);
        try {
            return pass.board.landTypeOverrides().get(permanentId);
        } finally {
            endPass(pass);
        }
    }

    // ===== board computation =====

    private record PermanentSlot(UUID controllerId, Permanent permanent, int position) {
    }

    /**
     * One continuous-effect instance from one source, carrying the CR 613.7 timestamp it applies
     * with. {@code floating} is non-null for effects created by resolved spells/abilities
     * ({@code GameData.floatingEffects}); otherwise the effect comes from {@code source}'s
     * STATIC slot. {@code effect} is the layer-3 view of the ability — the source's text
     * replacements already applied by {@link TextChangeTransformer} — while {@code original}
     * is the untransformed instance from the card's slot, which keys the identity-based
     * managed/contribution maps the static-bonus assembly looks effects up by.
     */
    private record EffectInstance(PermanentSlot source, CardEffect effect, CardEffect original,
                                  FloatingContinuousEffect floating,
                                  boolean characteristicDefining, long timestamp, int position) {
    }

    /**
     * Runs the whole-battlefield layer 4-6 passes: seeds one {@link CharacteristicState} per
     * permanent from its current (post-copy) card plus the permanent's persisted grants, then
     * applies each layer's effects in CDA-first, timestamp, position order across the whole
     * board before moving to the next layer. Setting effects clear the relevant characteristic
     * class before adding (later-timestamp setters win).
     */
    private void computeBoardState(GameData gameData, Pass pass) {
        List<PermanentSlot> slots = orderedPermanents(gameData);
        Map<UUID, CharacteristicState> states = new HashMap<>();
        Map<UUID, PermanentSlot> slotsById = new HashMap<>();
        LayeredBoardState board = new LayeredBoardState(states, new HashMap<>(), new HashSet<>(),
                Collections.newSetFromMap(new IdentityHashMap<>()), new IdentityHashMap<>(),
                new IdentityHashMap<>(), Collections.newSetFromMap(new IdentityHashMap<>()),
                new HashSet<>(), new HashMap<>(), new HashSet<>());
        // Publish the in-flight board immediately: nested queries made by handlers during the
        // layer 5/6 passes read the states as of the layers applied so far.
        pass.board = board;

        for (PermanentSlot slot : slots) {
            Permanent permanent = slot.permanent();
            CharacteristicState state = new CharacteristicState(permanent.getCard(), permanent);
            // The constructor seeds card values + persistent grants; the legacy engine also
            // treats transient (until-end-of-turn) grants as part of the object's types until
            // they become floating effects in a later migration step.
            for (CardType granted : permanent.getGrantedCardTypes()) {
                state.addCardType(granted);
            }
            for (CardSubtype granted : permanent.getTransientSubtypes()) {
                state.addSubtype(granted);
            }
            // Legacy one-shot color/keyword state is seeded before ANY layer runs so filter
            // leaves answering from the states never see less than the intrinsic values
            // (colors and keywords are untouched by layer 4).
            seedLegacyColorAndAbilityState(permanent, state);
            // Layer 3 on the object's own type line: a text change replacing a basic land
            // type word (Mind Bend targeting a Forest) rewrites the printed subtype itself,
            // and with it the land's intrinsic mana ability (CR 612, 305.6).
            applyTextChangesToPrintedLandTypes(permanent, state, board.landTypeOverrides());
            states.put(permanent.getId(), state);
            slotsById.put(permanent.getId(), slot);
        }

        applyLayer4(gameData, slots, slotsById, states, board);

        // Runtime one-shot type state not yet migrated to floating effects, applied with legacy
        // precedence: the transient "becomes the basic land type of your choice" self-override
        // (Tideshaper Mystic) beats static land-type setters, and "loses all creature types"
        // (Amoeboid Changeling) strips creature types absolutely.
        for (PermanentSlot slot : slots) {
            Permanent permanent = slot.permanent();
            CardSubtype transientOverride = permanent.getTransientLandTypeOverride();
            if (transientOverride != null) {
                setLandType(states.get(permanent.getId()), permanent.getId(),
                        transientOverride, board.landTypeOverrides());
            }
            if (permanent.isLosesAllCreatureTypesUntilEndOfTurn()) {
                states.get(permanent.getId()).removeSubtypesIf(StaticEffectSupport::isCreatureSubtype);
            }
        }

        // Layers 5 (colors) and 6 (abilities): the classified instances in CDA-first,
        // timestamp order.
        applyLayer5(gameData, slots, slotsById, states, board);
        applyLayer6(gameData, slots, slotsById, states, board);

        // Sublayer 7b (base P/T setting): every setter — static aura, one-shot floating,
        // animation, permanent exchange — ordered by one timestamp sequence.
        applyLayer7b(gameData, slots, slotsById, board);

        // Sublayer 7d (P/T switching): each active switch is its own step on the finished
        // 7a-7c values, so only the per-permanent parity matters.
        applyLayer7d(gameData, slots, slotsById, board);
    }

    private static LayerClassifier.LayerClassification classifyOrNull(CardEffect effect) {
        try {
            return LayerClassifier.classify(effect, false);
        } catch (IllegalArgumentException unclassified) {
            // Effects without a layer classification (and wrappers around them, e.g. the
            // conditional self-animations) stay legacy-only for now.
            return null;
        }
    }

    private List<PermanentSlot> orderedPermanents(GameData gameData) {
        List<PermanentSlot> slots = new ArrayList<>();
        int position = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                slots.add(new PermanentSlot(playerId, permanent, position++));
            }
        }
        return slots;
    }

    /**
     * Collects every effect instance classified into the given layer from all STATIC slots and
     * floating effects, ordered per CR 613.2b/613.7: characteristic-defining instances first,
     * then timestamp order; battlefield position breaks ties (test setups may still add
     * permanents with timestamp 0). Within one object's equal-timestamp effects, ability
     * removals apply before grants: an aura's "loses all other abilities" must not eat the
     * keyword the same aura grants (Deep Freeze's defender — the printed text says "other").
     */
    private List<EffectInstance> collectInstances(GameData gameData, List<PermanentSlot> slots,
                                                  Map<UUID, PermanentSlot> slotsById, Layer layer) {
        List<EffectInstance> instances = new ArrayList<>();
        for (PermanentSlot slot : slots) {
            for (CardEffect effect : slot.permanent().getCard().getEffects(EffectSlot.STATIC)) {
                LayerClassifier.LayerClassification classification = classifyOrNull(effect);
                if (classification == null || !classification.layers().contains(layer)) {
                    continue;
                }
                // Layer 3 (CR 613.2c): the pass applies the WORDS of the ability as rewritten
                // by the source's text changes; the transform preserves the effect's class, so
                // the classification of the original stands.
                CardEffect rewritten = TextChangeTransformer.transform(
                        effect, slot.permanent().getTextReplacements());
                instances.add(new EffectInstance(slot, rewritten, effect, null,
                        classification.characteristicDefining(),
                        slot.permanent().getTimestamp(), slot.position()));
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                LayerClassifier.LayerClassification classification = classifyOrNull(floating.effect());
                if (classification == null || !classification.layers().contains(layer)) {
                    continue;
                }
                PermanentSlot source = floating.sourcePermanentId() != null
                        ? slotsById.get(floating.sourcePermanentId()) : null;
                instances.add(new EffectInstance(source, floating.effect(), floating.effect(), floating,
                        classification.characteristicDefining(),
                        floating.timestamp(), Integer.MAX_VALUE));
            }
        }
        instances.sort(Comparator.comparing((EffectInstance i) -> !i.characteristicDefining())
                .thenComparingLong(EffectInstance::timestamp)
                .thenComparingInt(EffectInstance::position)
                .thenComparingInt(i -> abilityRemovalRank(i.effect())));
        return instances;
    }

    /** Equal-timestamp tie-break within one source: lose-all, then keyword removals, then grants. */
    private static int abilityRemovalRank(CardEffect effect) {
        if (effect instanceof LosesAllAbilitiesEffect) return 0;
        if (effect instanceof RemoveKeywordEffect) return 1;
        return 2;
    }

    // ===== within-layer application: CDA first, then dependency-ordered (CR 613.2b/613.8) =====

    /** Applies one collected instance of a layer against the given (real or trial) board. */
    @FunctionalInterface
    private interface InstanceApplier {
        void apply(GameData gameData, EffectInstance instance, List<PermanentSlot> slots,
                   Map<UUID, PermanentSlot> slotsById, LayeredBoardState board);
    }

    /**
     * Applies a layer's collected instances: the characteristic-defining prefix first (CR
     * 613.2b — {@link #collectInstances} sorts CDAs to the front), then the remaining
     * instances in CR 613.8 dependency order (falling back to the collection's timestamp
     * order for independent effects and dependency loops). The dependency computation runs
     * against the states as of the CDA applications, matching when the non-CDA instances
     * would apply.
     */
    private void applyInstances(GameData gameData, List<EffectInstance> instances,
                                List<PermanentSlot> slots, Map<UUID, PermanentSlot> slotsById,
                                LayeredBoardState board, InstanceApplier applier) {
        int split = 0;
        while (split < instances.size() && instances.get(split).characteristicDefining()) {
            applier.apply(gameData, instances.get(split), slots, slotsById, board);
            split++;
        }
        List<EffectInstance> rest = instances.subList(split, instances.size());
        for (EffectInstance instance : orderByDependency(gameData, rest, slots, slotsById, board, applier)) {
            applier.apply(gameData, instance, slots, slotsById, board);
        }
    }

    /**
     * CR 613.8a "whether the effect exists": a STATIC-slot instance stops applying once its
     * source's abilities are gone as of the layers (and same-layer instances) applied so far —
     * a land-type-setting effect removed the source's printed abilities in layer 4 (CR 305.7),
     * or, for layer 6 and later only, a "loses all abilities" hit the source ({@code
     * includeLoseAll}; the removal happens in layer 6 and is not retroactive on layers 1-5, so
     * layer 4/5 instances of a lose-all'd source still apply — a changeling under a lose-all
     * keeps its types). Floating effects are the one-shot results of already-resolved
     * spells/abilities and exist independently of their source.
     */
    private static boolean staticSourceAbilitiesGone(EffectInstance instance, LayeredBoardState board,
                                                     boolean includeLoseAll) {
        if (instance.floating() != null || instance.source() == null) {
            return false;
        }
        CharacteristicState state = board.states().get(instance.source().permanent().getId());
        if (state == null) {
            return false;
        }
        return state.isPrintedAbilitiesRemoved() || (includeLoseAll && state.isLosesAllAbilities());
    }

    /**
     * CR 613.8: orders a layer's non-CDA instances so every effect applies after the effects it
     * depends on. The relation is computed by trial application against copies of the current
     * states: instance A's fingerprint is the map of per-permanent operations it produces (see
     * {@link RecordingCharacteristicState}); A depends on B when the fingerprint changes once B
     * is applied first — covering existence (A's source's ability is gone and A produces
     * nothing), the affected set (A's scope matches different permanents), and what A attempts
     * to do. The relation is computed once against the layer-entry states (the CR strictly
     * re-evaluates dependencies after each application; with the tiny per-layer instance counts
     * the difference is theoretical) and topologically sorted; independent effects and ties
     * keep timestamp order, and dependency loops fall back to pure timestamp order (CR 613.8c).
     * Skipped entirely for 0 or 1 instances — the overwhelmingly common case.
     */
    private List<EffectInstance> orderByDependency(GameData gameData, List<EffectInstance> instances,
                                                   List<PermanentSlot> slots,
                                                   Map<UUID, PermanentSlot> slotsById,
                                                   LayeredBoardState board, InstanceApplier applier) {
        if (instances.size() <= 1) {
            return instances;
        }
        Pass pass = ACTIVE_PASS.get();
        if (pass == null || pass.board != board) {
            // Trials must swap the active pass's board; without one (never the case in
            // production — computeBoardState always runs under beginPass) keep timestamp order.
            return instances;
        }
        Map<EffectInstance, Map<UUID, List<String>>> baseFingerprints = new IdentityHashMap<>();
        for (EffectInstance instance : instances) {
            baseFingerprints.put(instance,
                    trialFingerprint(gameData, pass, instance, null, slots, slotsById, board, applier));
        }
        Map<EffectInstance, Set<EffectInstance>> dependsOn = new IdentityHashMap<>();
        for (EffectInstance a : instances) {
            for (EffectInstance b : instances) {
                if (a == b) continue;
                // An instance that produced no operations cannot change what another does.
                if (baseFingerprints.get(b).isEmpty()) continue;
                Map<UUID, List<String>> withB =
                        trialFingerprint(gameData, pass, a, b, slots, slotsById, board, applier);
                if (!withB.equals(baseFingerprints.get(a))) {
                    dependsOn.computeIfAbsent(a, key -> Collections.newSetFromMap(new IdentityHashMap<>()))
                            .add(b);
                }
            }
        }
        if (dependsOn.isEmpty()) {
            return instances;
        }
        // Kahn's algorithm preferring the collection's (timestamp, position) order; when no
        // remaining instance is dependency-free a loop exists — CR 613.8c ignores the
        // dependencies and the earliest timestamp applies next.
        List<EffectInstance> remaining = new ArrayList<>(instances);
        Set<EffectInstance> remainingSet = Collections.newSetFromMap(new IdentityHashMap<>());
        remainingSet.addAll(remaining);
        List<EffectInstance> ordered = new ArrayList<>(instances.size());
        while (!remaining.isEmpty()) {
            EffectInstance pick = null;
            for (EffectInstance candidate : remaining) {
                Set<EffectInstance> deps = dependsOn.get(candidate);
                if (deps == null || deps.stream().noneMatch(remainingSet::contains)) {
                    pick = candidate;
                    break;
                }
            }
            if (pick == null) {
                pick = remaining.get(0);
            }
            EffectInstance chosen = pick;
            ordered.add(chosen);
            remaining.removeIf(instance -> instance == chosen);
            remainingSet.remove(chosen);
        }
        return ordered;
    }

    /**
     * Trial-applies {@code first} (when non-null) and then {@code instance} onto recording
     * copies of the board's states, returning {@code instance}'s per-permanent operation
     * fingerprint. The trial board copies the layer-4 filter verdicts (CR 613.6 — later-layer
     * parts keep the layer-4-determined sets during trials too) and the land-type overrides;
     * managed/contribution bookkeeping goes to the trial board and is discarded. The active
     * pass's board is swapped for the duration so handlers and nested queries read the trial
     * states.
     */
    private Map<UUID, List<String>> trialFingerprint(GameData gameData, Pass pass, EffectInstance instance,
                                                     EffectInstance first, List<PermanentSlot> slots,
                                                     Map<UUID, PermanentSlot> slotsById,
                                                     LayeredBoardState board, InstanceApplier applier) {
        Map<UUID, CharacteristicState> trialStates = new HashMap<>();
        Map<UUID, RecordingCharacteristicState> recorders = new HashMap<>();
        for (Map.Entry<UUID, CharacteristicState> entry : board.states().entrySet()) {
            RecordingCharacteristicState recorder = new RecordingCharacteristicState(entry.getValue());
            recorders.put(entry.getKey(), recorder);
            trialStates.put(entry.getKey(), recorder);
        }
        Map<PermanentPredicate, Map<UUID, Boolean>> trialVerdicts = new IdentityHashMap<>();
        board.l4FilterVerdicts().forEach((filter, verdicts) -> trialVerdicts.put(filter, new HashMap<>(verdicts)));
        LayeredBoardState trialBoard = new LayeredBoardState(trialStates,
                new HashMap<>(board.landTypeOverrides()), new HashSet<>(board.marchAnimatedIds()),
                Collections.newSetFromMap(new IdentityHashMap<>()), new IdentityHashMap<>(),
                trialVerdicts, Collections.newSetFromMap(new IdentityHashMap<>()),
                new HashSet<>(board.l56Touched()), new HashMap<>(board.basePt7b()),
                new HashSet<>(board.switchedPt7d()));
        LayeredBoardState saved = pass.board;
        pass.board = trialBoard;
        try {
            if (first != null) {
                applier.apply(gameData, first, slots, slotsById, trialBoard);
            }
            recorders.values().forEach(RecordingCharacteristicState::startRecording);
            applier.apply(gameData, instance, slots, slotsById, trialBoard);
        } finally {
            pass.board = saved;
        }
        Map<UUID, List<String>> fingerprint = new HashMap<>();
        recorders.forEach((id, recorder) -> {
            if (!recorder.ops().isEmpty()) {
                fingerprint.put(id, recorder.ops());
            }
        });
        return fingerprint;
    }

    // ===== layer 4 =====

    private void applyLayer4(GameData gameData, List<PermanentSlot> slots,
                             Map<UUID, PermanentSlot> slotsById,
                             Map<UUID, CharacteristicState> states, LayeredBoardState board) {
        applyInstances(gameData, collectInstances(gameData, slots, slotsById, Layer.L4_TYPE),
                slots, slotsById, board, this::applyL4Instance);
    }

    private void applyL4Instance(GameData gameData, EffectInstance instance, List<PermanentSlot> slots,
                                 Map<UUID, PermanentSlot> slotsById,
                                 LayeredBoardState board) {
        // CR 613.8a(1)/CR 305.7: the source's printed abilities were removed by an
        // earlier-applied layer-4 land-type setter — this instance no longer exists.
        // (A layer-6 lose-all does NOT suppress layer-4 contributions; see the helper.)
        if (staticSourceAbilitiesGone(instance, board, false)) {
            manage(board, instance);
            return;
        }
        Map<UUID, CharacteristicState> states = board.states();
        Map<UUID, CardSubtype> landTypeOverrides = board.landTypeOverrides();
        switch (instance.effect()) {
            case GrantSubtypeEffect grant -> {
                manage(board, instance);
                for (PermanentSlot target : scopeTargets(instance, grant.scope(), grant.filter(), slots, slotsById, board)) {
                    CharacteristicState state = states.get(target.permanent().getId());
                    if (!grant.overriding()) {
                        state.addSubtype(grant.subtype());
                    } else if (BASIC_LAND_SUBTYPES.contains(grant.subtype())) {
                        setLandType(state, target.permanent().getId(), grant.subtype(), landTypeOverrides);
                    } else {
                        setCreatureType(state, grant.subtype());
                    }
                    record(board, instance, target, new L4Contribution(
                            grant.subtype(), grant.overriding(), false, null, null));
                }
            }
            case GrantCardTypeEffect grant -> {
                manage(board, instance);
                for (PermanentSlot target : scopeTargets(instance, grant.scope(), null, slots, slotsById, board)) {
                    states.get(target.permanent().getId()).addCardType(grant.cardType());
                    record(board, instance, target, new L4Contribution(
                            null, false, false, grant.cardType(), null));
                }
            }
            case GrantSupertypeToEnchantedPermanentEffect grant -> {
                manage(board, instance);
                for (PermanentSlot target : scopeTargets(instance, GrantScope.ENCHANTED_PERMANENT, null, slots, slotsById, board)) {
                    states.get(target.permanent().getId()).addSupertype(grant.supertype());
                    record(board, instance, target, new L4Contribution(
                            null, false, false, null, grant.supertype()));
                }
            }
            case GrantChosenSubtypeToOwnCreaturesEffect ignored -> {
                manage(board, instance);
                if (instance.source() == null) return;
                CardSubtype chosen = instance.source().permanent().getChosenSubtype();
                if (chosen == null) return;
                for (PermanentSlot target : scopeTargets(instance, GrantScope.OWN_CREATURES, null, slots, slotsById, board)) {
                    states.get(target.permanent().getId()).addSubtype(chosen);
                    record(board, instance, target, new L4Contribution(
                            chosen, false, false, null, null));
                }
            }
            case EnchantedPermanentBecomesTypeEffect becomes -> {
                manage(board, instance);
                for (PermanentSlot target : scopeTargets(instance, GrantScope.ENCHANTED_PERMANENT, null, slots, slotsById, board)) {
                    CharacteristicState state = states.get(target.permanent().getId());
                    if (becomes.isBasicLandSubtype()) {
                        setLandType(state, target.permanent().getId(), becomes.subtype(), landTypeOverrides);
                    } else {
                        setCreatureType(state, becomes.subtype());
                    }
                    record(board, instance, target, new L4Contribution(
                            becomes.subtype(), true, becomes.isBasicLandSubtype(), null, null));
                }
            }
            case EnchantedPermanentBecomesChosenTypeEffect ignored -> {
                manage(board, instance);
                if (instance.source() == null) return;
                CardSubtype chosen = instance.source().permanent().getChosenSubtype();
                if (chosen == null) return;
                for (PermanentSlot target : scopeTargets(instance, GrantScope.ENCHANTED_PERMANENT, null, slots, slotsById, board)) {
                    CharacteristicState state = states.get(target.permanent().getId());
                    if (BASIC_LAND_SUBTYPES.contains(chosen)) {
                        setLandType(state, target.permanent().getId(), chosen, landTypeOverrides);
                    } else {
                        setCreatureType(state, chosen);
                    }
                    record(board, instance, target, new L4Contribution(chosen, true, true, null, null));
                }
            }
            case NonbasicLandsBecomeTypeEffect becomes -> {
                manage(board, instance);
                for (PermanentSlot target : slots) {
                    if (isSource(instance, target)) continue;
                    CharacteristicState state = states.get(target.permanent().getId());
                    if (state.hasCardType(CardType.LAND)
                            && !state.getSupertypes().contains(CardSupertype.BASIC)) {
                        setLandType(state, target.permanent().getId(), becomes.subtype(), landTypeOverrides);
                        record(board, instance, target, new L4Contribution(
                                becomes.subtype(), true, true, null, null));
                    }
                }
            }
            case AnimateNoncreatureArtifactsEffect ignored -> {
                // NOT managed: the effect also contributes to 7b, so its legacy handler keeps
                // running during assembly (setting the animated flag) with the MV base P/T
                // injected there at this instance's timestamp.
                for (PermanentSlot target : slots) {
                    if (isSource(instance, target)) continue;
                    Permanent permanent = target.permanent();
                    CharacteristicState state = states.get(permanent.getId());
                    if (state.hasCardType(CardType.ARTIFACT) && !state.hasCardType(CardType.CREATURE)
                            && !isOneShotAnimated(permanent)) {
                        state.addCardType(CardType.CREATURE);
                        board.marchAnimatedIds().add(permanent.getId());
                    }
                }
            }
            case LoseAllCreatureTypesEffect ignored -> {
                // Only reachable as a floating effect (the STATIC slot never carries it).
                for (PermanentSlot target : floatingTargets(instance, slots, slotsById, board)) {
                    states.get(target.permanent().getId())
                            .removeSubtypesIf(StaticEffectSupport::isCreatureSubtype);
                }
            }
            case GrantKeywordEffect grant -> {
                // Only changeling grants are classified into layer 4: "gains all creature
                // types" defines the object's creature types (CR 702.73a), so the keyword is
                // made visible to later-layer subtype filters here — an Elf lord with an
                // EARLIER timestamp still boosts the target (no CR 613.8 dependency needed).
                // The keyword itself is (re-)applied and removable in layer 6.
                if (instance.floating() != null && grant.keywords().contains(Keyword.CHANGELING)) {
                    for (PermanentSlot target : floatingTargets(instance, slots, slotsById, board)) {
                        states.get(target.permanent().getId()).addKeyword(Keyword.CHANGELING);
                    }
                }
            }
            // Wrappers (ConditionalEffect, EnchantedPermanentConditionalEffect) never wrap
            // layer-4 effects today; they keep applying through the legacy handlers only.
            default -> {
            }
        }
    }

    /** Marks a static-sourced pure-L4 effect as owned by the pass: the legacy handler is
     *  skipped during assembly and the recorded contributions are replayed instead. Keyed by
     *  the ORIGINAL (pre-text-change) instance — the assembly iterates the card's slots. */
    private static void manage(LayeredBoardState board, EffectInstance instance) {
        if (instance.floating() == null) {
            board.managedL4Effects().add(instance.original());
        }
    }

    private static void record(LayeredBoardState board, EffectInstance instance, PermanentSlot target,
                               L4Contribution contribution) {
        if (instance.floating() != null) {
            return;
        }
        board.l4Contributions()
                .computeIfAbsent(instance.original(), key -> new HashMap<>())
                .put(target.permanent().getId(), contribution);
    }

    /** "Becomes a [land type]": clears the other land types, replaces the intrinsic mana
     *  ability, and removes the land's printed abilities — all as part of the layer-4 type
     *  change itself (CR 305.7), which is what makes an Urborg-style ability-granting land
     *  dependent on Blood Moon (CR 613.8). Later-timestamp setters overwrite the recorded
     *  override. */
    private void setLandType(CharacteristicState state, UUID permanentId, CardSubtype subtype,
                             Map<UUID, CardSubtype> landTypeOverrides) {
        state.removeSubtypesIf(LAND_SUBTYPES::contains);
        state.addSubtype(subtype);
        if (state.hasCardType(CardType.LAND)) {
            // CR 305.7 only strips lands; setting a basic land type on a non-land (nothing in
            // the pool does this today) would not remove its abilities.
            state.removePrintedAbilities();
        }
        landTypeOverrides.put(permanentId, subtype);
    }

    /** "Is a [creature type]" (overriding): clears the other creature types, then adds. */
    private void setCreatureType(CharacteristicState state, CardSubtype subtype) {
        state.removeSubtypesIf(StaticEffectSupport::isCreatureSubtype);
        state.addSubtype(subtype);
    }

    private static boolean isSource(EffectInstance instance, PermanentSlot target) {
        return instance.source() != null && instance.source().permanent() == target.permanent();
    }

    private List<PermanentSlot> scopeTargets(EffectInstance instance, GrantScope scope,
                                             PermanentPredicate filter, List<PermanentSlot> slots,
                                             Map<UUID, PermanentSlot> slotsById,
                                             LayeredBoardState board) {
        if (instance.floating() != null) {
            return floatingTargets(instance, slots, slotsById, board);
        }
        PermanentSlot source = instance.source();
        List<PermanentSlot> targets = new ArrayList<>();
        switch (scope) {
            case ENCHANTED_CREATURE, ENCHANTED_PERMANENT, EQUIPPED_CREATURE -> {
                Permanent sourcePermanent = source.permanent();
                if (sourcePermanent.isAttached()) {
                    PermanentSlot attached = slotsById.get(sourcePermanent.getAttachedTo());
                    if (attached != null && matchesL4Filter(attached, filter, board)) {
                        targets.add(attached);
                    }
                }
            }
            case ALL_PERMANENTS -> {
                for (PermanentSlot slot : slots) {
                    if (slot.permanent() != source.permanent() && matchesL4Filter(slot, filter, board)) {
                        targets.add(slot);
                    }
                }
            }
            case OWN_CREATURES, ALL_OWN_CREATURES, OPPONENT_CREATURES, ALL_CREATURES -> {
                for (PermanentSlot slot : slots) {
                    if (slot.permanent() == source.permanent()) continue;
                    boolean own = slot.controllerId().equals(source.controllerId());
                    boolean inScope = scope == GrantScope.ALL_CREATURES
                            || (scope == GrantScope.OPPONENT_CREATURES ? !own : own);
                    if (inScope && isCreatureForL4(slot.permanent(), board.states().get(slot.permanent().getId()))
                            && matchesL4Filter(slot, filter, board)) {
                        targets.add(slot);
                    }
                }
            }
            case OWN_TAPPED_CREATURES -> {
                for (PermanentSlot slot : slots) {
                    if (slot.permanent() == source.permanent()) continue;
                    if (slot.controllerId().equals(source.controllerId()) && slot.permanent().isTapped()) {
                        targets.add(slot);
                    }
                }
            }
            case ENCHANTED_PLAYER_CREATURES -> {
                Permanent sourcePermanent = source.permanent();
                if (sourcePermanent.isAttached()) {
                    for (PermanentSlot slot : slots) {
                        if (slot.permanent() == sourcePermanent) continue;
                        if (slot.controllerId().equals(sourcePermanent.getAttachedTo())
                                && isCreatureForL4(slot.permanent(), board.states().get(slot.permanent().getId()))
                                && matchesL4Filter(slot, filter, board)) {
                            targets.add(slot);
                        }
                    }
                }
            }
            default -> {
            }
        }
        return targets;
    }

    private List<PermanentSlot> floatingTargets(EffectInstance instance, List<PermanentSlot> slots,
                                                Map<UUID, PermanentSlot> slotsById,
                                                LayeredBoardState board) {
        FloatingContinuousEffect floating = instance.floating();
        if (floating == null) {
            return List.of();
        }
        if (floating.affectedPermanentId() != null) {
            PermanentSlot slot = slotsById.get(floating.affectedPermanentId());
            return slot == null ? List.of() : List.of(slot);
        }
        if (floating.scope() != null) {
            List<PermanentSlot> targets = new ArrayList<>();
            for (PermanentSlot slot : slots) {
                if (matchesL4Filter(slot, floating.scope(), board)) {
                    targets.add(slot);
                }
            }
            return targets;
        }
        return List.of();
    }

    /**
     * Evaluates a layer-4 scope filter against the in-progress characteristic states. Uses a
     * {@code null} FilterContext so non-type leaf predicates fall back to intrinsic values —
     * the pass must never recurse into {@code computeStaticBonus}. The verdict is memoized on
     * the board keyed by the filter <em>instance</em>: sibling effect parts of the same printed
     * ability share the filter object, and per CR 613.6 every part applies to the set of
     * objects determined when the first (layer 4) part applied — the legacy layer 5-7 handlers
     * pick these verdicts up via {@link #activeL4FilterVerdict}.
     */
    private boolean matchesL4Filter(PermanentSlot slot, PermanentPredicate filter,
                                    LayeredBoardState board) {
        if (filter == null) return true;
        boolean matches = predicateEvaluationService.matchesPermanentPredicate(
                board.states().get(slot.permanent().getId()), slot.permanent(), filter, null);
        board.l4FilterVerdicts()
                .computeIfAbsent(filter, key -> new HashMap<>())
                .put(slot.permanent().getId(), matches);
        return matches;
    }

    /**
     * The layer-4 verdict of the given filter instance for the given permanent, or {@code null}
     * when no pass is active or the filter was not evaluated for that permanent in layer 4.
     * Consulted by the legacy static-effect filter funnel so effect parts sharing a layer-4
     * part's filter apply to the layer-4-determined set (CR 613.6) instead of re-evaluating
     * against the finished states (where a self-referencing filter would negate its own output).
     */
    public static Boolean activeL4FilterVerdict(PermanentPredicate filter, UUID permanentId) {
        Pass pass = ACTIVE_PASS.get();
        if (pass == null || pass.board == null) return null;
        Map<UUID, Boolean> verdicts = pass.board.l4FilterVerdicts().get(filter);
        return verdicts == null ? null : verdicts.get(permanentId);
    }

    /**
     * Whether the permanent counts as a creature while applying layer 4: the type as computed so
     * far (natural, or added by an earlier-timestamp L4 effect) plus the legacy one-shot
     * animation flags. Conditional self-animations (Rusted Relic) are not visible here — their
     * conditions cannot be evaluated without recursing into the static-bonus computation.
     */
    private static boolean isCreatureForL4(Permanent permanent, CharacteristicState state) {
        return state.hasCardType(CardType.CREATURE) || isOneShotAnimated(permanent);
    }

    private static boolean isOneShotAnimated(Permanent permanent) {
        return permanent.isAnimatedUntilEndOfTurn()
                || permanent.isAnimatedUntilEndOfCombat()
                || permanent.isAnimatedUntilNextTurn()
                || permanent.isPermanentlyAnimated()
                || permanent.getCounterCount(CounterType.AWAKENING) > 0;
    }

    // ===== layers 5 and 6 =====

    /**
     * Seeds the legacy one-shot color and ability state that has not been migrated to floating
     * effects yet, then snapshots the seeded baseline. Mirrors {@code Permanent.getEffectiveColor}
     * and {@code Permanent.hasKeyword}: animation colors replace, the awakening counter makes
     * the land green, granted/until-next-turn keywords add, removed keywords subtract, the
     * legacy "loses all abilities until end of turn" flag clears everything at seed time (so
     * later-timestamp layered grants still apply, matching the old accumulator behavior).
     */
    private void seedLegacyColorAndAbilityState(Permanent permanent, CharacteristicState state) {
        if ((permanent.isAnimatedUntilEndOfTurn() || permanent.isAnimatedUntilEndOfCombat())
                && permanent.getAnimatedColor() != null) {
            state.replaceSeedColors(Set.of(permanent.getAnimatedColor()));
        } else if (permanent.getCounterCount(CounterType.AWAKENING) > 0) {
            state.replaceSeedColors(Set.of(CardColor.GREEN));
        } else if (permanent.isColorOverridden() && !permanent.getTransientColors().isEmpty()) {
            // Legacy one-shot color override (pre-migration state, e.g. AI simulation copies).
            state.replaceSeedColors(permanent.getTransientColors());
        } else {
            permanent.getTransientColors().forEach(state::addColor);
        }

        // The object's own printed protection is an ability: a layer-6 "loses all abilities"
        // with any timestamp removes it (protection grants applied later re-add). Seeded with
        // the object's text changes applied (CR 613.2c): Mind Bend rewriting "black" to "blue"
        // on Paladin en-Vec changes what its printed protection protects from.
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionFromColorsEffect protection && protection.scope() == null) {
                ProtectionFromColorsEffect rewritten = (ProtectionFromColorsEffect)
                        TextChangeTransformer.transform(protection, permanent.getTextReplacements());
                state.addProtectionColors(rewritten.colors());
            }
        }

        if (permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            // Legacy one-shot lose-all flag (no timestamp): treat as applied before every
            // layered instance, so static grants still stick — the old accumulator behavior.
            state.loseAllAbilities(0);
        } else {
            state.addKeywords(permanent.getGrantedKeywords());
            state.addKeywords(permanent.getUntilNextTurnKeywords());
            permanent.getRemovedKeywords().forEach(state::removeKeyword);
        }
        if (permanent.isLosesAllCreatureTypesUntilEndOfTurn()) {
            // Losing all creature types nullifies the Changeling grant (legacy semantics).
            state.removeKeyword(Keyword.CHANGELING);
        }

        state.snapshotSeededCharacteristics();
    }

    /**
     * Layer 3 applied to the object's own characteristics: a text replacement of one basic
     * land type word for another (Mind Bend on a Forest) rewrites the type word on the type
     * line, so the state's subtype — and, for a land, the intrinsic mana ability that type
     * carries (CR 305.6) — follows the new word. Recorded as the land's type override so the
     * tap funnel produces the new color; a later-layer L4 setter simply overwrites the entry
     * (layer order: text changes apply before type changes). Replacements compose in
     * application order, like everywhere else.
     */
    private void applyTextChangesToPrintedLandTypes(Permanent permanent, CharacteristicState state,
                                                    Map<UUID, CardSubtype> landTypeOverrides) {
        for (TextReplacement replacement : permanent.getTextReplacements()) {
            CardSubtype from = TextChangeTransformer.basicLandTypeForWord(replacement.fromWord());
            CardSubtype to = TextChangeTransformer.basicLandTypeForWord(replacement.toWord());
            if (from == null || to == null || !state.hasSubtype(from)) {
                continue;
            }
            state.removeSubtypesIf(subtype -> subtype == from);
            state.addSubtype(to);
            if (state.hasCardType(CardType.LAND)) {
                landTypeOverrides.put(permanent.getId(), to);
            }
        }
    }

    /**
     * Layer 5 (CR 613.2e): color-changing effects in timestamp order. Additive grants
     * ("in addition to its other colors" — Deep Freeze) add to the color set; setting effects
     * ("becomes red" — Incite, "is a black Zombie" — Nim Deathmantle) replace it (CR 105.3),
     * so of several setters the latest timestamp wins and earlier additive grants are wiped.
     */
    private void applyLayer5(GameData gameData, List<PermanentSlot> slots,
                             Map<UUID, PermanentSlot> slotsById,
                             Map<UUID, CharacteristicState> states, LayeredBoardState board) {
        applyInstances(gameData, collectInstances(gameData, slots, slotsById, Layer.L5_COLOR),
                slots, slotsById, board, this::applyL5Instance);
    }

    private void applyL5Instance(GameData gameData, EffectInstance instance, List<PermanentSlot> slots,
                                 Map<UUID, PermanentSlot> slotsById, LayeredBoardState board) {
        // CR 305.7: a land-type setter removed the source's printed abilities in layer 4.
        // (A layer-6 lose-all is not retroactive on layer 5.)
        if (staticSourceAbilitiesGone(instance, board, false)) {
            board.managedL56Effects().add(instance.original());
            return;
        }
        Map<UUID, CharacteristicState> states = board.states();
        if (instance.floating() != null) {
            CardColor color = switch (instance.effect()) {
                case GrantColorUntilEndOfTurnEffect becomes -> becomes.color();
                case GrantColorEffect grant -> grant.color();
                default -> null;
            };
            if (color == null) return;
            boolean setting = LayerClassifier.classify(instance.effect(), false).colorSetting();
            for (PermanentSlot target : floatingTargets(instance, slots, slotsById, board)) {
                CharacteristicState state = states.get(target.permanent().getId());
                if (setting) {
                    state.overrideColors(Set.of(color));
                } else {
                    state.addColor(color);
                }
                board.l56Touched().add(target.permanent().getId());
            }
            return;
        }
        applyStaticInstanceViaHandlers(gameData, instance, slots, board, (target, harvested) -> {
            if (harvested.getGrantedColors().isEmpty()) {
                return;
            }
            CharacteristicState state = states.get(target.permanent().getId());
            if (harvested.isColorOverriding()) {
                state.overrideColors(harvested.getGrantedColors());
            } else {
                harvested.getGrantedColors().forEach(state::addColor);
            }
            board.l56Touched().add(target.permanent().getId());
        });
    }

    /**
     * Layer 6 (CR 613.2f): ability adding/removing effects, characteristic-defining self-scans
     * (Cairn Wanderer family) first, then timestamp order. Grants add; a keyword removal removes
     * as of its timestamp (a later grant re-adds); "loses all abilities" clears every ability
     * accumulated so far and records its timestamp on the state so 7a can suppress the object's
     * own CDAs — without retroactively undoing contributions the removed abilities already made
     * in layers 2-5 (CR 613: layers apply in order; a changeling under a lose-all keeps its
     * creature types).
     */
    private void applyLayer6(GameData gameData, List<PermanentSlot> slots,
                             Map<UUID, PermanentSlot> slotsById,
                             Map<UUID, CharacteristicState> states, LayeredBoardState board) {
        applyInstances(gameData, collectInstances(gameData, slots, slotsById, Layer.L6_ABILITIES),
                slots, slotsById, board, this::applyL6Instance);
    }

    private void applyL6Instance(GameData gameData, EffectInstance instance, List<PermanentSlot> slots,
                                 Map<UUID, PermanentSlot> slotsById, LayeredBoardState board) {
        // An unattached scope-less protection static is the object's OWN printed protection
        // ability — already seeded into its state (and removable by a lose-all there); the
        // attachment handler would no-op anyway.
        if (instance.effect() instanceof ProtectionFromColorsEffect protection
                && protection.scope() == null && instance.source() != null
                && !instance.source().permanent().isAttached()) {
            return;
        }
        // CR 613.8a(1): the ability no longer exists — its source lost all abilities (an
        // earlier-applied layer-6 removal; dependency ordering guarantees the removal applies
        // first regardless of timestamps) or had its printed abilities removed by a layer-4
        // land-type setter (CR 305.7). A lose-all'd lord grants nothing.
        if (staticSourceAbilitiesGone(instance, board, true)) {
            board.managedL56Effects().add(instance.original());
            return;
        }
        Map<UUID, CharacteristicState> states = board.states();
        if (instance.floating() != null) {
            for (PermanentSlot target : floatingTargets(instance, slots, slotsById, board)) {
                CharacteristicState state = states.get(target.permanent().getId());
                switch (instance.effect()) {
                    case LosesAllAbilitiesEffect ignored -> state.loseAllAbilities(instance.timestamp());
                    case RemoveKeywordEffect remove -> state.removeKeyword(remove.keyword());
                    case GrantKeywordEffect grant -> state.addKeywords(grant.keywords());
                    default -> {
                        continue;
                    }
                }
                board.l56Touched().add(target.permanent().getId());
            }
            return;
        }
        applyStaticInstanceViaHandlers(gameData, instance, slots, board, (target, harvested) -> {
            CharacteristicState state = states.get(target.permanent().getId());
            boolean touched = false;
            // Removal before grants within one harvested instance: "loses all other
            // abilities" never eats what the same printed ability grants.
            if (harvested.isLosesAllAbilities()) {
                state.loseAllAbilities(instance.timestamp());
                touched = true;
            }
            for (Keyword removed : harvested.getRemovedKeywords()) {
                state.removeKeyword(removed);
                touched = true;
            }
            if (!harvested.getKeywords().isEmpty()) {
                state.addKeywords(harvested.getKeywords());
                touched = true;
            }
            if (!harvested.getProtectionColors().isEmpty()) {
                state.addProtectionColors(harvested.getProtectionColors());
                touched = true;
            }
            if (!harvested.getGrantedActivatedAbilities().isEmpty()) {
                harvested.getGrantedActivatedAbilities().forEach(state::addActivatedAbility);
                touched = true;
            }
            if (!harvested.getGrantedEffects().isEmpty()) {
                harvested.getGrantedEffects().forEach(state::addStaticEffect);
                touched = true;
            }
            if (touched) {
                board.l56Touched().add(target.permanent().getId());
            }
        });
    }

    // ===== sublayer 7b =====

    /** One base-P/T-setting application collected for the 7b pass; a {@code null} component
     *  leaves that component untouched (power-only exchanges). */
    private record BasePtEntry(UUID targetId, Integer power, Integer toughness, long timestamp, int position) {
    }

    /**
     * Sublayer 7b (CR 613.4b): applies every base-P/T-setting effect in timestamp order —
     * static setters (Lignify, Deep Freeze) at their attach timestamp, floating one-shots
     * (Diminish, Wings of Velis Vel, migrated animation base P/T) at their resolution
     * timestamp, March of the Machines' MV-based P/T at March's own timestamp (CR 613.4: one
     * timestamp with its layer-4 type change), and permanent exchange overrides (Evra, Tree of
     * Redemption) at the exchange's stamped timestamp. The last-applied value per component
     * wins; the static-bonus assembly overrides the 7a/intrinsic base with the result. The
     * object's own {@code SetPowerToughnessToAmountEffect} is its 7a CDA and is NOT collected
     * here — the assembly applies (and lose-all-suppresses, CR 613.4a) it before merging 7b.
     */
    private void applyLayer7b(GameData gameData, List<PermanentSlot> slots,
                              Map<UUID, PermanentSlot> slotsById, LayeredBoardState board) {
        List<BasePtEntry> entries = new ArrayList<>();

        // Permanent exchange overrides keep their Permanent-field storage but order like any
        // other 7b entry via the timestamp stamped at exchange time (0 = before everything,
        // for pre-migration state and hand-built test setups).
        for (PermanentSlot slot : slots) {
            Permanent permanent = slot.permanent();
            if (permanent.isBasePowerOverriddenPermanently()) {
                entries.add(new BasePtEntry(permanent.getId(), permanent.getPermanentBasePowerOverride(),
                        null, permanent.getPermanentBasePowerOverrideTimestamp(), slot.position()));
            }
            if (permanent.isBaseToughnessOverriddenPermanently()) {
                entries.add(new BasePtEntry(permanent.getId(), null, permanent.getPermanentBaseToughnessOverride(),
                        permanent.getPermanentBaseToughnessOverrideTimestamp(), slot.position()));
            }
        }

        for (EffectInstance instance : collectInstances(gameData, slots, slotsById, Layer.L7B_SET_PT)) {
            // CR 613.8a(1): a static setter whose source's abilities are gone (lose-all in
            // layer 6, or a layer-4 land-type set removing printed abilities) contributes no
            // 7b entry.
            if (staticSourceAbilitiesGone(instance, board, true)) {
                continue;
            }
            switch (instance.effect()) {
                case SetBasePowerToughnessEffect setPt -> {
                    if (instance.floating() != null) {
                        for (PermanentSlot target : floatingTargets(instance, slots, slotsById, board)) {
                            entries.add(new BasePtEntry(target.permanent().getId(), setPt.power(),
                                    setPt.toughness(), instance.timestamp(), instance.position()));
                        }
                    } else {
                        applyStaticInstanceViaHandlers(gameData, instance, slots, board, false, (target, harvested) -> {
                            if (harvested.isBasePTOverridden()) {
                                entries.add(new BasePtEntry(target.permanent().getId(),
                                        harvested.getBasePowerOverride(), harvested.getBaseToughnessOverride(),
                                        instance.timestamp(), instance.position()));
                            }
                        });
                    }
                }
                case AnimateNoncreatureArtifactsEffect ignored -> {
                    // Gated off for artifacts that animate themselves — their own animation
                    // defines the base P/T, not March's MV.
                    for (PermanentSlot target : slots) {
                        if (isSource(instance, target)) continue;
                        Permanent permanent = target.permanent();
                        if (board.marchAnimatedIds().contains(permanent.getId())
                                && !gameQueryService.hasSelfBecomeCreatureEffect(gameData, permanent)) {
                            int manaValue = permanent.getCard().getManaValue();
                            entries.add(new BasePtEntry(permanent.getId(), manaValue, manaValue,
                                    instance.timestamp(), instance.position()));
                        }
                    }
                }
                // Non-own-slot SetPowerToughnessToAmountEffect classifies into 7b but has no
                // producer today; conditional wrappers around setters stay legacy-only.
                default -> {
                }
            }
        }

        entries.sort(Comparator.comparingLong(BasePtEntry::timestamp).thenComparingInt(BasePtEntry::position));
        for (BasePtEntry entry : entries) {
            BasePt previous = board.basePt7b().get(entry.targetId());
            board.basePt7b().put(entry.targetId(), new BasePt(
                    entry.power() != null ? entry.power() : previous == null ? null : previous.power(),
                    entry.toughness() != null ? entry.toughness() : previous == null ? null : previous.toughness()));
        }
    }

    /**
     * Sublayer 7d (CR 613.4d): P/T switching. Every switch is a floating effect created by a
     * resolving spell/ability (Twisted Image, Turtleshell Changeling) and applies as its own
     * step on the P/T calculated by the layers before it — switches are never merged or
     * reordered, so two switches cancel and only the per-permanent parity survives.
     * {@code LayeredBoardState.switchedPt7d} holds the permanents with an ODD number of active
     * switches; {@code GameQueryService.getEffectivePower/getEffectiveToughness} swap the
     * finished 7a-7c values for them (a 7b setter or 7c pump resolving AFTER the switch still
     * slots in before the swap — layers, not timestamps, order them).
     */
    private void applyLayer7d(GameData gameData, List<PermanentSlot> slots,
                              Map<UUID, PermanentSlot> slotsById, LayeredBoardState board) {
        for (EffectInstance instance : collectInstances(gameData, slots, slotsById, Layer.L7D_SWITCH_PT)) {
            if (instance.floating() == null) {
                // SwitchPowerToughnessEffect has no static-slot producer; a switch is always
                // the one-shot result of a resolved spell/ability.
                continue;
            }
            UUID affectedId = instance.floating().affectedPermanentId();
            if (affectedId == null || !slotsById.containsKey(affectedId)) {
                continue;
            }
            if (!board.switchedPt7d().add(affectedId)) {
                board.switchedPt7d().remove(affectedId);
            }
        }
    }

    @FunctionalInterface
    private interface HarvestConsumer {
        void accept(PermanentSlot target, StaticBonusAccumulator harvested);
    }

    private void applyStaticInstanceViaHandlers(GameData gameData, EffectInstance instance,
                                                List<PermanentSlot> slots, LayeredBoardState board,
                                                HarvestConsumer harvest) {
        applyStaticInstanceViaHandlers(gameData, instance, slots, board, true, harvest);
    }

    /**
     * Applies one static-slot layered instance by invoking its legacy staticfx handler(s)
     * against every potential target into a throwaway accumulator (reusing all scope/filter
     * logic), handing each per-target result to {@code harvest}. With {@code manage} the effect
     * instance is marked managed so the static-bonus assembly suppresses the same handler's
     * layer 5/6 outputs — other-layer outputs (a lord boost's 7c power) still run there. The
     * 7b pass harvests unmanaged: its handlers' only output (the base-P/T override) is
     * consumed from the board's 7b result, which the assembly merges over the accumulator.
     */
    private void applyStaticInstanceViaHandlers(GameData gameData, EffectInstance instance,
                                                List<PermanentSlot> slots, LayeredBoardState board,
                                                boolean manage, HarvestConsumer harvest) {
        StaticEffectHandler handler = staticEffectRegistry.getHandler(instance.effect());
        StaticEffectHandler selfHandler = staticEffectRegistry.getSelfHandler(instance.effect());
        if (handler == null && selfHandler == null) {
            return;
        }
        if (manage) {
            // Keyed by the ORIGINAL (pre-text-change) instance — the assembly's suppression
            // check looks up the effect it iterates off the card's slot.
            board.managedL56Effects().add(instance.original());
        }
        PermanentSlot source = instance.source();
        if (source == null) {
            return;
        }
        if (handler != null) {
            for (PermanentSlot target : slots) {
                if (target.permanent() == source.permanent()) continue;
                StaticBonusAccumulator harvested = new StaticBonusAccumulator();
                handler.apply(new StaticEffectContext(source.permanent(), target.permanent(),
                        source.controllerId().equals(target.controllerId()), gameData),
                        instance.effect(), harvested);
                harvest.accept(target, harvested);
            }
        }
        if (selfHandler != null) {
            StaticBonusAccumulator harvested = new StaticBonusAccumulator();
            selfHandler.apply(new StaticEffectContext(source.permanent(), source.permanent(), true, gameData),
                    instance.effect(), harvested);
            harvest.accept(source, harvested);
        }
    }
}
