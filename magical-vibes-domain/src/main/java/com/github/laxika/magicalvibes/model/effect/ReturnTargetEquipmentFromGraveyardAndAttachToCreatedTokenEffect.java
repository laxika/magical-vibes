package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

/** Returns a targeted Equipment and attaches it to the token created by the same effect. */
public record ReturnTargetEquipmentFromGraveyardAndAttachToCreatedTokenEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
