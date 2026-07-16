package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a color, then create one token from {@code tokenTemplate} for each permanent of that
 * color." (Rith, the Awakener.)
 *
 * <p>On resolution the controller chooses a color; the count-and-create step runs once the color is
 * picked (see {@code ChoiceHandlerService.handleCreateTokensPerPermanentOfChosenColorChoice}). A
 * permanent is "of the chosen color" per its effective colors, with lands excluded (an oracle-loaded
 * land derives its colors from color identity, so a colorless Forest would otherwise wrongly count
 * as green — mirrors Persecute/Oona handling). The template's own {@code amount} is ignored — the
 * count comes from the number of matching permanents.
 *
 * @param tokenTemplate one token created per permanent of the chosen color
 */
public record CreateTokenPerPermanentOfChosenColorEffect(CreateTokenEffect tokenTemplate) implements CardEffect {
}
