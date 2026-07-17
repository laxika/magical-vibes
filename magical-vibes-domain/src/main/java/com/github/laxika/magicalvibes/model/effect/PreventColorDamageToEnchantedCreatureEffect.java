package com.github.laxika.magicalvibes.model.effect;

/**
 * Prismatic Ward: "Prevent all damage that would be dealt to enchanted creature by sources of the
 * chosen color." A static marker on the Aura; the chosen colour is read from the Aura permanent's
 * {@code getChosenColor()} (pair with {@link ChooseColorOnEnterEffect}). Unlike protection, this only
 * prevents damage — it does not stop blocking, targeting, or enchanting. Queried in
 * {@code GameQueryService.isColorDamageToEnchantedCreaturePrevented} from the creature-damage paths.
 */
public record PreventColorDamageToEnchantedCreatureEffect() implements CardEffect {
}
