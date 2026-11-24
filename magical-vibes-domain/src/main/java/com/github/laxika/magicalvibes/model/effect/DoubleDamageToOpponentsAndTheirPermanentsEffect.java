package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a source would deal damage to an opponent or a permanent an opponent
 * controls, that source deals double that damage to that player or permanent instead" (CR 614.1a). With
 * {@link Subjects#chosenPlayer()}, "... to the chosen player or a permanent they control" (Sawhorn Nemesis); with
 * {@link Subjects#you()} and {@link Subjects#source()}, "to you ... to this creature" (Goldnight Castigator); with
 * {@link Subjects#enchantedPlayer()} and no permanents, "to enchanted player" (Curse of Bloodletting); with no
 * permanents, "to an opponent" (Fiendish Duo).
 *
 * <p>Recipient-scoped, unlike the source-scoped doublers: it does not matter who controls the damage
 * source, only who or what is dealt the damage. Contrast {@link DoubleDamageEffect} (global, Furnace of Rath) and
 * {@link DoubleControllerDamageEffect} (sources controlled by this permanent's controller).
 *
 * <p>Applied via {@code GameQueryService.getDamageToRecipientMultiplier} (the enchanted player's through
 * {@code getEnchantedPlayerDamageMultiplier}); multiple instances stack multiplicatively. Used by Gisela, Blade of
 * Goldnight together with {@link PreventHalfDamageToControllerAndTheirPermanentsEffect}.
 *
 * @param players     the players whose damage is doubled: {@link Subjects#eachOpponent()}, {@link Subjects#chosenPlayer()},
 *                    {@link Subjects#you()} or {@link Subjects#enchantedPlayer()}
 * @param permanents  the permanents whose damage is doubled: {@code controlledBy(players)} (their permanents),
 *                    {@link Subjects#source()} (this permanent) or {@code null} (none)
 */
public record DoubleDamageToOpponentsAndTheirPermanentsEffect(PlayerSubject players, PermanentSubject permanents)
        implements OpponentRecipientDamageMultiplyingEffect, DoublingEffect {

    public DoubleDamageToOpponentsAndTheirPermanentsEffect {
        if (!(players instanceof PlayerSubject.Each each && each.relation() == PlayerSubject.Relation.OPPONENTS
                || players instanceof PlayerSubject.ChosenPlayer || players instanceof PlayerSubject.You
                || players instanceof PlayerSubject.EnchantedPlayer)) {
            throw new IllegalArgumentException("Unsupported damaged players: " + players);
        }
        if (permanents != null && !(permanents instanceof PermanentSubject.Source)
                && !permanents.equals(Subjects.controlledBy(players))) {
            throw new IllegalArgumentException("Doubles damage to their permanents, this one or none: " + permanents);
        }
    }

    public DoubleDamageToOpponentsAndTheirPermanentsEffect(PlayerSubject players) {
        this(players, Subjects.controlledBy(players));
    }

    public DoubleDamageToOpponentsAndTheirPermanentsEffect() {
        this(Subjects.eachOpponent());
    }

    /** "If a source would deal damage to [players]": no permanent's damage is doubled. */
    public static DoubleDamageToOpponentsAndTheirPermanentsEffect toPlayersOnly(PlayerSubject players) {
        return new DoubleDamageToOpponentsAndTheirPermanentsEffect(players, null);
    }

    @Override
    public int damageMultiplier() {
        return 2;
    }
}
