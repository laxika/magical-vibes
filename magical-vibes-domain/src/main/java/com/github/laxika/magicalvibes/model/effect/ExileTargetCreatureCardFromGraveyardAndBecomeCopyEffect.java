package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Exiles a target creature card from a graveyard, then makes the source a copy of it. */
public record ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect(EffectSlot retainedEffectSlot)
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
