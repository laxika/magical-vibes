package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record ReturnCardFromGraveyardEffect(
        GraveyardChoiceDestination destination,
        CardPredicate filter,
        GraveyardSearchScope source,
        boolean targetGraveyard,
        boolean returnAll,
        boolean thisTurnOnly,
        PermanentPredicate attachmentTarget
) implements CardEffect {

    public ReturnCardFromGraveyardEffect(GraveyardChoiceDestination destination, CardPredicate filter) {
        this(destination, filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false, null);
    }

    public ReturnCardFromGraveyardEffect(GraveyardChoiceDestination destination, CardPredicate filter,
                                         GraveyardSearchScope source) {
        this(destination, filter, source, false, false, false, null);
    }

    public ReturnCardFromGraveyardEffect(GraveyardChoiceDestination destination, CardPredicate filter,
                                         boolean targetGraveyard) {
        this(destination, filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, targetGraveyard, false, false, null);
    }

    @Override
    public boolean canTargetGraveyard() {
        return targetGraveyard;
    }
}
