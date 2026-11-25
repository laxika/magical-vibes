package com.github.laxika.magicalvibes.model.effect;

/**
 * Captivating Glance: "At the beginning of your end step, clash with an opponent. If you win,
 * gain control of enchanted creature. Otherwise, that player gains control of enchanted creature."
 * <p>
 * Non-targeting end-step trigger placed on {@code Triggers.endStep(ActivePlayer.YOU)}. The handler
 * performs a clash for the aura's controller and, based on the outcome, moves control of the
 * enchanted creature to the winner of the clash.
 */
/**
 * "Clash with an opponent. If you win, gain control of enchanted creature. Otherwise, that player gains control of
 * enchanted creature." (Captivating Glance). Its handler inserts the control change as a copy with
 * {@code enchantedId} and {@code recipientId} set.
 *
 * @param enchantedId the enchanted creature, once the clash is over
 * @param recipientId the clash's winner who gains control, once the clash is over
 * @param opponent    the opponent clashed with, or {@code null} until the controller has chosen one (CR 701.30b)
 */
public record ClashForControlOfEnchantedCreatureEffect(java.util.UUID enchantedId, java.util.UUID recipientId,
                                                       PlayerSubject opponent)
        implements CardEffect {
    public ClashForControlOfEnchantedCreatureEffect() {
        this(null, null, null);
    }
}
