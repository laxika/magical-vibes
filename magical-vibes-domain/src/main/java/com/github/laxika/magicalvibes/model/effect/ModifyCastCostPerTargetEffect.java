package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static: "[whose] spells cost {N} less / more to cast for each target" (Hinata, Dawn-Crowned) or "for each [permanent]
 * it targets" (Battlefield Thaumaturge). {@code amount} is the signed change per counted target: negative is
 * cheaper, positive costs more. Each distinct target counts once, however many times the spell targets it (Hinata's
 * ruling); with a {@code targetFilter} only distinct target permanents matching it count. {@code scope} says whose
 * spells change, judged against the source's controller; {@code spellFilter} ({@code null}: any spell) which spells.
 *
 * <ul>
 *   <li>Hinata: {@code (-1, SELF, null, null)} and {@code (1, OPPONENT, null, null)}.</li>
 *   <li>Battlefield Thaumaturge: {@code (-1, SELF, creature, instantOrSorcery)}.</li>
 * </ul>
 */
public record ModifyCastCostPerTargetEffect(int amount, CostModificationScope scope,
                                            PermanentPredicate targetFilter, CardPredicate spellFilter)
        implements CardEffect {
}
