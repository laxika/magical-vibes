package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Capability interface for static effects that reduce the activation cost of activated abilities of
 * matching cards in the controller's own graveyard (e.g. Embalmer's Tools: "Activated abilities of
 * creature cards in your graveyard cost {1} less to activate."). Controller-scoped — it benefits only
 * the effect's own controller, whose graveyard the abilities are activated from — and is collected by
 * {@code CastingCostService} without naming the concrete effect type.
 */
public interface GraveyardActivatedAbilityCostReducingEffect extends CardEffect {

    /** The graveyard cards whose activated abilities are cheaper to activate. */
    CardPredicate affectedGraveyardCards();

    /** Generic mana removed from a matching card's graveyard-ability activation cost (floored at 0). */
    int genericCostReduction();
}
