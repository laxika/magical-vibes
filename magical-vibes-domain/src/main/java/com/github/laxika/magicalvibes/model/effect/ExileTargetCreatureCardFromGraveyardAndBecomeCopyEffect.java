package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Trigger;

/**
 * Exiles a target creature card from a graveyard, then makes the source a copy of it.
 *
 * @param retainedTrigger the trigger of the source's triggered abilities the copy keeps, or {@code null} for none
 */
public record ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect(Trigger retainedTrigger)
        implements CardEffect {

    public ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
