package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * A card or permanent moves from one zone to another: dies, leaves the battlefield, is put into a graveyard,
 * is milled. Build it through {@link Triggers}.
 *
 * @param relation how the moving object relates to the ability's source
 * @param filter   what the moving object must be, as it last existed on the battlefield for a departure
 *                 (CR 603.10a), or {@code null} for any object: "a nontoken creature". The source of a
 *                 "this or another" relation matches whatever it was.
 * @param from     the zones it may leave; empty for any zone, including from outside the game
 * @param to       the zone it must go to, or {@code null} for any
 * @param excludedTo the zones it must not go to: {@code GRAVEYARD} for "leaves the battlefield without dying"
 * @param causes   how it must move; empty for any way
 * @param causedBy who must control the resolving spell or ability that moved it: "a spell or ability an opponent
 *                 controls causes a land you control to be put into a graveyard"; {@code ANY} for any or none
 * @param duringYourTurn whether it must move during its controller's turn: "during your turn"
 */
public record ZoneChangeTrigger(ObjectRelation relation, PermanentPredicate filter, Set<Zone> from, Zone to,
                                Set<Zone> excludedTo, Set<ZoneChangeCause> causes, PlayerRelation causedBy,
                                boolean duringYourTurn)
        implements Trigger {

    public ZoneChangeTrigger {
        Objects.requireNonNull(relation, "relation");
        Objects.requireNonNull(causedBy, "causedBy");
        from = from.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(from));
        excludedTo = excludedTo.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(excludedTo));
        causes = causes.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(causes));
        if (to != null && from.equals(Set.of(to))) {
            throw new IllegalArgumentException("A move inside one zone is no zone change");
        }
    }

    /** A trigger for a move caused by anything, at any time. */
    public ZoneChangeTrigger(ObjectRelation relation, PermanentPredicate filter, Set<Zone> from, Zone to,
                             Set<ZoneChangeCause> causes) {
        this(relation, filter, from, to, Set.of(), causes, PlayerRelation.ANY, false);
    }

    /** This trigger, for a move to any zone but {@code zones}. */
    public ZoneChangeTrigger notTo(Set<Zone> zones) {
        return new ZoneChangeTrigger(relation, filter, from, to, zones, causes, causedBy, duringYourTurn);
    }

    /** This trigger, for a move a resolving spell or ability that {@code player} controls caused. */
    public ZoneChangeTrigger causedBy(PlayerRelation player) {
        return new ZoneChangeTrigger(relation, filter, from, to, excludedTo, causes, player, duringYourTurn);
    }

    /** This trigger, for a move during its controller's turn only. */
    public ZoneChangeTrigger onlyDuringYourTurn() {
        return new ZoneChangeTrigger(relation, filter, from, to, excludedTo, causes, causedBy, true);
    }

    /** A trigger whose moving object may be anything that relates to the source as {@code relation} says. */
    public ZoneChangeTrigger(ObjectRelation relation, Set<Zone> from, Zone to, Set<ZoneChangeCause> causes) {
        this(relation, null, from, to, causes);
    }

    /**
     * Whether the game looks back in time for it (CR 603.10a): leaving the battlefield or a graveyard, and a
     * sacrifice. The ability then triggers as its object existed just before the move, so the source's own move
     * triggers it from the zone it left. An enters ability, "when this enters from a graveyard" included, is checked
     * after the event instead (CR 603.6a).
     */
    public boolean looksBack() {
        return from.size() == 1 && (from.contains(Zone.BATTLEFIELD) || from.contains(Zone.GRAVEYARD))
                && !entersBattlefield();
    }

    /** Whether it watches a permanent leave the battlefield, for any destination or one (CR 603.6c). */
    public boolean leavesBattlefield() {
        return from.equals(Set.of(Zone.BATTLEFIELD));
    }

    /** Whether it watches a permanent enter the battlefield (CR 603.6a). */
    public boolean entersBattlefield() {
        return to == Zone.BATTLEFIELD;
    }

    /**
     * The battlefield, except for the source's own move that doesn't look back: "when this card is put into a
     * graveyard from your library" triggers from the zone it arrives in, where it exists after the event
     * (CR 603.10, CR 113.6k). "The creature it haunts" functions from exile (CR 702.55c).
     */
    @Override
    public AbilityZones defaultZones() {
        if (relation == ObjectRelation.HAUNTED) {
            return AbilityZones.of(Zone.EXILE);
        }
        if (relation != ObjectRelation.THIS) {
            return AbilityZones.BATTLEFIELD;
        }
        if (looksBack()) {
            return AbilityZones.of(from.iterator().next());
        }
        return to == null ? AbilityZones.BATTLEFIELD : AbilityZones.of(to);
    }

    /** Whether a move from {@code fromZone} to {@code toZone} for {@code cause} is this trigger's kind of move. */
    public boolean matchesMove(Zone fromZone, Zone toZone, ZoneChangeCause cause) {
        return (from.isEmpty() || from.contains(fromZone))
                && (to == null || to == toZone)
                && !excludedTo.contains(toZone)
                && (causes.isEmpty() || causes.contains(cause));
    }
}
