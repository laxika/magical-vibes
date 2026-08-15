package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameOrExileForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "EMN", collectorNumber = "51")
public class CoaxFromTheBlindEternities extends Card {

    public CoaxFromTheBlindEternities() {
        addEffect(EffectSlot.SPELL, new SearchOutsideGameOrExileForCardToHandEffect(
                new CardSubtypePredicate(CardSubtype.ELDRAZI)));
    }
}
