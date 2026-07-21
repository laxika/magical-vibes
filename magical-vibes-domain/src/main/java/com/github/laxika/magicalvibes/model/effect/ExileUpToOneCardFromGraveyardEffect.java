package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may exile target card from a graveyard." A non-targeting, resolution-time select over every
 * card in every graveyard: the controller chooses up to one card to exile (they may choose none,
 * which covers the optional "you may"). Because it targets nothing at stack time it rides on any
 * resolution path — the Grixis Sojourners death trigger ({@code ON_DEATH}) and its folded cycling
 * ability ({@code [this, DrawCardEffect]}), mirroring how {@link ExileUpToNAttackingCreaturesEffect}
 * rides Resounding Silence's cycling ability.
 *
 * <p>Completed via {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen} when
 * {@code graveyardTargetOperation.resolutionTimeExileResume} is set; it exiles the chosen card and
 * resumes any remaining effects (e.g. the cycling draw).
 */
public record ExileUpToOneCardFromGraveyardEffect() implements CardEffect {
}
