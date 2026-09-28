package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Returns a targeted creature card from the controller's graveyard and attaches the source Equipment to it. */
public record ReturnTargetCreatureFromGraveyardAndAttachSourceEquipmentEffect(CardPredicate filter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
