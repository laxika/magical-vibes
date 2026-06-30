package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for {@code ON_CONTROLLER_CASTS_SPELL}: whenever the controller casts a spell
 * matching {@code spellFilter}, they may pay {@code tapCost} to copy that spell. The copy's
 * controller may choose new targets.
 * <p>
 * At trigger time, {@code SpellCastTriggerCollectorService} snapshots the cast spell and places a
 * {@link MayPayTapPermanentsEffect} wrapping {@link CopyControllerCastSpellEffect} on the stack
 * when {@code tapCost} is non-null.
 */
public record CopyControllerCastSpellOnSpellCastEffect(
        CardPredicate spellFilter,
        TapMultiplePermanentsCost tapCost
) implements CardEffect {
}
