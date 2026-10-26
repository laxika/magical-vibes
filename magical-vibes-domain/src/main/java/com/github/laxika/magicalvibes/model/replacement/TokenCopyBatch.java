package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * How a token-copy effect builds and enters the copies of a {@link TokenCreationEvent}, kept on the event so the
 * creation can continue after the creating player has ordered its replacements.
 *
 * @param entry           the resolving stack entry that creates the copies
 * @param sourcePermanent the permanent the effect copies from, whose attack target attacking copies take, or {@code null}
 * @param effect          the copy effect, with its copy exceptions and the riders of the created tokens
 * @param copyException   an extra exception applied to each copy as it is built, or {@code null}
 * @param attackTargets   the player, planeswalker or battle the copies of each copied card (by identity) attack, or
 *                        {@code null} when the copies take {@code sourcePermanent}'s attack target
 */
public record TokenCopyBatch(StackEntry entry, Permanent sourcePermanent, CreateTokenCopyOfTargetPermanentEffect effect,
                             Consumer<Card> copyException, Map<Card, UUID> attackTargets) {

    public TokenCopyBatch {
        if (attackTargets != null) {
            Map<Card, UUID> byIdentity = new IdentityHashMap<>(attackTargets);
            attackTargets = Collections.unmodifiableMap(byIdentity);
        }
    }

    public TokenCopyBatch(StackEntry entry, Permanent sourcePermanent, CreateTokenCopyOfTargetPermanentEffect effect,
                          Consumer<Card> copyException) {
        this(entry, sourcePermanent, effect, copyException, null);
    }

    /** The attack target of the copies of {@code copiedCard}, or {@code null} when none was given for it. */
    public UUID attackTargetOf(Card copiedCard) {
        return attackTargets == null ? null : attackTargets.get(copiedCard);
    }
}
