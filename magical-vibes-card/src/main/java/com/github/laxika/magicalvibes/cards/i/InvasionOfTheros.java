package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EpharaEverSheltering;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "23")
public class InvasionOfTheros extends Card {

    public InvasionOfTheros() {
        setBackFaceCard(new EpharaEverSheltering());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.AURA),
                new CardSubtypePredicate(CardSubtype.GOD),
                new CardSubtypePredicate(CardSubtype.DEMIGOD)
        ))));
    }

    @Override
    public String getBackFaceClassName() {
        return "EpharaEverSheltering";
    }
}
