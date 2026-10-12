package com.github.laxika.magicalvibes.model.replacement;

/**
 * What choosing one replacement-order option can still lead to, for players that pick without
 * looking ahead themselves (the AI): the smallest and largest {@link ReplacementEvent#amount()}
 * among the final events reachable after applying it, over every later order. Effects that are
 * not {@linkplain ReplacementOption#pure() pure} count as applied without changing the event.
 *
 * @param label  the option's prompt label
 * @param fewest the smallest reachable amount
 * @param most   the largest reachable amount
 */
public record ReplacementOptionPreview(String label, int fewest, int most) {
}
