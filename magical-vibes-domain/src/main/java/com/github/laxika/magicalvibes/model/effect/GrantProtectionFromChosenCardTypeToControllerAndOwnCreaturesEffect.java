package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC: the source's controller and creatures they control have protection from the card type
 * chosen for the source permanent. Pair with {@link ChooseCardTypeOnEnterEffect} for the choice.
 */
public record GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect() implements CardEffect {
}
