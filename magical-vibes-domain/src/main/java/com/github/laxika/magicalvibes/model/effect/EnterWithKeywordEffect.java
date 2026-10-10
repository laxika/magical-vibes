package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * As-enters replacement effect (CR 614.1c): "This creature enters with [keyword]." The permanent
 * gains the keyword as it enters, so it is on the battlefield before any ETB trigger is collected.
 * The keyword is a persistent grant on the permanent rather than a static ability of its card, so it
 * survives the permanent later becoming a copy of something else, and a copy of this card made later
 * does not have it (CR 707.2 — copies do not copy the grant).
 * <p>
 * Conditional variants ("If this creature was kicked, it enters with ... and with fear") wrap this in a
 * {@link ConditionalEffect} registered in {@code EffectSlot.ON_ENTER_BATTLEFIELD}. Handled in
 * {@code BattlefieldPlacementService} while the permanent enters; never goes on the stack.
 */
public record EnterWithKeywordEffect(Keyword keyword) implements ReplacementEffect {
}
