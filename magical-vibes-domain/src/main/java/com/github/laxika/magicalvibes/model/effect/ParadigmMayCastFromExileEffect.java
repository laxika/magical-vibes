package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a pending may-ability that casts an exiled Paradigm copy without paying its mana cost.
 * The exiled card id is stored on {@link com.github.laxika.magicalvibes.model.PendingMayAbility#targetCardId()}.
 */
public record ParadigmMayCastFromExileEffect() implements CardEffect {
}
