package com.github.laxika.magicalvibes.model.effect;

/**
 * "[Attackers'] creatures can't attack [protected players] (or planeswalkers they control)" for a duration, made by a
 * resolution: each protected player found as it resolves gets a floating
 * {@link CreaturesCantAttackControllerUnlessPredicateEffect} naming the attacking player found then, read by
 * {@code AttackLegalityService} whenever attackers are declared (CR 508.1c). A player isn't kept from attacking
 * themselves.
 *
 * <p>Forms: {@code ()} "until your next turn, creatures can't attack you" (Chronomantic Escape);
 * {@code (activePlayer(), you(), false, UNTIL_END_OF_COMBAT)} "they can't attack you this combat" (Champions of Minas
 * Tirith, the else of its payment); {@code (you(), targetPlayer(), false, UNTIL_END_OF_TURN)} "you can't attack that
 * player this turn" (Call for Aid); {@code (triggeringPlayer(), you(), true, UNTIL_END_OF_TURN)} "they can't attack you
 * or planeswalkers you control this turn" (Sandswirl Wanderglyph).
 *
 * @param attackers             whose creatures can't attack; {@code null} for every player's
 * @param protectedPlayers      who can't be attacked
 * @param protectsPlaneswalkers whether the protected players' planeswalkers can't be attacked either
 * @param duration              {@code UNTIL_YOUR_NEXT_TURN}, {@code UNTIL_END_OF_TURN} or {@code UNTIL_END_OF_COMBAT}
 */
public record CreaturesCantAttackControllerUntilNextTurnEffect(PlayerSubject attackers, PlayerSubject protectedPlayers,
                                                               boolean protectsPlaneswalkers, Duration duration)
        implements CardEffect {

    public CreaturesCantAttackControllerUntilNextTurnEffect {
        if (protectedPlayers == null) {
            throw new IllegalArgumentException("An attack ban protects someone");
        }
        if (duration != Duration.UNTIL_YOUR_NEXT_TURN && duration != Duration.UNTIL_END_OF_TURN
                && duration != Duration.UNTIL_END_OF_COMBAT) {
            throw new IllegalArgumentException("Unsupported attack ban duration: " + duration);
        }
    }

    /** "Until your next turn, creatures can't attack you." */
    public CreaturesCantAttackControllerUntilNextTurnEffect() {
        this(null, Subjects.you(), false, Duration.UNTIL_YOUR_NEXT_TURN);
    }

    @Override
    public TargetSpec targetSpec() {
        return protectedPlayers.declaresTarget() || attackers != null && attackers.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
