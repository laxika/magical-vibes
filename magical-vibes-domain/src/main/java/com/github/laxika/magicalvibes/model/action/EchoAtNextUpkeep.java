package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.HandCardCost;

import java.util.List;
import java.util.UUID;

/**
 * Delayed echo trigger for a permanent's next upkeep. The permanent id is resolved at the upkeep
 * so the trigger follows a control change and disappears if the permanent has left the battlefield.
 */
public record EchoAtNextUpkeep(UUID permanentId, String manaCost, DynamicAmount dynamicManaCost,
                               HandCardCost handCardCost, CostEffect cost, List<CardEffect> paidEffects,
                               Card sourceCard) implements DelayedAction {

    public EchoAtNextUpkeep {
        paidEffects = paidEffects == null ? List.of() : List.copyOf(paidEffects);
    }

    public EchoAtNextUpkeep(UUID permanentId, String manaCost, Card sourceCard) {
        this(permanentId, manaCost, null, null, null, List.of(), sourceCard);
    }

    public EchoAtNextUpkeep(UUID permanentId, String manaCost, List<CardEffect> paidEffects, Card sourceCard) {
        this(permanentId, manaCost, null, null, null, paidEffects, sourceCard);
    }

    public EchoAtNextUpkeep(UUID permanentId, DynamicAmount dynamicManaCost, Card sourceCard) {
        this(permanentId, null, dynamicManaCost, null, null, List.of(), sourceCard);
    }

    public EchoAtNextUpkeep(UUID permanentId, DynamicAmount dynamicManaCost,
                            List<CardEffect> paidEffects, Card sourceCard) {
        this(permanentId, null, dynamicManaCost, null, null, paidEffects, sourceCard);
    }

    public EchoAtNextUpkeep(UUID permanentId, HandCardCost handCardCost, Card sourceCard) {
        this(permanentId, null, null, handCardCost, null, List.of(), sourceCard);
    }

    public EchoAtNextUpkeep(UUID permanentId, HandCardCost handCardCost,
                            List<CardEffect> paidEffects, Card sourceCard) {
        this(permanentId, null, null, handCardCost, null, paidEffects, sourceCard);
    }

    public EchoAtNextUpkeep(UUID permanentId, CostEffect cost, Card sourceCard) {
        this(permanentId, null, null, null, cost, List.of(), sourceCard);
    }

    public EchoAtNextUpkeep(UUID permanentId, CostEffect cost,
                            List<CardEffect> paidEffects, Card sourceCard) {
        this(permanentId, null, null, null, cost, paidEffects, sourceCard);
    }
}
