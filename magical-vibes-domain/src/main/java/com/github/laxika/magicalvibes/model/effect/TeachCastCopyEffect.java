package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Copies the taught card and offers the controller the copy for its teach cost. */
public record TeachCastCopyEffect(UUID taughtCardId) implements CardEffect {
}
