package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public sealed interface ColorChoiceContext {

    record MindBendFromWord(UUID targetPermanentId) implements ColorChoiceContext {}

    record MindBendToWord(UUID targetPermanentId, String fromWord, boolean isColor) implements ColorChoiceContext {}
}
