package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;

public sealed interface ChoiceContext {

    record TextChangeFromWord(UUID targetPermanentId) implements ChoiceContext {}

    record TextChangeToWord(UUID targetPermanentId, String fromWord, boolean isColor) implements ChoiceContext {}

    record ManaColorChoice(UUID playerId, boolean fromCreature) implements ChoiceContext {}

    record DrawReplacementChoice(UUID playerId, DrawReplacementKind kind) implements ChoiceContext {}

    record CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes) implements ChoiceContext {}

    record KeywordGrantChoice(UUID targetPermanentId, List<Keyword> options) implements ChoiceContext {}

    record ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes) implements ChoiceContext {}

    record ProtectionColorChoice(UUID targetPermanentId, boolean includeArtifacts) implements ChoiceContext {}

    record SubtypeChoice(UUID permanentId) implements ChoiceContext {}

    record BasicLandTypeChoice(UUID permanentId) implements ChoiceContext {}

    /**
     * Tracks the sequential "each player names a card" flow for Conundrum Sphinx etc.
     * Players name in APNAP order. After all have named, top cards are revealed and
     * moved to hand (match) or bottom of library (no match).
     *
     * @param playerOrder  all player IDs in APNAP order
     * @param chosenNames  names chosen so far (playerId → chosen name)
     */
    record EachPlayerCardNameRevealChoice(List<UUID> playerOrder,
                                          Map<UUID, String> chosenNames) implements ChoiceContext {}

    /**
     * Sphinx Ambassador: the damaged player names a card after the controller has selected
     * a card from their library. The selected card is stored in
     * {@code GameData.pendingSphinxAmbassadorChoice}.
     */
    record SphinxAmbassadorNameChoice(UUID namingPlayerId, UUID controllerId) implements ChoiceContext {}

    /**
     * The controller chooses a permanent type at resolution time (e.g. Creeping Renaissance),
     * then all cards of that type are returned from the controller's graveyard.
     */
    record PermanentTypeChoice(UUID controllerId, GraveyardChoiceDestination destination,
                               String entryDescription) implements ChoiceContext {}
}
