package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Objects;
import java.util.Set;

/**
 * Static ability: "[permanents of these types] [entering the battlefield / dying] don't cause abilities [of permanents
 * these players control] to trigger". Torpor Orb, Hushwing Gryff and Tocatli Honor Guard ({@code ()}: creatures
 * entering, anyone's abilities), Doorkeeper Thrull ({@link #artifactsOrCreaturesEntering()}), Hushbringer (also
 * {@link #creaturesDying()}) and Elesh Norn, Mother of Machines ({@link #permanentsEnteringForOpponents()}).
 *
 * <p>Read by {@code GameQueryService}: the forms naming every player's abilities suppress the event's triggers as
 * the permanent enters or dies; a form naming some players' abilities is judged for each ability's controller as it
 * would trigger, with no type filter (the entering permanent is not known there).
 *
 * @param event              the event that doesn't cause abilities to trigger
 * @param types              the card types the permanent must have (one of them), empty for any permanent
 * @param abilityControllers the players whose permanents' abilities don't trigger
 */
public record CreatureEnteringDontCauseTriggersEffect(Event event, Set<CardType> types,
                                                      PlayerSubject abilityControllers) implements CardEffect {

    /** The event that doesn't cause abilities to trigger. */
    public enum Event {
        /** "entering the battlefield". */
        ENTERING,
        /** "dying". */
        DYING
    }

    public CreatureEnteringDontCauseTriggersEffect {
        Objects.requireNonNull(event, "event");
        types = types == null ? Set.of() : Set.copyOf(types);
        Objects.requireNonNull(abilityControllers, "abilityControllers");
        if (!appliesToEveryPlayersAbilities(abilityControllers) && (event != Event.ENTERING || !types.isEmpty())) {
            throw new IllegalArgumentException(
                    "A form naming some players' abilities is read only for any permanent entering");
        }
    }

    /** "Creatures entering the battlefield don't cause abilities to trigger." */
    public CreatureEnteringDontCauseTriggersEffect() {
        this(Event.ENTERING, Set.of(CardType.CREATURE), Subjects.eachPlayer());
    }

    /** "Artifacts and creatures entering the battlefield don't cause abilities to trigger." */
    public static CreatureEnteringDontCauseTriggersEffect artifactsOrCreaturesEntering() {
        return new CreatureEnteringDontCauseTriggersEffect(Event.ENTERING,
                Set.of(CardType.ARTIFACT, CardType.CREATURE), Subjects.eachPlayer());
    }

    /** "Creatures dying don't cause abilities to trigger." */
    public static CreatureEnteringDontCauseTriggersEffect creaturesDying() {
        return new CreatureEnteringDontCauseTriggersEffect(Event.DYING, Set.of(CardType.CREATURE),
                Subjects.eachPlayer());
    }

    /** "Permanents entering the battlefield don't cause abilities of permanents your opponents control to trigger." */
    public static CreatureEnteringDontCauseTriggersEffect permanentsEnteringForOpponents() {
        return new CreatureEnteringDontCauseTriggersEffect(Event.ENTERING, Set.of(), Subjects.eachOpponent());
    }

    /** Whether this form names every player's abilities. */
    public boolean appliesToEveryPlayersAbilities() {
        return appliesToEveryPlayersAbilities(abilityControllers);
    }

    private static boolean appliesToEveryPlayersAbilities(PlayerSubject players) {
        return players instanceof PlayerSubject.Each each && each.relation() == PlayerSubject.Relation.ALL;
    }
}
