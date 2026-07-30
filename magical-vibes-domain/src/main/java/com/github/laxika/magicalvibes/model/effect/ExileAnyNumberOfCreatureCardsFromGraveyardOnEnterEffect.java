package com.github.laxika.magicalvibes.model.effect;

/**
 * "As this creature enters, exile any number of creature cards from your graveyard."
 * <p>
 * An as-enters replacement effect (CR 614.1c): placed in {@code EffectSlot.ON_ENTER_BATTLEFIELD}
 * and handled during {@code BattlefieldEntryService.handleCreatureEnteredBattlefield} before ETB
 * triggers fire. The controller picks any number of creature cards from their own graveyard; the
 * chosen cards are exiled tracked with the entering permanent, so
 * {@link com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource} and
 * {@link com.github.laxika.magicalvibes.model.amount.TotalToughnessOfCardsExiledWithSource} can
 * derive the permanent's characteristic-defining power and toughness from them. Sutured Ghoul.
 */
public record ExileAnyNumberOfCreatureCardsFromGraveyardOnEnterEffect() implements ReplacementEffect {
}
