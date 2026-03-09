package com.github.laxika.magicalvibes.model;

import java.util.List;
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
}
