package com.github.laxika.magicalvibes.model.effect;

/**
 * Mandatory as-enters choice of a permanent the source's controller does not control, kept as the source's chosen
 * permanent: the "chosen permanent" its other abilities name ({@code PermanentIsChosenPermanentPredicate}, Predatory
 * Sludge's "when the chosen permanent is put into a graveyard from the battlefield").
 */
public record ChooseOpponentPermanentOnEnterEffect() implements CardEffect {
}
