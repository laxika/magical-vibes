package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "62")
public class FathomSeer extends Card {

    public FathomSeer() {
        addMorph("{0}", new ReturnPermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new DrawCardEffect(2));
    }
}
