package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
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
 * @param copyRider       set-up applied to each copy's permanent (not to added tokens) before it enters, given the
 *                        copied card and the copy, such as a link to a card that isn't part of the copiable values,
 *                        or {@code null}
 * @param tokenRider      set-up applied to every token the event creates before it enters, the copies and the
 *                        tokens a replacement adds or substitutes alike (Queen Allenal's Soldier, Jinnie Fay's Cats
 *                        or Dogs), for instructions of the creating effect such as a link to its source, or
 *                        {@code null}
 * @param instructionRider set-up applied to every token the event creates before it enters, given the copied card
 *                        the token was created for, for per-copied-card instructions of the creating effect such as
 *                        Mirror Match's "blocking that creature": a copy gets the card it copies, a token Jinnie Fay
 *                        substitutes gets the card of the token it replaces (her substitutes keep the creating
 *                        effect's instructions), and a token a replacement adds (Queen Allenal's Soldier) gets
 *                        {@code null}; or {@code null}
 * @param chooseAttackTargets whether the creating player chooses, as they enter, which defending player or
 *                        planeswalker each creature token of the event attacks (CR 508.4), once the replacements
 *                        have fixed how many tokens there are; {@code attackTargets} is then either {@code null}
 *                        (any opponent or a planeswalker one controls) or names, for each copied card, the opponent
 *                        whose planeswalkers its copies may attack instead of that opponent (myriad)
 * @param afterEntry      the creating effect's follow-up, run once with the game and every token the event created
 *                        after they have all entered, whichever call the creation finishes on, or {@code null}
 */
public record TokenCopyBatch(StackEntry entry, Permanent sourcePermanent, CreateTokenCopyOfTargetPermanentEffect effect,
                             Consumer<Card> copyException, Map<Card, UUID> attackTargets,
                             BiConsumer<Card, Permanent> copyRider, Consumer<Permanent> tokenRider,
                             BiConsumer<Card, Permanent> instructionRider, boolean chooseAttackTargets, BiConsumer<GameData, List<Permanent>> afterEntry) {

    public TokenCopyBatch {
        if (attackTargets != null) {
            Map<Card, UUID> byIdentity = new IdentityHashMap<>(attackTargets);
            attackTargets = Collections.unmodifiableMap(byIdentity);
        }
    }

    public TokenCopyBatch(StackEntry entry, Permanent sourcePermanent, CreateTokenCopyOfTargetPermanentEffect effect,
                          Consumer<Card> copyException) {
        this(entry, sourcePermanent, effect, copyException, null, null, null, null, false, null);
    }

    /** The attack target of the copies of {@code copiedCard}, or {@code null} when none was given for it. */
    public UUID attackTargetOf(Card copiedCard) {
        return attackTargets == null ? null : attackTargets.get(copiedCard);
    }
}
