package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.UUID;

/**
 * Death trigger for auras: when the enchanted creature dies, its controller chooses target creature
 * one of their opponents controls, and this aura returns from its owner's graveyard to the
 * battlefield attached to that creature. Used by Necrotic Plague.
 *
 * <p>The {@code enchantedCreatureControllerId} is baked in at trigger time by
 * {@code DeathTriggerCollectorService} — it captures the dying creature's controller, who chooses
 * the target when the trigger is put on the stack (CR 603.3d), so the legal targets are the
 * creatures that player does not control. The trigger itself stays controlled by the aura's
 * controller (CR 603.3a), and the chosen creature is carried as the stack entry's target.</p>
 *
 * @param enchantedCreatureControllerId the controller of the creature that just died; {@code null}
 *                                      in the card definition (baked in at trigger time)
 */
public record ReturnSourceAuraToOpponentCreatureOnDeathEffect(
        UUID enchantedCreatureControllerId
) implements CardEffect {

    /**
     * Card-definition constructor — the controller ID is not yet known.
     */
    public ReturnSourceAuraToOpponentCreatureOnDeathEffect() {
        this((UUID) null);
    }

    @Override
    public TargetSpec targetSpec() {
        if (enchantedCreatureControllerId == null) {
            return TargetSpec.NONE;
        }
        return TargetSpec.harmful(TargetPredicates.creature(),
                new PermanentNotPredicate(new PermanentControlledByPlayerPredicate(enchantedCreatureControllerId)));
    }
}
