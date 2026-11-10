package com.github.laxika.magicalvibes.model.effect;

/**
 * Renown N (CR 702.112a): "When this creature deals combat damage to a player, if it isn't
 * renowned, put N +1/+1 counters on it and it becomes renowned." Put on
 * {@code Triggers.dealsCombatDamageToPlayer()} with the keyword's intervening "if",
 * {@code TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceIsRenowned()))}: a renowned creature's
 * renown doesn't trigger, and a second instance resolving later does nothing (CR 603.4, CR 702.112c).
 */
public record RenownEffect(int amount) implements CardEffect {
}
