package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Intimidation Bolt's rider: "Other creatures can't attack this turn." A one-shot {@code SPELL}-slot
 * effect that installs a turn-scoped attack lock exempting only the creature the spell targeted (read
 * from the stack entry at resolution). Resolved by
 * {@code OtherCreaturesCantAttackThisTurnEffectHandler}, which adds a floating effect lasting until end of turn
 * (CR 611.2a) carrying the targeted creature as {@code exemptCreatureId}; the lock is enforced in
 * {@code AttackLegalityService.canAttack} and covers creatures that enter the battlefield later
 * this turn (a continuous restriction), not just those present when the spell resolved. Untargeted
 * itself — it reads the accompanying damage effect's target — so {@code targetSpec()} stays
 * {@code NONE}.
 *
 * @param exemptCreatureId the one creature that may attack, or {@code null} on the card's own effect, which names
 *                         none until it resolves
 */
public record OtherCreaturesCantAttackThisTurnEffect(UUID exemptCreatureId) implements CardEffect {

    public OtherCreaturesCantAttackThisTurnEffect() {
        this(null);
    }
}
