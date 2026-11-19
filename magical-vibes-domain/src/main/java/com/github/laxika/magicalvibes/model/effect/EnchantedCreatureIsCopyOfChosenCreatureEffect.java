package com.github.laxika.magicalvibes.model.effect;

/**
 * "Enchanted creature is a copy of the chosen creature." (Metamorphic Alteration)
 *
 * <p>A layer-1 copy effect (CR 613.2a) whose duration is "while this Aura is attached": the copy
 * is applied by a card swap through {@code PermanentCopierService} once the Aura is attached and
 * its {@code chosenPermanentId} is set, kept as a {@link ResolvedCopyEffect} floating effect lasting
 * {@link Duration#WHILE_ATTACHED}, and ended when the Aura leaves the battlefield or becomes
 * unattached. Add as a static ability together with a static {@link ChooseCreatureOnEnterEffect}.
 */
public record EnchantedCreatureIsCopyOfChosenCreatureEffect() implements CardEffect {
}
