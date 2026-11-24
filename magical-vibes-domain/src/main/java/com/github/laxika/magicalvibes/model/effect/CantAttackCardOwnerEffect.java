package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Static restriction on whom a creature may attack: {@code attacker} can't attack the players {@code players} names,
 * nor, when {@code includePlaneswalkers}, planeswalkers those players control. A battle is never restricted. Checked
 * as attackers are declared (CR 508.1c).
 *
 * <ul>
 *   <li>{@code attacker} is {@link Subjects#source()} for the creature's own (or granted) ability: "can't attack its
 *       owner [or planeswalkers its owner controls]" ({@link Subjects#ownerOf ownerOf(source())}: Xantcha, Alexios,
 *       Elrond's grant), "can't attack its controller" ({@code controllerOf(source())}: Capricopian).</li>
 *   <li>{@link Subjects#attachedCreature()} for an Aura's or Equipment's: "enchanted / equipped creature can't attack
 *       you [or planeswalkers you control]" ({@link Subjects#you()}: the Vows, Spectral Grasp, Assault Suit, Fealty to
 *       the Realm).</li>
 * </ul>
 *
 * <p>The players are judged for the ability's source as a static ability
 * ({@code SubjectResolutionService.namesPlayer}), so the restriction is read only while the source has the ability.
 */
public record CantAttackCardOwnerEffect(PermanentSubject attacker, PlayerSubject players,
                                        boolean includePlaneswalkers) implements CardEffect {

    public CantAttackCardOwnerEffect {
        Objects.requireNonNull(attacker, "attacker");
        Objects.requireNonNull(players, "players");
        if (!attacker.equals(Subjects.source()) && !attacker.equals(Subjects.attachedCreature())) {
            throw new IllegalArgumentException("attacker must be source() or attachedCreature(): " + attacker);
        }
    }

    /** "This creature can't attack its owner or planeswalkers its owner controls." */
    public CantAttackCardOwnerEffect() {
        this(true);
    }

    /** "This creature can't attack its owner [or planeswalkers its owner controls]." */
    public CantAttackCardOwnerEffect(boolean includePlaneswalkers) {
        this(Subjects.source(), Subjects.ownerOf(Subjects.source()), includePlaneswalkers);
    }

    /** "This creature can't attack [players] [or planeswalkers they control]." */
    public static CantAttackCardOwnerEffect cantAttack(PlayerSubject players, boolean includePlaneswalkers) {
        return new CantAttackCardOwnerEffect(Subjects.source(), players, includePlaneswalkers);
    }

    /** "Enchanted / equipped creature can't attack [players] [or planeswalkers they control]." */
    public static CantAttackCardOwnerEffect enchantedCreatureCantAttack(PlayerSubject players,
                                                                        boolean includePlaneswalkers) {
        return new CantAttackCardOwnerEffect(Subjects.attachedCreature(), players, includePlaneswalkers);
    }

    /** Whether this ability restricts the creature it's attached to rather than its own source. */
    public boolean restrictsAttachedCreature() {
        return attacker.equals(Subjects.attachedCreature());
    }
}
