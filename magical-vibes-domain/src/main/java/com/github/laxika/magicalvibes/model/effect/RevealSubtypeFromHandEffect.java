package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * "Reveal a [subtype] card from your hand. If you do, [thenEffect]." As it resolves, with no card of the subtype in
 * its controller's hand it does nothing; otherwise the controller reveals one (choosing which when several match) and
 * {@code thenEffect} is performed.
 * <p>
 * "You may reveal …" is this inside a {@link MayEffect}: the ability goes on the stack whenever it triggers and the
 * choice is made as it resolves (CR 603.5), and with no matching card in hand nothing is asked.
 */
public record RevealSubtypeFromHandEffect(CardSubtype subtype, CardEffect thenEffect) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return thenEffect.targetSpec();
    }

    @Override
    public com.github.laxika.magicalvibes.model.filter.PlayerRelation targetPlayerRelation() {
        return thenEffect.targetPlayerRelation();
    }
}
