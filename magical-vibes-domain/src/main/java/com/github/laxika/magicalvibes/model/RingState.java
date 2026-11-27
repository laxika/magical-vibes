package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.ControllerRingLevelAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsRingBearerPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.UUID;

/**
 * One player's Ring emblem (CR 701.54c): how many times the Ring has tempted them, counted up to 4 (the most its
 * abilities read), and their Ring-bearer (CR 701.54a), or {@code null} when they have none. Kept in the one store
 * {@link GameData#ringStates}; the emblem itself, carrying {@link #EMBLEM}'s triggered abilities, is in
 * {@link GameData#emblems} from the first time the Ring tempts that player.
 */
public record RingState(int level, UUID bearerId) {

    /** The emblem's name (CR 701.54c). */
    public static final String EMBLEM_NAME = "The Ring";

    /**
     * The Ring's triggered abilities (CR 701.54c), each existing only while the Ring has tempted its player at least as
     * many times as it needs (its event condition, never checked again on resolution): at two, "whenever your
     * Ring-bearer attacks, draw a card, then discard a card"; at three, "whenever your Ring-bearer becomes blocked by a
     * creature, the blocking creature's controller sacrifices it at end of combat" (once for each blocker, CR 509.3d);
     * at four, "whenever your Ring-bearer deals combat damage to a player, each opponent loses 3 life". Its static
     * ability, "your Ring-bearer is legendary and can't be blocked by creatures with greater power", is read by
     * {@code GameQueryService.isRingBearer}'s callers.
     */
    public static final CreateEmblemEffect EMBLEM = new CreateEmblemEffect(List.of(),
            "Your Ring-bearer is legendary and can't be blocked by creatures with greater power. "
                    + "As long as the Ring has tempted you two or more times, it has \"Whenever your Ring-bearer "
                    + "attacks, draw a card, then discard a card.\" As long as the Ring has tempted you three or more "
                    + "times, it has \"Whenever your Ring-bearer becomes blocked by a creature, the blocking "
                    + "creature's controller sacrifices it at end of combat.\" As long as the Ring has tempted you "
                    + "four or more times, it has \"Whenever your Ring-bearer deals combat damage to a player, each "
                    + "opponent loses 3 life.\"",
            Subjects.you(), List.of(), List.of(
            new TriggeredAbility(Triggers.creatureYouControlAttacks().withFilter(new PermanentIsRingBearerPredicate()),
                    TriggerOptions.DEFAULT.withEventCondition(new ControllerRingLevelAtLeast(2)),
                    SequenceEffect.of(new DrawCardEffect(1), new DiscardEffect(1, Subjects.you()))),
            new TriggeredAbility(Triggers.creatureYouControlBecomesBlocked()
                    .withFilter(new PermanentIsRingBearerPredicate()),
                    TriggerOptions.EACH_BLOCKER.withEventCondition(new ControllerRingLevelAtLeast(3)),
                    RegisterDelayedStepTriggerEffect.aboutAtNext(Triggers.endOfCombat(), Subjects.triggering(),
                            SacrificeSelfEffect.ofTriggeringPermanent(true, null))),
            new TriggeredAbility(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                    .withFilter(new PermanentIsRingBearerPredicate()),
                    TriggerOptions.DEFAULT.withEventCondition(new ControllerRingLevelAtLeast(4)),
                    new LoseLifeEffect(3, Subjects.eachOpponent()))));

    public RingState {
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("The Ring level must be between 1 and 4");
        }
    }

    /** The emblem after the Ring tempts its player once more, the Ring-bearer unchanged. */
    public RingState tempted() {
        return new RingState(Math.min(4, level + 1), bearerId);
    }

    /** The emblem with {@code newBearerId} as the Ring-bearer ({@code null}: none). */
    public RingState withBearer(UUID newBearerId) {
        return new RingState(level, newBearerId);
    }
}
