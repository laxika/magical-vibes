package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Returns permanent(s) to their owners' hands. Constructed only through the static factories below,
 * which are the safe, self-documenting way to pair a {@link BounceScope} with the parameters that
 * scope actually uses (a plain record can't hide its canonical constructor, so this is a final class
 * with a private constructor).
 *
 * <ul>
 *   <li>{@link #target()} / {@link #targetAndControllerLosesLife(int)} /
 *       {@link #targetAndControllerDraws(int)} — bounce the chosen target permanent(s); the life-loss
 *       variant makes each bounced permanent's controller lose life (Vapor Snag) and the draw variant
 *       makes each bounced permanent's controller draw (Call to Heel), the controller being
 *       snapshotted before the bounce.</li>
 *   <li>{@link #self()} — bounce the source permanent.</li>
 *   <li>{@link #allPermanentsMatching(PermanentPredicate)} — bounce every permanent matching the
 *       filter across all battlefields (null filter = every permanent).</li>
 *   <li>{@link #permanentsTargetPlayerControls(PermanentPredicate)} — bounce every permanent the
 *       target player controls matching the filter (River's Rebuke).</li>
 *   <li>{@link #permanentsTargetPlayerOwns(PermanentPredicate)} — bounce every permanent the target
 *       player owns matching the filter, regardless of controller (Hurkyl's Recall).</li>
 *   <li>{@link #enchanted()} — bounce the permanent the source Aura is attached to (Sun Clasp).</li>
 *   <li>{@link #grantingEquipment()} — bounce the Equipment that granted the resolving ability,
 *       captured at activation time.</li>
 * </ul>
 */
public final class ReturnToHandEffect implements RemovalEffect, BoardWipeEffect, CastTimeXValueEffect,
        CombatDamageTriggerContextEffect, CastTimeCreatureTypeChoiceEffect {

    private final BounceScope scope;
    private final PermanentPredicate filter;
    private final int lifeLoss;
    private final int drawCount;
    private final UUID enchantedPermanentId;
    private final UUID grantingEquipmentId;
    private final DynamicAmount castTimeXValue;
    private final CardEffect thenEffect;
    private final int minimumControlledNontokenCount;

    private ReturnToHandEffect(BounceScope scope, PermanentPredicate filter, int lifeLoss, int drawCount) {
        this(scope, filter, lifeLoss, drawCount, null, null, null, 0, null);
    }

    private ReturnToHandEffect(BounceScope scope, PermanentPredicate filter, int lifeLoss, int drawCount,
                               UUID enchantedPermanentId) {
        this(scope, filter, lifeLoss, drawCount, enchantedPermanentId, null, null, 0, null);
    }

    private ReturnToHandEffect(BounceScope scope, PermanentPredicate filter, int lifeLoss, int drawCount,
                               UUID enchantedPermanentId, DynamicAmount castTimeXValue) {
        this(scope, filter, lifeLoss, drawCount, enchantedPermanentId, castTimeXValue, null, 0, null);
    }

    private ReturnToHandEffect(BounceScope scope, PermanentPredicate filter, int lifeLoss, int drawCount,
                               UUID enchantedPermanentId, DynamicAmount castTimeXValue,
                               CardEffect thenEffect, int minimumControlledNontokenCount,
                               UUID grantingEquipmentId) {
        this.scope = scope;
        this.filter = filter;
        this.lifeLoss = lifeLoss;
        this.drawCount = drawCount;
        this.enchantedPermanentId = enchantedPermanentId;
        this.grantingEquipmentId = grantingEquipmentId;
        this.castTimeXValue = castTimeXValue;
        this.thenEffect = thenEffect;
        this.minimumControlledNontokenCount = minimumControlledNontokenCount;
    }

    public static ReturnToHandEffect target() {
        return new ReturnToHandEffect(BounceScope.TARGET, null, 0, 0);
    }

    public static ReturnToHandEffect target(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.TARGET, filter, 0, 0);
    }

    public static ReturnToHandEffect targetAndControllerLosesLife(int lifeLoss) {
        return new ReturnToHandEffect(BounceScope.TARGET, null, lifeLoss, 0);
    }

    public static ReturnToHandEffect targetAndControllerDraws(int drawCount) {
        return new ReturnToHandEffect(BounceScope.TARGET, null, 0, drawCount);
    }

    public static ReturnToHandEffect targetWithCastTimeXValue(DynamicAmount castTimeXValue) {
        return new ReturnToHandEffect(BounceScope.TARGET, null, 0, 0, null, castTimeXValue);
    }

    /** Returns the targeted creatures that have the creature type chosen while casting this spell. */
    public static ReturnToHandEffect targetCreaturesOfChosenType() {
        return new ReturnToHandEffect(BounceScope.TARGET_CHOSEN_CREATURE_TYPE, null, 0, 0);
    }

    public static ReturnToHandEffect self() {
        return new ReturnToHandEffect(BounceScope.SELF, null, 0, 0);
    }

    /** Returns the permanent whose event produced the resolving triggered ability. */
    public static ReturnToHandEffect triggering() {
        return new ReturnToHandEffect(BounceScope.TRIGGERING, null, 0, 0);
    }

    /**
     * Returns the resolving spell card itself to its owner's hand instead of the graveyard
     * (instants/sorceries that bounce themselves off the stack — Redeem the Lost's won-clash reward).
     */
    public static ReturnToHandEffect selfSpell() {
        return new ReturnToHandEffect(BounceScope.SELF_SPELL, null, 0, 0);
    }

    public static ReturnToHandEffect allPermanentsMatching(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.ALL_MATCHING, filter, 0, 0);
    }

    /**
     * Returns all matching permanents, then resolves {@code thenEffect} when at least the given
     * number of nontoken permanents controlled by the resolving player were returned.
     */
    public static ReturnToHandEffect allPermanentsMatchingThen(PermanentPredicate filter,
                                                                int minimumControlledNontokenCount,
                                                                CardEffect thenEffect) {
        return new ReturnToHandEffect(BounceScope.ALL_MATCHING, filter, 0, 0, null, null, thenEffect,
                minimumControlledNontokenCount, null);
    }

    public static ReturnToHandEffect permanentsTargetPlayerControls(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.TARGET_PLAYERS_PERMANENTS, filter, 0, 0);
    }

    public static ReturnToHandEffect permanentsTargetPlayerOwns(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.TARGET_PLAYERS_OWNED, filter, 0, 0);
    }

    /**
     * Returns every Aura attached to the targeted permanent to its owner's hand, whoever controls
     * the Aura (Scarab of the Unseen). The Auras themselves are not targeted.
     */
    public static ReturnToHandEffect aurasAttachedToTarget() {
        return new ReturnToHandEffect(BounceScope.AURAS_ATTACHED_TO_TARGET, null, 0, 0);
    }

    /**
     * Returns the permanent the source Aura is attached to to its owner's hand (Sun Clasp).
     * Non-targeting; re-derives the host from the Aura's {@code attachedTo} at resolution.
     */
    public static ReturnToHandEffect enchanted() {
        return new ReturnToHandEffect(BounceScope.ENCHANTED, null, 0, 0);
    }

    /**
     * {@link #enchanted()} with the host permanent already resolved, used as last known information
     * when the Aura is no longer on the battlefield at resolution (Phantom Wings sacrifices itself
     * as the activation cost). Bound at activation time by {@code ActivatedAbilityExecutionService}.
     */
    public static ReturnToHandEffect enchantedSnapshot(UUID enchantedPermanentId) {
        return new ReturnToHandEffect(BounceScope.ENCHANTED, null, 0, 0, enchantedPermanentId);
    }

    /** Returns the Equipment that granted the resolving ability to its owner's hand. */
    public static ReturnToHandEffect grantingEquipment() {
        return new ReturnToHandEffect(BounceScope.GRANTING_EQUIPMENT, null, 0, 0,
                null, null, null, 0, null);
    }

    /**
     * Returns the granting Equipment using the permanent captured when the ability was activated.
     * If the Equipment has left the battlefield, this effect does nothing.
     */
    public static ReturnToHandEffect grantingEquipmentSnapshot(UUID grantingEquipmentId) {
        return new ReturnToHandEffect(BounceScope.GRANTING_EQUIPMENT, null, 0, 0,
                null, null, null, 0, grantingEquipmentId);
    }

    public UUID enchantedPermanentId() {
        return enchantedPermanentId;
    }

    public UUID grantingEquipmentId() {
        return grantingEquipmentId;
    }

    public BounceScope scope() {
        return scope;
    }

    public PermanentPredicate filter() {
        return filter;
    }

    public int lifeLoss() {
        return lifeLoss;
    }

    public int drawCount() {
        return drawCount;
    }

    public CardEffect thenEffect() {
        return thenEffect;
    }

    public int minimumControlledNontokenCount() {
        return minimumControlledNontokenCount;
    }

    @Override
    public DynamicAmount castTimeXValue() {
        return castTimeXValue;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return scope == BounceScope.SELF ? TriggerContext.SOURCE_SELF : null;
    }

    @Override
    public TargetSpec targetSpec() {
        // Only the single-target scope targets a battlefield permanent (PERMANENT reproduces its
        // requireBattlefieldTarget guard); the target-players scopes target a player (the old
        // validator imposed no guard there). SELF acts on the source permanent without choosing a
        // target, but marks it as self-targeting so trigger collectors retain the source id.
        if (scope == BounceScope.TARGET || scope == BounceScope.TARGET_CHOSEN_CREATURE_TYPE
                || scope == BounceScope.AURAS_ATTACHED_TO_TARGET) {
            if (scope == BounceScope.TARGET_CHOSEN_CREATURE_TYPE) {
                return TargetSpec.benign(TargetPredicates.creature());
            }
            return filter == null
                    ? TargetSpec.benign(TargetPredicates.permanent())
                    : TargetSpec.benign(TargetPredicates.permanent(), filter);
        }
        if (scope == BounceScope.TARGET_PLAYERS_PERMANENTS || scope == BounceScope.TARGET_PLAYERS_OWNED) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (scope == BounceScope.SELF) {
            return new TargetSpec(null, false, null, true, 1);
        }
        return TargetSpec.NONE;
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return scope == BounceScope.ENCHANTED;
    }

    @Override
    public RemovalKind removalKind() {
        // Only a single-target bounce is targeted removal; the mass/self scopes are board
        // sweeps or self-return, not single-target removal.
        return scope == BounceScope.TARGET || scope == BounceScope.TARGET_CHOSEN_CREATURE_TYPE
                ? RemovalKind.BOUNCE : null;
    }

    @Override
    public boolean sweepsBoard() {
        // Only the all-matching scope is a board sweep; the targeted / self scopes are not.
        return scope == BounceScope.ALL_MATCHING;
    }

    @Override
    public boolean requiresCastTimeCreatureTypeChoice() {
        return scope == BounceScope.TARGET_CHOSEN_CREATURE_TYPE;
    }
}
