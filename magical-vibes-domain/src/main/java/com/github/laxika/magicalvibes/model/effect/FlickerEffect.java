package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Exile permanent(s) and return them to the battlefield under their owner's control (CR 610.3) as
 * new objects — counters, attached Auras/Equipment and other state are lost, and tokens cease to
 * exist in exile. When {@code returnUnderController} is true (Restoration Angel), the card returns
 * under the effect controller's control instead, keeping a stolen creature permanently. For a
 * self-flicker, this flag also distinguishes "under your control" from "under its owner's control."
 *
 * <p>Unifies the former {@code ExileTargetPermanentAndReturnAtEndStepEffect},
 * {@code ExileSelfAndReturnAtEndStepEffect},
 * {@code ExilePermanentsTargetPlayerControlsAndReturnAtStepEffect} and
 * {@code ExileTargetPermanentAndReturnImmediatelyEffect}. The {@link FlickerScope} selects which
 * permanent(s) leave and the {@link ReturnTiming} selects whether they come back immediately or via
 * a delayed trigger at the beginning of the next {@code returnStep}.
 *
 * <p>The bonus fields ({@code bonusSubtype}/{@code bonusEffect}/{@code plusOnePlusOneCountersOnReturn})
 * apply when the permanent returns. The counter field is supported for both immediate and delayed
 * returns. When {@code bonusSubtype} is set with
 * {@code plusOnePlusOneCountersOnReturn}, counters apply only if the exiled permanent had that
 * subtype. {@code returnUnderController} is only meaningful for {@link ReturnTiming#IMMEDIATE}
 * TARGET flickers and self-flickers that return under the effect controller's control.
 * {@code grantHaste} gives each returning permanent haste and is only meaningful for
 * {@link ReturnTiming#AT_STEP}.
 */
public record FlickerEffect(
        FlickerScope scope,
        PermanentPredicate filter,
        ReturnTiming timing,
        TurnStep returnStep,
        boolean returnTapped,
        CardSubtype bonusSubtype,
        CardEffect bonusEffect,
        int plusOnePlusOneCountersOnReturn,
        boolean returnUnderController,
        boolean grantHaste,
        boolean returnAtOwnerNextEndStep,
        boolean plusOnePlusOneCountersOnlyOnCreatures,
        int loyaltyCountersOnPlaneswalkersOnReturn,
        Set<Keyword> grantedKeywordsOnReturn) implements CardEffect {

    public FlickerEffect {
        grantedKeywordsOnReturn = grantedKeywordsOnReturn == null
                ? Set.of()
                : Set.copyOf(grantedKeywordsOnReturn);
    }

    public FlickerEffect(FlickerScope scope, PermanentPredicate filter, ReturnTiming timing,
                         TurnStep returnStep, boolean returnTapped, CardSubtype bonusSubtype,
                         CardEffect bonusEffect, int plusOnePlusOneCountersOnReturn,
                         boolean returnUnderController, boolean grantHaste,
                         boolean returnAtOwnerNextEndStep,
                         boolean plusOnePlusOneCountersOnlyOnCreatures,
                         int loyaltyCountersOnPlaneswalkersOnReturn) {
        this(scope, filter, timing, returnStep, returnTapped, bonusSubtype, bonusEffect,
                plusOnePlusOneCountersOnReturn, returnUnderController, grantHaste,
                returnAtOwnerNextEndStep, plusOnePlusOneCountersOnlyOnCreatures,
                loyaltyCountersOnPlaneswalkersOnReturn, Set.of());
    }

    public FlickerEffect(FlickerScope scope, PermanentPredicate filter, ReturnTiming timing,
                         TurnStep returnStep, boolean returnTapped, CardSubtype bonusSubtype,
                         CardEffect bonusEffect, int plusOnePlusOneCountersOnReturn,
                         boolean returnUnderController, boolean grantHaste,
                         boolean returnAtOwnerNextEndStep) {
        this(scope, filter, timing, returnStep, returnTapped, bonusSubtype, bonusEffect,
                plusOnePlusOneCountersOnReturn, returnUnderController, grantHaste,
                returnAtOwnerNextEndStep, false, 0);
    }

    public FlickerEffect(FlickerScope scope, PermanentPredicate filter, ReturnTiming timing,
                         TurnStep returnStep, boolean returnTapped, CardSubtype bonusSubtype,
                         CardEffect bonusEffect, int plusOnePlusOneCountersOnReturn,
                         boolean returnUnderController, boolean grantHaste) {
        this(scope, filter, timing, returnStep, returnTapped, bonusSubtype, bonusEffect,
                plusOnePlusOneCountersOnReturn, returnUnderController, grantHaste, false);
    }

    /** Exile target permanent, return it at the beginning of the next end step (Glimmerpoint Stag). */
    public static FlickerEffect exileTargetReturnAtEndStep() {
        return exileTargetReturnAtEndStep(false);
    }

    /** Exile target permanent, return it (tapped iff {@code returnTapped}) at the next end step (Mystifying Maze). */
    public static FlickerEffect exileTargetReturnAtEndStep(boolean returnTapped) {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.AT_STEP,
                TurnStep.END_STEP, returnTapped, null, null, 0, false, false);
    }

    /** Exile target permanent, returning it under its owner's control at that owner's next end step. */
    public static FlickerEffect exileTargetReturnAtOwnerNextEndStep() {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.AT_STEP,
                TurnStep.END_STEP, false, null, null, 0, false, false, true);
    }

    /** Exile target permanent, return it at the beginning of the next end step with +1/+1 counters. */
    public static FlickerEffect exileTargetReturnAtEndStepWithCounters(int counters) {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.AT_STEP,
                TurnStep.END_STEP, false, null, null, counters, false, false);
    }

    /** Exile target permanent, returning it with +1/+1 counters only if it is a creature. */
    public static FlickerEffect exileTargetReturnAtEndStepWithCreatureCounters(int counters) {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.AT_STEP,
                TurnStep.END_STEP, false, null, null, counters, false, false,
                false, true, 0);
    }

    public static FlickerEffect exileTargetReturnAtEndStepWithPlusOnePlusOneAndLoyaltyCounters(int counters) {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.AT_STEP,
                TurnStep.END_STEP, false, null, null, counters, false, false,
                false, true, counters);
    }

    /** Exile this permanent, return it under your control at the beginning of the next end step (Argent Sphinx). */
    public static FlickerEffect exileSelfReturnAtEndStep() {
        return new FlickerEffect(FlickerScope.SELF, null, ReturnTiming.AT_STEP,
                TurnStep.END_STEP, false, null, null, 0, true, false);
    }

    /** Exile this permanent, return it tapped under its owner's control at the next end step. */
    public static FlickerEffect exileSelfReturnAtEndStepUnderOwnerControl(boolean returnTapped) {
        return new FlickerEffect(FlickerScope.SELF, null, ReturnTiming.AT_STEP,
                TurnStep.END_STEP, returnTapped, null, null, 0, false, false);
    }

    /**
     * Exile this permanent, immediately return it under the effect controller's control
     * (Deadeye Navigator granted ability — "Exile this creature, then return it to the battlefield
     * under your control").
     */
    public static FlickerEffect flickerSelfUnderYourControl() {
        return new FlickerEffect(FlickerScope.SELF, null, ReturnTiming.IMMEDIATE,
                TurnStep.END_STEP, false, null, null, 0, true, false);
    }

    /** Exile every permanent matching {@code filter} the target player controls, return each at {@code returnStep} (Sudden Disappearance). */
    public static FlickerEffect exilePlayersPermanentsReturnAtStep(PermanentPredicate filter, TurnStep returnStep) {
        return new FlickerEffect(FlickerScope.TARGET_PLAYERS_PERMANENTS, filter, ReturnTiming.AT_STEP,
                returnStep, false, null, null, 0, false, false);
    }

    /**
     * Exile every permanent matching {@code filter} the effect's controller controls (no target) and
     * return them all together at the beginning of the next {@code returnStep}, hasted
     * (Legion's Initiative).
     */
    public static FlickerEffect exileControllersPermanentsReturnAtStepWithHaste(
            PermanentPredicate filter, TurnStep returnStep) {
        return new FlickerEffect(FlickerScope.CONTROLLERS_PERMANENTS, filter, ReturnTiming.AT_STEP,
                returnStep, false, null, null, 0, false, true);
    }

    /** Exile target permanent, immediately return it under its owner's control (Ghostly Flicker). */
    public static FlickerEffect flickerTarget() {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.IMMEDIATE,
                TurnStep.END_STEP, false, null, null, 0, false, false);
    }

    public static FlickerEffect controllersChooseAnyNumberCreaturesRepeatedByX() {
        return new FlickerEffect(FlickerScope.CONTROLLERS_PERMANENTS,
                new PermanentIsCreaturePredicate(), ReturnTiming.IMMEDIATE,
                TurnStep.END_STEP, false, null, null, 0, false, false);
    }

    /**
     * Exile target permanent, immediately return it under the effect controller's control
     * (Restoration Angel — keeps a temporarily stolen creature permanently).
     */
    public static FlickerEffect flickerTargetUnderYourControl() {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.IMMEDIATE,
                TurnStep.END_STEP, false, null, null, 0, true, false);
    }

    /** Immediate flicker that returns the permanent with {@code counters} +1/+1 counters (Daydream). */
    public static FlickerEffect flickerTargetWithCounters(int counters) {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.IMMEDIATE,
                TurnStep.END_STEP, false, null, null, counters, false, false);
    }

    /** Immediate flicker that applies {@code bonusEffect} if the exiled permanent had {@code bonusSubtype} (Siren's Ruse). */
    public static FlickerEffect flickerTargetWithBonus(CardSubtype bonusSubtype, CardEffect bonusEffect) {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.IMMEDIATE,
                TurnStep.END_STEP, false, bonusSubtype, bonusEffect, 0, false, false);
    }

    /**
     * Immediate flicker that returns the permanent with {@code counters} +1/+1 counters only if it
     * had {@code bonusSubtype} (Essence Flux — Spirit gets a +1/+1 counter).
     */
    public static FlickerEffect flickerTargetWithBonusCounters(CardSubtype bonusSubtype, int counters) {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.IMMEDIATE,
                TurnStep.END_STEP, false, bonusSubtype, null, counters, false, false);
    }

    /** Immediate flicker that returns the permanent with the given keywords until end of turn. */
    public static FlickerEffect flickerTargetWithKeywords(Set<Keyword> keywords) {
        return new FlickerEffect(FlickerScope.TARGET, null, ReturnTiming.IMMEDIATE,
                TurnStep.END_STEP, false, null, null, 0, false, false,
                false, false, 0, keywords);
    }

    @Override
    public TargetSpec targetSpec() {
        if (scope == FlickerScope.TARGET) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        if (scope == FlickerScope.TARGET_PLAYERS_PERMANENTS) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }
}
