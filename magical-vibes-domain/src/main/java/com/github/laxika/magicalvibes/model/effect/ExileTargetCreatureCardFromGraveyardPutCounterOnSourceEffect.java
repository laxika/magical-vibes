package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Exile target creature card from a graveyard, then put a +1/+1 counter on the source creature.
 */
public record ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect(
        GraveyardSearchScope scope) implements CardEffect {

    /**
     * The link this ability records on the card it exiles, which "creature cards exiled with it" reads
     * ({@link Subjects#exiledWithSourceBy}, CR 607.2a).
     */
    public static final String ABILITY_LINK = "exile-creature-card-from-graveyard";

    public ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect() {
        this(GraveyardSearchScope.ALL_GRAVEYARDS);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), scope));
    }
}
