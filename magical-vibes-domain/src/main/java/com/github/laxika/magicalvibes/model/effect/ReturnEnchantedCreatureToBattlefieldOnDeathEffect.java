package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import java.util.List;
import java.util.UUID;

/**
 * Death trigger for auras: when the enchanted creature dies, return that creature card from
 * its owner's graveyard to the battlefield. Used by Abduction (owner's control) and by
 * Unhallowed Pact / False Demise ({@code underAuraControllersControl} — the Aura's controller
 * gets it).
 *
 * <p>The {@code dyingCreatureCardId} is baked in at trigger time by
 * {@code DeathTriggerCollectorService} — it captures the dying creature's card ID so the
 * resolution logic can find it in the graveyard.</p>
 *
 * <p>Contrast {@link ReturnEnchantedCreatureToOwnerHandOnDeathEffect} (Demonic Vigor), which
 * returns the dying creature to its owner's hand instead of the battlefield.</p>
 *
 * @param dyingCreatureCardIds         physical card IDs represented by the creature that just died;
 *                                     empty in the card definition and baked in at trigger time
 * @param underAuraControllersControl  {@code true} to put the creature onto the battlefield under
 *                                     the Aura controller's control, {@code false} for its owner's.
 *                                     When the two differ the returned permanent is tracked as a
 *                                     stolen creature so the control change persists.
 * @param enterTapped                   {@code true} if the creature enters the battlefield tapped
 * @param enterWithCounter             optional counter put on the returned permanent as it enters
 */
public record ReturnEnchantedCreatureToBattlefieldOnDeathEffect(
        List<UUID> dyingCreatureCardIds,
        boolean underAuraControllersControl,
        boolean enterTapped,
        CounterType enterWithCounter
) implements CardEffect {

    /**
     * Card-definition constructor for the "under its owner's control" form — the dying creature's
     * card ID is not yet known.
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect() {
        this(List.of(), false, false, null);
    }

    /**
     * Card-definition constructor — the dying creature's card ID is not yet known.
     *
     * @param underAuraControllersControl see the record component
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(boolean underAuraControllersControl) {
        this(List.of(), underAuraControllersControl, false, null);
    }

    /**
     * Card-definition constructor with an optional tapped entry.
     *
     * @param underAuraControllersControl see the record component
     * @param enterTapped                  see the record component
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(boolean underAuraControllersControl,
                                                              boolean enterTapped) {
        this(List.of(), underAuraControllersControl, enterTapped, null);
    }

    /**
     * Trigger-time constructor retaining the counterless form.
     *
     * @param dyingCreatureCardId          see the record component
     * @param underAuraControllersControl  see the record component
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(UUID dyingCreatureCardId,
                                                              boolean underAuraControllersControl) {
        this(List.of(dyingCreatureCardId), underAuraControllersControl, false, null);
    }

    /**
     * Trigger-time constructor with an optional tapped entry.
     *
     * @param dyingCreatureCardId          see the record component
     * @param underAuraControllersControl  see the record component
     * @param enterTapped                  see the record component
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(UUID dyingCreatureCardId,
                                                              boolean underAuraControllersControl,
                                                              boolean enterTapped) {
        this(List.of(dyingCreatureCardId), underAuraControllersControl, enterTapped, null);
    }

    /**
     * Card-definition constructor with an optional enter-with counter.
     *
     * @param underAuraControllersControl see the record component
     * @param enterWithCounter             see the record component
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(boolean underAuraControllersControl,
                                                              CounterType enterWithCounter) {
        this(List.of(), underAuraControllersControl, false, enterWithCounter);
    }

    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(
            UUID dyingCreatureCardId, boolean underAuraControllersControl,
            boolean enterTapped, CounterType enterWithCounter) {
        this(List.of(dyingCreatureCardId), underAuraControllersControl, enterTapped, enterWithCounter);
    }

    public UUID dyingCreatureCardId() {
        return dyingCreatureCardIds.isEmpty() ? null : dyingCreatureCardIds.getFirst();
    }
}
