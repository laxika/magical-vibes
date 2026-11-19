package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.Objects;

/**
 * Returns permanent(s) to their owners' hands. Which permanents is the {@link #subject()}; the static factories below
 * name the common ones and pair each with the parameters it uses (a plain record can't hide its canonical
 * constructor, so this is a final class with a private constructor).
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
 *   <li>{@link #enchantedAndAuras()} — bounce the permanent the source Aura is attached to and
 *       every Aura attached to that permanent (Mark of Eviction).</li>
 *   <li>{@link #grantingEquipment()} — bounce the Equipment that granted the resolving ability.</li>
 *   <li>{@link #selfSpell()} — the resolving instant or sorcery returns itself to its owner's hand instead of going
 *       to the graveyard; it has no subject.</li>
 * </ul>
 *
 * <p>A target subject ({@code target()}, or one holding it) declares a benign permanent target with the optional
 * target filter; a {@code controlledBy} / {@code ownedBy} set of target player's permanents declares a benign player
 * target; any other subject declares nothing.
 */
public final class ReturnToHandEffect implements RemovalEffect, BoardWipeEffect, CastTimeXValueEffect, CastTimeCreatureTypeChoiceEffect {

    private static final PermanentPredicate AURA = new PermanentHasSubtypePredicate(CardSubtype.AURA);

    private final PermanentSubject subject;
    private final PermanentPredicate filter;
    private final boolean orSuspendedCard;
    private final boolean ofChosenCreatureType;
    private final int lifeLoss;
    private final int drawCount;
    private final DynamicAmount castTimeXValue;
    private final CardEffect thenEffect;
    private final int minimumControlledNontokenCount;

    /** Returns the permanents {@code subject} names to their owners' hands. */
    public ReturnToHandEffect(PermanentSubject subject) {
        this(Objects.requireNonNull(subject, "subject (selfSpell() is the subjectless bounce)"),
                null, false, false, 0, 0, null, null, 0);
    }

    private ReturnToHandEffect(PermanentSubject subject, PermanentPredicate filter, boolean orSuspendedCard,
                               boolean ofChosenCreatureType, int lifeLoss, int drawCount,
                               DynamicAmount castTimeXValue, CardEffect thenEffect,
                               int minimumControlledNontokenCount) {
        if (filter != null && (subject == null || !subject.declaresTarget())) {
            throw new IllegalArgumentException("A target filter needs a target subject: " + subject);
        }
        if ((lifeLoss > 0 || drawCount > 0 || orSuspendedCard || ofChosenCreatureType)
                && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("Only a target bounce has a controller rider or a target mode: "
                    + subject);
        }
        this.subject = subject;
        this.filter = filter;
        this.orSuspendedCard = orSuspendedCard;
        this.ofChosenCreatureType = ofChosenCreatureType;
        this.lifeLoss = lifeLoss;
        this.drawCount = drawCount;
        this.castTimeXValue = castTimeXValue;
        this.thenEffect = thenEffect;
        this.minimumControlledNontokenCount = minimumControlledNontokenCount;
    }

    private static ReturnToHandEffect of(PermanentSubject subject) {
        return new ReturnToHandEffect(subject, null, false, false, 0, 0, null, null, 0);
    }

    private static PermanentPredicate orAny(PermanentPredicate filter) {
        return filter == null ? new PermanentTruePredicate() : filter;
    }

    public static ReturnToHandEffect target() {
        return of(Subjects.target());
    }

    /** Returns a target nonland permanent or suspended card to its owner's hand. */
    public static ReturnToHandEffect targetNonlandPermanentOrSuspendedCard() {
        return new ReturnToHandEffect(Subjects.target(), null, true, false, 0, 0, null, null, 0);
    }

    /**
     * Returns the chosen target permanent(s) together with the source permanent as a single
     * simultaneous event ("Return Wizard Mentor and target creature you control to their owners' hands").
     */
    public static ReturnToHandEffect targetAndSelf() {
        return of(Subjects.allOf(Subjects.source(), Subjects.target()));
    }

    public static ReturnToHandEffect target(PermanentPredicate filter) {
        return new ReturnToHandEffect(Subjects.target(), filter, false, false, 0, 0, null, null, 0);
    }

    public static ReturnToHandEffect targetAndControllerLosesLife(int lifeLoss) {
        return new ReturnToHandEffect(Subjects.target(), null, false, false, lifeLoss, 0, null, null, 0);
    }

    public static ReturnToHandEffect targetAndControllerDraws(int drawCount) {
        return new ReturnToHandEffect(Subjects.target(), null, false, false, 0, drawCount, null, null, 0);
    }

    public static ReturnToHandEffect targetWithCastTimeXValue(DynamicAmount castTimeXValue) {
        return new ReturnToHandEffect(Subjects.target(), null, false, false, 0, 0, castTimeXValue, null, 0);
    }

    /** Returns the targeted creatures that have the creature type chosen while casting this spell. */
    public static ReturnToHandEffect targetCreaturesOfChosenType() {
        return new ReturnToHandEffect(Subjects.target(), null, false, true, 0, 0, null, null, 0);
    }

    public static ReturnToHandEffect self() {
        return of(Subjects.source());
    }

    /** Returns the permanent whose event produced the resolving triggered ability. */
    public static ReturnToHandEffect triggering() {
        return of(Subjects.triggering());
    }

    /**
     * Returns the resolving spell card itself to its owner's hand instead of the graveyard
     * (instants/sorceries that bounce themselves off the stack — Redeem the Lost's won-clash reward).
     */
    public static ReturnToHandEffect selfSpell() {
        return of(null);
    }

    public static ReturnToHandEffect allPermanentsMatching(PermanentPredicate filter) {
        return of(Subjects.controlledBy(Subjects.eachPlayer(), orAny(filter)));
    }

    /**
     * Returns all matching permanents, then resolves {@code thenEffect} when at least the given
     * number of nontoken permanents controlled by the resolving player were returned.
     */
    public static ReturnToHandEffect allPermanentsMatchingThen(PermanentPredicate filter,
                                                                int minimumControlledNontokenCount,
                                                                CardEffect thenEffect) {
        return new ReturnToHandEffect(Subjects.controlledBy(Subjects.eachPlayer(), orAny(filter)), null, false,
                false, 0, 0, null, thenEffect, minimumControlledNontokenCount);
    }

    public static ReturnToHandEffect permanentsTargetPlayerControls(PermanentPredicate filter) {
        return of(Subjects.controlledBy(Subjects.targetPlayer(), orAny(filter)));
    }

    public static ReturnToHandEffect permanentsTargetPlayerOwns(PermanentPredicate filter) {
        return of(Subjects.ownedBy(Subjects.targetPlayer(), orAny(filter)));
    }

    /**
     * Returns every Aura attached to the targeted permanent to its owner's hand, whoever controls
     * the Aura (Scarab of the Unseen). The Auras themselves are not targeted.
     */
    public static ReturnToHandEffect aurasAttachedToTarget() {
        return of(Subjects.attachmentsOf(Subjects.target(), AURA));
    }

    /** Returns the permanent the source Aura is attached to to its owner's hand (Sun Clasp). Non-targeting. */
    public static ReturnToHandEffect enchanted() {
        return of(Subjects.attached());
    }

    /**
     * Returns the permanent the source Aura is attached to and every Aura attached to that
     * permanent, the Auras first, as one simultaneous event (Mark of Eviction).
     */
    public static ReturnToHandEffect enchantedAndAuras() {
        return of(Subjects.allOf(Subjects.attachmentsOf(Subjects.attached(), AURA), Subjects.attached()));
    }

    /** Returns the Equipment that granted the resolving ability to its owner's hand. */
    public static ReturnToHandEffect grantingEquipment() {
        return of(Subjects.granter());
    }

    /** Which permanents return; {@code null} only for {@link #selfSpell()}. */
    public PermanentSubject subject() {
        return subject;
    }

    /** Whether the resolving spell returns itself to its owner's hand ({@link #selfSpell()}). */
    public boolean returnsResolvingSpell() {
        return subject == null;
    }

    /** Whether the target may also be a suspended card in exile ({@link #targetNonlandPermanentOrSuspendedCard()}). */
    public boolean orSuspendedCard() {
        return orSuspendedCard;
    }

    /** Whether only the targets of the creature type chosen while casting return. */
    public boolean ofChosenCreatureType() {
        return ofChosenCreatureType;
    }

    /** This bounce with its subject replaced and every other parameter kept (an activation binding its granter). */
    public ReturnToHandEffect withSubject(PermanentSubject updatedSubject) {
        return new ReturnToHandEffect(updatedSubject, filter, orSuspendedCard, ofChosenCreatureType, lifeLoss,
                drawCount, castTimeXValue, thenEffect, minimumControlledNontokenCount);
    }

    /**
     * The permanent filter: the target filter of a target bounce, else the filter of the set a {@code controlledBy} or
     * {@code ownedBy} subject names, else {@code null}.
     */
    public PermanentPredicate filter() {
        return switch (subject) {
            case PermanentSubject.ControlledBy controlledBy -> controlledBy.filter();
            case PermanentSubject.OwnedBy ownedBy -> ownedBy.filter();
            case null, default -> filter;
        };
    }

    /** A copy of this bounce with its {@link #filter()} replaced and every other parameter kept. */
    public ReturnToHandEffect withFilter(PermanentPredicate updatedFilter) {
        return switch (subject) {
            case PermanentSubject.ControlledBy controlledBy ->
                    withSubject(Subjects.controlledBy(controlledBy.players(), orAny(updatedFilter)));
            case PermanentSubject.OwnedBy ownedBy -> withSubject(Subjects.ownedBy(ownedBy.players(),
                    orAny(updatedFilter)));
            case null, default -> new ReturnToHandEffect(subject, updatedFilter, orSuspendedCard,
                    ofChosenCreatureType, lifeLoss, drawCount, castTimeXValue, thenEffect,
                    minimumControlledNontokenCount);
        };
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

    /** Whether the set is every matching permanent some players control (the board-sweep shape). */
    private boolean isSweep() {
        return subject instanceof PermanentSubject.ControlledBy controlledBy
                && controlledBy.players() instanceof PlayerSubject.Each;
    }

    /** Whether the subject reads target player's permanents ({@code controlledBy} / {@code ownedBy} target player). */
    private boolean readsTargetPlayersPermanents() {
        PlayerSubject players = switch (subject) {
            case PermanentSubject.ControlledBy controlledBy -> controlledBy.players();
            case PermanentSubject.OwnedBy ownedBy -> ownedBy.players();
            case null, default -> null;
        };
        return players != null && players.declaresTarget();
    }

    /** Whether the subject reads the effect's permanent target, itself or for its attachments. */
    private boolean readsPermanentTarget() {
        return subject != null && (subject.declaresTarget()
                || subject instanceof PermanentSubject.AttachmentsOf attachments
                && attachments.permanents().declaresTarget());
    }

    @Override
    public TargetSpec targetSpec() {
        // A target permanent reproduces the old requireBattlefieldTarget guard; a target player's set targets the
        // player (the old validator imposed no guard there). source() acts on the source permanent without choosing
        // a target, but marks it as self-targeting so trigger collectors retain the source id.
        if (orSuspendedCard) {
            return TargetSpec.benign(TargetPredicates.anyOf(
                    TargetPredicates.permanents(new PermanentNotPredicate(new PermanentIsLandPredicate())),
                    TargetPredicates.exileCard()));
        }
        if (ofChosenCreatureType) {
            return TargetSpec.benign(TargetPredicates.creature());
        }
        if (readsPermanentTarget()) {
            return filter == null
                    ? TargetSpec.benign(TargetPredicates.permanent())
                    : TargetSpec.benign(TargetPredicates.permanent(), filter);
        }
        if (readsTargetPlayersPermanents()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (subject instanceof PermanentSubject.Source) {
            return new TargetSpec(null, false, null, true, 1);
        }
        return TargetSpec.NONE;
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached
                || subject instanceof PermanentSubject.AllOf allOf
                && allOf.subjects().stream().anyMatch(PermanentSubject.Attached.class::isInstance);
    }

    @Override
    public RemovalKind removalKind() {
        // Only a bounce of the target permanent itself is targeted removal; the set, attachment and self subjects
        // are board sweeps or self-return, not single-target removal.
        return subject != null && subject.declaresTarget() ? RemovalKind.BOUNCE : null;
    }

    @Override
    public boolean sweepsBoard() {
        return isSweep();
    }

    @Override
    public boolean requiresCastTimeCreatureTypeChoice() {
        return ofChosenCreatureType;
    }
}
