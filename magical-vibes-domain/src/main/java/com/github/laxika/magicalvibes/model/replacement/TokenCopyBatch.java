package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;

import java.util.function.Consumer;

/**
 * How a token-copy effect builds and enters the copies of a {@link TokenCreationEvent}, kept on the event so the
 * creation can continue after the creating player has ordered its replacements.
 *
 * @param entry           the resolving stack entry that creates the copies
 * @param sourcePermanent the permanent the effect copies from, whose attack target attacking copies take, or {@code null}
 * @param effect          the copy effect, with its copy exceptions and the riders of the created tokens
 * @param copyException   an extra exception applied to each copy as it is built, or {@code null}
 */
public record TokenCopyBatch(StackEntry entry, Permanent sourcePermanent, CreateTokenCopyOfTargetPermanentEffect effect,
                             Consumer<Card> copyException) {
}
