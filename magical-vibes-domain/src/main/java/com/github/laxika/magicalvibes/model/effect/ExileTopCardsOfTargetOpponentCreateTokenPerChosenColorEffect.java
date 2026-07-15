package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Choose a color. Target opponent exiles the top {@code count} cards of their library. For each
 * card of the chosen color exiled this way, create a token from {@code tokenTemplate}." (Oona,
 * Queen of the Fae.)
 *
 * <p>On resolution the controller chooses a color; the actual exile-and-count-and-create runs once
 * the color is picked (see {@code ChoiceHandlerService}). A card is "of the chosen color" per its
 * printed colors (lands excluded, mirroring Persecute). {@code count} is a {@link DynamicAmount} so
 * the {@code X} from an {@code {X}...} activation cost flows through.
 *
 * @param count         how many top cards the target opponent exiles
 * @param tokenTemplate one token created per exiled card of the chosen color (its {@code amount} is
 *                      ignored — the count comes from the number of matching cards)
 */
public record ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffect(
        DynamicAmount count, CreateTokenEffect tokenTemplate) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
