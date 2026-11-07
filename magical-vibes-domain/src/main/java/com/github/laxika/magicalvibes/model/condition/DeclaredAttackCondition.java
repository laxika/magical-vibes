package com.github.laxika.magicalvibes.model.condition;

/**
 * A condition that is a fact about the attack as it was declared: "if you attacked with creatures with total power 6 or
 * greater this combat" (pack tactics), "if you attacked with exactly one other creature this combat" (Creepy
 * Puppeteer). As an attack trigger's intervening "if" (CR 603.4) it is judged as the ability triggers, from the
 * declared attackers; as the ability resolves the answer is the same, as the attack already happened, so the attack
 * collector takes its wrapper off once judged rather than recount attackers that may have left combat since.
 */
public interface DeclaredAttackCondition {
}
