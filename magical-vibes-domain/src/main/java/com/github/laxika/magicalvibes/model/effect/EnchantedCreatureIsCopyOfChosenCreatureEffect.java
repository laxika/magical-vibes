package com.github.laxika.magicalvibes.model.effect;

/**
 * "Enchanted creature is a copy of the chosen creature." (Metamorphic Alteration)
 *
 * <p>A layer-1 copy effect (CR 613.2a) whose duration is "while this Aura is attached": the copy
 * is applied by a card swap through {@code PermanentCopierService} once the Aura is attached and
 * its {@code chosenPermanentId} is set, and reverted when the Aura leaves the battlefield or
 * becomes unattached. Place in {@code EffectSlot.STATIC} together with a
 * {@link ChooseCreatureOnEnterEffect} in {@code EffectSlot.ON_ENTER_BATTLEFIELD}.
 */
public record EnchantedCreatureIsCopyOfChosenCreatureEffect() implements CardEffect {
}
