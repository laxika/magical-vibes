package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;

/**
 * Echo [cost], a static marker a card adds for the keyword: "At the beginning of your upkeep, if this permanent came
 * under your control since the beginning of your last upkeep, sacrifice it unless you pay [cost]" (CR 702.30a). The
 * trigger index lists that triggered ability ({@code KeywordTriggers.echo}) for a permanent with this effect as the
 * layers leave it, so it is matched and put on the stack as the permanent's other upkeep abilities are.
 *
 * <p>Exactly one of the costs is set: a fixed mana cost, a generic mana amount evaluated as the ability resolves
 * (Volcano Hellion's "echo {X}, where X is your life total"), a hand-card cost such as discarding a card, or another
 * {@link CostEffect} such as sacrificing permanents.
 *
 * @param paidEffects what happens when the echo cost is paid ("when this creature's echo cost is paid, ...")
 */
public record EchoEffect(String manaCost, DynamicAmount dynamicManaCost, HandCardCost handCardCost, CostEffect cost,
                         List<CardEffect> paidEffects) implements CardEffect {

    public EchoEffect {
        paidEffects = paidEffects == null ? List.of() : List.copyOf(paidEffects);
    }

    public EchoEffect(String manaCost) {
        this(manaCost, null, null, null, List.of());
    }

    public EchoEffect(String manaCost, List<CardEffect> paidEffects) {
        this(manaCost, null, null, null, paidEffects);
    }

    public EchoEffect(DynamicAmount dynamicManaCost) {
        this(null, dynamicManaCost, null, null, List.of());
    }

    public EchoEffect(DynamicAmount dynamicManaCost, List<CardEffect> paidEffects) {
        this(null, dynamicManaCost, null, null, paidEffects);
    }

    public EchoEffect(HandCardCost handCardCost) {
        this(null, null, handCardCost, null, List.of());
    }

    public EchoEffect(HandCardCost handCardCost, List<CardEffect> paidEffects) {
        this(null, null, handCardCost, null, paidEffects);
    }

    public EchoEffect(CostEffect cost) {
        this(null, null, null, cost, List.of());
    }

    public EchoEffect(CostEffect cost, List<CardEffect> paidEffects) {
        this(null, null, null, cost, paidEffects);
    }
}
