package com.github.laxika.magicalvibes.model.condition;

/**
 * Met while the player the condition is judged for (the context's target: a target player, or the player a static cast
 * lock is judged for) could cast a sorcery: it is their turn, a main phase, and the stack is empty (CR 307.5). It is
 * timing only, never a cast of a sorcery spell, so an effect that stops casting sorceries doesn't change it. "Each
 * opponent can cast spells only any time they could cast a sorcery" (Teferi) restricts while it isn't met.
 */
public record TargetPlayerCouldCastSorcery() implements Condition {

    @Override
    public String conditionName() {
        return "any time they could cast a sorcery";
    }

    @Override
    public String conditionNotMetReason() {
        return "not sorcery timing";
    }
}
