package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSH", collectorNumber = "81")
public class TrickstersStratagem extends Card {

    public TrickstersStratagem() {
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(1,
                                new PermanentIsCreaturePredicate()));
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addSpellEffect(new DrawDiscardAndConniveEffect(true));
    }
}
