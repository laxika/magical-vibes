package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
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
 * <p>Current migration state: {@link #computeBoardState} runs a whole-battlefield <b>layer 4</b>
 * pass with real CR 613 semantics — one {@link CharacteristicState} per permanent, type-changing
 * effects collected from every source (static abilities, floating effects) and applied across the
 * whole board in CDA-first, then timestamp order (CR 613.2b/613.7, position fallback for equal
 * timestamps). Layers 5–7 still run through the legacy {@code StaticBonusAccumulator} in
 * {@link GameQueryService#computeStaticBonus}, but their scope/filter checks read the
 * L4-corrected states via {@link #activeStateFor} while a pass is active.
 *
 * <p>Dependency (CR 613.8) is NOT implemented yet — ordering is timestamp-only.
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
     * The finished layer-4 board computation: per-permanent characteristic states, the resolved
     * land-type override per permanent (drives the land's intrinsic mana ability per CR 305.7),
     * the permanents animated by an {@link AnimateNoncreatureArtifactsEffect} (whose MV-based
     * base P/T is consumed as a 7b entry by the legacy accumulator assembly), and the recorded
     * per-target decisions of the purely-type-changing effects the pass took over from the
     * legacy handlers (identity-keyed by effect instance).
     */
    public record LayeredBoardState(Map<UUID, CharacteristicState> states,
                                    Map<UUID, CardSubtype> landTypeOverrides,
                                    Set<UUID> marchAnimatedIds,
                                    Set<CardEffect> managedL4Effects,
                                    Map<CardEffect, Map<UUID, L4Contribution>> l4Contributions,
                                    Map<PermanentPredicate, Map<UUID, Boolean>> l4FilterVerdicts) {

        /** True if the layer-4 pass owns this effect's application — the legacy static handler
         *  must be skipped and {@link #replayL4Contribution} used instead. */
        public boolean isManagedL4(CardEffect effect) {
            return managedL4Effects.contains(effect);
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
     * One in-flight layered computation for a {@code GameData}. Registered on a ThreadLocal so
     * nested {@code computeStaticBonus} calls (handlers evaluating predicates on other permanents)
     * reuse the board state and memoized bonuses instead of recomputing, and so predicate leaf
     * checks can answer type questions from the L4-corrected states ({@link #activeStateFor}).
     */
    public static final class Pass {
        private final GameData gameData;
        private final LayeredBoardState board;
        private final Map<UUID, GameQueryService.StaticBonus> bonusMemo = new HashMap<>();
        private final Pass parent;

        private Pass(GameData gameData, LayeredBoardState board, Pass parent) {
            this.gameData = gameData;
            this.board = board;
            this.parent = parent;
        }

        public LayeredBoardState board() {
            return board;
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

    /** Computes the layer-4 board state and registers it as the active pass on this thread. */
    public Pass beginPass(GameData gameData) {
        Pass pass = new Pass(gameData, computeBoardState(gameData), ACTIVE_PASS.get());
        ACTIVE_PASS.set(pass);
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
     * Ambient hook for predicate evaluation: the L4-corrected state of the given permanent while
     * a layered pass is active on this thread, or {@code null} outside a pass. Subtype/type leaf
     * predicates route through this so layer 5–7 filters see the types decided in layer 4.
     */
    public static CharacteristicState activeStateFor(UUID permanentId) {
        Pass pass = ACTIVE_PASS.get();
        return pass == null ? null : pass.board.states().get(permanentId);
    }

    /**
     * The basic land type this permanent's land types were set to by the latest-timestamp
     * land-type-setting effect (Sea's Claim, Blood Moon, Tideshaper Mystic, ...), or {@code null}
     * if no such effect applies. Determines the land's mana ability per CR 305.7.
     */
    public CardSubtype landTypeOverrideFor(GameData gameData, UUID permanentId) {
        Pass pass = activePass(gameData);
        LayeredBoardState board = pass != null ? pass.board : computeBoardState(gameData);
        return board.landTypeOverrides().get(permanentId);
    }

    // ===== board computation =====

    private record PermanentSlot(UUID controllerId, Permanent permanent, int position) {
    }

    /**
     * A layer-4 effect instance: one continuous effect from one source, carrying the CR 613.7
     * timestamp it applies with. {@code floating} is non-null for effects created by resolved
     * spells/abilities ({@code GameData.floatingEffects}); otherwise the effect comes from
     * {@code source}'s STATIC slot.
     */
    private record L4Instance(PermanentSlot source, CardEffect effect,
                              FloatingContinuousEffect floating,
                              boolean characteristicDefining, long timestamp, int position) {
    }

    /**
     * Runs the whole-battlefield layer-4 pass: seeds one {@link CharacteristicState} per
     * permanent from its current (post-copy) card plus the permanent's persisted grants, then
     * applies every type-changing effect in CDA-first, timestamp, position order. Setting
     * effects clear the relevant type class before adding (later-timestamp setters win).
     */
    public LayeredBoardState computeBoardState(GameData gameData) {
        List<PermanentSlot> slots = orderedPermanents(gameData);
        Map<UUID, CharacteristicState> states = new HashMap<>();
        Map<UUID, PermanentSlot> slotsById = new HashMap<>();
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
            states.put(permanent.getId(), state);
            slotsById.put(permanent.getId(), slot);
        }

        List<L4Instance> instances = new ArrayList<>();
        for (PermanentSlot slot : slots) {
            for (CardEffect effect : slot.permanent().getCard().getEffects(EffectSlot.STATIC)) {
                LayerClassifier.LayerClassification classification = classifyOrNull(effect);
                if (classification == null || !classification.layers().contains(Layer.L4_TYPE)) {
                    continue;
                }
                instances.add(new L4Instance(slot, effect, null,
                        classification.characteristicDefining(),
                        slot.permanent().getTimestamp(), slot.position()));
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                LayerClassifier.LayerClassification classification = classifyOrNull(floating.effect());
                if (classification == null || !classification.layers().contains(Layer.L4_TYPE)) {
                    continue;
                }
                PermanentSlot source = floating.sourcePermanentId() != null
                        ? slotsById.get(floating.sourcePermanentId()) : null;
                instances.add(new L4Instance(source, floating.effect(), floating,
                        classification.characteristicDefining(),
                        floating.timestamp(), Integer.MAX_VALUE));
            }
        }
        // CR 613.2b: characteristic-defining instances first, then timestamp order (CR 613.7);
        // battlefield position breaks ties (test setups add permanents with timestamp 0).
        instances.sort(Comparator.comparing((L4Instance i) -> !i.characteristicDefining())
                .thenComparingLong(L4Instance::timestamp)
                .thenComparingInt(L4Instance::position));

        Map<UUID, CardSubtype> landTypeOverrides = new HashMap<>();
        Set<UUID> marchAnimatedIds = new HashSet<>();
        Set<CardEffect> managedL4Effects = Collections.newSetFromMap(new IdentityHashMap<>());
        Map<CardEffect, Map<UUID, L4Contribution>> l4Contributions = new IdentityHashMap<>();
        Map<PermanentPredicate, Map<UUID, Boolean>> l4FilterVerdicts = new IdentityHashMap<>();
        LayeredBoardState board = new LayeredBoardState(states, landTypeOverrides, marchAnimatedIds,
                managedL4Effects, l4Contributions, l4FilterVerdicts);
        for (L4Instance instance : instances) {
            applyL4Instance(instance, slots, slotsById, states, board);
        }

        // Runtime one-shot type state not yet migrated to floating effects, applied with legacy
        // precedence: the transient "becomes the basic land type of your choice" self-override
        // (Tideshaper Mystic) beats static land-type setters, and "loses all creature types"
        // (Amoeboid Changeling) strips creature types absolutely.
        for (PermanentSlot slot : slots) {
            Permanent permanent = slot.permanent();
            CardSubtype transientOverride = permanent.getTransientLandTypeOverride();
            if (transientOverride != null) {
                setLandType(states.get(permanent.getId()), permanent.getId(),
                        transientOverride, landTypeOverrides);
            }
            if (permanent.isLosesAllCreatureTypesUntilEndOfTurn()) {
                states.get(permanent.getId()).removeSubtypesIf(StaticEffectSupport::isCreatureSubtype);
            }
        }

        return board;
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

    private void applyL4Instance(L4Instance instance, List<PermanentSlot> slots,
                                 Map<UUID, PermanentSlot> slotsById,
                                 Map<UUID, CharacteristicState> states,
                                 LayeredBoardState board) {
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
            // Wrappers (ConditionalEffect, EnchantedPermanentConditionalEffect) never wrap
            // layer-4 effects today; they keep applying through the legacy handlers only.
            default -> {
            }
        }
    }

    /** Marks a static-sourced pure-L4 effect as owned by the pass: the legacy handler is
     *  skipped during assembly and the recorded contributions are replayed instead. */
    private static void manage(LayeredBoardState board, L4Instance instance) {
        if (instance.floating() == null) {
            board.managedL4Effects().add(instance.effect());
        }
    }

    private static void record(LayeredBoardState board, L4Instance instance, PermanentSlot target,
                               L4Contribution contribution) {
        if (instance.floating() != null) {
            return;
        }
        board.l4Contributions()
                .computeIfAbsent(instance.effect(), key -> new HashMap<>())
                .put(target.permanent().getId(), contribution);
    }

    /** "Becomes a [land type]": clears the other land types and replaces the intrinsic mana
     *  ability (CR 305.7). Later-timestamp setters overwrite the recorded override. */
    private void setLandType(CharacteristicState state, UUID permanentId, CardSubtype subtype,
                             Map<UUID, CardSubtype> landTypeOverrides) {
        state.removeSubtypesIf(LAND_SUBTYPES::contains);
        state.addSubtype(subtype);
        landTypeOverrides.put(permanentId, subtype);
    }

    /** "Is a [creature type]" (overriding): clears the other creature types, then adds. */
    private void setCreatureType(CharacteristicState state, CardSubtype subtype) {
        state.removeSubtypesIf(StaticEffectSupport::isCreatureSubtype);
        state.addSubtype(subtype);
    }

    private static boolean isSource(L4Instance instance, PermanentSlot target) {
        return instance.source() != null && instance.source().permanent() == target.permanent();
    }

    private List<PermanentSlot> scopeTargets(L4Instance instance, GrantScope scope,
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

    private List<PermanentSlot> floatingTargets(L4Instance instance, List<PermanentSlot> slots,
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
        if (pass == null) return null;
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
}
