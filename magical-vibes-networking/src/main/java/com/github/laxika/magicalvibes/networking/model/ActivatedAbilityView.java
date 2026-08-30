package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.List;

/**
 * @param variableCounterCostType name of the {@code CounterType} whose counters the ability removes
 *                                X of as a cost ("Remove X theft counters from this enchantment:
 *                                ..."), or {@code null} when it has no such cost. The client prompts
 *                                for X capped by that counter's count on the permanent, the way it
 *                                does for {@code variableLoyaltyCost}.
 */
public record ActivatedAbilityView(String description, boolean requiresTap, boolean needsTarget, boolean needsSpellTarget, String manaCost, Integer loyaltyCost, int minTargets, int maxTargets, boolean isManaAbility, boolean variableLoyaltyCost, String variableCounterCostType, boolean requiresXValue, boolean xValueFromControlledCreatureCounters, CardColor xValueFromCardsInHandColor, boolean xValueFromWaterbendCost, int xValueMin, int modalChoicesRequired, int modalChoicesMax, List<ModalOptionView> modalOptions) {
}
