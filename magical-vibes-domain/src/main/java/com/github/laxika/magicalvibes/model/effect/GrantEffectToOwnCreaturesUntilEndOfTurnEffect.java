package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * When resolved, grants every creature the controller currently controls a temporary triggered
 * ability until end of turn. Creatures that enter (or change control) later this turn do not
 * receive it — snapshot at resolution (e.g. Driven / Despair).
 *
 * <p>The granted effect is stored via
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryTriggeredEffect(EffectSlot, CardEffect)}
 * and cleared at cleanup by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 *
 * @param slot          the trigger slot to grant (e.g. {@link EffectSlot#ON_COMBAT_DAMAGE_TO_PLAYER})
 * @param grantedEffect the effect to fire when the trigger condition is met
 */
public record GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
        EffectSlot slot,
        CardEffect grantedEffect
) implements CardEffect {
}
