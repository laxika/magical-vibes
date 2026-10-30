package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * "You may reveal a [subtype] card from your hand. If you do, [thenEffect]."
 * <p>
 * The ability goes on the stack whenever it triggers (CR 603.5), regardless of the hand's contents.
 * On resolution, if the controller's hand contains no card with the specified subtype no choice is
 * offered and the ability does nothing; otherwise the controller may reveal a matching card, and if
 * they do, {@code thenEffect} is performed.
 */
public record MayRevealSubtypeFromHandEffect(CardSubtype subtype, CardEffect thenEffect, String prompt) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return thenEffect.targetSpec();
    }

    @Override
    public com.github.laxika.magicalvibes.model.filter.PlayerRelation targetPlayerRelation() {
        return thenEffect.targetPlayerRelation();
    }
}
