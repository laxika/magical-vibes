package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public sealed interface ColorChoiceContext {

    record TextChangeFromWord(UUID targetPermanentId) implements ColorChoiceContext {}

    record TextChangeToWord(UUID targetPermanentId, String fromWord, boolean isColor) implements ColorChoiceContext {}
}
