package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public sealed interface ColorChoiceContext {

    record TextChangeFromWord(UUID targetPermanentId) implements ColorChoiceContext {}

    record TextChangeToWord(UUID targetPermanentId, String fromWord, boolean isColor) implements ColorChoiceContext {}

    record ManaColorChoice(UUID playerId, boolean fromCreature) implements ColorChoiceContext {}

    record DrawReplacementChoice(UUID playerId, DrawReplacementKind kind) implements ColorChoiceContext {}

    record CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes) implements ColorChoiceContext {}

    record KeywordGrantChoice(UUID targetPermanentId, List<Keyword> options) implements ColorChoiceContext {}

    record ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes) implements ColorChoiceContext {}

    record ProtectionColorChoice(UUID targetPermanentId, boolean includeArtifacts) implements ColorChoiceContext {}

    record SubtypeChoice(UUID permanentId) implements ColorChoiceContext {}

    record BasicLandTypeChoice(UUID permanentId) implements ColorChoiceContext {}

    /**
     * Tracks the sequential "each player names a card" flow for Conundrum Sphinx etc.
     * Players name in APNAP order. After all have named, top cards are revealed and
     * moved to hand (match) or bottom of library (no match).
     *
     * @param playerOrder  all player IDs in APNAP order
     * @param chosenNames  names chosen so far (playerId → chosen name)
     */
    record EachPlayerCardNameRevealChoice(List<UUID> playerOrder,
                                          Map<UUID, String> chosenNames) implements ColorChoiceContext {}

    /**
     * Sphinx Ambassador: the damaged player names a card after the controller has selected
     * a card from their library. The selected card is stored in
     * {@code GameData.pendingSphinxAmbassadorChoice}.
     */
    record SphinxAmbassadorNameChoice(UUID namingPlayerId, UUID controllerId) implements ColorChoiceContext {}
}
