package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a source would deal damage to an opponent or a permanent an opponent
 * controls, that source deals double that damage to that player or permanent instead." With
 * {@link Subjects#chosenPlayer()}, "... to the chosen player or a permanent they control" (Sawhorn Nemesis).
 *
 * <p>Recipient-scoped, unlike the source-scoped doublers: it does not matter who controls the damage
 * source, only that the damage is dealt to an opponent of this permanent's controller or to a permanent
 * such an opponent controls. Contrast {@link DoubleDamageEffect} (global, Furnace of Rath),
 * {@link DoubleControllerDamageEffect} (sources controlled by this permanent's controller) and
 * {@link DoubleDamageToEnchantedPlayerEffect} (one enchanted player only).
 *
 * <p>Applied via {@code GameQueryService.getDamageToRecipientMultiplier}; multiple instances stack
 * multiplicatively. Used by Gisela, Blade of Goldnight together with
 * {@link PreventHalfDamageToControllerAndTheirPermanentsEffect}.
 */
public record DoubleDamageToOpponentsAndTheirPermanentsEffect(PlayerSubject players)
        implements OpponentRecipientDamageMultiplyingEffect, DoublingEffect {

    public DoubleDamageToOpponentsAndTheirPermanentsEffect {
        if (!(players instanceof PlayerSubject.Each || players instanceof PlayerSubject.ChosenPlayer)) {
            throw new IllegalArgumentException("Doubles damage to the opponents or the chosen player: " + players);
        }
    }

    public DoubleDamageToOpponentsAndTheirPermanentsEffect() {
        this(Subjects.eachOpponent());
    }

    @Override
    public int damageMultiplier() {
        return 2;
    }
}
