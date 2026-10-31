package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;

@CardRegistration(set = "SNC", collectorNumber = "58")
public class RunOutOfTown extends Card {

    public RunOutOfTown() {
        addSpellEffect(new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0));
    }
}
