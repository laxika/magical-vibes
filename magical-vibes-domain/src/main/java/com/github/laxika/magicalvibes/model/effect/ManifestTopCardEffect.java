package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * {@code player} manifests the top {@code amount} cards of their library, one at a time (CR 701.40e). The amount is
 * evaluated as the effect resolves: "manifest a number of cards equal to the number of counters on it" is
 * {@code new ManifestTopCardEffect(new CountersOnTriggeringPermanent())} (Omarthis, Ghostfire Initiate). "Its controller
 * manifests the top card of their library" after an exile is {@code new ManifestTopCardEffect(new Fixed(1),
 * Subjects.controllerOf(Subjects.target()))} (Reality Shift).
 *
 * @param amount how many cards to manifest
 * @param player who manifests from their own library
 */
public record ManifestTopCardEffect(DynamicAmount amount, PlayerSubject player) implements CardEffect {

    public ManifestTopCardEffect {
        Objects.requireNonNull(player, "player");
    }

    /** The effect's controller manifests {@code amount} cards. */
    public ManifestTopCardEffect(DynamicAmount amount) {
        this(amount, Subjects.you());
    }

    /** Manifests the top card. */
    public ManifestTopCardEffect() {
        this(new Fixed(1));
    }
}
