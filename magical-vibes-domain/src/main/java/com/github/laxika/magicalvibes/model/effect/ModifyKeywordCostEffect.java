package com.github.laxika.magicalvibes.model.effect;

/**
 * Static: "[keyword] costs [whose players pay] cost {N} less / more". {@code amount} is the signed change of the
 * cost's generic mana: negative is cheaper, positive costs more. {@code scope} says whose payments change, judged
 * against the source's controller: {@code SELF} "costs you pay" / "abilities you activate", {@code OPPONENT} "costs
 * your opponents pay", {@code ALL} every player's ("all morph costs").
 *
 * <ul>
 *   <li>Memory Crystal: {@code (BUYBACK, -2, ALL)}; Warbringer: {@code (DASH, -2, SELF)}.</li>
 *   <li>Catalyst Stone: {@code (FLASHBACK, -2, SELF)} and {@code (FLASHBACK, 2, OPPONENT)}.</li>
 *   <li>Fluctuator: {@code (CYCLING, -2, SELF)}; Silver-Fur Master: {@code (NINJUTSU, -1, SELF)}.</li>
 *   <li>Exiled Doomsayer: {@code (MORPH, 2, ALL)}; Inquisitive Glimmer: {@code (UNLOCK, -1, SELF)}.</li>
 * </ul>
 */
public record ModifyKeywordCostEffect(KeywordCost cost, int amount, CostModificationScope scope)
        implements CardEffect {

    /** "[keyword] costs you pay cost {N} less": a reduction of the controller's payments. */
    public static ModifyKeywordCostEffect reduceYours(KeywordCost cost, int amount) {
        return new ModifyKeywordCostEffect(cost, -amount, CostModificationScope.SELF);
    }
}
