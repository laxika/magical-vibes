package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Exile target creature card from a graveyard, then put a +1/+1 counter on the source creature.
 */
public record ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect(
        GraveyardSearchScope scope, String abilityLink) implements CardEffect {

    /**
     * The link the printed ability records on the card it exiles, which "creature cards exiled with it" reads
     * ({@link Subjects#exiledWithSourceBy}, CR 607.2a).
     */
    public static final String ABILITY_LINK = "exile-creature-card-from-graveyard";

    public ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect(GraveyardSearchScope scope) {
        this(scope, ABILITY_LINK);
    }

    public ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect() {
        this(GraveyardSearchScope.ALL_GRAVEYARDS);
    }

    /**
     * This exile as another object borrowed it, recording {@code link} instead: the borrower's own "cards exiled with
     * it" don't count what it exiles (CR 607.2a, 607.5).
     */
    public ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect withAbilityLink(String link) {
        return new ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect(scope, link);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), scope));
    }
}
