package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: prevent all damage that would be dealt to the source permanent's controller and
 * the permanents they control by sources whose name equals the card name chosen by the source
 * permanent (tracked via {@code permanent.chosenName}). Unlike
 * {@link PlayerHasProtectionFromChosenNameEffect} (Runed Halo) this only prevents damage — it does
 * not stop targeting or enchanting — and it also covers the controller's permanents.
 * Pair with {@link ChooseCardNameOnEnterEffect}. Used by Gideon's Intervention.
 */
public record PreventDamageFromChosenNameEffect() implements CardEffect {
}
